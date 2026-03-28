/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Ported from NeoForge 1.21.1 to Forge 1.20.1.
 */

package plus.dragons.createdragonsplus.data.lang;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ForeignLanguageProvider implements DataProvider {
    private final String modid;
    private final String templateLocale;
    private final PackOutput.PathProvider langPathProvider;
    private final ResourceManager resourceManager;

    private static volatile @Nullable Method getManagerMethod;

    public ForeignLanguageProvider(String modid, String templateLocale, PackOutput output, ExistingFileHelper existingFileHelper) {
        this.modid = modid;
        this.templateLocale = templateLocale;
        this.langPathProvider = output.createPathProvider(Target.RESOURCE_PACK, "lang");
        this.resourceManager = invokeGetManager(existingFileHelper, PackType.CLIENT_RESOURCES);
    }

    public ForeignLanguageProvider(String modid, PackOutput output, ExistingFileHelper existingFileHelper) {
        this(modid, "en_us", output, existingFileHelper);
    }

    private static ResourceManager invokeGetManager(ExistingFileHelper helper, PackType type) {
        try {
            Method method = getManagerMethod;
            if (method == null) {
                synchronized (ForeignLanguageProvider.class) {
                    method = getManagerMethod;
                    if (method == null) {
                        method = ExistingFileHelper.class.getDeclaredMethod("getManager", PackType.class);
                        method.setAccessible(true);
                        getManagerMethod = method;
                    }
                }
            }
            return (ResourceManager) method.invoke(helper, type);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to reflectively access ExistingFileHelper#getManager", e);
        }
    }

    protected CompletableFuture<JsonObject> getTemplateLocalization() {
        return CompletableFuture.supplyAsync(() -> {
            File file = this.langPathProvider
                    .json(new ResourceLocation(this.modid, this.templateLocale))
                    .toFile();
            try (FileInputStream inputStream = new FileInputStream(file)) {
                return GsonHelper.parse(new InputStreamReader(inputStream));
            } catch (IOException exception) {
                throw new JsonIOException(exception);
            }
        }, Util.backgroundExecutor());
    }

    protected CompletableFuture<JsonObject> getForeignTemplateLocalization(Resource resource) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStream inputStream = resource.open()) {
                return GsonHelper.parse(new InputStreamReader(inputStream));
            } catch (IOException exception) {
                throw new JsonIOException(exception);
            }
        }, Util.backgroundExecutor());
    }

    protected boolean isForeignLanguageFile(ResourceLocation location) {
        if (!this.modid.equals(location.getNamespace()))
            return false;
        String[] paths = location.getPath().split("/");
        if (paths.length != 2)
            return false;
        return paths[1].endsWith(".json") && !paths[1].equals(this.templateLocale + ".json");
    }

    protected JsonObject combine(JsonObject template, JsonObject foreign) {
        JsonObject result = new JsonObject();
        Map<String, JsonElement> unlocalized = new LinkedHashMap<>();
        for (var entry : template.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            if (foreign.has(key)) result.add(key, foreign.get(key));
            else unlocalized.put(key, value);
        }
        if (!unlocalized.isEmpty()) {
            result.addProperty("_comment.unlocalized", "Remove this line after finishing localization.");
            unlocalized.forEach(result::add);
        }
        return result;
    }

    protected void save(CachedOutput output, String locale, JsonObject result) {
        Path path = this.langPathProvider.json(new ResourceLocation(this.modid, locale));
        DataProvider.saveStable(output, result, path);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        CompletableFuture<JsonObject> readTemplate = this.getTemplateLocalization();
        CompletableFuture<?>[] all = this.resourceManager
                .listResources("lang", this::isForeignLanguageFile)
                .entrySet()
                .stream()
                .map(entry -> {
                    String locale = entry.getKey().getPath().split("/")[1].replace(".json", "");
                    return readTemplate.thenCombineAsync(
                            this.getForeignTemplateLocalization(entry.getValue()),
                            this::combine,
                            Util.backgroundExecutor()).thenAcceptAsync(result -> this.save(output, locale, result), Util.backgroundExecutor());
                })
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(all);
    }

    @Override
    public String getName() {
        return "ForeignLanguage";
    }
}
