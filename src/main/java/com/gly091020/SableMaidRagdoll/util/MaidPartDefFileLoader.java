package com.gly091020.SableMaidRagdoll.util;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLPaths;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;

public class MaidPartDefFileLoader {
    public static final Path BASE_PATH = FMLPaths.GAMEDIR.get().resolve(SableMaidRagdoll.MODID);
    private static final Map<String, DefFile> DEF_FILES = new HashMap<>();
    private static final Gson GSON = new Gson();

    public static void init(){
        if(!BASE_PATH.toFile().isDirectory()){
            try{
                Files.createDirectories(BASE_PATH);
                releaseFromJar();
            } catch (IOException e) {
                SableMaidRagdoll.LOGGER.warn("创建文件夹时出错", e);
            }
        }
        load();
    }

    private static void load(){
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

    private static void releaseFromJar(){
        final String path = String.format("/assets/%s/ragdoll_data", SableMaidRagdoll.MODID);
        try {
            copyFolder(path, BASE_PATH);
        } catch (Exception e) {
            SableMaidRagdoll.LOGGER.warn("提取默认数据失败：", e);
            return;
        }
        SableMaidRagdoll.LOGGER.info("释放默认文件成功");
    }

    // 来自 GetJarResources.java 感谢 943
    public static void copyFolder(String sourcePath, Path targetPath) throws Exception {
        URL url = SableMaidRagdoll.class.getResource(sourcePath);
        if (url == null) {
            return;
        }
        URI uri = url.toURI();
        Path sourceFolderPath = Paths.get(uri);
        try (Stream<Path> stream = Files.walk(sourceFolderPath, Integer.MAX_VALUE)) {
            stream.forEach(source -> {
                Path relativePath = sourceFolderPath.relativize(source);
                String relativePathString = relativePath.toString().replace('\\', '/');
                Path target = targetPath.resolve(relativePathString);
                try {
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Path parentDir = target.getParent();
                        if (parentDir != null && !Files.isDirectory(parentDir)) {
                            Files.createDirectories(parentDir);
                        }
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (Exception e) {
                    SableMaidRagdoll.LOGGER.warn("读取时出现错误：", e);
                }
            });
        }
    }

    public record DefFile(
            Map<String, MaidPartBlockEntity.MaidBlockShape> parts,
            List<RenderDataWithoutModelName> renderData,
            List<PartPosData> partPosData,
            List<JointData> jointData
    ){
        public static final Codec<DefFile> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.unboundedMap(Codec.STRING, MaidPartBlockEntity.MaidBlockShape.CODEC).fieldOf("parts").forGetter(DefFile::parts),
                Codec.list(RenderDataWithoutModelName.CODEC).fieldOf("renderData").forGetter(DefFile::renderData),
                Codec.list(PartPosData.CODEC).fieldOf("partPosData").forGetter(DefFile::partPosData),
                Codec.list(JointData.CODEC).fieldOf("jointData").forGetter(DefFile::jointData)
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

        public PartPosData getPartPosData(String partName){
            for(PartPosData posData: partPosData)
                if(posData.partName.equals(partName))
                    return posData;
            return null;
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

    // 此处的position以1/16个方块为单位，rotate是角度值
    public record PartPosData(String partName, Vec3 position, Vec3 rotate){
        public static final Codec<PartPosData> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("partName").forGetter(PartPosData::partName),
                Vec3.CODEC.fieldOf("pos").forGetter(PartPosData::position),
                Vec3.CODEC.fieldOf("rotate").forGetter(PartPosData::rotate)
        ).apply(i, PartPosData::new));

        public Pose3d getPose(Vec3 origin){
            Pose3d pose = new Pose3d();
            pose.position().set(origin.add(position.scale(1 / 16f)).toVector3f());
            pose.orientation().rotateXYZ(
                    Math.toRadians(rotate.x),
                    Math.toRadians(rotate.y),
                    Math.toRadians(rotate.z)
            );
            return pose;
        }
    }

    // pos为相对坐标，以1/16个方块为单位
    // pos原点在方块中心
    // contacts表示两者是否可以发生碰撞
    public record JointData(String partA, String partB,
                            Vec3 posA, Vec3 posB,
                            Optional<JointMotorData> motor,
                            Optional<Boolean> contacts){
        public static final Codec<JointData> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("partA").forGetter(JointData::partA),
                Codec.STRING.fieldOf("partB").forGetter(JointData::partB),
                Vec3.CODEC.fieldOf("posA").forGetter(JointData::posA),
                Vec3.CODEC.fieldOf("posB").forGetter(JointData::posB),
                Codec.optionalField("motor", JointMotorData.CODEC, false).forGetter(JointData::motor),
                Codec.optionalField("contacts", Codec.BOOL, false).forGetter(JointData::contacts)
        ).apply(i, JointData::new));

        public Vector3dc getVector3dcA(ServerSubLevel subLevel){
            var p1 = new Pose3d(subLevel.logicalPose());
            return localToWorld(p1, new Vector3d(posA.x, posA.y, posA.z).div(16));
        }

        public Vector3dc getVector3dcB(ServerSubLevel subLevel){
            var p1 = subLevel.logicalPose();
            return localToWorld(p1, new Vector3d(posB.x, posB.y, posB.z).div(16));
        }

        // 在碰Pose3dc.transformPosition我就是傻逼
        public static Vector3d localToWorld(Pose3dc pose, Vector3dc local) {
            var dest = new Vector3d();
            double x = local.x();
            double y = local.y();
            double z = local.z();

            Vector3dc s = pose.scale();
            x *= s.x();
            y *= s.y();
            z *= s.z();

            pose.orientation().transform(x, y, z, dest);

            return dest.add(pose.rotationPoint());
        }
    }

    // 用于自动回正
    public record JointMotorData(double stiffness, double damping){
        public static final Codec<JointMotorData> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.DOUBLE.fieldOf("stiffness").forGetter(JointMotorData::stiffness),
                Codec.DOUBLE.fieldOf("damping").forGetter(JointMotorData::damping)
        ).apply(i, JointMotorData::new));
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
