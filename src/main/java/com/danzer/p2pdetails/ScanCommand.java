package com.danzer.p2pdetails;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import appeng.core.AEConfig;
import appeng.me.GridAccessException;
import appeng.me.GridNode;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.p2p.PartP2PTunnel;
import appeng.parts.p2p.PartP2PTunnelME;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ScanCommand extends CommandBase {

    private static final Field OUTER_PROXY_FIELD = findOuterProxyField();

    @Override
    public String getName() {
        return "p2pdetails";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/p2pdetails <scan|setnext|shownext|clearnext> ...";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> getTabCompletions(
        MinecraftServer server,
        ICommandSender sender,
        String[] args,
        BlockPos targetPos
    ) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "scan", "setnext", "shownext", "clearnext");
        }
        if (args.length == 2 && "scan".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, "summary", "isolated", "unconnected", "full", "near", "all");
        }
        return new ArrayList<String>();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getUsage(sender));
        }

        if ("setnext".equalsIgnoreCase(args[0])) {
            handleSetNext(sender, args);
            return;
        }

        if ("shownext".equalsIgnoreCase(args[0])) {
            handleShowNext(sender);
            return;
        }

        if ("clearnext".equalsIgnoreCase(args[0])) {
            handleClearNext(sender);
            return;
        }

        if (!"scan".equalsIgnoreCase(args[0]) || args.length > 3) {
            throw new WrongUsageException(getUsage(sender));
        }

        String mode = "all";
        if (args.length >= 2) {
            mode = args[1].toLowerCase(Locale.ROOT);
        }
        if (!isValidMode(mode)) {
            throw new WrongUsageException(getUsage(sender));
        }

        int limit = Math.max(1, Math.min(P2PDetailsConfig.scanDetailLines, 20));
        if (args.length >= 3) {
            limit = parseInt(args[2], 1, 50);
        }

        World world = sender.getEntityWorld();
        if (world == null) {
            world = server.getWorld(0);
        }
        if (world == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "[P2PDetails] 无法获取世界对象"));
            return;
        }

        List<TunnelRecord> records = collectTunnelRecords(world);
        ScanResult result = analyze(records);
        sendResult(sender, world, result, mode, limit);
    }

    private void handleSetNext(ICommandSender sender, String[] args) throws CommandException {
        if (sender.getCommandSenderEntity() == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "[P2PDetails] 该命令仅玩家可用"));
            return;
        }
        if (args.length != 2) {
            throw new WrongUsageException("/p2pdetails setnext <频道号(十进制或0x十六进制)>");
        }

        int value = parseFrequencyArg(args[1]);
        ItemStack stack = getHeldConfigTool(sender.getCommandSenderEntity());
        if (stack == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "[P2PDetails] 请主手持有 P2P配置工具"));
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setInteger(ItemP2PConfigTool.TAG_FREQUENCY, value);
        sender.sendMessage(new TextComponentString(
            TextFormatting.AQUA + "[P2PDetails] " + TextFormatting.RESET
                + "已保存配置工具频道为 "
                + String.format(Locale.ROOT, "0x%04X", value)
                + " (" + value + ")"
        ));
    }

    private void handleShowNext(ICommandSender sender) {
        if (sender.getCommandSenderEntity() == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "[P2PDetails] 该命令仅玩家可用"));
            return;
        }

        ItemStack stack = getHeldConfigTool(sender.getCommandSenderEntity());
        if (stack == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "[P2PDetails] 请主手持有 P2P配置工具"));
            return;
        }
        Integer configured = ItemP2PConfigTool.getConfiguredFrequency(stack);
        if (configured == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "[P2PDetails] 当前工具未配置频道"));
            return;
        }
        int value = configured.intValue();
        sender.sendMessage(new TextComponentString(
            TextFormatting.AQUA + "[P2PDetails] " + TextFormatting.RESET
                + "当前工具频道: "
                + String.format(Locale.ROOT, "0x%04X", value)
                + " (" + value + ")"
        ));
    }

    private void handleClearNext(ICommandSender sender) {
        if (sender.getCommandSenderEntity() == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "[P2PDetails] 该命令仅玩家可用"));
            return;
        }

        ItemStack stack = getHeldConfigTool(sender.getCommandSenderEntity());
        if (stack == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "[P2PDetails] 请主手持有 P2P配置工具"));
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) {
            tag.removeTag(ItemP2PConfigTool.TAG_FREQUENCY);
        }
        sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "[P2PDetails] " + TextFormatting.RESET + "已清除配置工具频道"));
    }

    private int parseFrequencyArg(String raw) throws CommandException {
        String text = raw.trim().toLowerCase(Locale.ROOT);
        try {
            int value;
            if (text.startsWith("0x")) {
                value = Integer.parseInt(text.substring(2), 16);
            } else {
                value = Integer.parseInt(text, 10);
            }
            if (value < 1 || value > 65535) {
                throw new NumberFormatException("out of range");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new WrongUsageException("/p2pdetails setnext <1-65535 或 0x0001-0xFFFF>");
        }
    }

    private ItemStack getHeldConfigTool(Entity entity) {
        if (!(entity instanceof EntityPlayer)) {
            return null;
        }
        ItemStack stack = ((EntityPlayer) entity).getHeldItemMainhand();
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemP2PConfigTool)) {
            return null;
        }
        return stack;
    }

    private List<TunnelRecord> collectTunnelRecords(World world) {
        List<TunnelRecord> records = new ArrayList<TunnelRecord>();
        for (TileEntity tile : world.loadedTileEntityList) {
            if (!(tile instanceof appeng.api.parts.IPartHost)) {
                continue;
            }

            appeng.api.parts.IPartHost host = (appeng.api.parts.IPartHost) tile;
            for (EnumFacing side : EnumFacing.values()) {
                appeng.api.parts.IPart part = host.getPart(side);
                if (!(part instanceof PartP2PTunnel)) {
                    continue;
                }

                PartP2PTunnel<?> tunnel = (PartP2PTunnel<?>) part;
                Connectivity connectivity = getConnectivity(tunnel);
                short frequency = tunnel.getFrequency();
                int used = -1;
                if (tunnel instanceof PartP2PTunnelME) {
                    used = getUsedChannels(getOuterProxy((PartP2PTunnelME) tunnel));
                }

                records.add(new TunnelRecord(
                    frequency,
                    tunnel.isOutput(),
                    tile.getPos(),
                    side,
                    connectivity.online,
                    connectivity.connected,
                    tunnel instanceof PartP2PTunnelME,
                    used
                ));
            }
        }
        return records;
    }

    private ScanResult analyze(List<TunnelRecord> records) {
        ScanResult result = new ScanResult();
        result.total = records.size();

        Map<Short, FrequencyStat> stats = new HashMap<Short, FrequencyStat>();
        for (TunnelRecord record : records) {
            if (record.frequency == 0) {
                result.unlinked++;
                continue;
            }

            FrequencyStat stat = stats.get(record.frequency);
            if (stat == null) {
                stat = new FrequencyStat();
                stats.put(record.frequency, stat);
            }

            if (record.output) {
                stat.outputs++;
            } else {
                stat.inputs++;
            }
        }

        int denseCapacity = AEConfig.instance().getDenseChannelCapacity();
        for (TunnelRecord record : records) {
            if (record.frequency != 0) {
                FrequencyStat stat = stats.get(record.frequency);
                boolean hasBothSides = stat != null && stat.inputs > 0 && stat.outputs > 0;
                if (!hasBothSides) {
                    result.isolated.add(record);
                } else if (!record.connected || !record.online) {
                    result.unconnected.add(record);
                }
            }

            if (!record.meTunnel || record.usedChannels < 0 || denseCapacity <= 0) {
                continue;
            }

            if (record.usedChannels >= denseCapacity) {
                result.full.add(record);
                continue;
            }

            if (isNearCapacity(record.usedChannels, denseCapacity)) {
                result.near.add(record);
            }
        }

        return result;
    }

    private void sendResult(ICommandSender sender, World world, ScanResult result, String mode, int limit) {
        String prefix = TextFormatting.AQUA + "[P2PDetails] " + TextFormatting.RESET;
        if ("summary".equals(mode) || "all".equals(mode)) {
            sender.sendMessage(new TextComponentString(
                prefix + "扫描维度 " + world.provider.getDimension() + " 完成，发现 " + result.total + " 个 P2P 端口（未绑定 " + result.unlinked + "）"
            ));
            sender.sendMessage(new TextComponentString(
                prefix + "孤立频率端口: " + result.isolated.size() +
                " | 同频未连通: " + result.unconnected.size() +
                " | 满载链路: " + result.full.size() +
                " | 近满载链路: " + result.near.size()
            ));
        } else {
            sender.sendMessage(new TextComponentString(
                prefix + "扫描维度 " + world.provider.getDimension() + " 完成，模式: " + mode
            ));
        }

        if ("summary".equals(mode)) {
            return;
        }

        if ("isolated".equals(mode)) {
            sendCategory(sender, "孤立频率端口", result.isolated, limit, TextFormatting.YELLOW);
            return;
        }

        if ("unconnected".equals(mode)) {
            sendCategory(sender, "同频未连通", result.unconnected, limit, TextFormatting.RED);
            return;
        }

        if ("full".equals(mode)) {
            sendCategory(sender, "满载链路", result.full, limit, TextFormatting.DARK_RED);
            return;
        }

        if ("near".equals(mode)) {
            sendCategory(sender, "近满载链路", result.near, limit, TextFormatting.GOLD);
            return;
        }

        sendCategory(sender, "孤立频率端口", result.isolated, limit, TextFormatting.YELLOW);
        sendCategory(sender, "同频未连通", result.unconnected, limit, TextFormatting.RED);
        sendCategory(sender, "满载链路", result.full, limit, TextFormatting.DARK_RED);
        sendCategory(sender, "近满载链路", result.near, limit, TextFormatting.GOLD);
    }

    private void sendCategory(
        ICommandSender sender,
        String title,
        List<TunnelRecord> records,
        int limit,
        TextFormatting color
    ) {
        if (records.isEmpty()) {
            sender.sendMessage(new TextComponentString(color + title + "：未发现问题"));
            return;
        }

        sender.sendMessage(new TextComponentString(color + title + "（显示前 " + Math.min(records.size(), limit) + " 条）"));
        for (int i = 0; i < Math.min(records.size(), limit); i++) {
            TunnelRecord record = records.get(i);
            sender.sendMessage(new TextComponentString(TextFormatting.GRAY + " - " + formatRecord(record)));
        }
    }

    private String formatRecord(TunnelRecord record) {
        String mode = record.output ? "输出" : "输入";
        String state = record.online ? (record.connected ? "已连通" : "未连通") : "离线";
        String frequency = record.frequency == 0
            ? "未绑定"
            : String.format(Locale.ROOT, "0x%04X", Short.toUnsignedInt(record.frequency));
        String channels = record.meTunnel
            ? ", 频道 " + Math.max(0, record.usedChannels) + "/" + AEConfig.instance().getDenseChannelCapacity()
            : "";

        return frequency + " | " + mode + " | " + state + " | " + formatPos(record.pos, record.side) + channels;
    }

    private String formatPos(BlockPos pos, EnumFacing side) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " (" + side.getName() + ")";
    }

    private Connectivity getConnectivity(PartP2PTunnel<?> tunnel) {
        try {
            Iterator<?> iterator = tunnel.isOutput() ? tunnel.getInputs().iterator() : tunnel.getOutputs().iterator();
            while (iterator.hasNext()) {
                if (iterator.next() instanceof PartP2PTunnel) {
                    return new Connectivity(true, true);
                }
            }
            return new Connectivity(true, false);
        } catch (GridAccessException e) {
            return new Connectivity(false, false);
        }
    }

    private AENetworkProxy getOuterProxy(PartP2PTunnelME tunnel) {
        if (OUTER_PROXY_FIELD == null) {
            return null;
        }

        try {
            return (AENetworkProxy) OUTER_PROXY_FIELD.get(tunnel);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private int getUsedChannels(AENetworkProxy proxy) {
        if (proxy == null) {
            return 0;
        }

        return proxy.getNode() instanceof GridNode
            ? ((GridNode) proxy.getNode()).usedChannels()
            : 0;
    }

    private boolean isNearCapacity(int used, int capacity) {
        int reserve = Math.max(0, P2PDetailsConfig.nearCapacityReserve);
        if (reserve <= 0) {
            return false;
        }
        return used >= Math.max(1, capacity - reserve);
    }

    private boolean isValidMode(String mode) {
        return "summary".equals(mode)
            || "isolated".equals(mode)
            || "unconnected".equals(mode)
            || "full".equals(mode)
            || "near".equals(mode)
            || "all".equals(mode);
    }

    private static Field findOuterProxyField() {
        try {
            Field field = PartP2PTunnelME.class.getDeclaredField("outerProxy");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static class Connectivity {
        private final boolean online;
        private final boolean connected;

        private Connectivity(boolean online, boolean connected) {
            this.online = online;
            this.connected = connected;
        }
    }

    private static class FrequencyStat {
        private int inputs;
        private int outputs;
    }

    private static class TunnelRecord {
        private final short frequency;
        private final boolean output;
        private final BlockPos pos;
        private final EnumFacing side;
        private final boolean online;
        private final boolean connected;
        private final boolean meTunnel;
        private final int usedChannels;

        private TunnelRecord(
            short frequency,
            boolean output,
            BlockPos pos,
            EnumFacing side,
            boolean online,
            boolean connected,
            boolean meTunnel,
            int usedChannels
        ) {
            this.frequency = frequency;
            this.output = output;
            this.pos = pos;
            this.side = side;
            this.online = online;
            this.connected = connected;
            this.meTunnel = meTunnel;
            this.usedChannels = usedChannels;
        }
    }

    private static class ScanResult {
        private int total;
        private int unlinked;
        private final List<TunnelRecord> isolated = new ArrayList<TunnelRecord>();
        private final List<TunnelRecord> unconnected = new ArrayList<TunnelRecord>();
        private final List<TunnelRecord> full = new ArrayList<TunnelRecord>();
        private final List<TunnelRecord> near = new ArrayList<TunnelRecord>();
    }
}
