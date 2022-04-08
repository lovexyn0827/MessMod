# MessMod
一个用于提供额外的世界访问能力及一些增强功能的Minecraft Mod  

正如其名字中所说，这个Mod包含了很多方面的功能（世界控制，信息显示，特性更改以及一些Bug修复等），有些还是随便写的，非常乱，就连代码风格也很乱。

其他语言： 
[English](/README.md)  

## 需求
1. Fabric Loader 0.7.4+  
2. gnembon的地毯Mod（fabric-carpet）（部分功能需要，所以强烈推荐，但并非必需）  
3. Minecraft 1.16.4或1.16.5  
4. Minecraft 1.16.x 需要的所有东西 

## 命令
参数名称用尖括号括住，方括号中为可选部分。

##### `/ensure <pos>`

获取`<pos>`处的方块以及方块实体（若有）信息，用以确认某个方块是否存在渲染错误或是幽灵方块。

##### `/entityconfig <targets> enableStepHeight|disableStepHeight`

允许或禁止`<targets>`指定的实体直接走上方块。  

##### `/entityconfig localPlayer enableStepHeight|disableStepHeight`

允许或禁止客户端玩家直接走上方块。  

##### `/entityfield <target> get <fieldName>`

获取代表target指定的实体的对象中名为`<fieldName`>的字段。如果没有加载混淆映射表，此处的名称是类似于field_827的intermediary names，需要人工查阅以便理解。  

##### `/entityfield <target> listAvailableFields`

获取代表`<target>`指定的实体的对象中所有字段的名称列表。  

##### `/entityfield <target> set <fieldName> <newValue>`

将代表`<target>`指定的实体的对象中名为`<fieldName>`的字段的值设为`<newValue>`。支持的类型：int、float、double、boolean（现在可能设置失败）和Vec3d。  

##### `/entitylog sub <target>`

开始监视`<target>`所指定的实体并将其每一游戏刻的坐标与Motion以及被监视的字段存储到一个CSV表格文件中。注意部分记录会被临时保存到一个缓冲区而不是立即写入到文件当中，可以使用`/entitylog flush`将那些内容立即写入。

记录文件会被保存到世界文件夹中的`entitylog`文件夹中

##### `/entitylog unsub <target>`

停止监视`<target>`所指定的实体并将缓冲区中内容立即写入到文件。在停止后重新开始对一个实体监视会重新创建一个日志文件而非继续原文件。

##### `/entitylog listenfield <type> <field>`

监视指定的实体的一个字段，执行该指令会使当前所有对特定实体的监视重新开始。`<type>`暂时不会将该指令的影响范围限制到其对应的实体类型。

##### `/entitylog flush`

将缓冲区中内容立即写入到文件而不停止监视。

##### `/explode <pos> <power> [<fire>]`

在`<pos>`处产生一个威力为`<power>`的爆炸，可以指定`<fire>`为`true`使爆炸生成火焰。爆炸威力可以是任意单精度浮点数，包括无穷大甚至NaN  

##### `/messcfg`

获取当前Mod版本。  

##### `/messcfg reloadConfig`

从`mcwmem.prop`获取最新的配置信息。

##### `/messcfg <option>`

查看选项`<option>`的值以及相关帮助信息。

##### `/messcfg <option> <value>`

将选项`<option>`的值设为`<value>`，详见文档的下一节。

##### `/modify <targets> <key> <val>`

修改`<targets>`指定的实体的属性，是部分`/entityfield`的简便用法。  

##### `/modify <target> remove`

移除`<targets>`指定的实体。  

##### `/moventity <targets> <delta> entity self|piston|shulkerBox|player|shulker`

使用`Entity.move()`移动`<targets>`指定的实体，距离由`<delta>`指定，通常选择“self”作为最后一个参数。执行完成后输出实际位移。  

##### `/moventity <targets> <delta> projectile`

使用弹射物移动的方式移动`<target>`指定的实体，距离由`<delta>`指定。目前似乎存在一些Bug。  

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

##### `/raycast blocks <from> <to> [visual]`

检查从`<from>`到`<to>`的有向线段是否被方块阻挡，方式类似于弹射物的碰撞检查和爆炸接触率计算中的raycast。执行后输出检查过的所有方块坐标（会有重复），如果发生了阻挡就输出阻挡线段的方块的坐标及其阻挡线段的面以及交点的坐标，否则输出Missed。如果给出了visual，检查过程会被可视化，即可能检查的方块网格被显示为浅绿色，阻挡线段的方块碰撞箱会被显示橙色，所在网格会被显示为紫色，线段的被阻挡前后的部分分别为青色和红色。

##### `/raycast entities <from> <to> <expand> <excludeSender> [visual]`

检查`<from>`到`<to>`的有向线段是否被方块阻挡，方式类似于弹射物的碰撞检查。执行后输出阻挡线段的实体类型（或自定义名称）及其ID和碰撞点坐标，否则输出Missed。`<expand>`可以影响获取实体的范围，模拟弹射物的碰撞检查时可以指定为弹射物碰撞箱宽度的一半加上1。

##### `/repeat <times> <feedbacks> ...`

重复执行某个命令`<times>`次，可以在`<feedback>`中指定是否输出命令的输出。

##### `/rng world setSeed <seed>`

设置当前维度的RNG（随机数生成器）种子为`<seed>`。  

##### `/rng world next int <bounds>`

取当前维度的随机数生成器生成的下一个在区间[0,`<bounds>`)内的整数。

##### `/rng world next int|float|double|boolean`

取当前维度的随机数生成器生成的下一个随机数。 

##### `/rng <target> ...`

和前三条命令类似，只是RNG来自`<target>`指定的实体而非该维度。  

##### `/setexplosionblock <blockState> [<fireState>]`

使爆炸在破坏方块时放置`<blockState>`指定的方块而不是空气，并将爆炸将要放置的火全部替换成`<fireState>`指定的方块。  

##### `/tileentity get <pos>`

取`<pos>`处的方块实体信息。  

##### `/tileentity set <pos> <type> [<tag>]`

设置`<pos>`处的为`<type>`指定的方块实体。可以指定`<tag>`作为方块实体的初始数据（此时，若`<pos>`处的方块不支持该方块实体，设置会失败）。  

##### `/tileentity remove <pos>`

移除`<pos>`处的方块实体。在目前版本中，如果该处存在一个需要方块实体的方块，在移除后该处方块实体会被该方块重新设置（一个Bug）。  

## 配置项

以下选项均通过`/messcfg`命令设置，格式均为`/messcfg <选项名> <值>`，如启用实体碰撞箱显示可使用`/messcfg serverSyncedBox true`

##### `blockShapeToBeRendered  `

设置在选项`renderBlockShape`为true时被渲染的方块轮廓类型。`COLLIDER`对应方块的碰撞箱，OUTLINE对应方块在玩家对准时显示的边界。

允许取值：`COLLIDER|OUTLINE|RAYSAST|SIDES|VISUAL`

默认值：`COLLIDER`

##### `commandExecutionRequirment`

该Mod中定义的命令是否需要OP权限。

允许取值：true或false

默认值：false

##### `creativeUpwardsSpeed`

调节创造模式下玩家向上飞行（加）速度大小。

允许取值：0-1024以内的实数

默认值：0.05

##### `debugStickSkipsInvaildState`

防止调试棒将方块调整到非法状态。目前这个选项在一些情况下不能正常工作，例如调整铁轨的shape属性时仍可以将其调整到非法状态使其掉落。

允许取值：true或false

默认值：false

##### `disableExplosionExposureCalculation`

禁用爆炸接触率计算以缩短堆叠TNT的爆炸卡顿时间。这会使方块无法防止其后方方块为爆炸影响。

允许取值：true或false

默认值：false

##### `disableProjectileRandomness`

取消弹射物的随机速度，用于部分测试，不过不要忘记关闭。

允许取值：true或false

默认值：false

##### `enabledTools`

启用或禁用工具物品，具体用法见下文。  

允许取值：true或false

默认值：false

##### `endEyeTeleport`

在使用末影之眼时将玩家传送到玩家注视的地方。

允许取值：true或false

默认值：false

##### `entityExplosionInfluence`

爆炸影响到实体时在聊天栏输出实体被影响的情况。在安装Lithium后可能不能正常工作。

允许取值：true或false

默认值：false

##### `entityExplosionRaysVisiblity`

启用或禁用实体爆炸射线渲染。 

允许取值：true或false

默认值：false

##### `entityExplosionRaysLifetime`

设置实体爆炸射线的显示时长，单位为刻。

允许取值：任意正整数

默认值：300

##### `hudAlignMode`

设置实体信息HUD的位置。

允许取值：BOTTOM_LEFT|BOTTOM_RIGHT|TOP_LEFT|TOP_RIGHT

默认值：TOP_RIGHT

##### `hudTextSize`

调节HUD字体大小。

允许取值：0-10间任意实数

默认值：1

##### `maxClientTicksPerFrame`

设置FPS低于20时每帧客户端可以运行几个游戏刻，将其设为一个较小值可能修复低FPS时无法切换飞行状态的Bug。 

允许取值：任意正整数

默认值：10

##### `maxEndEyeTpRadius`

设置`endEyeTeleport`功能的最远传送距离。

允许取值：任意正实数

默认值：180

##### `mobFastKill`

设置在生物被/kill ...杀死时是否被直接移除而非承受伤害。

允许取值：true或false

默认值：false

##### `projectileChunkLoading`

允许弹射物在其运算过程中自动加载区块，在制作珍珠炮时可能会有所帮助。注意该功能可能会在弹射物以极高的速度飞行时造成极大的卡顿。

允许取值：true或false

默认值：false

##### `projectileChunkLoadingPermanence`

弹射物在`projectileChunkLoading`启用时永久加载区块。

允许取值：true或false

默认值：false

##### `projectileChunkLoadingRange`

设置`projectileChunkLoading`功能中创建的强加载范围的半径。

允许取值：任意整数（包括零和负数）

默认值：3

##### `renderBlockShape`

启用或禁用方块轮廓渲染。

允许取值：true或false

默认值：false

##### `renderFluidShape`

启用或禁用流体轮廓、高度和流向向量的渲染。

允许取值：true或false

默认值：false

##### `renderRedstoneGateInfo`

查看视线对准的中继器和比较器的输出信号等级

允许取值：true或false

默认值：false

##### `serverSyncedBox`

启用或禁用服务端碰撞箱显示。  

允许取值：true或false

默认值：false

##### `superSuperSecretSetting`

**<font color=red>[TODO]</font>**wlujkgfdhlqcmyfdhj...千万不要打开！

允许取值：true或false

默认值：false

##### `tntChunkLoading`

允许TNT实体在其运算过程中自动加载区块。注意该功能可能会在TNT以极高的速度飞行时造成极大的卡顿。

允许取值：true或false

默认值：false

##### `tntChunkLoadingPermanence`

TNT在`tntChunkLoading`启用时永久加载区块。

允许取值：true或false

默认值：false

##### `tntChunkLoadingRange`

设置`tntChunkLoading`功能中创建的强加载范围的半径。

允许取值：任意整数（包括零和负数）

默认值：3

## 快捷键

**`F3 + E`**：开关显示当前玩家注视的实体信息的HUD。  

**`F3 + M`**：开关显示客户端玩家信息的HUD。与投影的默认设置冲突，可以手动修改。

**`F3 + S`**：开关显示服务端玩家信息的HUD。

## 渲染器
**实体信息HUD**：内容参考下文。实体的所有数据取自服务端游戏刻末尾,客户端玩家信息取自客户端tick末。

**实体爆炸射线**：显示爆炸中心与实体碰撞箱内一些选中的点间的用于决定实体内爆炸影响的程度的线段。射线中被阻挡的部分显示为红色，否则为绿色。   

**服务端实体碰撞箱**：不要总是相信F3 + B提供的碰撞箱，因为它们是经客户端为使运动更平滑调整过后的，在运动时可能会“跟不上”服务端。实体的碰撞箱会被渲染为绿色。

**流体方块数据**：显示玩家对准的流体的等级、高度、范围和流向向量。

**方块边界箱**：显示玩家对准的方块的碰撞箱或OUTLINE Shape。

**红石装置信号输出等级**：显示玩家对准的中继器和比较器的输出等级。

## 工具物品（Scarpet实现）
**红砖**：使用/tick freeze暂停服务端游戏刻。 
**骨头**：在服务端游戏刻被/tick freeze暂停时继续执行一段时间，执行的刻数等于该组骨头的个数。 
**下界合金锭**：直接移除所有非玩家实体。  

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

## 其他特性
1.结构方块的渲染距离被拓宽，可以在很远处被看到。
2.在未安装地毯端时执行命令遇到未知错误时会输出Stacktrace，在安装地毯端后也可以通过启用`superSecretSetting`规则做到这一点。

## 注意事项

1.该Mod仍在开发中，一些功能可能不可用或存在bug，如果发现了请告诉我。  
2.客户端玩家是`CilentPlayerEntity`而不是对应的`ServerPlayerEntity`，因为玩家运动的大部分运算在客户端进行。 对查看服务端玩家信息的支持可能在后续版本中加入。
3.在该版本中，专用服务器不被支持，请只在单人游戏或局域网（仅房主所在客户端）中使用该Mod。  
4.一些命令，像/explode \~ \~ \~ 2147483647 true可能造成服务端卡死，请注意。