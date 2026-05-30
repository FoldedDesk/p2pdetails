package com.danzer.p2pdetails;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod.EventBusSubscriber(modid = P2PDetailsMod.MOD_ID)
public final class ModRecipes {

    private ModRecipes() {
    }

    @SubscribeEvent
    public static void onRegisterRecipes(RegistryEvent.Register<IRecipe> event) {
        Item memoryCard = findAe2MemoryCard();
        if (memoryCard == null) {
            return;
        }

        ShapedOreRecipe recipe = new ShapedOreRecipe(
            new ResourceLocation(P2PDetailsMod.MOD_ID, "p2p_config_tool"),
            new ItemStack(ModItems.P2P_CONFIG_TOOL),
            " R ",
            " M ",
            " Q ",
            'R', "dustRedstone",
            'M', new ItemStack(memoryCard),
            'Q', "gemQuartz"
        );
        recipe.setRegistryName(new ResourceLocation(P2PDetailsMod.MOD_ID, "p2p_config_tool"));
        event.getRegistry().register(recipe);
    }

    private static Item findAe2MemoryCard() {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("appliedenergistics2", "memory_card"));
        if (item != null) {
            return item;
        }
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation("appliedenergistics2", "tool_memory_card"));
    }
}
