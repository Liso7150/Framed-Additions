package com.mystic.framed_additions.client;

import com.google.gson.JsonObject;
import com.mystic.framed_additions.CopiedFramedBlock;
import com.mystic.framed_additions.FramedAdditions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class DynamicFramedResources extends AbstractPackResources {
    private static final String PACK_ID = "framed_additions_dynamic";

    private DynamicFramedResources() {
        super(PACK_ID, true);
    }

    public static void add(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }
        event.addRepositorySource(consumer -> {
            Pack pack = Pack.readMetaAndCreate(
                    PACK_ID,
                    Component.literal("Framed Additions"),
                    true,
                    id -> new DynamicFramedResources(),
                    PackType.CLIENT_RESOURCES,
                    Pack.Position.TOP,
                    PackSource.BUILT_IN
            );
            if (pack != null) {
                consumer.accept(pack);
            }
        });
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... paths) {
        if (paths.length == 1 && "pack.mcmeta".equals(paths[0])) {
            return supplier("{\"pack\":{\"description\":\"framed additions\",\"pack_format\":15}}");
        }
        return null;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type != PackType.CLIENT_RESOURCES || !FramedAdditions.MODID.equals(location.getNamespace())) {
            return null;
        }
        String path = location.getPath();
        if (path.startsWith("blockstates/")) {
            return blockstate(path.substring("blockstates/".length()));
        }
        if (path.startsWith("models/item/")) {
            return itemModel(path.substring("models/item/".length()));
        }
        if ("lang/en_us.json".equals(path)) {
            return supplier("{\"itemGroup.framed_additions\":\"Framed Additions\",\"item.framed_additions.copy\":\"Framed %s\"}");
        }
        return null;
    }

    @Override
    public void listResources(PackType type, String namespace, String prefix, PackResources.ResourceOutput output) {
        if (type != PackType.CLIENT_RESOURCES || !FramedAdditions.MODID.equals(namespace)) {
            return;
        }
        if ("lang".equals(prefix)) {
            ResourceLocation lang = new ResourceLocation(FramedAdditions.MODID, "lang/en_us.json");
            IoSupplier<InputStream> supplier = getResource(type, lang);
            if (supplier != null) {
                output.accept(lang, supplier);
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? Set.of(FramedAdditions.MODID) : Set.of();
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) throws IOException {
        if (!"pack".equals(serializer.getMetadataSectionName())) {
            return null;
        }
        JsonObject pack = new JsonObject();
        pack.addProperty("description", "framed additions");
        pack.addProperty("pack_format", 15);
        JsonObject root = new JsonObject();
        root.add("pack", pack);
        return serializer.fromJson(root);
    }

    @Override
    public void close() {
    }

    @Nullable
    private static IoSupplier<InputStream> blockstate(String path) {
        CopiedFramedBlock block = find(path);
        if (block == null) {
            return null;
        }
        StringBuilder json = new StringBuilder("{\"variants\":{");
        boolean first = true;
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append('"').append(variantKey(state)).append("\":{\"model\":\"framedblocks:block/framed_cube\"}");
        }
        json.append("}}");
        return supplier(json.toString());
    }

    @Nullable
    private static IoSupplier<InputStream> itemModel(String path) {
        if (find(path) == null) {
            return null;
        }
        return supplier("{\"parent\":\"framedblocks:block/framed_cube\"}");
    }

    @Nullable
    private static CopiedFramedBlock find(String path) {
        if (path.endsWith(".json")) {
            path = path.substring(0, path.length() - ".json".length());
        }
        ResourceLocation id = new ResourceLocation(FramedAdditions.MODID, path);
        if (ForgeRegistries.BLOCKS.getValue(id) instanceof CopiedFramedBlock block) {
            return block;
        }
        return null;
    }

    private static String variantKey(BlockState state) {
        List<Property<?>> properties = new ArrayList<>(state.getProperties());
        properties.sort(Comparator.comparing(Property::getName));
        StringBuilder key = new StringBuilder();
        for (Property<?> property : properties) {
            if (key.length() > 0) {
                key.append(',');
            }
            key.append(property.getName()).append('=').append(propertyValue(state, property));
        }
        return key.toString();
    }

    private static <T extends Comparable<T>> String propertyValue(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    private static IoSupplier<InputStream> supplier(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        return () -> new ByteArrayInputStream(bytes);
    }
}
