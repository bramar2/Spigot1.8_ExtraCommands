package me.bramar.extracommands.customenchants;

import lombok.Getter;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.bukkit.Material.*;

public class ToolBlocks {
    private static final Set<Material> pickaxe;
    private static final Set<Material> axe;
    private static final Set<Material> shovel;
    static {
        // Pickaxe, manually removed: STONESLAB, DOUBLESTONESLAB
        // ANDESITE, DIORITE, POLISHED ANDESITE, POLISHED DIORITE -> is of type STONE
        // STONE BRICK -> SMOOTH BRICK
        // Filtered: STONESLAB, DOUBLESTONESLAB
        pickaxe = new HashSet<>(Arrays.asList(
                BEDROCK, OBSIDIAN, ENDER_CHEST, ANVIL, COAL_BLOCK, DIAMOND_BLOCK, EMERALD_BLOCK, IRON_BLOCK, REDSTONE_BLOCK, ENCHANTMENT_TABLE,
                IRON_FENCE, IRON_DOOR, IRON_DOOR_BLOCK, MOB_SPAWNER, DISPENSER, DROPPER, FURNACE, BURNING_FURNACE, GOLD_BLOCK, COAL_ORE, DIAMOND_ORE,
                EMERALD_ORE, ENDER_STONE, GOLD_ORE, HOPPER, IRON_ORE, IRON_TRAPDOOR, LAPIS_BLOCK, LAPIS_ORE, QUARTZ_ORE, REDSTONE_ORE, BRICK_STAIRS, BRICK,
                CAULDRON, COBBLESTONE, COBBLESTONE_STAIRS, COBBLE_WALL, MOSSY_COBBLESTONE, NETHER_BRICK, NETHER_BRICK_ITEM, NETHER_FENCE, NETHER_BRICK_STAIRS,
                STONE, SMOOTH_BRICK, SMOOTH_STAIRS, PRISMARINE, STAINED_CLAY, HARD_CLAY, QUARTZ_BLOCK, QUARTZ_STAIRS, SANDSTONE, SANDSTONE_STAIRS, BREWING_STAND,
                STONE_PLATE, IRON_PLATE, GOLD_PLATE, NETHERRACK, RAILS, ACTIVATOR_RAIL, POWERED_RAIL, DETECTOR_RAIL, ICE, PACKED_ICE
        ));
        // Axe
        // Filtered: STONESLAB, DOUBLESTONESLAB (as WOOD)
        axe = new HashSet<>(Arrays.asList(
            WOODEN_DOOR, TRAP_DOOR, CHEST, WORKBENCH, FENCE, FENCE_GATE, BIRCH_FENCE, BIRCH_FENCE_GATE, ACACIA_FENCE, ACACIA_FENCE_GATE, SPRUCE_FENCE,
                SPRUCE_FENCE_GATE, DARK_OAK_FENCE, DARK_OAK_FENCE_GATE, JUNGLE_FENCE, JUNGLE_FENCE_GATE, JUKEBOX, LOG, LOG_2, WOOD, WOOD_STAIRS, BIRCH_WOOD_STAIRS,
                ACACIA_STAIRS, SPRUCE_WOOD_STAIRS, DARK_OAK_STAIRS, JUNGLE_WOOD_STAIRS, BOOKSHELF, JACK_O_LANTERN, PUMPKIN, SIGN, SIGN_POST, NOTE_BLOCK, WOOD_PLATE,
                COCOA, DAYLIGHT_DETECTOR, DAYLIGHT_DETECTOR_INVERTED, HUGE_MUSHROOM_1, HUGE_MUSHROOM_2, VINE
        ));
        // Shovel
        // Filtered: -
        shovel = new HashSet<>(Arrays.asList(
                CLAY, DIRT, GRASS, GRAVEL, MYCEL, DIRT, SAND, SOUL_SAND, SNOW, SNOW_BLOCK
        ));
    }

    public static Set<Material> getPickaxe() {
        return pickaxe;
    }

    public static Set<Material> getAxe() {
        return axe;
    }

    public static Set<Material> getShovel() {
        return shovel;
    }
}
