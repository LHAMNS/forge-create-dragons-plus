/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.data.runtime;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.hash.HashCode;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.forgespi.locating.IModFile;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Ported from NeoForge 1.21.1 to Forge 1.20.1.
 * Key changes:
 * - No PackLocationInfo, PackSelectionConfig, Pack.Metadata, ResourcesSupplier
 * - Uses Pack.create() instead of Pack.readMetaAndCreate()
 * - ModContainer/IModFile from Forge SPI instead of NeoForge
 * - Uses IoSupplier pattern for resource access
 */
public final class RuntimePackResources implements PackResources, RepositorySource, CachedOutput {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Joiner PATH_JOINER = Joiner.on("/");
    private final IModFile file;
    private final PackType type;
    private final Pack.Position position;
    private final PackMetadataSection metadata;
    private final String packId;
    private final Component title;
    private final PackOutput output;
    private final Map<Path, IoSupplier<InputStream>> resources = new HashMap<>();

    public RuntimePackResources(String name, ModContainer modContainer, PackType type, Pack.Position position, Component title, Component description) {
        var modInfo = modContainer.getModInfo();
        var modId = modInfo.getModId();
        this.packId = new ResourceLocation(modId, name).toString();
        this.file = modInfo.getOwningFile().getFile();
        this.type = type;
        this.position = position;
        this.title = title;
        this.metadata = new PackMetadataSection(description, SharedConstants.getCurrentVersion().getPackVersion(type));
        this.output = new PackOutput(file.findResource(""));
    }

    public PackOutput getPackOutput() {
        return output;
    }

    public void addDataProvider(DataProvider provider) {
        LOGGER.info("Starting provider [{}] for runtime resource [{}]", provider, packId);
        Stopwatch stopwatch = Stopwatch.createStarted();
        provider.run(this).join();
        LOGGER.info("{} finished after {} ms", provider, stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    @Override
    public void loadPacks(Consumer<Pack> loader) {
        var self = this;
        Pack pack = Pack.readMetaAndCreate(
                packId,
                title,
                true,
                id -> self,
                type,
                position,
                PackSource.BUILT_IN);
        if (pack != null) {
            loader.accept(pack);
        }
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... pathName) {
        Path path = file.findResource(pathName);
        return resources.get(path);
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        Path path = file.findResource(type.getDirectory(), location.getNamespace(), location.getPath());
        return resources.get(path);
    }

    @Override
    public void listResources(PackType type, String namespace, String directory, ResourceOutput output) {
        if (this.type != type)
            return;
        Path namespacePath = file.findResource(type.getDirectory(), namespace);
        Path directoryPath = namespacePath.resolve(directory);
        resources.forEach((path, resource) -> {
            if (path.startsWith(directoryPath)) {
                String filePath = PATH_JOINER.join(namespacePath.relativize(path));
                ResourceLocation location = ResourceLocation.tryBuild(namespace, filePath);
                if (location == null)
                    LOGGER.warn("Invalid path in pack: {}:{}, ignoring", namespace, filePath);
                else
                    output.accept(location, resource);
            }
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        Path directoryPath = file.findResource(type.getDirectory());
        return resources.keySet().stream()
                .map(path -> directoryPath.relativize(path).getName(0).toString())
                .distinct()
                .filter(namespace -> {
                    if (ResourceLocation.isValidNamespace(namespace))
                        return true;
                    LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", namespace, this.packId);
                    return false;
                })
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) {
        if (deserializer == PackMetadataSection.TYPE)
            return (T) metadata;
        return null;
    }

    @Override
    public String packId() {
        return packId;
    }

    @Override
    public void close() {}

    @Override
    public void writeIfNeeded(Path filePath, byte[] data, HashCode hashCode) {
        resources.put(filePath, () -> new ByteArrayInputStream(data));
    }
}
