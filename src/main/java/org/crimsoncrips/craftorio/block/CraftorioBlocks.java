package org.crimsoncrips.craftorio.block;

import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.item.CraftorioItems;

import java.util.function.Supplier;


public class CraftorioBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craftorio.MODID);

    public static final DeferredBlock<Block> SINKER = register1("sinker", () -> new SinkerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).noOcclusion().pushReaction(PushReaction.BLOCK).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK).strength(3.0F, 100.0F)));

    public static final DeferredBlock<Block> AUTO_SINKER = register1("auto_sinker", () -> new AutoSinkerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).noOcclusion().pushReaction(PushReaction.BLOCK).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK).strength(3.0F, 100.0F)));

    public static final DeferredBlock<Block> VALUE_CONDENSER = register1("value_condenser", () -> new ValueCondenserBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).noOcclusion().pushReaction(PushReaction.BLOCK).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK).strength(3.0F, 100.0F)));

    public static final DeferredBlock<Block> AUTO_VALUE_CONDENSER = register1("auto_value_condenser", () -> new AutoValueCondenserBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).noOcclusion().pushReaction(PushReaction.BLOCK).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK).strength(3.0F, 100.0F)));

    public static final DeferredBlock<Block> AREA_SCANNER = register1("area_scanner", () -> new AreaScannerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).noOcclusion().pushReaction(PushReaction.BLOCK).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK).strength(3.0F, 100.0F)));

    public static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block) {
        DeferredBlock<T> ret = BLOCKS.register(name, block);
        CraftorioItems.ITEMS.register(name, () -> new BlockItem(ret.get(), new Item.Properties()));
        return ret;
    }

    public static <T extends Block> DeferredBlock<T> register1(String name, Supplier<T> block) {
        DeferredBlock<T> ret = BLOCKS.register(name, block);
        CraftorioItems.ITEMS.register(name, () -> new BlockItem(ret.get(), new Item.Properties().stacksTo(1)));
        return ret;
    }
}