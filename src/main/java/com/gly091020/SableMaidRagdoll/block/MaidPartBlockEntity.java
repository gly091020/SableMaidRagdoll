package com.gly091020.SableMaidRagdoll.block;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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

import java.util.List;
import java.util.stream.IntStream;

public class MaidPartBlockEntity extends BlockEntity {
    private static final String DEFAULT_MODEL_ID = "touhou_little_maid:hakurei_reimu";

    private VoxelShape shape;
    private MaidBlockShape maidBlockShape;
    private RenderData renderData;
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
            return shape;
        }
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
}
