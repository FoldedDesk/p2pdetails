# Update Notes: 1.0.4 -> 1.0.7

## English

### Player-facing Changes
- Added full English localization (`en_us.lang`) and aligned Chinese localization text.
- Reduced duplicated TOP lines by hiding redundant vanilla-style channel info by default.
- Added scan command improvements and docs:
  - `/p2pdetails scan [summary|isolated|unconnected|full|near|all] [limit]`
  - clearer defaults and limits for detail output.
- Added/clarified config options:
  - `showGenericChannelInfo`
  - `nearCapacityReserve`
  - `extendedOutputLimit`
  - `scanDetailLines`
- Improved config hot-reload behavior from in-game config UI.

### Project / Release Pipeline
- README is now bilingual (Chinese + English) with command and config guidance.
- Added automated weekly build workflow.
- Added CI build on push to `main`.
- Added tag-triggered GitHub Release workflow for packaged releases.

### Compatibility
- Target platform remains `Minecraft 1.12.2 + AE2 + TOP`.

---

## 中文

### 玩家可见更新
- 新增完整英语本地化（`en_us.lang`），并同步优化中文本地化文本。
- 默认减少 TOP 中重复信息，隐藏与原版/现有显示重复的频道提示行。
- 扫描命令能力与文档完善：
  - `/p2pdetails scan [summary|isolated|unconnected|full|near|all] [limit]`
  - 明确了默认参数行为与明细条数限制。
- 新增/明确以下配置项：
  - `showGenericChannelInfo`
  - `nearCapacityReserve`
  - `extendedOutputLimit`
  - `scanDetailLines`
- 改进了游戏内配置界面修改后的热更新生效逻辑。

### 项目 / 发布流程更新
- README 升级为中英双语，并补充命令与配置说明。
- 新增每周自动构建工作流。
- 新增 `main` 分支 push 自动构建。
- 新增基于 tag 的 GitHub Release 自动发布工作流。

### 兼容性
- 目标平台仍为 `Minecraft 1.12.2 + AE2 + TOP`。
