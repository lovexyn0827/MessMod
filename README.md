# MessMod
A Minecraft mod that provides extra ability to manipulation a world and some enachantments .

Just like what the name says, the Mod contains functions in many fields (like world manipulation, information providing, feature changes, bug fixes, etc.) , therefore the mod seems to be messy, even the style of the source code is messy.

In other languages:  
[简体中文](/README_zh_cn.md)

## Requires:
1. Fabric Loader 0.7.4+.  
2. The fabric-carpet by gnembon.(Strongly recommended, but not necessary)  
3. Minecraft 1.16.4 or 1.16.5.  
4. Everything Minecraft 1.16.x requires.  

## Commands: 
Names of arguments are wrapped by pointy semicolons, and optional components are wrapped by squared semicolons.

##### `/ensure <pos>`

Obtain the information of block state and block entity data (if exists) at `<pos>` to check if the block is rendered wrongly or is a ghost block.

##### `/entityconfig <targets> enableStepHeight|disableStepHeight`

Make `<targets>` can/cannot step on blocks directly.

##### `/entityconfig localPlayer enableStepHeight|disableStepHeight`

Similar to the last,but the target is always the local player.  

##### `/entityfield <target> get <fieldName>`

Get the value of `<field>` in the object which represents `<target>`.Note that the names are intermediary names like `field_827`, please use a mapping to translate them. 

##### `/entityfield <target> listAvailableFields`

List all fields defined or inherited by the class of`<target>`. 

##### `/entityfield <target> set <fieldName> <newValue>`

Set the value of `<field>` in the object which represents `<target>` to `<newValue>`.Supported types : int,float,double,boolean(may fail to be set now),String, Vec3d(use "," as delimiter).  

##### `/entitylog sub <target>`

Start recording the Motion, position and listened fields of `<target>` every tick and save them to a CSV file. Note that some of the records will be save to a buffer temporarily rather than being written to the file immediately, `/entity flush` could be used instead.

Logs are saved to the `entitylog` folder in the world folder.

##### `/entitylog unsub <target>`

Stop monitoring `<target>` and save the records in the buffer to the disk.

##### `/entitylog listenfield <type> <field>`

Mark `<field>` 'listened' so that the value of it will be recorded if any entity containing this field is subscribed by using `/entitylog sub <target>`, execution of this command will restart all  recording process. Temporarily, `<type>` wouldn't restrict the scope of influence of this command.

##### `/entitylog flush`

Save the records in the buffer to the disk without stopping recording.

##### `/explode <pos> <power> <fire>`

Create an explosion with the power of `<power>` at `<pos>`,and create fire if the optional argument `<fire>` is true.  The power of explosions can be any single preciseness floating-point number, including Infinities and even NaN.

##### `/messcfg`

Display the version and current config of the mod..

##### `/messcfg <option>`

Get the current value of `<option>` and related helping informations.

##### `/messcfg <option> <value>`

Set the value of `<option>` to `<value>`. See the next section of the document.

##### `/messcfg reloadConfig`

Update options from `mcwmem.prop`. 

##### `/modify <targets> <key> <val>`

Change the value `<key>` in the entities with to `<val>`.Much simpler than using `/entityfield`.  

##### `/modify <target> remove`

Remove `<target>` from the world.  

##### `/moventity <targets> <delta> entity self|piston|shulkerBox|player|shulker`

Move the entity using `Entity.move()`. The distance is determined by `<delta>`.Usually,we should use "self" as the last argument.  After the execution, the actual displacement of the entity is outputted.

##### `/moventity <targets> <delta> projectile`

Move the entity in the way projectile entities moves. The distance is determined by `<delta>`.The command seems to be buggy now.

##### `/poi get <pos>`

Get the name of the POI at `<pos>`.  

##### `/poi getDistanceToNearestOccupied <pos>`

Get the distance (Maybe the Manhattan distance, and the unit may be a subchunk) to the nearest working station.

##### `/poi scan <center> <radius> <type>`

Find POIs with the given type in a sphere whose center and radius is given in the command.

##### `/poi scanCobic <corner1> <corner2> <type>`

Find POIs with the given type in a Cube whose two opposite corners are given in the command.

##### `/poi set <pos> <type> <replace>`

Set the POI(Point of Interest) at `<pos> `to `<type>` if there is not a POI at `<pos>` or `<replace>` is true.  

##### `/raycast blocks <from> <to> [visual]`

Check if the direction-ed segment whose two vertexes are given in the command is blocked by any block, just like the collision checking of projectiles and the raycasts in the calculations of exposure of explosions. After execution, the coordination of checked blocks will be output (there may be duplication) , and if the line was blocked, the coordination of the block who blocked the line, the face  blocked the line and the coordination of the point  where the line was blocked are output. If visual is present, the process of the checking will be visualized, that is , all grids which checked blocks is in is displayed in light green, the colliding shape of the block which blocked the line is displayed in orange, while the grid the block is in is displayed in purple, the part of the unblocked line is displayed in magenta while the blocked part is displayed in red.

##### `/raycast entities <from> <to> <expand> <excludeSender> [visual]`

Check if the direction-ed line segment whose two vertexes are given in the command is blocked by any entity, just like the collision checking of projectiles. After the execution, if the line was blocked, the type or the custom name of the entity which blocked the line and the coordination of the point where the line was blocked will be output. The argument expand determines the range of checking, should be the half of the width of the bounding box of the projectile add 1 in projectile collision checking simulation.

##### `/repeat <times> <feedbacks> ...`

Repeat executing a command for given times, the argument <feedback> can be used to indicate if the feedback of the command is output.

##### `/rng world setSeed <seed>`

Get the current seed of the RNG of the dimension.  

##### `/rng world next int <bounds>`

Get the next integer in the range [0, bounds) generated by the RNG of the dimension.

##### `/rng world next int|float|double|boolean|gaussian`

Get the next value generated by the RNG of the dimension.

##### `/rng <target> ...`

Do something on the RNG of `<target>`，just like the last two commands.  

##### `/setexplosionblock <blockState> <fireState>`

Make explosions place `<blockState>` instead of air,`<fireState>` instead of fire.  

##### `/tileentity get <pos>`

Get the information of the block entity at `<pos>`.

##### `/tileentity set <pos> <type> <tag>`

Set the block entity at `<pos>` to `<type>`.Optionally,you can specify a `<tag>` as the initial data.(It may fail to execute when the block at `<pos>` is not suitable.)  

##### `/tileentity remove <pos>`

Remove the block entity at `<pos>`.In current version of the mod,if there is a block which needs a block entity,the block entity will be reset after removing(that is a bug).  

## Options

The following options could be set with the command `/messcfg`, and the format of the command is `/messcfg <option> <value>`. For example, to enable the entity boundary box renderer, the command `/messcfg serverSyncedBox true` could be used.

##### `blockShapeToBeRendered`

Specify the type of block shape rendered when `renderBlockShape` is enabled. The `COLLIDER` shape is the  shape used to do calculations about collisions, while the `OUTLINE` shape is the shape used to determine which block the player is looking at.

Possible values: `COLLIDER|OUTLINE|RAYSAST|SIDES|VISUAL`

Default value: `COLLIDER`

##### `commandExecutionRequirment`

Whether or not execution of commands defined by this mod require OP permission.

Possible values: true or false

Default value: false

##### `creativeUpwardsSpeed`

Set the speed which the player is flying upwards at in the creative mode.

Possible values: Real number between 0 and 1024

Default value: 0.05

##### `debugStickSkipsInvaildState`

Prevent debug sticks change blocks to a invalid state. By now, the option **doesn't work** in some cases, like changing the `shape` property of a rail can still turn the rail in to an illegal state and get broken.  

Possible values: true or false

Default value: false

##### `disableExplosionExposureCalculation`

Disable the calculation of explosion exposure to reduce the lag caused by stacked TNT explosions. This will also mean that blocks cannot prevent entities from be influenced by explosions.

Possible values: true or false

Default value: false

##### `disableProjectileRandomness`

Remove the random speed of projectiles. It could be used in testing, but don't forget to disable it if not needed.

Possible values: true or false

Default value: false

##### `enabledTools`

Enable or disable item tools (See below).  

Possible values: true or false

Default value: false

##### `endEyeTeleport`

When the player uses ender eyes, teleport it to where it looks at.

Possible values: true or false

Default value: false

##### `entityExplosionInfluence`

Send how entities are affected by explosions. This feature may not work properly if Lithium is loaded.

Possible values: true or false

Default value: false

##### `entityExplosionRaysLifetime`

Set how many ticks the rendered rays remains. 

Possible values: Any positive integer.

Default value: 300

##### `entityExplosionRaysVisiblity`

Enable or disable explosion ray (used to calculate the exposure of entities) renderer.  

Possible values: true or false

Default value: false

##### `hudAlignMode`

Move the HUDs to the given location.   

Possible values: BOTTOM_LEFT|BOTTOM_RIGHT|TOP_LEFT|TOP_RIGHT

Default value: TOP_RIGHT

##### `hudTextSize`

Set the size of the text in the HUDs.

Possible values: Any real number between 0 and 10.

Default value: 1

##### `maxClientTicksPerFrame`

Set the maximum number of ticks can be processed within a single frame when the FPS is lower than 20, setting it to a low value may fix the bug which makes players cannot toggle the flying state when the FPS is too low. 

Possible values: Any positive integer.

Default value: 10

##### `maxEndEyeTpRadius`

Set the maximum range of teleporting with `endEyeTeleport`. 

Possible values: Any positive real number.

Default value: 180

##### `mobFastKill`

`/kill` removes mobs directly instead of damage them. 

Possible values: true or false

Default value: false

##### `projectileChunkLoading`

Allow or disallow projectiles to load chunks in their processing, maybe helpful in testing pearl canons.  Note that if a projectile flies at a extremely high speed when the option is set to true.

Possible values: true or false

Default value: false

##### `projectileChunkLoadingPermanentence`

Projectiles load the chunks they are in permanently when `projectileChunkLoading` is enabled.

Possible values: true or false

Default value: false

##### `projectileChunkLoadingRange`

Set the radius of entity processing chunks created by `projectileChunkLoading`.

Possible values: Any integer.

Default value: 3

##### `renderBlockShape`

Enable or disable block outline rendering.

Possible values: true or false

Default value: false

##### `renderFluidShape`

Enable or disable the renderer of outlines, heights, and vectors of flowing directions of target fluids.

Possible values: true or false

Default value: false

##### `renderRedstoneGateInfo`

Display the output level of repeaters and comparators the player looks at.

Possible values: true or false

Default value: false

##### `serverSyncedBox`

Enable or disable the server-side hitbox renderer.  

Possible values: true or false

Default value: false

##### `superSuperSecretSetting`

**<font color=red>[TODO]</font>**wlujkgfdhlqcmyfdhj...Anyway, never turn this on!

Possible values: true or false

Default value: false

##### `tntChunkLoading`

Allow or disallow TNT entities to load chunks in their processing, maybe helpful in making some kind of TNT canons. Note that if an TNT entity flies at a extremely high speed when the option is set to true.

Possible values: true or false

Default value: false

##### `tntChunkLoadingPermanence`

TNT entities load the chunks they are in permanently when `tntChunkLoading` is enabled.

Possible values: Any non-negative integer.

Default value: false

##### `tntChunkLoadingRange`

Set the radius of entity processing chunks created by `tntChunkLoading`.

Possible values: Any integer.

Default value: 3

## Key binds:
**`F3 + E`** : Toggle the HUD that displays the information of the entity which the player is looking at.  

**`F3 + M`** : Toggle the HUD that displays the information of the local player.  

**`F3 + S`**: Toggle the HUD that displays the information of the server-side player.  

## Renderers: 
**Entity information HUD**: See below. Information of the entity that the player is looking at is got at the end of server ticks,and the information of local player is got at the end of client ticks. 

**Explosion rays**: It renders the lines between the center of a explosion and some chose points in the hit box of entities affected by the explosion,which determines how much damage the entities will take,how much the velocity of the entity will change,etc. 

**Server-side hitboxes**: Don't always believe the hitboxes provided by F3 + B because they are adjusted by the client to make movements smoother and don't "keep up with" the server-side when the entity is moving.

**Data of fluid blocks**: Display the bounding box, height, level and a vector describing the flowing direction of the targeted fluid block.

**Bounding boxes of blocks**: Display the bounding box (the collision box or OUTLINE Shape) of the targeted block.

**The output level of redstone gates**: Display the erdstong singal level of the targeted redstone gate (i.e. repeators and comparators).

## Item Tools: 
**Brick**:Pause or continue ticking using /tick freeze provided by the Carpet  
**Bone**:Continue ticking for some ticks when ticking is paused by /tick freeze. The number of the ticks equals to the count of the stack of bone.  
**Netherite Ingot**:Remove all non-player entity.  

## HUD lines:
`Header line` :Include the ID(equals to the number of Entity instance created before the entity is loaded),name(custom name or the type in /summon) and age(ticks since the entity is loaded). 
`Pos` :The position of the entity.  
`Motion` :The value of field motion (MCP), velocity(Yarn) or `deltaMovement` (Offical) in the entity's object.
`Delta` :The distance the entity moved in the last update. It can be treated as velocity.  
`Yaw,Pitch` :Nothing,but the facing of the entity.  
`Fall Distance` :The distance the entity has fallen since the entity leaved the ground,but may be influenced by various factors like updating in lava.  
`General State` :Some boolean states that all is available in all entities. More information can be found below.  
`Health` :LivingEntity(include players,armor stands and mobs)'s health.  
`Forward,Sideways,Upward` :Some values related to mob's AI or player's input.  
`Speed` :Two values representing how fast a LivingEntity moves.  
`Living State` :Some boolean states that only exists in LivingEntity. More information can be found below.  
`Fuse` :The length of a TNT's fuse. The value is always 80 in a normal TNT.  
`Power` :The acceleration of a fireball. Note: A fireball has a drag of 0.05gt^-1,so the fireball won't accelerate forever.  
`Velocity Decay` :The value of a boat's drag,which can be different value when the boat in on different grounds.  

## Shorten States
### General
`Gl` :Entities that has the state is rendered with a glowing outline.  
`Inv` :Entities that has the state won't be affected by most damages.  
`Col` :Entities that has the state can collide with blocks and other entities than has the state.  
`NG` :Entities that has the state won't be influenced by gravity.  
`HC` :The entity is collided with a block horizontally.  
`VC` :The entity is collided with a block vertically.  
`Wet` :The some parts of the entity is in water.  
`Sp` :The entity is sprinting.  
`Sn` :The entity is sneaking.
`De` :The entity is descending(like sneak);
`Sw` :The entity is swimming.  
`Og`:The entity is on the ground.

### Living
`Hurt` :Represents the entity took any damage(include 0 and negative amounts) in the last tick. 
`Fly` :The entity is fallflying, using a elytra.  
`Slp` :The entity is sleeping.  
`Dead` :The entity's health is zero or lower,meaning the entity is dead.  

## Other Features

1. Structure blocks can be seen when the player is very far from the block.
2. Stacktrace will be printed when the Carpet Mod is not loaded. If the Carpet Mod is loaded, enabling the `superSecretSetting` has the same effect.

## Notice:
1. The mod is still in development,some feature is not available or buggy,please tell me if you find something wrong.  
2. The local player is a `ClientPlayerEntity` instead of a `ServerPlayerEntity` because most of the calculation of players' movement is calculated on client side and values in `ServerPlayerEntity` can be zero and useless,meaning that if you are looking at a remote player,some values can be wrong.  
3. Dedicated server is not supported in current version,so only use the mod in single player mode or a LAN server.  
4. Some command like /explode \~ \~ \~ 2147483647 true can freeze the server,be careful.