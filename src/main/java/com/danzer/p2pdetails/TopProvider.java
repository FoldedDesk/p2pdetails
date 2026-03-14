package com.danzer.p2pdetails;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.me.GridAccessException;
import appeng.me.GridNode;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.parts.p2p.PartP2PTunnel;
import appeng.parts.p2p.PartP2PTunnelME;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.apiimpl.ProbeInfo;
import mcjty.theoneprobe.apiimpl.elements.ElementText;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

public class TopProvider implements IProbeInfoProvider {

    private static final List<String> PACKAGED_MOD_IDS = Arrays.asList(
        "packagedauto",
        "packagedexcrafting",
        "packagedavaritia",
        "packageddraconic",
        "packagedmekemicals"
    );
    private static final String[] PACKAGED_CLASS_MARKERS = {
        "packagedauto",
        "packagedexcrafting",
        "packagedavaritia",
        "packageddraconic",
        "packagedmekemicals"
    };
    private static final Field OUTER_PROXY_FIELD = findOuterProxyField();
    private static final Field ELEMENT_TEXT_FIELD = findElementTextField();

    @Override
    public String getID() {
        return "p2pdetails:ae2_top_provider";
    }

    @Override
    public void addProbeInfo(
        ProbeMode mode,
        IProbeInfo probeInfo,
        EntityPlayer player,
        World world,
        IBlockState blockState,
        IProbeHitData data
    ) {
        TileEntity tile = world.getTileEntity(data.getPos());
        IPart part = resolvePart(tile, data);

        if (part instanceof PartP2PTunnel) {
            sanitizeProbeInfo(probeInfo, true);
            addP2PInfo((PartP2PTunnel<?>) part, mode, probeInfo);
            return;
        }

        if (part instanceof IGridProxyable) {
            sanitizeProbeInfo(probeInfo, false);
            addGenericChannelInfo(((IGridProxyable) part).getProxy(), probeInfo);
            return;
        }

        if (tile instanceof IGridProxyable) {
            sanitizeProbeInfo(probeInfo, false);
            addGenericChannelInfo(((IGridProxyable) tile).getProxy(), probeInfo);
            return;
        }

        AENetworkProxy proxy = getOptionalProxy(part);
        if (proxy != null) {
            sanitizeProbeInfo(probeInfo, false);
            addGenericChannelInfo(proxy, probeInfo);
            return;
        }

        proxy = getOptionalProxy(tile);
        if (proxy != null) {
            sanitizeProbeInfo(probeInfo, false);
            addGenericChannelInfo(proxy, probeInfo);
            return;
        }

        IGridNode node = getOptionalNode(part);
        if (node != null) {
            sanitizeProbeInfo(probeInfo, false);
            addGenericChannelInfo(node, probeInfo);
            return;
        }

        node = getOptionalNode(tile);
        if (node != null) {
            sanitizeProbeInfo(probeInfo, false);
            addGenericChannelInfo(node, probeInfo);
        }
    }

    private void addP2PInfo(PartP2PTunnel<?> tunnel, ProbeMode mode, IProbeInfo probeInfo) {
        short frequency = tunnel.getFrequency();
        String frequencyText = frequency == 0
            ? TextFormatting.GRAY + "P2P 频率: 未绑定"
            : TextFormatting.AQUA + "P2P 频率: " + formatFrequency(frequency);
        probeInfo.text(frequencyText);

        boolean output = tunnel.isOutput();
        ConnectionSnapshot snapshot = getConnections(tunnel);
        snapshot.extended = mode == ProbeMode.EXTENDED;
        String modeLabel = output ? "输出" : "输入";
        probeInfo.text(
            statusColor(snapshot.connected, snapshot.online) +
            "P2P 状态: " + modeLabel + " / " + snapshot.stateLabel()
        );

        if (snapshot.connected) {
            if (output && !snapshot.targets.isEmpty()) {
                probeInfo.text(TextFormatting.GREEN + "连接到: " + snapshot.targets.get(0));
            } else if (!output) {
                probeInfo.text(TextFormatting.GREEN + "已连接输出: " + snapshot.targets.size());
            }

            if (mode == ProbeMode.EXTENDED && !output) {
                for (int i = 0; i < Math.min(snapshot.targets.size(), 4); i++) {
                    probeInfo.text(TextFormatting.DARK_GREEN + " - " + snapshot.targets.get(i));
                }
            }
        }

        if (tunnel instanceof PartP2PTunnelME) {
            addMeP2PChannelInfo((PartP2PTunnelME) tunnel, probeInfo, snapshot);
        }
    }

    private void addMeP2PChannelInfo(PartP2PTunnelME tunnel, IProbeInfo probeInfo, ConnectionSnapshot snapshot) {
        int denseCapacity = AEConfig.instance().getDenseChannelCapacity();
        boolean output = tunnel.isOutput();

        if (output) {
            int used = getUsedChannels(getOuterProxy(tunnel));
            probeInfo.text(channelColor(used, denseCapacity) + "单输出承载: " + used + "/" + denseCapacity);
            addCapacityWarning(probeInfo, "此 P2P 输出", used, denseCapacity);
            return;
        }

        int used = getUsedChannels(getOuterProxy(tunnel));
        probeInfo.text(channelColor(used, denseCapacity) + "总传输频道: " + used + "/" + denseCapacity);
        if (snapshot.connected) {
            probeInfo.text(TextFormatting.GREEN + "输出端数量: " + snapshot.tunnels.size());
        }
        addCapacityWarning(probeInfo, "此 P2P 输入", used, denseCapacity);

        if (!snapshot.tunnels.isEmpty()) {
            int activeOutputs = 0;
            for (PartP2PTunnel<?> outputTunnel : snapshot.tunnels) {
                if (outputTunnel instanceof PartP2PTunnelME) {
                    int outputUsed = getUsedChannels(getOuterProxy((PartP2PTunnelME) outputTunnel));
                    if (outputUsed > 0) {
                        activeOutputs++;
                    }
                }
            }

            probeInfo.text(TextFormatting.AQUA + "活跃输出端: " + activeOutputs + "/" + snapshot.tunnels.size());

            if (snapshot.extended && activeOutputs > 0) {
                addOutputBreakdown(probeInfo, snapshot, denseCapacity);
            }
        }
    }

    private void addGenericChannelInfo(AENetworkProxy proxy, IProbeInfo probeInfo) {
        if (proxy == null) {
            return;
        }

        addGenericChannelInfo(proxy.getNode(), probeInfo);
    }

    private void addGenericChannelInfo(IGridNode node, IProbeInfo probeInfo) {
        if (!AEConfig.instance().isFeatureEnabled(AEFeature.CHANNELS)) {
            return;
        }

        if (!(node instanceof GridNode)) {
            return;
        }

        GridNode gridNode = (GridNode) node;
        EnumSet<GridFlags> flags = gridNode.getFlags();
        int used = gridNode.usedChannels();

        if (!flags.contains(GridFlags.REQUIRE_CHANNEL)
            && !flags.contains(GridFlags.DENSE_CAPACITY)
            && !flags.contains(GridFlags.COMPRESSED_CHANNEL)) {
            return;
        }

        if (flags.contains(GridFlags.DENSE_CAPACITY)) {
            int max = AEConfig.instance().getDenseChannelCapacity();
            probeInfo.text(channelColor(used, max) + "频道: " + used + "/" + max);
            return;
        }

        if (flags.contains(GridFlags.COMPRESSED_CHANNEL)) {
            int max = AEConfig.instance().getNormalChannelCapacity();
            probeInfo.text(channelColor(used, max) + "频道: " + used + "/" + max);
            return;
        }

        if (flags.contains(GridFlags.REQUIRE_CHANNEL)) {
            boolean hasChannel = node.meetsChannelRequirements();
            probeInfo.text(
                (hasChannel ? TextFormatting.GREEN : TextFormatting.RED) +
                "频道占用: " + (hasChannel ? "1/1" : "0/1")
            );
        }
    }

    private AENetworkProxy getOptionalProxy(Object target) {
        if (target == null || !shouldProbeReflectively(target)) {
            return null;
        }

        Object value = invokeNoArgMethod(target, "getProxy");
        if (value instanceof AENetworkProxy) {
            return (AENetworkProxy) value;
        }

        value = findFieldValue(target, AENetworkProxy.class);
        return value instanceof AENetworkProxy ? (AENetworkProxy) value : null;
    }

    private IGridNode getOptionalNode(Object target) {
        if (target == null || !shouldProbeReflectively(target)) {
            return null;
        }

        Object value = invokeNoArgMethod(target, "getGridNode");
        if (value instanceof IGridNode) {
            return (IGridNode) value;
        }

        value = invokeNoArgMethod(target, "getActionableNode");
        if (value instanceof IGridNode) {
            return (IGridNode) value;
        }

        value = findFieldValue(target, IGridNode.class);
        return value instanceof IGridNode ? (IGridNode) value : null;
    }

    private boolean shouldProbeReflectively(Object target) {
        if (!isPackagedSeriesLoaded()) {
            return false;
        }

        String className = target.getClass().getName().toLowerCase();
        for (String marker : PACKAGED_CLASS_MARKERS) {
            if (className.contains(marker)) {
                return true;
            }
        }

        return false;
    }

    private boolean isPackagedSeriesLoaded() {
        for (String modId : PACKAGED_MOD_IDS) {
            if (Loader.isModLoaded(modId)) {
                return true;
            }
        }

        return false;
    }

    private Object invokeNoArgMethod(Object target, String methodName) {
        Class<?> type = target.getClass();
        while (type != null && type != Object.class) {
            try {
                Method method = type.getDeclaredMethod(methodName);
                method.setAccessible(true);
                return method.invoke(target);
            } catch (ReflectiveOperationException ignored) {
                type = type.getSuperclass();
            }
        }

        return null;
    }

    private Object findFieldValue(Object target, Class<?> expectedType) {
        Class<?> type = target.getClass();
        while (type != null && type != Object.class) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                if (!expectedType.isAssignableFrom(field.getType())) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    return field.get(target);
                } catch (IllegalAccessException ignored) {
                    return null;
                }
            }

            type = type.getSuperclass();
        }

        return null;
    }

    private IPart resolvePart(TileEntity tile, IProbeHitData data) {
        if (!(tile instanceof IPartHost)) {
            return null;
        }

        Vec3d hit = data.getHitVec();
        BlockPos pos = data.getPos();
        Vec3d localHit = new Vec3d(hit.x - pos.getX(), hit.y - pos.getY(), hit.z - pos.getZ());
        SelectedPart selected = ((IPartHost) tile).selectPart(localHit);
        return selected == null ? null : selected.part;
    }

    private ConnectionSnapshot getConnections(PartP2PTunnel<?> tunnel) {
        try {
            ConnectionSnapshot snapshot = new ConnectionSnapshot();
            snapshot.online = true;
            if (tunnel.isOutput()) {
                Iterator<?> iterator = tunnel.getInputs().iterator();
                collectConnections(iterator, snapshot);
            } else {
                Iterator<?> iterator = tunnel.getOutputs().iterator();
                collectConnections(iterator, snapshot);
            }

            return snapshot;
        } catch (GridAccessException e) {
            ConnectionSnapshot snapshot = new ConnectionSnapshot();
            snapshot.online = false;
            return snapshot;
        }
    }

    private void collectConnections(Iterator<?> iterator, ConnectionSnapshot snapshot) {
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (!(next instanceof PartP2PTunnel)) {
                continue;
            }

            PartP2PTunnel<?> target = (PartP2PTunnel<?>) next;
            snapshot.connected = true;
            snapshot.tunnels.add(target);
            snapshot.targets.add(formatTunnelTarget(target));
        }
    }

    private String formatTunnelTarget(PartP2PTunnel<?> tunnel) {
        TileEntity tile = tunnel.getTile();
        if (tile == null) {
            return "未知位置";
        }

        BlockPos pos = tile.getPos();
        EnumFacing facing = tunnel.getSide().getFacing();
        return String.format("%d, %d, %d (%s)", pos.getX(), pos.getY(), pos.getZ(), facing.getName());
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

        IGridNode node = proxy.getNode();
        return node instanceof GridNode ? ((GridNode) node).usedChannels() : 0;
    }

    private String statusColor(boolean connected, boolean online) {
        if (!online) {
            return TextFormatting.GRAY.toString();
        }
        return connected ? TextFormatting.GREEN.toString() : TextFormatting.RED.toString();
    }

    private String channelColor(int used, int max) {
        if (max <= 0) {
            return TextFormatting.GRAY.toString();
        }
        if (used >= max) {
            return TextFormatting.RED.toString();
        }
        if (isNearCapacity(used, max)) {
            return TextFormatting.YELLOW.toString();
        }
        return TextFormatting.GREEN.toString();
    }

    private void addCapacityWarning(IProbeInfo probeInfo, String label, int used, int capacity) {
        if (capacity <= 0) {
            return;
        }

        if (used >= capacity) {
            probeInfo.text(TextFormatting.RED + "警告: " + label + "已满载");
            return;
        }

        if (isNearCapacity(used, capacity)) {
            probeInfo.text(TextFormatting.YELLOW + "注意: " + label + "接近满载");
        }
    }

    private boolean isNearCapacity(int used, int capacity) {
        return used >= Math.max(1, capacity - 4);
    }

    private void addOutputBreakdown(IProbeInfo probeInfo, ConnectionSnapshot snapshot, int denseCapacity) {
        int limit = Math.min(snapshot.tunnels.size(), 4);
        for (int i = 0; i < limit; i++) {
            PartP2PTunnel<?> tunnel = snapshot.tunnels.get(i);
            if (!(tunnel instanceof PartP2PTunnelME)) {
                continue;
            }

            int used = getUsedChannels(getOuterProxy((PartP2PTunnelME) tunnel));
            if (used <= 0) {
                continue;
            }

            String target = snapshot.targets.get(i);
            probeInfo.text(channelColor(used, denseCapacity) + " - " + target + ": " + used + "/" + denseCapacity);
        }
    }

    private String formatFrequency(short frequency) {
        return String.format("0x%04X", Short.toUnsignedInt(frequency));
    }

    private void sanitizeProbeInfo(IProbeInfo probeInfo, boolean p2p) {
        if (!(probeInfo instanceof ProbeInfo)) {
            return;
        }

        List<IElement> elements = ((ProbeInfo) probeInfo).getElements();
        for (Iterator<IElement> iterator = elements.iterator(); iterator.hasNext(); ) {
            IElement element = iterator.next();
            String text = getElementText(element);
            if (text == null) {
                continue;
            }

            if (shouldRemoveOnlineText(text) || (p2p && shouldRemoveP2PText(text))) {
                iterator.remove();
            }
        }
    }

    private boolean shouldRemoveOnlineText(String text) {
        return containsAny(text,
            "device online",
            "device offline",
            "设备在线",
            "设备离线"
        );
    }

    private boolean shouldRemoveP2PText(String text) {
        String normalized = text.toLowerCase();
        if (normalized.matches(".*\\b[0-9a-f]{4}\\b.*")) {
            return true;
        }

        return containsAny(text,
            "unlinked",
            "one output",
            "many outputs",
            "one input",
            "many inputs",
            "未连接",
            "输入端",
            "输出端"
        );
    }

    private boolean containsAny(String text, String... needles) {
        String normalized = text.toLowerCase();
        for (String needle : needles) {
            if (normalized.contains(needle.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private String getElementText(IElement element) {
        if (!(element instanceof ElementText) || ELEMENT_TEXT_FIELD == null) {
            return null;
        }

        try {
            return (String) ELEMENT_TEXT_FIELD.get(element);
        } catch (IllegalAccessException e) {
            return null;
        }
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

    private static Field findElementTextField() {
        try {
            Field field = ElementText.class.getDeclaredField("text");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static class ConnectionSnapshot {
        private final List<String> targets = new ArrayList<String>();
        private final List<PartP2PTunnel<?>> tunnels = new ArrayList<PartP2PTunnel<?>>();
        private boolean connected;
        private boolean extended;
        private boolean online;

        private String stateLabel() {
            if (!online) {
                return "离线";
            }
            return connected ? "已连接" : "未连接";
        }
    }
}
