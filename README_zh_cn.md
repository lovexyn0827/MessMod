# Minecraft世界访问增强Mod
`一个用于提供额外的世界访问能力的Minecraft Mod`  
其他语言： 
[English](/README.md)  
## 需求
1.Fabric Loader  
2.gnembon的地毯Mod（fabric-carpet）  
3.Minecraft 1.16.4（最好是1.16.4）  
4.Minecraft 1.16.4 需要的所有东西  
## 命令
`/entityconfig <targets> enableStepHeight|disableStepHeight`：允许或禁止targets指定的实体直接走上方块。  
`/entityconfig localPlayer enableStepHeight|disableStepHeight`：允许或禁止客户端玩家直接走上方块。  
`/entityfield <target> get <fieldName>`：获取代表target指定的实体的对象中名为fieldName的域变量。请注意，此处的名称是类似于field_827的intermediary names，在自动读取混淆映射被支持前需要人工查阅以便理解。  
`/entityfield <target> listAvailableFields`：获取代表target指定的实体的对象中所有域变量的名称列表。  
`/entityfield <target> set <fieldName> <newValue>`：将代表target指定的实体的对象中名为fieldName的域变量的值设为newValue。支持的类型：int、float、double、boolean（现在可能设置失败）和Vec3d。  
`/explode <pos> <power> <fire>`：在pos处产生一个威力为power的爆炸，可以指定fire为true使爆炸生成火焰。  
`/mcwmem`：获取当前Mod版本。  
`/mcwmem setDisplay <location>`：将实体信息HUD的位置移至location指定的位置。  
`/mcwmem reloadConfig`：从mcwmem.prop获取最新的配置信息。  
`/mcwmem entityExplosionRaysVisiblity <bool>`：启用或禁用实体爆炸射线渲染。  
`/mcwmem entityExplosionRaysVisiblity setLifetime <ticks>`：设置实体爆炸射线的显示时长，单位为刻。  
`/mcwmem serverSyncedBox <bool>`：启用或禁用服务端碰撞箱显示。  
`/mcwmem mobFastKill <bool>`：设置在生物被/kill ...杀死时是否被直接移除而非承受伤害。  
`/mcwmem enabledTools <bool>`：启用或禁用工具物品，具体用法见下文。  
~`/mcwmem entityExplosionInfluence <bool>`~：设置是否在爆炸影响到实体时在聊天栏输出实体被影响的情况。在当前版本中存在一个bug，使该信息在非开发环境下不会被输出，所以只做保留。  
`/modify <targets> <key> <val>`：修改targets指定的实体的属性，是部分/entityfield的简便用法。  
`/modify <target> remove`：移除targets指定的实体。  
`/moventity <targets> <delta> entity self|piston|shulkerBox|player|shulker`：使用Entity.move()移动targets指定的实体，距离由delta指定，通常选择“self”作为最后一个参数。  
`/moventity <targets> <delta> projectile`：使用弹射物移动的方式移动target指定的实体，距离由delta指定。目前似乎存在一个bug。  
`/poi get <pos>`：取pos处的POI（兴趣点）。  
`/poi set <pos> <type> <replace>`：将pos处设为type指定的POI，若已有POI且replace为false则不会设置。  
`/rng world setSeed <seed>`：设置当前维度的RNG（随机数生成器）种子为seed。  
`/rng world next int|float|double|boolean`：取当前维度的随机数生成器生成的下一个随机数。当生成int时，可以在下一个参数中指定上界。  
`/rng <target> ...`：和前两条命令类似，只是RNG来自target指定的实体而非该维度。  
`/setexplosionblock <blockState> <fireState>`：使爆炸在破坏方块时放置blockState指定的方块而不是空气，并将爆炸将要放置的火全部替换成fireState指定的方块。  
`/tileentity get <pos>`：取pos处的方块实体信息。  
`/tileentity set <pos> <type> <tag>`：设置pos处的为type指定的方块实体。~可以指定tag作为方块实体的初始数据~（此时，若pos处的方块不支持该方块实体，设置会失败）。  
`/tileentity remove <pos>`：移除pos处的方块实体。在目前版本中，如果该处存在一个需要方块实体的方块，在移除后该处方块实体会被该方块重新设置（一个bug）。  
## 快捷键
`F3+E`：开关显示当前玩家注视的实体信息的HUD。  
`F3+M`：开关显示客户端玩家信息的HUD。
## 渲染器
`实体信息HUD`：内容参考下文。注视实体的所有数据取自服务端游戏刻末尾,客户端玩家信息取自客户端tick末。
`实体爆炸射线`：显示爆炸中心与实体碰撞箱内一些选中的点间的用于决定实体内爆炸影响的程度的线段。被阻挡的线显示为白色，否则为蓝色。   
`服务端碰撞箱`：不要总是相信F3+B提供的碰撞箱，因为它们是经客户端为使运动更平滑调整过后的，在运动时可能会“跟不上”服务端。  
## 工具物品
`红砖`：使用/tick freeze暂停服务端游戏刻。  
`骨头`：在服务端游戏刻被/tick freeze暂停时继续执行一段时间，执行的刻数等于该组骨头的个数。  
`下界合金锭`：移除所有非玩家实体。  
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
`Wet` :表示实体的一部分处于水中.  
`Sp` :表示实体在疾跑。  
`Sn` :表示实体在潜行。  
`De` :表示实体在下蹲（类似于潜行)。  
`Sw` :表示实体在游泳。  
### Living
`Hurt` :表示实体在上一刻中受到了伤害。  
`Fly` :表示实体在使用鞘翅飞行。  
`Slp` :表示实体在睡觉  
`Dead` :表示实体的生命值是0或更低，或者说已经死亡。  
## 其他
1.该Mod仍在开发中，一些功能可能不可用或存在bug，如果发现了请告诉我。  
2.客户端玩家是CilentPlayerEntity而不是对应的ServerPlayerEntity，因为玩家运动的大部分运算在客户端进行。  
3.在该版本中，专用服务器不被支持，请只在单人游戏或局域网中使用该Mod。  
4.一些命令，像/explode \~ \~ \~ 2147483647 true可能造成服务端卡死，请注意。
