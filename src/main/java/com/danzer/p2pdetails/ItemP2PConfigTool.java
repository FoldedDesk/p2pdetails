package com.danzer.p2pdetails;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.parts.p2p.PartP2PTunnel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;

public class ItemP2PConfigTool extends Item {

    public static final String TAG_FREQUENCY = "ConfiguredFrequency";

    public ItemP2PConfigTool() {
        setRegistryName(P2PDetailsMod.MOD_ID, "p2p_config_tool");
        setUnlocalizedName(P2PDetailsMod.MOD_ID + ".p2p_config_tool");
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            P2PDetailsMod.openP2PConfigGui();
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public EnumActionResult onItemUse(
        EntityPlayer player,
        World world,
        BlockPos pos,
        EnumHand hand,
        EnumFacing facing,
        float hitX,
        float hitY,
        float hitZ
    ) {
        ItemStack stack = player.getHeldItem(hand);
        Integer configured = getConfiguredFrequency(stack);
        if (configured == null) {
            if (!world.isRemote) {
                player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "[P2PDetails] 请先右键空气打开配置工具并保存频道"));
            }
            return EnumActionResult.FAIL;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof IPartHost)) {
            return EnumActionResult.PASS;
        }

        IPart part = ((IPartHost) tile).getPart(facing);
        if (!(part instanceof PartP2PTunnel)) {
            return EnumActionResult.PASS;
        }

        if (!world.isRemote) {
            int value = configured.intValue();
            boolean ok = P2PDetailsMod.setTunnelFrequency((PartP2PTunnel<?>) part, (short) value);
            if (ok) {
                player.sendMessage(new TextComponentString(
                    TextFormatting.AQUA + "[P2PDetails] " + TextFormatting.RESET
                        + "已将该 P2P 设为频道 "
                        + String.format("0x%04X", value)
                        + " (" + value + ")"
                ));
            } else {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "[P2PDetails] 设置频道失败"));
            }
        }
        return EnumActionResult.SUCCESS;
    }

    public static Integer getConfiguredFrequency(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(TAG_FREQUENCY)) {
            return null;
        }
        int value = tag.getInteger(TAG_FREQUENCY);
        return (value >= 1 && value <= 65535) ? Integer.valueOf(value) : null;
    }
}
