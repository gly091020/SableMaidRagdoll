package com.gly091020.SableMaidRagdoll.block;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.util.MaidPartDefFileLoader;
import com.gly091020.SableMaidRagdoll.util.MaidRagdollHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

public class MaidPartBlockEntity extends BlockEntity {
    private VoxelShape shape;
    private MaidBlockShape maidBlockShape;
    private RenderData renderData;
    private List<BEJointData> jointData;
    public MaidPartBlockEntity(BlockPos pos, BlockState state) {
        super(SableMaidRagdoll.MAID_PART_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("shape", Tag.TAG_COMPOUND)) {
            MaidBlockShape.CODEC.parse(NbtOps.INSTANCE, tag.get("shape"))
                    .resultOrPartial(err -> SableMaidRagdoll.LOGGER.debug("decode failed: {}", err))
                    .ifPresent(shape -> {
                        this.maidBlockShape = shape;
                        this.shape = shape.toVoxelShape();
                    });
        }
        if(tag.contains("renderData", Tag.TAG_COMPOUND))
            RenderData.CODEC.parse(NbtOps.INSTANCE, tag.get("renderData"))
                    .resultOrPartial(err -> SableMaidRagdoll.LOGGER.debug("decode failed: {}", err))
                    .ifPresent(renderData -> this.renderData = renderData);
        if(tag.contains("jointData", Tag.TAG_LIST))
            BEJointData.LIST_CODEC.parse(NbtOps.INSTANCE, tag.get("jointData"))
                    .resultOrPartial(err -> SableMaidRagdoll.LOGGER.debug("decode failed: {}", err))
                    .ifPresent(jointData -> this.jointData = jointData);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(tag, provider);

        if(maidBlockShape != null)
            MaidBlockShape.CODEC.encodeStart(NbtOps.INSTANCE, maidBlockShape)
                    .resultOrPartial(err -> SableMaidRagdoll.LOGGER.debug("encode failed: {}", err))
                    .ifPresent(encoded -> tag.put("shape", encoded));
        if(renderData != null)
            RenderData.CODEC.encodeStart(NbtOps.INSTANCE, renderData)
                    .resultOrPartial(err -> SableMaidRagdoll.LOGGER.debug("encode failed: {}", err))
                    .ifPresent(encoded -> tag.put("renderData", encoded));
        if(jointData != null)
            BEJointData.LIST_CODEC.encodeStart(NbtOps.INSTANCE, jointData)
                    .resultOrPartial(err -> SableMaidRagdoll.LOGGER.debug("encode failed: {}", err))
                    .ifPresent(encoded -> tag.put("jointData", encoded));
    }

    public VoxelShape getShape(){
        if(shape != null)return shape;
        return Shapes.block();
    }

    public void updateBlockShape(){
        if(level == null)return;
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 8);
    }

    public void setMaidBlockShape(MaidBlockShape shape){
        this.maidBlockShape = shape;
        this.shape = maidBlockShape.toVoxelShape();
        updateBlockShape();
        setChanged();
    }

    public void setRenderData(RenderData renderData) {
        this.renderData = renderData;
        setChanged();
    }

    public void setJointData(List<BEJointData> data){
        this.jointData = Collections.unmodifiableList(data);
        setChanged();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
        return saveWithoutMetadata(provider);
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        loadAdditional(tag, provider);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public RenderData getRenderData() {
        return renderData;
    }

    public List<BEJointData> getJointData() {
        return jointData;
    }

    public MaidBlockShape getMaidBlockShape() {
        return maidBlockShape;
    }

    public record MaidBlockShape(List<MaidPartBlockEntity.Box> boxes){
        public static final Codec<MaidBlockShape> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.list(MaidPartBlockEntity.Box.CODEC).fieldOf("boxes").forGetter(MaidBlockShape::boxes)
        ).apply(i, MaidBlockShape::new));

        public VoxelShape toVoxelShape() {
            VoxelShape shape = Shapes.empty();
            for (MaidPartBlockEntity.Box box : boxes()) {
                shape = Shapes.or(
                        shape,
                        Shapes.box(
                                box.minX(), box.minY(), box.minZ(),
                                box.maxX(), box.maxY(), box.maxZ()
                        )
                );
            }
            if(shape.isEmpty())return Shapes.block();
            return shape;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(level == null || jointData == null || level.isClientSide)return;
        var container = (ServerSubLevelContainer) ServerSubLevelContainer.getContainer(level);
        if(container == null)return;
        var self = (ServerSubLevel) SableCompanion.INSTANCE.getContaining(level, getBlockPos());
        if(self == null)return;
        for (BEJointData beJointData: jointData){
            var a = (ServerSubLevel) container.getSubLevel(beJointData.subLevelA);
            var b = (ServerSubLevel) container.getSubLevel(beJointData.subLevelB);
            if(a == null || b == null)return;

            PhysicsConstraintHandle handle;
            try{
                handle = MaidRagdollHelper.createJoint(container, a, b, beJointData.posA, beJointData.posB);
            } catch (Exception e) {
                SableMaidRagdoll.LOGGER.debug("连接时错误：", e);
                continue;
            }
            handle.setContactsEnabled(beJointData.contacts.orElse(true));
            if(beJointData.motor.isEmpty())continue;
            var motorData = beJointData.motor.get();
            var target = computeAxisTargets(a.logicalPose().orientation(), b.logicalPose().orientation());
            handle.setMotor(ConstraintJointAxis.ANGULAR_X, target.x, motorData.stiffness(), motorData.damping(), false, 0.0f);
            handle.setMotor(ConstraintJointAxis.ANGULAR_Y, target.y, motorData.stiffness(), motorData.damping(), false, 0.0f);
            handle.setMotor(ConstraintJointAxis.ANGULAR_Z, target.z, motorData.stiffness(), motorData.damping(), false, 0.0f);

        }
    }

    public static Vec3 computeAxisTargets(Quaterniond from, Quaterniond to) {
        Quaterniond delta = new Quaterniond(from).conjugate().mul(to);
        Vector3d euler = delta.getEulerAnglesXYZ(new Vector3d());
        return new Vec3(euler.x, euler.y, euler.z);
    }

    public record Box(float minX, float minY, float minZ,
                      float maxX, float maxY, float maxZ) {
        public static final Codec<Box> CODEC =
                Codec.INT_STREAM.comapFlatMap(stream ->
                                Util.fixedSize(stream, 3).map(arr ->
                                        Box.fromSize(arr[0], arr[1], arr[2])
                                ),
                        box -> IntStream.of(
                                (int) ((box.maxX - box.minX) * 16f),
                                (int) ((box.maxY - box.minY) * 16f),
                                (int) ((box.maxZ - box.minZ) * 16f)
                        )
                ).stable();

        public static Box fromSize(float xSize, float ySize, float zSize){
            return new Box((8 - xSize / 2f) / 16f, (8 - ySize / 2f) / 16f, (8 - zSize / 2f) / 16f, (8 + xSize / 2f) / 16f, (8 + ySize / 2f) / 16f, (8 + zSize / 2f) / 16f);
        }
    }

    public record RenderData(String modelName, String partName, Vec3 transform, Vec3 rotate){
        public static final Codec<RenderData> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("modelName").forGetter(RenderData::modelName),
                Codec.STRING.fieldOf("partName").forGetter(RenderData::partName),
                Vec3.CODEC.fieldOf("transform").forGetter(RenderData::transform),
                Vec3.CODEC.fieldOf("rotate").forGetter(RenderData::rotate)
        ).apply(i, RenderData::new));
    }

    public record BEJointData(UUID subLevelA, UUID subLevelB, Vec3 posA, Vec3 posB, Optional<MaidPartDefFileLoader.JointMotorData> motor, Optional<Boolean> contacts){
        public static final Codec<BEJointData> CODEC = RecordCodecBuilder.create(i -> i.group(
                UUIDUtil.CODEC.fieldOf("subLevelA").forGetter(BEJointData::subLevelA),
                UUIDUtil.CODEC.fieldOf("subLevelB").forGetter(BEJointData::subLevelB),
                Vec3.CODEC.fieldOf("posA").forGetter(BEJointData::posA),
                Vec3.CODEC.fieldOf("posB").forGetter(BEJointData::posB),
                Codec.optionalField("motor", MaidPartDefFileLoader.JointMotorData.CODEC, false).forGetter(BEJointData::motor),
                Codec.optionalField("contacts", Codec.BOOL, false).forGetter(BEJointData::contacts)
        ).apply(i, BEJointData::new));

        public static final Codec<List<BEJointData>> LIST_CODEC = Codec.list(CODEC);
    }
}
