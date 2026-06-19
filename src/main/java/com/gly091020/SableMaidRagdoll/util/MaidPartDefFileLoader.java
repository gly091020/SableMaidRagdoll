package com.gly091020.SableMaidRagdoll.util;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class MaidPartDefFileLoader {
    public static final Path BASE_PATH = FMLPaths.CONFIGDIR.get().resolve(SableMaidRagdoll.MODID);
    private static final Map<String, DefFile> DEF_FILES = new HashMap<>();
    private static final Gson GSON = new Gson();

    public static void init(){
        try{
            Files.createDirectories(BASE_PATH);
        } catch (IOException e) {
            SableMaidRagdoll.LOGGER.warn("创建文件夹时出错", e);
        }
        load();
    }

    public static void load(){
        DEF_FILES.clear();
        SableMaidRagdoll.LOGGER.info("开始加载定义文件");
        try (Stream<Path> stream = Files.list(BASE_PATH)) {
            stream.filter(path -> path.toFile().isDirectory()).forEach(MaidPartDefFileLoader::loadDir);
        } catch (IOException e) {
            SableMaidRagdoll.LOGGER.warn("出现错误", e);
        }
        SableMaidRagdoll.LOGGER.info("已完成{}文件的加载", DEF_FILES.size());
    }

    private static void loadDir(Path dir){
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(path -> path.toFile().isFile()).forEach(
                    path -> {
                        var id = dir.toFile().getName() + ":" + path.toFile().getName().replace(".json", "");
                        try{
                            loadFile(id, GSON.fromJson(Files.readString(path), JsonElement.class));
                        } catch (IOException e) {
                            SableMaidRagdoll.LOGGER.warn("出现错误", e);
                        }
                    }
            );
        } catch (IOException e) {
            SableMaidRagdoll.LOGGER.warn("出现错误", e);
        }
    }

    private static void loadFile(String id, JsonElement data){
        DefFile.CODEC.parse(JsonOps.INSTANCE, data)
                .resultOrPartial(error -> SableMaidRagdoll.LOGGER.warn("加载{}时失败：{}", id, error))
                .ifPresent(defFile -> DEF_FILES.put(id, defFile));
    }

    public record DefFile(
            Map<String, MaidPartBlockEntity.MaidBlockShape> parts,
            List<RenderDataWithoutModelName> renderData
    ){
        public static final Codec<DefFile> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.unboundedMap(Codec.STRING, MaidPartBlockEntity.MaidBlockShape.CODEC).fieldOf("parts").forGetter(DefFile::parts),
                Codec.list(RenderDataWithoutModelName.CODEC).fieldOf("renderData").forGetter(DefFile::renderData)
        ).apply(i, DefFile::new));

        public MaidPartBlockEntity.RenderData createRenderData(String modelName, String partName){
            for(RenderDataWithoutModelName renderDataWithoutModelName: renderData){
                if(!Objects.equals(partName, renderDataWithoutModelName.partName))continue;
                return new MaidPartBlockEntity.RenderData(modelName, partName,
                        renderDataWithoutModelName.transform.scale(1.0 / 16.0),
                        renderDataWithoutModelName.rotate
                );
            }
            return null;
        }

        public MaidPartBlockEntity.MaidBlockShape createShape(String partName){
            return parts.get(partName);
        }
    }

    // 此处的transform以1/16个方块为单位，rotate是角度值
    public record RenderDataWithoutModelName(String partName, Vec3 transform, Vec3 rotate){
        public static final Codec<RenderDataWithoutModelName> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("partName").forGetter(RenderDataWithoutModelName::partName),
                Vec3.CODEC.fieldOf("transform").forGetter(RenderDataWithoutModelName::transform),
                Vec3.CODEC.fieldOf("rotate").forGetter(RenderDataWithoutModelName::rotate)
        ).apply(i, RenderDataWithoutModelName::new));
    }

    public static Collection<DefFile> getAllDefFile(){
        return DEF_FILES.values();
    }

    public static Map<String, DefFile> getDefFileMap(){
        return DEF_FILES;
    }

    public static DefFile getDefFile(String modelName){
        return DEF_FILES.get(modelName);
    }
}
