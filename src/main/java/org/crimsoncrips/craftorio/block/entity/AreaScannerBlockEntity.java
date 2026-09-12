package org.crimsoncrips.craftorio.block.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.inventory.AreaScannerMenu;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public class AreaScannerBlockEntity extends BlockEntity implements MenuProvider {

    public static final int MAX_SIZE = 48;
    public static final int MAX_OFFSET = 64;
    private static final int DEFAULT_SIZE = 3;

    private static final LevelResource SCANS_DIR = new LevelResource(Craftorio.MODID + "/area_scans");

    private int offsetX = 0;
    private int offsetY = 0;
    private int offsetZ = 0;
    private int sizeX = DEFAULT_SIZE;
    private int sizeY = DEFAULT_SIZE;
    private int sizeZ = DEFAULT_SIZE;

    public AreaScannerBlockEntity(BlockPos pos, BlockState blockState) {
        super(CraftorioBlockEntityTypes.AREA_SCANNER.get(), pos, blockState);
    }

    public int getOffsetX() { return offsetX; }
    public int getOffsetY() { return offsetY; }
    public int getOffsetZ() { return offsetZ; }
    public int getSizeX() { return sizeX; }
    public int getSizeY() { return sizeY; }
    public int getSizeZ() { return sizeZ; }

    public void setRegion(int offsetX, int offsetY, int offsetZ, int sizeX, int sizeY, int sizeZ) {
        this.offsetX = Mth.clamp(offsetX, -MAX_OFFSET, MAX_OFFSET);
        this.offsetY = Mth.clamp(offsetY, -MAX_OFFSET, MAX_OFFSET);
        this.offsetZ = Mth.clamp(offsetZ, -MAX_OFFSET, MAX_OFFSET);
        this.sizeX = Mth.clamp(sizeX, 1, MAX_SIZE);
        this.sizeY = Mth.clamp(sizeY, 1, MAX_SIZE);
        this.sizeZ = Mth.clamp(sizeZ, 1, MAX_SIZE);
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private BlockPos getRegionMin() {
        BlockPos start = worldPosition.offset(offsetX, offsetY, offsetZ);
        BlockPos end = start.offset(sizeX - 1, sizeY - 1, sizeZ - 1);
        return new BlockPos(Math.min(start.getX(), end.getX()), Math.min(start.getY(), end.getY()), Math.min(start.getZ(), end.getZ()));
    }

    private BlockPos getRegionMax() {
        BlockPos start = worldPosition.offset(offsetX, offsetY, offsetZ);
        BlockPos end = start.offset(sizeX - 1, sizeY - 1, sizeZ - 1);
        return new BlockPos(Math.max(start.getX(), end.getX()), Math.max(start.getY(), end.getY()), Math.max(start.getZ(), end.getZ()));
    }

    public void scanAndWriteFile(ServerPlayer player) {
        if (level == null) return;

        BlockPos min = getRegionMin();
        BlockPos max = getRegionMax();

        Map<Block, Integer> counts = new TreeMap<>((a, b) ->
                BuiltInRegistries.BLOCK.getKey(a).toString().compareTo(BuiltInRegistries.BLOCK.getKey(b).toString()));

        int totalBlocks = 0;
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR) continue;

            counts.merge(block, 1, Integer::sum);
            totalBlocks++;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Area Scanner Report\n");
        builder.append("Scanner position: ").append(worldPosition.getX()).append(", ").append(worldPosition.getY()).append(", ").append(worldPosition.getZ()).append("\n");
        builder.append("Scanned region: (").append(min.getX()).append(", ").append(min.getY()).append(", ").append(min.getZ())
                .append(") to (").append(max.getX()).append(", ").append(max.getY()).append(", ").append(max.getZ()).append(")\n");
        builder.append("Total blocks (excluding air): ").append(totalBlocks).append("\n\n");

        BigInteger grandTotal = BigInteger.ZERO;
        for (Map.Entry<Block, Integer> entry : counts.entrySet()) {
            Block block = entry.getKey();
            int count = entry.getValue();
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            Item item = block.asItem();

            builder.append(id).append(" - Count: ").append(count);
            if (item != Items.AIR) {
                BigInteger valueEach = CraftorioMisc.checkValue(new ItemStack(item), player, false);
                BigInteger valueTotal = valueEach.multiply(BigInteger.valueOf(count));
                grandTotal = grandTotal.add(valueTotal);
                builder.append(" - Value each: ").append(valueEach).append(" - Total value: ").append(valueTotal);
            } else {
                builder.append(" - Value each: N/A - Total value: N/A");
            }
            builder.append("\n");
        }

        builder.append("\nGrand total value: ").append(grandTotal).append("\n");

        try {
            Path dir = player.getServer().getWorldPath(SCANS_DIR);
            Files.createDirectories(dir);
            String fileName = "area_scan_" + worldPosition.getX() + "_" + worldPosition.getY() + "_" + worldPosition.getZ() + "_" + System.currentTimeMillis() + ".txt";
            Files.writeString(dir.resolve(fileName), builder.toString(), StandardCharsets.UTF_8);

            player.sendSystemMessage(Component.translatable("misc.craftorio.area_scan_success", fileName).withStyle(ChatFormatting.GREEN));
        } catch (IOException e) {
            Craftorio.LOGGER.error("Failed to write area scan report", e);
            player.sendSystemMessage(Component.translatable("misc.craftorio.area_scan_failed").withStyle(ChatFormatting.RED));
        }
    }

    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) return false;
        return player.distanceToSqr((double) this.worldPosition.getX() + 0.5, (double) this.worldPosition.getY() + 0.5, (double) this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("OffsetX", offsetX);
        tag.putInt("OffsetY", offsetY);
        tag.putInt("OffsetZ", offsetZ);
        tag.putInt("SizeX", sizeX);
        tag.putInt("SizeY", sizeY);
        tag.putInt("SizeZ", sizeZ);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.offsetX = tag.getInt("OffsetX");
        this.offsetY = tag.getInt("OffsetY");
        this.offsetZ = tag.getInt("OffsetZ");
        this.sizeX = tag.contains("SizeX") ? tag.getInt("SizeX") : DEFAULT_SIZE;
        this.sizeY = tag.contains("SizeY") ? tag.getInt("SizeY") : DEFAULT_SIZE;
        this.sizeZ = tag.contains("SizeZ") ? tag.getInt("SizeZ") : DEFAULT_SIZE;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AreaScannerMenu(id, inventory, this);
    }
}
