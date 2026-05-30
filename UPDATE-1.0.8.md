# Update Notes: 1.0.8

## 中文

### 新增与调整
- 新增道具 `P2P配置工具`（外观复用 AE2 内存卡模型）。
- `P2P配置工具` 右键打开 GUI，可输入频道并保存到工具自身。
- 现在可直接用 `P2P配置工具` 右键 P2P 隧道，立即将其设置为已保存频道（不再依赖“下一个频道”临时状态）。

### 输入体验优化
- GUI 文案简化为 `Frequency:`，移除了 `(1-65535)` 提示文本。
- 频道输入支持以下格式：
  - 十进制：如 `15781`
  - 十六进制带前缀：如 `0x3DA5`
  - 十六进制无前缀：如 `3DA5`

### 配方修复
- 修复 `P2P配置工具` 配方不生效问题。
- 改为代码侧注册配方，并兼容 AE2 内存卡的常见物品名差异，提升整合包兼容性。

### 版本
- Mod 版本提升为 `1.0.8`。

---

## English

### Added & Changed
- Added a new item: `P2P Config Tool` (uses AE2 memory card model style).
- Right-click the tool to open a GUI and save a frequency into the tool itself.
- You can now right-click a P2P tunnel directly with the tool to apply the saved frequency immediately (no temporary "next frequency" state required).

### Input UX Improvements
- GUI label is now simplified to `Frequency:` (removed the `(1-65535)` hint text).
- Frequency input now supports:
  - Decimal: e.g. `15781`
  - Hex with prefix: e.g. `0x3DA5`
  - Hex without prefix: e.g. `3DA5`

### Recipe Fix
- Fixed the issue where the `P2P Config Tool` recipe did not take effect.
- Recipe registration is now done in code, with compatibility fallback for common AE2 memory card item ID variants.

### Version
- Mod version bumped to `1.0.8`.
