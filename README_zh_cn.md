# Just A Mess（一团糟）
`一个用于提供额外的世界访问能力及一些增强功能的Minecraft Mod`  

正如其名字中所说，这个Mod包含了很多方面的功能（世界控制，信息显示，特性更改以及一些Bug修复等），有些还是随便写的，非常乱，就连代码风格也很乱。

其他语言： 
[English](/README.md)  

## 需求
1.Fabric Loader 0.7.4+  
2.gnembon的地毯Mod（fabric-carpet）（部分功能需要，所以强烈推荐，但并非必需）  
3.Minecraft 1.16.4或1.16.5  
4.Minecraft 1.16.x 需要的所有东西  

## 命令
参数名称用尖括号括住，方括号中为可选部分。
`/entityconfig <targets> enableStepHeight|disableStepHeight`：允许或禁止targets指定的实体直接走上方块。  
`/entityconfig localPlayer enableStepHeight|disableStepHeight`：允许或禁止客户端玩家直接走上方块。  
`/entityfield <target> get <fieldName>`：获取代表target指定的实体的对象中名为fieldName的域变量。请注意，此处的名称是类似于field_827的intermediary names，在自动读取混淆映射被支持前需要人工查阅以便理解。  
`/entityfield <target> listAvailableFields`：获取代表target指定的实体的对象中所有域变量的名称列表。  
`/entityfield <target> set <fieldName> <newValue>`：将代表target指定的实体的对象中名为fieldName的域变量的值设为newValue。支持的类型：int、float、double、boolean（现在可能设置失败）和Vec3d。  
`/explode <pos> <power> [<fire>]`：在pos处产生一个威力为power的爆炸，可以指定fire为true使爆炸生成火焰。爆炸威力可以是任意单精度浮点数，包括无穷大甚至NaN  
`/messcfg`：获取当前Mod版本。   
`/messcfg blockShapeToBeRendered  COLLIDER|OUTLINE|RAYSAST|SIDES|VISUAL`：设置在选项renderBlockShape为true时被渲染的方块轮廓类型。COLLIDER对应方块的碰撞箱，OUTLINE对应方块在玩家对准时显示的边界。
`/messcfg enabledTools <bool>`：启用或禁用工具物品，具体用法见下文。  
`/messcfg entityExplosionInfluence <bool>`：设置是否在爆炸影响到实体时在聊天栏输出实体被影响的情况。在当前版本中存在一个bug，使该信息在非开发环境下不会被输出，所以只做保留。   
`/messcfg entityExplosionRaysVisiblity <bool>`：启用或禁用实体爆炸射线渲染。 
`/messcfg entityExplosionRaysVisiblity setLifetime <ticks>`：设置实体爆炸射线的显示时长，单位为刻。
`/messcfg mobFastKill <bool>`：设置在生物被/kill ...杀死时是否被直接移除而非承受伤害。
`/messcfg reloadConfig`：从mcwmem.prop获取最新的配置信息。
`/messcfg renderBlockShape <bool>`：启用或禁用方块轮廓渲染。
`/messcfg renderFluidShape <bool>`：启用或禁用流体轮廓、高度和流向向量的渲染。
`/messcfg serverSyncedBox <bool>`：启用或禁用服务端碰撞箱显示。  
`/messcfg setHudDisplay bottomLeft|bottomRight|topLeft|TopRight`：设置实体信息HUD的位置。
`/messcfg debugStickSkipsInvaildState <bool>`：防止调试棒将方块调整到非法状态。目前这个选项在一些情况下不能正常工作，例如调整铁轨的shape属性时仍可以将其调整到非法状态。 
`/messcfg tntChunkLoading <bool>`：允许或禁止TNT实体在运算过程中加载区块，在制作一些TNT炮时可能会有所帮助。 
`/messcfg projectileChunkLoading <bool>`：允许或禁止弹射物实体在运算过程中加载区块，在制作珍珠炮时可能会有所帮助。 
`/messcfg maxClientTicksPerFrame <ticks>`：设置FPS低于20时每帧客户端可以运行几个游戏刻，将其设为一个较小值可能修复低FPS时无法切换飞行状态的Bug。 
`/modify <targets> <key> <val>`：修改targets指定的实体的属性，是部分/entityfield的简便用法。  
`/modify <target> remove`：移除targets指定的实体。  
`/moventity <targets> <delta> entity self|piston|shulkerBox|player|shulker`：使用Entity.move()移动targets指定的实体，距离由delta指定，通常选择“self”作为最后一个参数。执行完成后输出实际位移。  
`/moventity <targets> <delta> projectile`：使用弹射物移动的方式移动target指定的实体，距离由delta指定。目前似乎存在一些bug。  
`/poi get <pos>`：取pos处的POI（兴趣点）。 
`/poi getDistanceToNearestOccupied <pos>`：返回最近的村民工作站点在某种尺度下的距离（可能是以区段为单位的曼哈顿距离）。
`/poi scan <center> <radius> <type>`：寻找以center为中心，半径为radius的球中类型为type的POI，并输出所在方块坐标。
`/poi scanCobic <corner1> <corner2> <type>`：寻找给定的两个角落间长方体区域内类型为type的POI。
`/poi set <pos> <type> <replace>`：将pos处设为type指定的POI，若已有POI且replace为false则不会设置。 
`/raycast blocks <from> <to> [visual]`：检查从from到to的有向线段是否被方块阻挡，方式类似于弹射物的碰撞检查和爆炸接触率计算中的raycast。执行后输出检查过的所有方块坐标（会有重复），如果发生了阻挡就输出阻挡线段的方块的坐标及其阻挡线段的面以及交点的坐标，否则输出Missed。如果给出了visual，检查过程会被可视化，即可能检查的方块网格被显示为浅绿色，阻挡线段的方块碰撞箱会被显示橙色，所在网格会被显示为紫色，线段的被阻挡前后的部分分别为青色和红色。
`/raycast entities <from> <to> <expand> <excludeSender> [visual]`：检查from到to的有向线段是否被方块阻挡，方式类似于弹射物的碰撞检查。执行后输出阻挡线段的实体类型（或自定义名称）及其ID和碰撞点坐标，否则输出Missed。expand可以影响获取实体的范围，模拟弹射物的碰撞检查时可以指定为弹射物碰撞箱宽度的一半加上1。
`/repeat <times> <feedbacks> ...`：重复执行某个命令times次，可以在feedback中指定是否输出命令的输出。
`/rng world setSeed <seed>`：设置当前维度的RNG（随机数生成器）种子为seed。  
`/rng world next int <bounds>`：取当前维度的随机数生成器生成的下一个在区间[0,bounds)内的整数。
`/rng world next int|float|double|boolean`：取当前维度的随机数生成器生成的下一个随机数。 
`/rng <target> ...`：和前三条命令类似，只是RNG来自target指定的实体而非该维度。  
`/setexplosionblock <blockState> [<fireState>]`：使爆炸在破坏方块时放置blockState指定的方块而不是空气，并将爆炸将要放置的火全部替换成fireState指定的方块。  
`/tileentity get <pos>`：取pos处的方块实体信息。  
`/tileentity set <pos> <type> [<tag>]`：设置pos处的为type指定的方块实体。~可以指定tag作为方块实体的初始数据~（此时，若pos处的方块不支持该方块实体，设置会失败）。  
`/tileentity remove <pos>`：移除pos处的方块实体。在目前版本中，如果该处存在一个需要方块实体的方块，在移除后该处方块实体会被该方块重新设置（一个bug）。  

## 快捷键
`F3+E`：开关显示当前玩家注视的实体信息的HUD。  
`F3+M`：开关显示客户端玩家信息的HUD。与投影的默认设置冲突，可以手动修改。

## 渲染器
`实体信息HUD`：内容参考下文。实体的所有数据取自服务端游戏刻末尾,客户端玩家信息取自客户端tick末。
`实体爆炸射线`：显示爆炸中心与实体碰撞箱内一些选中的点间的用于决定实体内爆炸影响的程度的线段。射线中被阻挡的部分显示为红色，否则为绿色。   
`服务端碰撞箱`：不要总是相信F3+B提供的碰撞箱，因为它们是经客户端为使运动更平滑调整过后的，在运动时可能会“跟不上”服务端。实体的碰撞箱会被渲染为绿色。

## 工具物品（Scarpet实现）
`红砖`：使用/tick freeze暂停服务端游戏刻。  
`骨头`：在服务端游戏刻被/tick freeze暂停时继续执行一段时间，执行的刻数等于该组骨头的个数。  
`下界合金锭`：直接移除所有非玩家实体。  

## HUD信息项
`首行`：包含实体ID（等于该实体对象创建前创建的Entity实例总数），名称（/summon中使用的实体类型或自定义名称），年龄（实体最近一次被加载后存在的刻数）。  
`Pos`：实体的坐标。  
`Motion`：Entity类中域变量motion（MCP），velocity（Yarn）或deltaMovement（官方）的值,一般接近或等于实体的速度。  
`Delta`：实体上一刻的位移，可视为速度。  
`Yaw,Pitch`：实体的水平方向角和仰角。  
`Fall Distance`：实体自上一次着陆以来的向下移动的长度，但也可能受到一些其他因素的影响，如落入岩浆。  
`General State`：所有实体共有的一些布尔（非真即假）属性，详见下文。  
`Health`：LivingEntity（生物、玩家和盔甲架）的生命值。  
`Forward,Sideways,Upward`：与实体运动AI有关。  
`Speed`：与实体运动速度（分别是路上和空中）正相关。  
`Living State`：LivingEntity共有的一些布尔（非真即假）属性，详见下文。  
`Fuse`：TNT的引线长度，单位为刻。人工点燃的TNT中值总为80。  
`Power`：火球的加速度。注意，火球有0.05gt^-1的阻力，所以不会一直加速。  
`Velocity Decay`：船的阻力系数，在不同地面上不同。  
## 缩写的状态（state）
### General
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
2.在未安装地毯端时执行命令遇到未知错误时会输出Stacktrace，在安装地毯端后也可以通过启用superSecretSetting规则做到这一点。

## 注意事项

1.该Mod仍在开发中，一些功能可能不可用或存在bug，如果发现了请告诉我。  
2.客户端玩家是CilentPlayerEntity而不是对应的ServerPlayerEntity，因为玩家运动的大部分运算在客户端进行。 
3.在该版本中，专用服务器不被支持，请只在单人游戏或局域网（仅房主所在客户端）中使用该Mod。  
4.一些命令，像/explode \~ \~ \~ 2147483647 true可能造成服务端卡死，请注意。