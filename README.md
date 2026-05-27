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

- 基础语法
  - `/p2pdetails scan [mode] [limit]`
- 参数说明
  - `mode`（可选）：`summary`、`isolated`、`unconnected`、`full`、`near`、`all`
  - `limit`（可选）：每个分类最多显示条数，范围 `1~50`
  - 不传 `mode` 时默认 `all`
  - 不传 `limit` 时使用配置项 `scanDetailLines`（实际会被限制在 `1~20`）
- 子命令/模式详解
  - `/p2pdetails scan summary`
    - 仅显示统计摘要：总端口数、未绑定数、孤立频率、同频未连通、满载、近满载
  - `/p2pdetails scan isolated [limit]`
    - 显示“孤立频率端口”：同一频率只有输入端或只有输出端
  - `/p2pdetails scan unconnected [limit]`
    - 显示“同频未连通”：频率两端存在，但状态离线或未建立连接
  - `/p2pdetails scan full [limit]`
    - 显示“满载链路”：ME P2P 使用频道达到 Dense 容量上限
  - `/p2pdetails scan near [limit]`
    - 显示“近满载链路”：由 `nearCapacityReserve` 判定的预警区间
  - `/p2pdetails scan all [limit]`
    - 先输出摘要，再按分类输出问题明细
- 常用示例
  - `/p2pdetails scan`
  - `/p2pdetails scan all 10`
  - `/p2pdetails scan summary`
  - `/p2pdetails scan isolated 20`
  - `/p2pdetails scan near 5`
- 权限说明
  - 命令权限等级为 `0`，默认普通玩家可执行（以服务端权限策略为准）

### 配置文件与配置项

- 配置文件路径（常见）
  - 客户端/单机：`.minecraft/config/p2pdetails.cfg`
  - 服务端：`<server_root>/config/p2pdetails.cfg`
- 配置项说明
  - `nearCapacityReserve`
    - 进入“近满载”前保留频道数，`0` 为关闭预警
  - `showGenericChannelInfo`
    - 是否显示 AE2 通用频道占用补充行（默认 `false`，用于避免和 TOP 原生频道显示重复）
  - `extendedOutputLimit`
    - TOP `EXTENDED` 模式最大输出明细条数
  - `scanDetailLines`
    - `/p2pdetails scan` 各分类默认输出条数
- 生效方式
  - 游戏内 Mod 配置界面修改后会同步配置（无需重启游戏）
  - 手动编辑 `p2pdetails.cfg` 通常需要重新进入世界/重启游戏以确保加载最新值

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

- Base syntax
  - `/p2pdetails scan [mode] [limit]`
- Arguments
  - `mode` (optional): `summary`, `isolated`, `unconnected`, `full`, `near`, `all`
  - `limit` (optional): max lines per category, range `1~50`
  - Default `mode` is `all`
  - Default `limit` comes from config `scanDetailLines` (clamped to `1~20` in code)
- Subcommands / Modes
  - `/p2pdetails scan summary`
    - Summary only: total ports, unlinked, isolated, unconnected, full, near-capacity
  - `/p2pdetails scan isolated [limit]`
    - Shows isolated frequencies (only inputs or only outputs for a frequency)
  - `/p2pdetails scan unconnected [limit]`
    - Shows same-frequency links that are offline or not connected
  - `/p2pdetails scan full [limit]`
    - Shows full ME P2P links (used channels reached dense capacity)
  - `/p2pdetails scan near [limit]`
    - Shows near-capacity ME P2P links based on `nearCapacityReserve`
  - `/p2pdetails scan all [limit]`
    - Prints summary, then detailed category outputs
- Examples
  - `/p2pdetails scan`
  - `/p2pdetails scan all 10`
  - `/p2pdetails scan summary`
  - `/p2pdetails scan isolated 20`
  - `/p2pdetails scan near 5`
- Permission
  - Command permission level is `0` (normally usable by regular players, subject to server policy)

### Config File & Options

- Config file path (common)
  - Client/Single-player: `.minecraft/config/p2pdetails.cfg`
  - Dedicated server: `<server_root>/config/p2pdetails.cfg`
- Options
  - `nearCapacityReserve`
    - Remaining-channel threshold for near-capacity warning (`0` disables it)
  - `showGenericChannelInfo`
    - Whether to show extra generic AE2 channel lines (default `false` to avoid duplicate lines with TOP built-in display)
  - `extendedOutputLimit`
    - Max output entries shown in TOP `EXTENDED` mode
  - `scanDetailLines`
    - Default detail lines per category in `/p2pdetails scan`
- Reload behavior
  - Changes made via in-game Mod config UI are synced at runtime
  - Manual edits to `p2pdetails.cfg` usually require re-entering world/restarting game to guarantee reload

### Build

```bash
./gradlew clean build
```
