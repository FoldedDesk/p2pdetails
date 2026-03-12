# P2P Details

1.12.2 Forge addon for `Applied Energistics 2 UEL v0.56.7` and `TheOneProbe-CE 1.3.6-fix1`.

## 当前功能

- TOP 显示 P2P 频率
- TOP 显示 P2P 是否连接
- TOP 显示 P2P 连接目标坐标
- ME P2P 显示已传输频道数
- ME P2P 输入端显示总传输频道
- ME P2P 输出端显示单输出承载
- EXTENDED 模式下列出活跃输出端和各自频道占用
- ME P2P 满载时显示警告
- ME P2P 接近满载时显示预警
- 对 AE2 原版需要频道的部件/方块补充频道显示
  - 例如接口部件、接口方块等
- 对封包合成系列模组增加可选兼容
  - 检测到模组加载后，尝试反射读取其 AE2 网络节点并显示频道占用

## 工程说明

- 依赖 jar 直接使用当前目录下的本地文件:
  - `ae2-uel-v0.56.7.jar`
  - `TheOneProbe-CE-1.12-1.3.6-fix1.jar`
- Minecraft / Forge 目标:
  - `1.12.2`
  - `Forge 14.23.5.2860`
- 构建链:
  - `ForgeGradle 3`
  - `Gradle 4.10.3`
- Java:
  - `Java 13` 运行 Gradle / ForgeGradle
  - 编译目标仍是 `Java 8`

## 构建

- 当前项目已在 [gradle.properties](/Users/danzer/P2PDetails/gradle.properties) 固定 `org.gradle.java.home` 到本机的 `Java 13`
- 如果你想在当前 shell 里也切到 Java 8:
  - `source ./use-java8.sh`
- 如果你想把当前 shell 切到和 Gradle 一致的 Java 13:
  - `source ./use-java13.sh`
- 如果你想把当前 shell 切到和 Gradle 一致的 Java 17:
  - `source ./use-java17.sh`
- Unix/macOS:
  - `./gradlew build`
- Windows:
  - `gradlew.bat build`

## 说明

- 已附带 `gradlew` wrapper。
- 首次执行 wrapper 会下载 `Gradle 4.10.3`。
- Forge 相关依赖已额外配置国内镜像回退:
  - `https://forge.fastmcmirror.org`
  - `https://mirror.sjtu.edu.cn/bmclapi/maven`
- 当前环境没有执行完整编译:
  - 外网下载仍可能影响 Gradle/ForgeGradle 依赖拉取
  - 这个 1.12.2 工程现在使用 `Java 13` 跑构建链，产物仍按 `Java 8` 目标编译
