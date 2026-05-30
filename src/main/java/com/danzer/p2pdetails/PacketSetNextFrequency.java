package com.danzer.p2pdetails;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSetNextFrequency implements IMessage {

    private int frequency;

    public PacketSetNextFrequency() {
    }

    public PacketSetNextFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.frequency = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.frequency);
    }

    public static class Handler implements IMessageHandler<PacketSetNextFrequency, IMessage> {

        @Override
        public IMessage onMessage(PacketSetNextFrequency message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    int value = message.frequency;
                    if (value < 1 || value > 65535) {
                        player.sendMessage(new TextComponentString(
                            TextFormatting.RED + "[P2PDetails] 频道号无效，范围 1-65535"
                        ));
                        return;
                    }

                    ItemStack held = player.getHeldItemMainhand();
                    if (held.isEmpty() || !(held.getItem() instanceof ItemP2PConfigTool)) {
                        player.sendMessage(new TextComponentString(
                            TextFormatting.RED + "[P2PDetails] 请手持 P2P配置工具 后再保存"
                        ));
                        return;
                    }

                    NBTTagCompound tag = held.getTagCompound();
                    if (tag == null) {
                        tag = new NBTTagCompound();
                        held.setTagCompound(tag);
                    }
                    tag.setInteger(ItemP2PConfigTool.TAG_FREQUENCY, value);
                    player.sendMessage(new TextComponentString(
                        TextFormatting.AQUA + "[P2PDetails] " + TextFormatting.RESET
                            + "已保存配置工具频道为 "
                            + String.format("0x%04X", value)
                            + " (" + value + ")"
                    ));
                }
            });
            return null;
        }
    }
}
