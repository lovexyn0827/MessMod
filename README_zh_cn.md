# MessMod

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/MessMod.png)

![](https://img.shields.io/modrinth/dt/messmod?label=Total%20Modrinth%20Downloads)

一个用于提供额外的世界访问能力及一些增强功能的Minecraft Mod。

正如其名，该模组包含了世界控制、信息显示、特性更改、操作优化以及一些Bug修复等多种功能，整体而言是相当的Messy。

其他语言： 
[English](/README.md)  

## 特色功能

- 更准确的实体碰撞箱显示；
- 十种直观易用的渲染器（详见下文）；
- 使/tick更简捷的工具物品；
- 实体信息实时显示与记录（导出为表格）；
- 从代码层面控制游戏运作；
- 功能多样的Accessing Path；
- 示波器与波形发生器；
- 导出指定区域为存档；
- 使用Ctrl + Z / Ctrl + U撤销/重做方块放置与破坏；
- 区块网格生成；
- 伪弱加载区块；
- 以及更多。

## 需求

1. Fabric Loader 0.8.0+  
2. gnembon的地毯Mod（fabric-carpet）（部分功能需要，所以强烈推荐，但并非必需）  
3. Minecraft 1.16.4及以上
4. Minecraft需要的所有事项

## 快速入门

### 安装

1. 从Modrinth下载与Minecraft版本匹配的JAR，并复制到`mod`文件夹。
2. 启动游戏，并打开一个创造存档。

### 尝试HUD

1. 按下`F3 + E`并注视任何一个实体。
2. 或按下`F3 + M`并四处走动，查看HUD是怎样工作的。

### 启用渲染器

1. 运行`/messcfg serverSyncedBox true`，这样所有实体都会显示一个绿色的碰撞箱。
2. 运行`/messcfg chunkLoadingInfoRenderer true`并手持鹦鹉螺壳，查看周围区块的加载状态。

### 打开示波器

1. 运行`/messcfg hayOscilloscope true`。
2. 放置一个干草块，并使用侦测器高频对其充能。
3. 右键干草块，查看波形。

### 延长物品栏

1. 运行`/messcfg hotbarLength 18`，然后物品栏槽位数量会加倍。

### 进一步探究选项

> 如果曾使用过Carpet模组，您可能会感觉下面的操作思路很熟悉。

1. 运行`/messcfg`。
2. 点击一个感兴趣的标签，如“渲染”或是“区块”。
3. 将鼠标悬置于一个选项上方，查看详细描述。
4. 试着用刚才的方法启用 / 修改一个选项。

### 下一步……

该文件的其余部分是MessMod的完整文档，包含对每条指令，每个选项与特性的详细描述。试着从其中抓取对您最有价值的功能，祝您好运！

## 命令

参数名称用尖括号括住，方括号中为可选部分。

### Accessing Path设置

##### `/accessingpath compile <name> <inputType>`

将名为`<name>`的自定义节点编译为字节码以提高执行效率。可以指定一个或多个`<inputType>`来指定前几个节点的预期输入类型，格式为`pkg1/Class1,pkg2/Class2`，其中第一个节点的输入类型必须是引用类型。

目前该功能可能存在一些问题。

##### `/accessingpath defineNode <name> <temproary> <backend>`

将`<backend>`指定的Accessing Path定义为一个名为`<name>`的自定义Accessing Path节点。如果 `<temproary>`属性为`false`， 该自定义节点会被存储到存档文件夹中。

##### `/accessingpath list`

获取自定义节点列表。

##### `/accessingpath undefineNode <name>`

删除名为`<name>`的自定义节点。

### 实体计数

##### `/countentities [<selector>]`

获取可由`<selector>`选取的实体的数量。忽略`<selector>`参数时给出当前维度实体总数。

##### `/countentities <selector> <stackedWith>`

获取可由`<selector>`选取且坐标与实体`<stackedWith>`完全相同的实体的数量。

##### `/countentities <selector> <stackedWith> <maxDistanceVec>`

获取可由`<selector>`选取且坐标与实体`<stackedWith>`的坐标的向量差各轴分量均小于`<maxDistanceVec>`对应轴上分量的实体的数量。

##### `/countentities <selector> <stackedWith> <maxDistance>`

获取可由`<selector>`选取且坐标与实体`<stackedWith>`的坐标间距离小于`<maxDistance>`的实体的数量。

### 在游戏内绘制标记

##### `/drawshape box <corner1> <corner2> [<color>] [<life>] [<fill>] [<space>]`

在`<corner1>`和 `<conrner2>`之间绘制一个立方体（轴对齐边界箱）。

`<color>`：立方体各棱的颜色。

`<fill>`：立方体各面的颜色。若需透明，请使用`reset`。

`<life>`：图形显示时长，以游戏刻计，默认为100。

`<space>`：图形所属的“图形空间”，用于在指令`/drawshape clear <space>`中批量移除图形。

##### `/drawshape line <corner1> <corner2> [<color>] [<life>] [<space>]`

再`<corner1>`和`<corner2>`之间绘制一条直线。

##### `/drawshape text <pos> <text> [<color>] [<life>] [<scale>] [<space>]`

在`<pos>`处绘制`<text>`指定的一条浮动文本。

`<scale>`：文本大小，默认为1。

##### `/drawshape image <pos> <axis> <url> [<life>] [<scale>] [<space>]`

从指定的`<url>`加载图片并绘制于`<pos>`处。

`<axis>`：绘制的图片所垂直的坐标轴。

`<scale>`：绘制图片的大小，以格计。默认为1格。 

##### `/drawshape clear [<space>]` 

清除绘制的图形。若“图形空间”`<space>`未指定，则清除所有图形。

### 方块状态校验

##### `/ensure <pos>`

获取`<pos>`处的方块以及方块实体（若有）信息，用以确认某个方块是否存在渲染错误或是幽灵方块。

### 实体行为设置

##### `/entityconfig <targets> enableStepHeight|disableStepHeight`

允许或禁止`<targets>`指定的实体直接走上方块。  

##### `/entityconfig localPlayer enableStepHeight|disableStepHeight`

允许或禁止客户端玩家直接走上方块。  

### 实体字段读写

##### `/entityfield <target> get <fieldName> [<path>]`

获取`<target>`指定的实体的对象中名为`<fieldName>`的字段，可以指定Accessing Path。如果没有加载混淆映射表，此处的名称是类似于`field_827`的intermediary names，需要人工查阅以便理解。  

##### `/entityfield <target> listAvailableFields`

获取`<target>`指定的实体的对象中所有字段的名称列表。  

##### `/entityfield <target> set <fieldName> <newValue> [<path>]`

将`<target>`指定的实体的对象中名为`<fieldName>`的字段的值设为`<newValue>`。支持的类型与相应的格式与Accessing Path字面值相似。 也可以给出一个AccessingPath来使新值写入到对应字段中的某个具体位置。

### 实体信息记录

##### `/entitylog sub <target> [<policy>]`

开始监视`<target>`所指定的实体并将其每一游戏刻的坐标与Motion以及被监视的字段存储到一个CSV表格文件中。注意部分记录会被临时保存到一个缓冲区而不是立即写入到文件当中，可以使用`/entitylog flush`将那些内容立即写入。

记录文件会被保存到世界文件夹中的`entitylog`文件夹中，文件名格式类似：

`2023-08-19_17-12-21@12-C-villager.csv`

其中“@”前为开始记录的时间，此后分别是实体数字ID、记录时机（“C”、“S”与“M”分别对应客户端，服务段与两端共存）和实体名称或者是类型ID。

也可以在参数`<policy>`指定一个存储策略，可能的取值与含义如下：

- `SERVER_ONLY`：只存储服务端的运算中记录的数据；

- `CLIENT_ONLY`：只存储客户端的运算中记录的数据；
- `SEPARATED`：同时记录服务端与客户端产生的数据，并存储到两个文件中；
- `MIXED`：同时记录服务端与客户端产生的数据，并存储到一个文件中（不推荐使用）。

##### `/entitylog unsub <target>`

停止监视`<target>`所指定的实体并将缓冲区中内容立即写入到文件。在停止后重新开始对一个实体监视会重新创建一个日志文件而非继续原文件。

##### `/entitylog listenField <type> <field> [<name> [<path>]]`

监视指定的实体的一个字段，可指定自定义名称和AccessingPath。`<type>`暂时不会将该指令的影响范围限制到其对应的实体类型。

##### `/entitylog flush`

将缓冲区中内容立即写入到文件而不停止监视。

##### `/entitylog stopListenField <field>`

停止监视一个字段。

##### `/entitylog listListenedFields`

列出被监听的所有字段。

##### `/entitylog autoSub <entityType>`

开始自动监视某一类型的实体。被监视实体的存储策略与默认存储策略相同。

##### `/entitylog stopAutoSub <entityType>`

停止自动监视某一类型的实体。

##### `/entitylog autoSubName <name>`

开始自动监视名为`<name>`所指定名称的实体。被监视实体的存储策略与默认存储策略相同。

##### `/entitylog stopAutoSubName <name>`

停止自动监视名为`<name>`所指定名称的实体。

##### `/entitylog countLoggedEntities`

获取目前正在被记录数据的实体数目。

##### `/entitylog setDefaultStoragePolicy <policy>`

设置默认的存储策略，未设置时为`SERVER_ONLY`。

### 实体信息侧边栏

##### `/entitysidebar add <target> <field> <name> [<whereToUpdate> [<path>]]`

向实体信息侧边栏中增加由实体`<target>`提供的一行信息，可以指定更新时机和AccessingPath。

支持的服务端更新时机（按时间排序）：

- `WEATHER_CYCLE`：天气和时间运算开始时；
- `CHUNK`：区块加载卸载和区块刻运算开始时；
- `SCHEDULED_TICK`：计划刻运算开始时；
- `VILLAGE`：袭击运算开始时；
- `BLOCK_EVENT`：方块事件运算开始时；
- `ENTITY`：实体运算开始时；
- `TILE_ENTITY`：方块实体运算开始时；
- `DIM_REST`：方块实体运算结束时；
- `TICKED_ALL_WORLDS`：所有维度运算完成且玩家输入等异步处理未开始时；
- `SERVER_TASKS`：玩家输入等异步处理开始时；
- `REST`：异步处理结束时。

支持的客户端更新时机（按时间排序）：

- `CLIENT_TICK_START`：客户端游戏刻运算开始时；
- `CLIENT_TICK_END`：客户端游戏刻运算结束时。

##### `/entitysidebar remove <name>`

从实体信息侧边栏中移除一行。

### 产生爆炸

##### `/explode <pos> <power> [<fire>]`

在`<pos>`处产生一个威力为`<power>`的爆炸，可以指定`<fire>`为`true`使爆炸生成火焰。爆炸威力可以是任意单精度浮点数，包括无穷大甚至NaN。

### 导出区域为存档

##### `/exportsave addComponent <comp>`

添加一个将被保存的存档组件。可用的组件类型有：

- `REGION`：区域文件，包括方块、方块实体与实体（1.16-）等；

+	`POI`：POI数据；
+	`GAMERULE`：Gamerule（游戏规则）数据；
+	`ENTITY`：实体数据（1.17+）；
+	`RAID`：袭击数据；
+	`MAP_LOCAL`：与选区相交的地图数据；
+	`MAP_OTHER`：与选区不相交的地图数据；
+	`ICON`：存档图标；
+	`ADVANCEMENTS_SELF`：导出者（若导出者不是玩家，则可以为所有玩家）的进度信息；
+	`ADVANCEMENT_OTHER`：导出者以外玩家的进度信息；
+	`PLAYER_SELF`：导出者（若导出者不是玩家，则可以为所有玩家）的玩家数据；
+	`PLAYER_OTHER`：导出者以外玩家的玩家数据；
+	`STAT_SELF`：导出者（若导出者不是玩家，则可以为所有玩家）的统计信息；

- `STAT_OTHER`：导出者以外玩家的统计信息；

+	`SCOREBOARD`：记分板数据；
+	`FORCE_CHUNKS_LOCAL`：选区内的强制加载区块数据；
+	`FORCE_CHUNKS_OTHER`：选取外的强制加载区块数据；
+	`DATA_COMMAND_STORAGE`：`/data`指令数据存储；
+	`CARPET`：Carpet配置文件；
+	`MESSMOD`：MessMod配置文件与自定义节点信息。

也可以使用类似于DOS文件名通配符的格式选取多个项目。

##### `/exportsave addRegion <name> <corner1> <corner2> [<dimension>]`

添加一个选区，选取的区域会被按区块对齐以便保存。如果没有指定`<dimension>`参数，则选区对应的维度以命令执行者所在的维度为准。

##### ` /exportsave deleteRegion <name>`

删除一个选区。

##### `/exportsave export <name> <worldgen>`

开始导出存档，导出的存档会保存在`存档文件夹/exported_saves`下。

可以使用`<worldGen>` 选项给出一个世界生成配置：

- `COPY`：与原存档相同；
- `VOID`：虚空；
- `BEDROCK`：一层基岩；
- `GLASS`：一层白色玻璃；
- `PLAIN`：一层草方块。

##### `/exportsave listComponents`

获取目前准备导出的所有存档组件。

##### `/exportsave listRegions`

获取已创建的所有选区。

##### `/exportsave preview <name> <ticks>`

预览一个选区，显示时长由`<ticks>`指定。

##### `/exportsave removeComponent <comp>`

排除一个存档组件。

##### `/exportsave reset`

恢复至初始状态。

### 向容器填充物品

##### `/fillinventory <corner1> <corner2> full <item>`

将`<corner1>`和`<corner2>`指定的位置之间所有容器（箱子、漏斗等）的所有槽位填充为`<item>`指定的物品。

##### `/fillinventory <corner1> <corner2> replace <from> <to>`

将`<corner1>`和`<corner2>`指定的位置之间所有容器中由`<from>`指定的物品替换为`<to>`。

##### `/fillinventory <corner1> <corner2> sorter <main> <occupier>`

将`<corner1>`和`<corner2>`指定的位置之间所有容器中按物品分类器的模式填充，即在第一个槽位放置一个有`<main>`指定的物品，并在其它槽位各放置一个`<occupier>`指定的物品。

### 冻结实体运算

##### `/freezentity freeze|resume <entities>`

冻结或恢复被选中的实体的运算。此命令现已弃用，建议改用`/lazyload`模拟弱加载区块。

### HUD自定义

##### `/hud subField target <entityType> <field> [<name> [<path>]]`

向目标实体信息HUD加入一个被监听字段，可指定自定义名称和AccessingPath。实体类型参数目前除参与提供对field参数的建议和获取对应字段（Field实例）外无其他作用。

##### `/hud subField client|server <field> [<name> [<path>]]`

向客户端或服务端玩家信息HUD加入一个被监听字段，可指定自定义名称和AccessingPath。

##### `/hud unsubField target|client|server <name>`

从一个HUD中移除一个字段监听项。

##### `/hud setHudTarget <profile>`

设置多人环境下服务端玩家信息HUD和目标实体信息HUD的数据获取中所用的玩家。

##### `/hud listFields target|client|server`

获取一个HUD中的自定义字段。

### 制造卡顿

##### `/lag once <nanoseconds> [<thread>]`

让游戏的某一线程`<thread>`卡死`<nonoseconds>`纳秒，可用于某些测试。如未指定线程则卡死服务端线程。

##### `/lag while <nanoseconds> <ticks> <phase>`

在此后`<ticks>`游戏刻内每个游戏刻的`<phase>`阶段开始时卡顿`<nanoseconds>`纳秒，用于更灵活地调整MSPT。

### 模拟弱加载区块

##### `/lazyload add <corner1>`

将方块坐标`<corner1>`所在的区块标记为弱加载。标记后区块中的实体将不会被运算。

##### `/lazyload remove <corner1>`

移除方块坐标`<corner1>`所在的区块的弱加载标记。

##### `/lazyload add <corner1> <corner2>`

将以方块坐标`<corner1>`所在的区块和方块坐标`<corner2>`所在的区块为两个相对顶点的矩形区域中区块标记为弱加载。

##### `/lazyload remove <corner1> <corner2>`

移除以方块坐标`<corner1>`所在的区块和方块坐标`<corner2>`所在的区块为两个相对顶点的矩形区域中区块的弱加载标记。

### 加载Java Agent

##### `/loadjavaagent fromFile | fromURL <path>`

从指定的本地JAR文件或URL加载Java Agent。

### 记录区块行为

##### ` /logchunkbehavior listSubscribed`

获取关注的区块事件列表。

##### `/logchunkbehavior start`

开始监听区块事件。记录的信息会被写入`世界文件夹/chunklog`下的一个CSV文件中。

文件中7列数据的意义如下：

- `Event`：事件的名称；
- `Pos`：对应的区块坐标；
- `Dimension`：事件发生的维度ID；
- `GameTime`：事件发生的游戏内时间（以游戏刻计）；
- `RealTime`：事件发生的相对真实时间（以纳秒计）；
- `Thread`：事件发生的线程，其中“Server Thread”为服务端主线程；
- `Cause`：事件发生的原因，默认被禁用，可以使用选项`blamingMode`启用；
- `Addition`：事件的附加信息。

##### `/logchunkbehavior stop`

停止监听区块事件。

##### `/logchunkbehavior subscribe <events>`

关注一个区块事件。支持的区块事件列表：

- `LOADING`：开始加载区块；
- `UNLOADING`：开始卸载区块；
- `GENERATION`：开始或继续生成区块；
- `UPGRADE`：开始一个区块生成阶段；
- `END_LOADING`：一个区块加载完成；
- `END_UNLOADING`：一个区块卸载完成；
- `END_GENERATION`：一个区块生成完成或暂停；
- `END_UPGRADE`：一个区块的一个生成阶段完成；
- `SCHEDULER_LOADING`：计划加载一个区块；
- `SCHEDULER_UNLOADING`：计划卸载一个区块；
- `SCHEDULER_GENERATION`：计划生成一个区块；
- `SCHEDULER_UPGRADE`：计划开始一个区块生成阶段；
- `TICKET_ADDITION`：添加区块加载票；
- `TICKET_REMOVAL`：移除区块加载票（不含过期）；
- `PLAYER_TICKER_UPDATE`：尝试更新玩家区块加载票；
- `ASYNC_TASKS`：执行`ServerChunkManager.Main`中的异步任务；
- `ASYNC_TASK_SINGLE`：执行单个异步任务；
- `ASYNC_TASK_ADDITION`：添加异步任务；
- `SCM_TICK`：`ServerChunkManager.tick()`方法；
- `CTM_TICK`：`ChunkTicketManager.tick()`方法；
- `SCM_INIT_CACHE`：清空`ServerChunkManager`的区块缓存；
- `CTPS_LEVEL`：调用`ChunkTaskPrioritySystem.updateLevel()`方法；
- `CTPS_CHUNK`：调用`ChunkTaskPrioritySystem.enqueueChunk()`方法；
- `CTPS_REMOVE`：调用`ChunkTaskPrioritySystem.removeChunk()`方法。

也可以使用类似于DOS文件名通配符的格式选取多个项目。

##### `/logchunkbehavior unsubscribe <events>`

取消关注一个区块事件。

### 记录实体死亡原因

##### `/logdeath sub <target>`

开始监听`<target>`指定的死亡事件。`<target>`具有如下格式：

````
killer+directKiller(cause->victim)@(min..max)
````

对参数简介如下：

- `killer`：“有意”杀死实体的实体。
- `directKiller`：更直接地造成死亡的实体。例如，当掠夺者射杀村民时，掠夺者是`killer`，而箭矢则是`directKiller`。
- `cause`：实体死亡的原因，如`explosion`（爆炸）、`drowned`（溺水）等。
- `victim`：被杀死的实体。
- `min`：“最后一击”的最低伤害值。
- `min`：“最后一击”的最高伤害值。

所有参数均可被忽略，而且前四个参数还可以被设为“*”，以不对其进行限制。

实例：`player+tnt(explosion->zombie)@(0..1)`，`(->item)`

下面是描述`<target>`格式的正则表达式，以供参考：

```
^(?<killer>[!a-zA-Z_]*|\*)??(\+(?<directKiller>[!a-zA-Z_]*|\*))??(\((?<cause>[!a-zA-Z_\.]*|\*)\))??\-\>(?<victim>[!a-zA-Z_]*|\*)??(@(?<min>[0-9\\.]+)??\.\.(?<max>[0-9\.]+)??)?$
```

##### `/logdeath unsub <target>`

停止监听一些死亡事件。可以使用DOS文件通配符以匹配多个对象。

##### `/logdeath subVictimField | subDamageField <target> <name> <path>`

监听被杀死的实体或者伤害对应的`DamageSource`对象的字段。注意`name`只是给予监听器的一个名称，而不是字段名，所监听的实际值由指定的Accessing Path获取。

##### `/logdeath unsubVictimField | unsubDamageField <target> <name>`

取消监听被杀死的实体或者伤害对应的`DamageSource`对象的字段。

##### `/logdeath setVisible <target> <visible>`

设置`<target>`对应的死亡的“死亡报告”是否会被输出到聊天栏。

##### `/logdeath stats [<target>]`

获取所监听的死亡事件的统计信息。

##### `/logdeath autoStats`

获取自动分类的实体死亡原因统计。

##### `/logdeath autoStats reset`

重置自动统计信息。

##### `/logdeath reset [<target>]`

重置统计信息。若未指定`<target>`，自动统计亦会被重置。

### 监视活塞推动实体

##### `/logmovement sub <target>`

监听实体受活塞，潜影盒以及潜影贝的推动影响的情况。

##### `/logmovement unsub <target>`

停止监听实体受推动影响的情况。

### 监听数据包传输

##### `/logpacket sub|unsub <type>`

监听|取消监听客户端和服务端之间传输的数据包。因为一些原因，结果只会被输出到日志中。

也可以使用类似于DOS文件名通配符的格式选取多个项目。

##### `/logpacket startRecording <format> <saveTo>`

开始录制数据包信息，并将其存储在`<saveTo>`指定的文件中。目前只支持CSV格式。

##### `/logpacket stopRecording`

停止录制数据包信息。

##### `/logpacket stats [<types>]`

获取统计信息。`<types>`中可以使用DOS文件名通配符以选择若干数据包类。

##### `/logpacket stats reset`

重置统计信息。

### MessMod配置

##### `/messcfg`

获取当前Mod版本和配置标签列表。  

##### `/messcfg reloadConfig`

从`mcwmem.prop`获取最新的配置信息。

##### `/messcfg setGlobal <option> <value>`

将选项`<option>`的对于新打开的存档的默认值及当前存档的局部取值设为`<value>`。

##### `/messcfg <option>`

查看选项`<option>`的值以及相关帮助信息。

##### `/messcfg <option> <value>`

在当前存档范围内将选项`<option>`的值设为`<value>`，详见文档的下一节。

##### `/messcfg list [<label>]`

列出带有某一标签的选项，如果不指定标签，则所有选项均会被列出。

可用的标签如下：

- `MESSMOD`：用于配置MessMod自身功能的选项；
- `ENTITY`：与实体相关的选项；
- `RENDERER`：与渲染器相关的选项；
- `INTERACTION_TWEAKS`：用于为交互提供便利的选项；
- `EXPLOSION`：与爆炸相关的选项；
- `RESEARCH`：主要面向机制研究的选项；
- `REDSTONE`：与调试红石电路有关的选项；
- `CHUNK`：与区块有关的选项；
- `BUGFIX`：与Bug修复有关的选项；
- `BREAKING_OPTIMIZATION`：会破坏部分原版机制的高强度优化功能；
- `MISC`：杂项。

### 修改实体属性

##### `/modify <targets> <key> <val>`

修改`<targets>`指定的实体的属性，是部分`/entityfield`的简便用法。  

##### `/modify <target> remove`

移除`<targets>`指定的实体。  

### 模拟实体移动

##### `/moventity <targets> <delta> entity self|piston|shulkerBox|player|shulker`

使用`Entity.move()`移动`<targets>`指定的实体，距离由`<delta>`指定，通常选择“self”作为最后一个参数。执行完成后输出实际位移。  

##### `/moventity <targets> <delta> projectile`

使用弹射物移动的方式移动`<target>`指定的实体，距离由`<delta>`指定。目前似乎存在一些Bug。

### 便捷地为实体命名

##### `/namentity <entities> <name>`

为选定的实体进行命名。

### Name Items Without Anvils

##### `/nameitem <name>`

将玩家（或执行此命令的实体）主手所持的物品命名为`<name>`。

### 杀死部分实体

##### `/partlykill <entities> <possibility>`

杀死一部分选中的实体，所选实体被杀死的概率为`<possibility>`.

### POI检索与可视化

##### `/poi get <pos>`

取`<pos>`处的POI（兴趣点）。 

##### `/poi getDistanceToNearestOccupied <pos>`

返回最近的村民工作站点在某种尺度下的距离（可能是以区段为单位的曼哈顿距离）。

##### `/poi scan <center> <radius> <type>`

寻找以`<center>`为中心，半径为`<radius>`的球中类型为`<type>`的POI，并输出所在方块坐标。

##### `/poi scanCobic <corner1> <corner2> <type>`

寻找给定的两个角落间长方体区域内类型为`<type>`的POI。

##### `/poi set <pos> <type> <replace>`

将`<pos>`处设为`<type>`指定的POI，若已有POI且`<replace>`为false则不会设置。

##### `/poi visualize <center> <radius> <type>` 

可视化到`<center>`的距离在`<radius>`（m）内且类型为`<type>`的POI。所有被可视化的POI会被显示为一个绿色立方体，同时若该POI被占用，它还会拥有一个红色框架。

### 模拟Raycast

##### `/raycast blocks <from> <to> [visual]`

检查从`<from>`到`<to>`的有向线段是否被方块阻挡，方式类似于弹射物的碰撞检查和爆炸接触率计算中的raycast。执行后输出检查过的所有方块坐标（会有重复），如果发生了阻挡就输出阻挡线段的方块的坐标及其阻挡线段的面以及交点的坐标，否则输出Missed。如果给出了visual，检查过程会被可视化，即可能检查的方块网格被显示为浅绿色，阻挡线段的方块碰撞箱会被显示橙色，所在网格会被显示为紫色，线段的被阻挡前后的部分分别为青色和红色。

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/raycast-block.png)

##### `/raycast entities <from> <to> <expand> <excludeSender> [visual]`

检查`<from>`到`<to>`的有向线段是否被方块阻挡，方式类似于弹射物的碰撞检查。执行后输出阻挡线段的实体类型（或自定义名称）及其ID和碰撞点坐标，否则输出Missed。`<expand>`可以影响获取实体的范围，模拟弹射物的碰撞检查时可以指定为弹射物碰撞箱宽度的一半加上1。

### 重复执行指令

##### `/repeat <times> <feedbacks> ...`

重复执行某个命令`<times>`次，可以在`<feedback>`中指定是否输出命令的输出。

### 实体骑乘

##### `/ride <passengers> <vehicle> <force>`

让`<passengers>`指定的实体骑乘`<vehicle>`。

### RNG控制

##### `/rng world setSeed <seed>`

设置当前维度的RNG（随机数生成器）种子为`<seed>`。  

##### `/rng world next int <bounds>`

取当前维度的随机数生成器生成的下一个在区间[0,`<bounds>`)内的整数。

##### `/rng world next int|float|double|boolean`

取当前维度的随机数生成器生成的下一个随机数。 

##### `/rng <target> ...`

和前三条命令类似，只是RNG来自`<target>`指定的实体而非该维度。  

### 底层版的setblock命令

##### `/setblockraw <pos> <block> [<flags> [<depth> [<p2>]]]`

直接使用`setBlockState()`方法放置方块。

`<flag>`为8时，可以用来放置幽灵方块。

若`<p2>`存在，该命令将以`/fill`的行为运作，并在`<pos>`和`<p2>`之间放置方块.

### 设置爆炸产生的方块

##### `/setexplosionblock <blockState> [<fireState>]`

使爆炸在破坏方块时放置`<blockState>`指定的方块而不是空气，并将爆炸将要放置的火全部替换成`<fireState>`指定的方块。  

### 生成

##### `/stackentity <entity> <count>`

在`<entity>`所在的位置生成(`<count>` - 1)个实体，以生成一个具有`<count>`个实体的实体堆。

### 方块实体读写

##### `/tileentity add <pos> <type> <tag>`

在`<pos>`添加`<type>`指定的方块实体。可以指定`<tag>`作为方块实体的初始数据。（若`<pos>`处的方块不支持该方块实体，设置可能会失败）

##### `/tileentity get <pos>`

取`<pos>`处的方块实体信息。  

##### `/tileentity set <pos> <type> [<tag>]`

设置`<pos>`处的为`<type>`指定的方块实体。可以指定`<tag>`作为方块实体的初始数据。

##### `/tileentity remove <pos>`

移除`<pos>`处的方块实体。在目前版本中，如果该处存在一个需要方块实体的方块，在移除后该处方块实体会被该方块重新设置（一个Bug）。  

### Update A Large Area Of Blocks

##### `/touch <from> [<to>]`

从所有方向对`<from>`和`<to>`之间的所有方块发送NC和PP方块更新。若未指定`<to>`，则只对`<from>`处的方块发出更新。

##### `/touch <from> <to> nc [<srcPos> [<srcBlock>]]`

对`<from>`和`<to>`之间的所有方块发送NC更新

若未提供任何可选参数，被选中的方块会从各个方向收到更新；若已指定，方块会被`World.updateNeighbor()` 更新，参数会从按指令参数原样传递。如果`<srcBlock>`被省略，世界中`<srcPos>`处的方块会被作为默认值。

##### `/touch <from> <to> pp [<maxUpdateDepth> [<flags> [<oldState> [<newState>]]]]`

对`<from>`和`<to>`之间的所有方块发送PP更新

因为涉及了太多技术细节，请阅读源码中的`TouchCommand.java`获取详细信息。

### 变量管理

##### `/variable set <slot> new <constructor> [<args>]`

使用`<constructor>`指定的构造器构建一个新对象放入变量`<slot>`当中。可以使用类似于方法节点的方式指定构造器，可用的格式如下：

- `packagename/ClassName`
- `packagename/ClassName<构造器参数数量>`
- `packagename/ClassName<构造器Descriptor>`

如果需要的话，可以使用一串用英文逗号分开的字面值作为参数。

##### `/variable set <slot> literal <value>`

将字面值`<value>`的值放入变量`<slot>`当中。

##### `/variable set <slot> entity [<path>]`

将实体存入`<slot>`指定的变量中。可以指定一个可选的Accessing Path，以在存储前对实体进行变换，并存储Accessing Path的输出而不是实体本身。

##### `/variable set <slot> <objSrc> [<path>]`

将`<objSrc>`提供的对象放入变量`<slot>`当中。可用的对象提供者有：

- `server`：当前的`MinecraftServer`实例；
- `sender`：指令执行者对应的`ServerCommandSource`实例；
- `world`：执行者所在维度的`ServerWorld`实例；
- `senderEntity`：执行者的`Entity`实例；
- `client`：`MinecraftClient`实例；
- `clientWorld`：当前的`ClientWorld`实例；
- `clientPlayer`：客户端玩家实例。
- `new`：新创建的对象。示例：`java/lang/Thread<0>()`。

可以指定一个可选的Accessing Path，以在存储前对对象进行变换，并存储Accessing Path的输出而不是对象本身。

##### `/variable map <slotSrc> <slotDst> <path>`

将变量`<slotSrc>`中的值经Accessing Path处理后的结果存储到变量`<slotDst>`中。

##### `/variable print <slot> array | toString | dumpFields`

输出变量`<slot>`的值，最后一个参数决定输出的格式：

- `array`：使用`Arrays.toString()`进行格式化，只适用于数组；
- `toString`：使用变量的`toString()`方法进行格式化，后面可指定一个Accessing Path来在输出前对对象进行一些变换；
- `dumpFields`：输出变量的所有非静态字段及内容，后面可指定输出时的最大展开层数。

##### `/variable list`

列出目前定义的变量列表。

### 信号发生器

##### `/wavegen new <pos> <wavedef>`

将`<pos>`处的织布机方块设为波形发生器（信号发生器），并发出由`<wavedef>`定义的波形。注意波形发生器只有在`<pos>`处的方块为织布机时才能正常工作。

波形可由如下两种语法定义：

- **简单模式** 波形以字符S开头，后面紧随一串形如`H刻数`或`L刻数`的“波形阶段”，其中H和L分别对应高低电平。

  例如，`SH15L9`表示一个15 gt高电平 + 9 gt低电平的周期性波形。

  信号输出在对应游戏刻的计划刻阶段开始时改变。

- **标准模式**  波形由若干个形如`(开始=>结束)L信号等级ST`的“波形阶段”组成，规定了每个阶段的开始时间，结束时间，输出等级，以及开始与结束时是否会发出方块更新。

  时间可以用两种格式指定。一种形如`+游戏刻偏移量 运算阶段`，表示阶段开始于上一个阶段结束若干游戏刻后指定的运算阶段（见`/entitysidebar`指令）开始时，或结束于阶段开始若干游戏刻后指定的运算阶段开始时；另一种形如`!游戏刻偏移量 运算阶段`，表示阶段开始或结束于在整个波形的若干游戏刻后指定运算阶段开始时。

  运算阶段亦可按下表缩写：

  | 运算阶段         | 缩写   |
  | ---------------- | ------ |
  | `WEATHER_CYCLE`  | `WTU`  |
  | `CHUNK`          | `CU`   |
  | `SCHEDULED_TICK` | `NTE`  |
  | `VILLAGE`        | `RAID` |
  | `BLOCK_EVENT`    | `BE`   |
  | `ENTITY`         | `EU`   |
  | `TILE_ENTITY`    | `TE`   |
  | `DIM_REST`       | `END`  |
  
  理论上，可以指定大于15，甚至是负值的红石信号。
  
  `S` 和`T`分别是用于抑制在阶段开始与结束时发出的方块更新的可选标志。
  
  同时，这里还给出了一处形如`次数[阶段序列]`（或`次数*[阶段序列]`）的语法，用于重复一系列波形阶段若干次。该语法的作用等价于将方括号内的内容原样重复指定次数，这意味着，括号内不允许使用形如`!刻数`的‘绝对时间偏移“。该结构可嵌套。
  
  此处给出两个实例：
  
  `(+5SCHEDULE_TICK=>+2DIM_REST)L15T`：在上一个阶段结束5 gt后的计划刻（`5SCHEDULE_TICK`）阶段开始输出等级为15的红石信号，持续到2 gt后的`DIM_REST`阶段（世界运算结束时），在阶段开始时发出方块更新，但不再阶段结束时发出更新。
  
  `(!0=>!5ENTITY)L10`：波形的第0 gt的计划刻阶段开始输出10级红石信号，持续到第5 gt的实体运算（`ENTITY`）阶段，并在开始与结束时发出方块更新。
  
  `3[(+0=>+0)L15 2[(+2=>+0)L15 (+0=>+0)L0]] (+1=>+3)L15`：展开为`(+0=>+0)L15 (+2=>+0)L15 (+0=>+0)L0 (+2=>+0)L15 (+0=>+0)L0 (+0=>+0)L15 (+2=>+0)L15 (+0=>+0)L0 (+2=>+0)L15 (+0=>+0)L0 (+0=>+0)L15 (+2=>+0)L15 (+0=>+0)L0 (+2=>+0)L15 (+0=>+0)L0 (+1=>+3)L15`
  
  次外，波形定义的开始处可以给出一个时间偏移，使波形的开始处相对于游戏时间（Game Time）为波形整体长度（由最后一个阶段的结束时刻决定）的整倍数的位置偏移若干游戏刻。注意时间偏移量必须包含符号（`+1`而不是`1`）。例如，`+3`表示波形开始于每次游戏时间除以波形长度的余数为3时。

##### `/wavegen remove <pos>`

Remove the wave generator at `<pos>`.

## 配置项

##### `accessingPathDynamicAutoCompletion`

支持自动补全Accessing Path中的字段名和方法名。

可能取值：`true`或`false`

默认值：`true`

##### `accessingPathInitStrategy`

共有3种初始化策略： 

 - 旧版本模式：每个Accessing Path只在第一次被使用时进行初始化，然后这一结果用于访问后续的所有输入对象 
 - 标准模式：Accessing Path会对每个不同对象进行初始化，然后结果会被缓存直到相应对象被清理。
 - 严格模式：Accessing Path每次被使用时都会重新进行初始化。

可能取值：

- `LEGACY`
- `STANDARD`
- `STRICT`

默认值：`STANDARD`

##### `allowSelectingDeadEntities`

允许目标选择器`@e`选中死亡的实体。

可能取值：`true`或`false`

默认值：`false`

##### `allowTargetingSpecialEntities`

允许玩家将光标对准物品、雪球和箭矢等实体以获取相应的指令建议。

可能取值：`true`或`false`

默认值：`false`

##### `antiHostCheating`

对局域网游戏的房主启用反作弊。

可能取值：`true`或`false`

默认值：`false`

##### `attackableTnt`

TNT可以被玩家的攻击杀死。

可能取值：`true`或`false`

默认值：`false`

##### `blameThreshold`

事件原因可以被相信并记录时的最小置信度。不要将其设为`IMPOSSIBLE`。

可能取值：

- `IMPOSSIBLE`
- `UNLIKELY`
- `POSSIBLE`
- `PROBABLE`
- `DEFINITE`

默认值：`POSSIBLE`

##### `blamingMode`

指定区块加载等事件的原因的记录方式。

- `DISABLED`：完全禁用原因记录。
- `SIMPLE_TRACE`：记录事件发生位置的堆栈踪迹而不进行反混淆。
- `DEOBFUSCATED_TRACE`：记录反混淆后的堆栈踪迹。输出质量最好，但会增大日志文件大小与性能开销。
- `ANALYZED`：使用堆栈踪迹计算出几个标签。这时日志文件会明显减小，但性能消耗却可能更大。

可能取值：

- `DISABLED`
- `SIMPLE_TRACE`
- `DEOBFUSCATED_TRACE`
- `ANALYZED`

默认值：`DISABLED`

##### `blockInfoRendererUpdateInFrozenTicks`

游戏运算被地毯端中/tick指令暂停时方块信息渲染器的行为。

可能取值：

- `NORMALLY`
- `PAUSE`
- `NO_REMOVAL`

默认值：`NORMALLY`

##### `blockPlacementHistory`

记录玩家最近放置的方块用于撤销/重做。如果期间方块被玩家以外的事物更改，那么撤销操作可能会引起不可预料的后果。

可能取值：`true`或`false`

默认值：`false`

##### `blockShapeToBeRendered`

选项`renderBlockShape`为`true`时被渲染的方块轮廓类型。`COLLIDER`对应方块的碰撞箱，`OUTLINE`对应方块在玩家对准时显示的边界。详见Github中Wiki。

可能取值：

- `OUTLINE`
- `SIDES`
- `VISUAL`
- `RAYCAST`
- `COLLISION`

默认值：`COLLISION`

##### `chunkLoadingInfoRenderRadius`

区块加载状态显示半径，以区块计。

可能取值：任意非负整数

默认值：`4`

##### `chunkLoadingInfoRenderer`

手持鹦鹉螺壳时显示区块加载状态。可能会被移至区块加载小地图（ChunkMap）中。

可能取值：`true`或`false`

默认值：`false`

##### `chunkLogAutoArchiving`

自动存档一个会话内产生的区块行为记录（若有），存档文件可在`世界文件夹/chunklog/archives`找到。

可能取值：`true`或`false`

默认值：`true`

##### `clayBlockPlacer`

使用粘土球放置任意技术性方块（如下界传送门）。粘土球的名称指定了放置的方块，例如，在手持名为`grass_block[snowy=true]`的粘土球时，可以通过右键放置积雪的草方块。

可能取值：`true`或`false`

默认值：`false`

##### `commandExecutionRequirment`

该Mod中定义的命令是否需要OP权限。

可能取值：`true`或`false`

默认值：`true`

##### `craftingTableBUD`

监听工作台收到的方块更新。

可能取值：`true`或`false`

默认值：`false`

##### `creativeNoVoidDamage`

为创造模式下的玩家禁用虚空伤害。

可能取值：`true`或`false`

默认值：`false`

##### `creativeUpwardsSpeed`

调节创造模式下玩家向上飞行（加）速度大小。以m/s为单位，玩家真正的最终速度大约为该选项值的150倍。

可能取值：任意实数

默认值：`NaN`

##### `debugStickSkipsInvaildState`

防止调试棒将方块调整到非法状态。目前这个选项在一些情况下不能正常工作，例如调整铁轨的shape属性时仍可以将其调整到非法状态使其掉落。

可能取值：`true`或`false`

默认值：`false`

##### `dedicatedServerCommands`

在单人游戏中启用专用于服务器的指令。

可能取值：`true`或`false`

默认值：`false`

##### `defaultSaveComponents`

默认包含在导出的存档中的存档组件。

可能取值：`[]`（空列表）或一个`a,b,c`形式的列表，包含一些下方的一些项目：

- `REGION`
- `POI`
- `GAMERULES`
- `RAID`
- `MAP_LOCAL`
- `MAP_OTHER`
- `ICON`
- `ADVANCEMENTS_SELF`
- `ADVANCEMENT_OTHER`
- `PLAYER_SELF`
- `PLAYER_OTHER`
- `STAT_SELF`
- `STAT_OTHER`
- `SCOREBOARD`
- `FORCE_CHUNKS_LOCAL`
- `FORCE_CHUNKS_OTHER`
- `DATA_COMMAND_STORAGE`
- `CARPET`
- `MESSMOD`

默认值：`REGION,POI`

##### `detailedChunkTaskLogging`

在区块事件记录器（`/logchunkbehavior`）生成的文件中包含关于区块任务的更多细节.

可能取值：`true`或`false`

默认值：`false`

##### `directChunkAccessForMessMod`

MessMod中一些持续工作的功能（如渲染器）可以直接从`ThreadedAnvilChunkStorage`中获取区块而不使用`ServerChunkManager`，以避免其对区块管理系统造成潜在影响。

可能取值：`true`或`false`

默认值：`true`

##### `disableChunkLoadingCheckInCommands`

禁用指令中对方块是否被加载的检查。

可能取值：`true`或`false`

默认值：`false`

##### `disableClientChunkUnloading`

忽略来自服务端的区块卸载请求。

可能取值：`true`或`false`

默认值：`false`

##### `disableCreativeForcePickup`

创造模式下的玩家在物品栏被填满时不再拾起箭矢和物品。

可能取值：`true`或`false`

默认值：`false`

##### `disableEnchantCommandRestriction`

移除`/enchant`中对附魔等级与附魔兼容性的检查。

可能取值：`true`或`false`

默认值：`false`

##### `disableExplosionExposureCalculation`

禁用爆炸接触率计算以缩短堆叠TNT的爆炸卡顿时间。这会使方块无法防止其后方实体被爆炸影响。
在安装Lithium后可能不能正常工作。

可能取值：`true`或`false`

默认值：`false`

##### `disableItemUsageCooldown`

禁用末影珍珠和紫颂果等物品的的使用冷却时长。

可能取值：`true`或`false`

默认值：`false`

##### `disableProjectileRandomness`

取消弹射物的随机速度，用于部分测试，不过不要忘记关闭。

可能取值：`true`或`false`

默认值：`false`

##### `dumpTargetEntityDataOnClient`

输出客户端实体信息而不是服务端实体信息。

可能取值：`true`或`false`

默认值：`false`

##### `dumpTargetEntityDataWithCtrlC`

使用Ctrl + C时输出目标实体信息。

可能取值：`true`或`false`

默认值：`false`

##### `dumpTargetEntityDataWithPaper`

主手持有纸的玩家右键实体时输出目标实体信息。

可能取值：`true`或`false`

默认值：`false`

##### `dumpTargetEntityNbt`

输出目标实体的NBT数据。如果玩家手持的物品有自定义名称，那么当物品没有附魔时该名称会被解析为一个作用于实体NBT的NBT路径，否则，这一名称会被作为应用于实体的Entity实例的一个Accessing Path。

可能取值：`true`或`false`

默认值：`true`

##### `dumpTargetEntitySummonCommand`

生成用于生成目标实体的一条指令。如果手持的物品被附魔，实体的完整NBT数据（不含UUID）会被添加到 指令中，否则，只有实体类型，维度、坐标、动量与朝向信息会出现在指令中。

可能取值：`true`或`false`

默认值：`true`

##### `enabledTools`

启用或禁用工具物品。

 - 骨头：`/tick step <骨头数目>` 
 - 红砖：`/tick freeze` 
 - 下界合金锭：`/kill @e[type!=player]`

可能取值：`true`或`false`

默认值：`false`

##### `endEyeTeleport`

在使用末影之眼时将玩家传送到玩家注视的地方。

可能取值：`true`或`false`

默认值：`false`

##### `entityExplosionImpulseScale`

调整爆炸对实体造成的变速大小。

可能取值：任意实数

默认值：`1.0`

##### `entityExplosionInfluence`

爆炸影响到实体时在聊天栏输出实体被影响的情况。在测试TNT复制阵列时请禁用该选项，否则游戏可能会被卡死。
与Lithium不兼容。

可能取值：`true`或`false`

默认值：`false`

##### `entityExplosionRaysLifetime`

设置实体爆炸射线的显示时长，单位为刻。

可能取值：任意整数

默认值：`300`

##### `entityExplosionRaysVisiblity`

启用或禁用实体爆炸射线渲染。在测试TNT复制阵列时请禁用该选项，否则游戏可能会被卡死。

可能取值：`true`或`false`

默认值：`false`

##### `entityLogAutoArchiving`

自动存档一个会话内产生的实体数据记录（若有），存档文件可在`世界文件夹/entitylog/archives`找到。

可能取值：`true`或`false`

默认值：`true`

##### `expandedStructureBlockRenderingRange`

增加结构方块的渲染距离。

可能取值：`true`或`false`

默认值：`false`

##### `fillHistory`

记录`/fill`产生的方块变更以便在将来撤销或重做。

可能取值：`true`或`false`

默认值：`false`

##### `fletchingTablePulseDetectingMode`

指定被记录的脉冲类型。

可能取值：

- `POSITIVE`
- `NEGATIVE`
- `BOTH`

默认值：`POSITIVE`

##### `fletchingTablePulseDetector`

记录制箭台接收到的红石脉冲的长度。

可能取值：`true`或`false`

默认值：`false`

##### `flowerFieldRenderer`

显示当前区域可能在使用骨粉时生成的花朵种类。

可能取值：`true`或`false`

默认值：`false`

##### `flowerFieldRendererRadius`

`flowerFieldRenderer`的最大渲染距离。

可能取值：任意正整数

默认值：`16`

##### `flowerFieldRendererSingleLayer`

将`flowerFieldRenderer`生成的图像渲染在同一层（范围内最高方块顶部的水平面）。

可能取值：`true`或`false`

默认值：`false`

##### `generateChunkGrid`

在地表生层一层玻璃来标记区块。

可能取值：`true`或`false`

默认值：`false`

##### `getEntityRangeExpansion`

在原版的`getEntity()`方法中，只有与给定AABB的切比雪夫距离小于2m的区段中的实体会被“看到” ，有时这会导致一些较大实体与外界的交互出现问题。将该选项改为一个较大值可以修复这一Bug。

可能取值：任意正实数

默认值：`2.0`

##### `hayOscilloscope`

将干草块作为一个示波器 / 信号分析仪。

可能取值：`true`或`false`

默认值：`false`

##### `hayOscilloscopeChannelVisibilityBroadcast`

同步所有客户端的示波器通道可见性设置。

可能取值：`true`或`false`

默认值：`true`

##### `hideSurvivalSaves`

隐藏可能的生存存档以防止其被意外地打开。只能在全局范围内设置。

可能取值：`true`或`false`

默认值：`false`

##### `hotbarLength`

物品栏可包含的物品堆叠数量。目前该功能并未完全完成，如物品栏的保存等特性暂不可用。

可能取值：1至36间的一个整数（含两端）

默认值：`9`

##### `hudAlignMode`

设置实体信息HUD的位置。

可能取值：

- `TOP_LEFT`
- `TOP_RIGHT`
- `BOTTIM_LEFT`
- `BOTTOM_RIGHT`

默认值：`TOP_RIGHT`

##### `hudStyles`

HUD的渲染样式, 包括了下面的零个至多个标志: 

 - B：渲染背景
 - L：左对齐行标题并右对齐数据
 - R：将行标题改为红色

可能取值：任意字符串

默认值：`(BL)^2/(mR)`

##### `hudTextSize`

调节HUD字体大小。

可能取值：任意正实数

默认值：`1.0`

##### `independentEntityPickerForInfomation`

独立地为信息提供者（目前只包含指令UUID建议）选取目标实体。

可能取值：`true`或`false`

默认值：`false`

##### `interactableB36`

允许玩家破坏36号方块或对准它放置物品。

可能取值：`true`或`false`

默认值：`false`

##### `language`

该Mod的主要语言。

可能取值：

- `-FOLLOW_SYSTEM_SETTINGS-`
- `zh_cn`
- `zh_cn_FORCELOAD`
- `en_us`
- `en_us_FORCELOAD`

默认值：`-FOLLOW_SYSTEM_SETTINGS-`

##### `flowerFieldRendererSingleLayer`

将织布机作为一个波形发生器，其输出可由`/wavegen`指令配置。

可能取值：`true`或`false`

默认值：`false`

##### `maxClientTicksPerFrame`

FPS低于20时每帧可以运行多少个客户端游戏刻。

可能取值：任意正整数

默认值：`10`

##### `maxEndEyeTpRadius`

设置endEyeTeleport功能的最远传送距离。

可能取值：任意正实数

默认值：`180`

##### `minecartPlacementOnNonRailBlocks`

允许玩家在地面上直接放置矿车。

可能取值：`true`或`false`

默认值：`false`

##### `mobFastKill`

在生物被`/kill`杀死时被直接移除而非承受伤害。

可能取值：`true`或`false`

默认值：`false`

##### `optimizedEntityPushing`

跳过不会被推动的实体的挤压运算。实体挤压伤害会被影响。

可能取值：`true`或`false`

默认值：`false`

##### `playerInputsWhenScreenOpened`

按下指定按键（默认为F6）时，允许玩家在进行输入指令等操作时移动或改变视角。

可能取值：`true`或`false`

默认值：`false`

##### `playerInputsWhenScreenOpenedHotkey`

`playerInputsWhenScreenOpened`选项使用的快捷键，由按键码（Key code）指定：

- `F1` - `F12`：290 - 301

可能取值：`true`或`false`

默认值：`false`

##### `projectileChunkLoading`

允许弹射物在其运算过程中自动加载区块，在制作珍珠炮时可能会有所帮助。注意该功能可能会在弹射物以极高的速度飞行时造成极大的卡顿。

可能取值：`true`或`false`

默认值：`false`

##### `projectileChunkLoadingPermanence`

弹射物在`projectileChunkLoading`启用时永久加载区块。

可能取值：`true`或`false`

默认值：`false`

##### `projectileChunkLoadingRange`

`projectileChunkLoading`功能中创建的强加载范围半径。

可能取值：任意非负整数

默认值：`3`

##### `projectileRandomnessScale`

弹射物随机性大小。大部分弹射物附加的随机初始速度会被乘以该选项的值。

可能取值：任意实数

默认值：`1.0`

##### `quickMobMounting`

潜行时用刷怪蛋向载具中放置生物。

可能取值：`true`或`false`

默认值：`false`

##### `quickStackedEntityKilling`

杀死被砖块敲击的实体以及所有位置与其相同的实体。可以使用指令`quickStackedEntityKillingOneTypeOnly`规定是否只杀死与玩家直接交互的实体同类型的实体。

可能取值：`true`或`false`

默认值：`false`

##### `quickStackedEntityKillingOneTypeOnly`

在触发`quickStackedEntityKilling`功能时只杀死被砖块敲击的实体以及所有位置与其相同的实体。

可能取值：`true`或`false`

默认值：`false`

##### `railNoAutoConnection`

防止铁轨的形态被邻近的铁轨影响。

可能取值：`true`或`false`

默认值：`false`

##### `rejectChunkTicket`

防止区块被以某种形式加载。

可能取值：`[]`（空列表）或一个`a,b,c`形式的列表，包含一些下方的一些项目：

- `start`
- `dragon`
- `player`
- `forced`
- `light`
- `portal`
- `post_teleport`
- `unknown`

默认值：`[]`

##### `renderBlockShape`

方块轮廓渲染。

可能取值：`true`或`false`

默认值：`false`

##### `renderFluidShape`

流体轮廓、高度和流向向量渲染。

可能取值：`true`或`false`

默认值：`false`

##### `renderRedstoneGateInfo`

显示玩家注视的红石中继器或红石比较器的信号等级。

可能取值：`true`或`false`

默认值：`false`

##### `resistanceReducesVoidDamage`

抗性提升效果可以减免虚空伤害。

可能取值：`true`或`false`

默认值：`false`

##### `serverSyncedBox`

服务端碰撞箱显示。

可能取值：`true`或`false`

默认值：`false`

##### `serverSyncedBoxRenderRange`

服务端碰撞箱的渲染范围（以m为单位的切比雪夫距离）。任何非正值会被认为是无穷大。

可能取值：任意实数

默认值：`-1`

##### `serverSyncedBoxUpdateModeInFrozenTicks`

游戏运算被地毯端中/tick指令暂停时服务端碰撞箱渲染器的行为。

可能取值：

- `NORMALLY`
- `PAUSE`
- `NO_REMOVAL`

默认值：`NORMALLY`

##### `skipUnloadedChunkInRaycasting`

忽略未加载区块中的潜在碰撞，可以降低远距离raycast的卡顿。

可能取值：`true`或`false`

默认值：`false`

##### `skippedGenerationStages`

跳过一些世界生成阶段。不支持跳过`biome`和`full`，它们的缺失会使服务端崩溃。

可能取值：`[]`（空列表）或一个`a,b,c`形式的列表，包含一些下方的一些项目：

- `empty`
- `structure_starts`
- `structure_references`
- `noise`
- `surface`
- `carvers`
- `liquid_carvers`
- `features`
- `light`
- `spawn`
- `heightmaps`

默认值：`[]`

##### `smartCursorCustomWordDelimiters`

自定义的分隔符列表，在smartCursorMode = CUSTOM时有效。此处出现的所有字符均会被视为分隔符。

可能取值：任何字符串

默认值：` `

##### `smartCursorMode`

在使用Ctrl + 方向键或Ctrl + 退格编辑指令时，允许光标在空格以外的分隔符（如实体选择器中的逗号）处停住。

可能取值：

- `VANILLA`
- `NON_LITERAL`
- `NERDY`
- `NORMAL`
- `GREEDY`
- `CUSTOM`

默认值：`VANILLA`

##### `stableHudLocation`

使HUD的位置在数据长度频繁变化时更加稳定。

可能取值：`true`或`false`

默认值：`true`

##### `strictAccessingPathParsing`

解释阶段严格检查Accessing Path。目前这一严格检查系统尚未完善，不建议启用该选项。

可能取值：`true`或`false`

默认值：`false`

##### `superSuperSecretSetting`

wlujkgfdhlqcmyfdhj...千万不要打开！

可能取值：`true`或`false`

默认值：`false`

##### `survivalStatusBarInCreativeMode`

创造模式下显示生存模式状态栏（生命值与饥饿值）。

可能取值：`true`或`false`

默认值：`false`

##### `survivalXpBarInCreativeMode`

创造模式下显示经验栏。

可能取值：`true`或`false`

默认值：`false`

##### `tntChunkLoading`

允许TNT实体在其运算过程中自动加载区块。

可能取值：`true`或`false`

默认值：`false`

##### `tntChunkLoadingPermanence`

TNT在`tntChunkLoading`启用时永久加载区块。

可能取值：`true`或`false`

默认值：`false`

##### `tntChunkLoadingRange`

设置`tntChunkLoading`功能中创建的强加载范围的半径。

可能取值：任意非负整数

默认值：`3`

##### `tpsGraphScale`

缩放TPS（MSPT）图表以使其可以在屏幕中完整显示。

可能取值：任意正实数

默认值：`1.0`

##### `vanillaDebugRenderers`

启用一些原版调试渲染器。

可能取值：`[]`（空列表）或一个`a,b,c`形式的列表，包含一些下方的一些项目：

- `pathfindingDebugRenderer`
- `waterDebugRenderer`
- `chunkBorderDebugRenderer`
- `heightmapDebugRenderer`
- `collisionDebugRenderer`
- `neighborUpdateDebugRenderer`
- `caveDebugRenderer`
- `structureDebugRenderer`
- `skyLightDebugRenderer`
- `worldGenAttemptDebugRenderer`
- `blockOutlineDebugRenderer`
- `chunkLoadingDebugRenderer`
- `villageDebugRenderer`
- `villageSectionsDebugRenderer`
- `beeDebugRenderer`
- `raidCenterDebugRenderer`
- `goalSelectorDebugRenderer`
- `gameTestDebugRenderer`

默认值：`[]`

## 快捷键

**`F3 + E`**：开关显示当前玩家注视的实体信息的HUD。  

**`F3 + M`**：开关显示客户端玩家信息的HUD。与投影的默认设置冲突，可以手动修改。

**`F3 + S`**：开关显示服务端玩家信息的HUD。

**`Ctrl + Z`**：撤销方块放置/破坏及`/fill`操作（需要`blockPlacementHistory`和`fillHistory`）

**`Ctrl + Y`**：重做方块放置/破坏及`/fill`操作（需要`blockPlacementHistory`和`fillHistory`）

**`Ctrl + C`**：在聊天栏输出玩家注视的实体的NBT或生成相应实体所用的指令。（需要`dumpTargetEntityDataWithCtrlC`）

## 渲染器

#### 实体信息HUD

内容参考下文。实体的所有数据取自服务端游戏刻末尾,客户端玩家信息取自客户端tick末。

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/HUD-demo.png)

#### 实体爆炸射线

显示爆炸中心与实体碰撞箱内一些选中的点间的用于决定实体内爆炸影响的程度的线段。射线中被阻挡的部分显示为红色，否则为绿色。   

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/explosion-ray-demo.png)

#### 服务端实体碰撞箱

不要总是相信F3 + B提供的碰撞箱，因为它们是经客户端为使运动更平滑调整过后的，在运动时可能会“跟不上”服务端。实体的碰撞箱会被渲染为绿色。

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/server-synced-box-demo-0.png)

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/server-synced-box-demo-1.png)

#### 流体方块数据

显示玩家对准的流体的等级、高度、范围和流向向量。

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/fluid-info-demo.png)

#### 方块边界箱

显示玩家对准的方块的碰撞箱或OUTLINE Shape。

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/block-box-demo.png)

#### 红石装置信号输出等级

显示玩家对准的中继器和比较器的输出等级。

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/redstone-gate-demo.png)

#### 区块加载状态显示

显示区块的加载状态，分强加载、弱加载与边界加载与以下三级，分别在区块中心最高处显示为红色、绿色与灰色的半透明色块。需要启用选项`chunkLoadingInfoRenderer`并手持鹦鹉螺壳。

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/chunk-loading-status.png)

## 工具物品（需要Carpet和选项`enabledTools`）

#### 红砖

使用/tick freeze暂停服务端游戏刻。 

#### 骨头

在服务端游戏刻被/tick freeze暂停时继续执行一段时间，执行的刻数等于该组骨头的个数。 

#### 下界合金锭

直接移除所有非玩家实体。  

## HUD信息项

首行包含实体ID（等于该实体对象创建前创建的Entity实例总数），名称（`/summon`中使用的实体类型或自定义名称），年龄（实体最近一次被加载后存在的刻数）。 
`Pos`：实体的坐标。 

`Motion`：Entity类中域变量`motion`（MCP），`velocity`（Yarn）或`deltaMovement`（官方）的值,一般接近或等于实体的速度。 

`Delta`：实体上一刻的位移，可视为速度。 

`Yaw,Pitch`：实体的水平方向角和仰角。 

`Fall Distance`：实体自上一次着陆以来的向下移动的长度，但也可能受到一些其他因素的影响，如落入岩浆。 

`General State`：所有实体共有的一些布尔（非真即假）属性，详见下文。 

`Health`：LivingEntity（生物、玩家和盔甲架）的生命值。 

`Forward,Sideways,Upward`：与实体运动AI有关。 

`Speed`：与实体运动速度（分别是路上和空中）正相关。 

`Living State`：`LivingEntity`共有的一些布尔（非真即假）属性，详见下文。 

`Fuse`：TNT的引线长度，单位为刻。人工点燃的TNT中值总为80。 

`Power`：火球的加速度。注意，火球有0.05gt^-1的阻力，所以不会一直加速。 

`Velocity Decay`：船的阻力系数，在不同地面上不同。  

## 缩写的状态（state）

### 通用

`Gl` :拥有该状态的实体有一个发光的轮廓。 

`Inv` :拥有该状态的实体免疫大部分伤害。 

`Col` :拥有该状态的实体毁于其他实体发生碰撞。 

`NG` :拥有该状态的实体不受重力影响。 

`HC` :表示实体在上一次移动中发生了水平方向的碰撞。 

`VC` :表示实体在上一次移动中发生了竖直方向的碰撞。 

`Wet` :表示实体的一部分处于水中。

`Sbm`：实体浸没于水中

`Sp` :表示实体在疾跑。 

`Sn` :表示实体在潜行。 

`De` :表示实体在下蹲（类似于潜行)。 

`Sw` :表示实体在游泳。 

`Og` :表示实体已经着地。

### Living

`Hurt` :表示实体在上一刻中受到了伤害。 

`Fly` :表示实体在使用鞘翅飞行。 

`Slp` :表示实体在睡觉 

`Dead` :表示实体的生命值是0或更低，或者说已经死亡。

## Accessing Path

见[Wiki](https://github.com/lovexyn0827/MessMod/wiki/Accessing-Path_zh_cn)。

## 实体选择器选项

#### `id`

允许取值：一个整数或区间，和原版的level选项类似

选择数字ID（对应entityId或networkId字段）为指定值或属于指定区间的实体。

#### `side`

允许取值：`client`或`server`

指定从何处选取实体。注意该特性不是线程安全的，所以它只应用于一些简单的没有其他影响（一般为只有读操作）的指令中。

该特性只在单人游戏中可用。

#### `typeRegex`

允许取值：一个在引号内的正则表达式。

指定选取的实体类型ID（含命名空间）满足的正则表达式。

#### `nameRegex`

允许取值：一个在引号内的正则表达式。

指定选取的实体名称满足的正则表达式。

#### `class`

允许取值：一个在引号内的正则表达式。

指定选取的实体类名（包名可选）满足的正则表达式。

## 示波器

选项`havOscilloscope`为true时，干草块可被用作示波器，以记录输入的红石信号。

右键干草块可打开如下的GUI：

![oscilloscope_gui_zh_cn.png](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/oscilloscope_gui_zh_cn.png)

### 通道

每个包含接收着红石信号的干草块的位置被视为一个通道，由一个数字ID和底部方块在地图中显示的颜色标识。在干草块第一次检测到红石信号时，一个通道就会被创建，此后其ID和颜色将不再变化。

值得注意的是，因为通道与位置一一对应，如果干草块被（活塞等）移动，那么它将与一个新的通道相绑定。换句话说，通道不能被移动。

### 工作模式

示波器具有数字模式和模拟模式两种工作模式。

如下图，模拟模式下，不同通道的波形会被显示在相同的位置，如下图所示：

![oscilloscope_analog.png](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/oscilloscope_analog.png)

而数字模式下，不同通道的波形会平行显示，且只显示各通道有没有受到信号，而不考虑信号等级。

![oscilloscope_digital.png](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/oscilloscope_digital.png)

In both modes, holding left button of the mouse when the cursor is on some waves, a tool tip is drawn to display the level, start time, end time, and length of pulses.

两种模式下，对着波形按下鼠标左键可显示一个浮动框，以查看对应的信号等级，开始时间，结束时间，和信号脉冲的长度。

### 波形区域

可以使用鼠标滚轮简单地操作波形区域：

**`滚轮`**：水平滑动；

**`Shift + 滚轮`**：快速水平滑动

**`Crtl + 滚轮`**：水平缩放

**`Alt + 滚轮`**：竖直滑动

**`Alt + Shift + 滚轮`**：快速竖直滑动

**`Crrl + Alt + 滚轮`**：水平缩放

次外，还可以使用键盘方向键来进行滑动或缩放（按住`Ctrl`）。

### 触发

当接收到的信号等级上升或下降到一个设定的等级（触发电平），对应通道会产生一个称为“触发”的事件。点击通道设置栏中的触发模式设置可开始监听来自特定通道的触发。

检测到触发时，波形区域会自动发生滚动，以将产生触发的边沿对齐到水平标记（波形区上方的黄色三角，可拖动）的位置。

波形区域右侧也有一系列可拖动的小三角，用于标识不同通道的触发电平。

### 主动探测

主动探测使示波器可以检测到不产生方块更新的信号等级变化（例如当红石线改变朝向，并因为连接到一个方块，或从一个方块断开时）该模式下，干草块将会在每个运算阶段开始时测量其接受的信号等级，并检查是否与先前的测量不同。

所有通道均默认为主动测量模式，如需禁用，可取消勾选通道列表中的“主动”复选框。

值得注意的是，所有通道必须先由一次带有方块更新的信号等级变动创建后才能主动探测信号。

### 微时序支持

默认情况下，水平轴上的每个游戏刻会被划分为若干部分，每一部分代表一个运算阶段（如计划刻和实体运算）。不同运算阶段中的边沿会根据它们所在的运算阶段，显示在不同的水平位置。

可以勾选

## Mapping加载机制

1. 如果Minecraft已经被反混淆为Yarn名（由Entity类判断），则Mapping不会被加载。
2. 在`mapping`文件夹下查找对应的Mapping（`<Minecraft版本>.tiny`，如`1.16.4.tiny`）并加载。
3. 否则，从Fabric的官方Maven仓库下载对应版本的最新Mapping。
4. 否则，若安装有TIS Carpet Addition，则使用TIS Carpet Addition内置的Mapping。
5. 若全部失败，Mapping不会被加载。

## 高级Mixin

该模组当中的一些Mixin可能会对MC的运行性能造成较为明显的影响或对涉及多线程的原版行为产生意外的影响，所以，那些Mixin会被定义为可选的。默认情况下，这些Mixin处于启用状态，如果需要禁用相关的Mixin，可以在标题界面下按下F8（不适用于MacOS）或者修改游戏目录下的`advanced_mixins.prop`。在修改完毕后需要重启客户端或服务端才能使设置生效。

禁用高级Mixin可能会使相关的功能不再可用。

## 其他特性

1. 在未安装Carpet时执行命令遇到未知错误时会输出Stacktrace，在安装Carpet后也可以通过启用`superSecretSetting`规则做到这一点。
2. 在第一次在安装有MessMod时打开某一个生存存档时会弹出一条警告。

## 注意事项

1. 该Mod仍在开发中，一些功能可能不可用或存在bug，若发现欢迎反馈。  
3. 该版本对专用服务器的支持很弱，存在大量稳定性问题，一般情况下请只在单人游戏或局域网（仅房主所在客户端）中使用该Mod。  
4. 一些命令，像/explode \~ \~ \~ 2147483647 true可能造成服务端卡死，请注意。
4. 该模组不是为生存模式设计的，因为它可能会破坏原版机制或允许玩家意外作弊，尤其是当一些选项被修改后。为确保这一点，可以启用选项`hideSurvivalSaves`。

## 关于

这个Mod最初是我在2021年2月为了研究实体运动而开发的，因此HUD、碰撞箱、工具物品与`/entityfield`是这个Mod最早的一批特性。后来，更多的特性在它们被用到时被引入

在2022年4月后我加快了Mod的开发进度，添加了很多新特性并对一些旧特性进行了彻底的重构。截止到2025年10月19日，该Mod共添加了38条指令，100个选项和10个渲染器。