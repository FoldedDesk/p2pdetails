# P2P Details

[English](#english) | [中文](#中文)

## 中文

`P2P Details` 是一个面向 `Minecraft 1.12.2 + AE2` 的辅助 Mod，用于在游戏内快速查看 P2P 与频道状态，定位连线问题与容量风险。

### CurseForge

- https://www.curseforge.com/minecraft/mc-mods/p2pdetails

### 主要功能

- 在 The One Probe（TOP）中显示 P2P 频率、输入/输出模式、在线/连通状态
- 显示连接目标信息（输出端目标坐标、输入端连接输出数量）
- 显示 ME P2P 频道负载
- 输入端显示总传输频道、输出端显示单输出承载
- EXTENDED 模式下显示活跃输出与分摊明细（可配置条目上限）
- 满载与近满载预警（近满载阈值可配置）
- 为 AE2 中需要频道的部件/方块补充频道占用显示
- 对 Packaged 系列模组提供可选兼容显示
- 提供扫描命令，快速定位孤立频率、未连通、满载与近满载问题

### 扫描命令

- `/p2pdetails scan`
- `/p2pdetails scan all`
- `/p2pdetails scan summary`
- `/p2pdetails scan isolated|unconnected|full|near|all [limit]`

### 配置项

- `nearCapacityReserve`：进入“近满载”前保留频道数，`0` 为关闭预警
- `extendedOutputLimit`：TOP EXTENDED 模式最大输出明细条数
- `scanDetailLines`：`/p2pdetails scan` 每类默认输出条数

### 构建

```bash
./gradlew clean build
```

---

## English

`P2P Details` is a utility mod for `Minecraft 1.12.2 + AE2` that helps you inspect P2P/channel states in-game and quickly diagnose routing and capacity issues.

### CurseForge

- https://www.curseforge.com/minecraft/mc-mods/p2pdetails

### Features

- Shows P2P frequency, input/output mode, online/connected state in The One Probe (TOP)
- Displays connection targets (target position for outputs, output count for inputs)
- Shows ME P2P channel load
- Input side shows total transferred channels, output side shows per-output load
- EXTENDED mode shows active outputs and per-output breakdown (configurable limit)
- Full and near-capacity warnings (near-capacity threshold is configurable)
- Adds channel usage display for AE2 parts/blocks that require channels
- Optional compatibility display for Packaged-series mods
- Provides scan commands to detect isolated frequencies, unconnected links, full and near-full links

### Scan Commands

- `/p2pdetails scan`
- `/p2pdetails scan all`
- `/p2pdetails scan summary`
- `/p2pdetails scan isolated|unconnected|full|near|all [limit]`

### Config

- `nearCapacityReserve`: remaining channels threshold for near-capacity warning (`0` disables it)
- `extendedOutputLimit`: max output entries shown in TOP EXTENDED mode
- `scanDetailLines`: default detail lines per category in `/p2pdetails scan`

### Build

```bash
./gradlew clean build
```
