# MessMod

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/MessMod.png)

A Minecraft mod that contains many features ranging from world 
manipulation and information providing to bug fixes and so on, allowing 
you to take more control of the game, see more information, and do some 
work easier.

Just like what the name says, the Mod contains functions in many fields, therefore the mod seems to be messy, even the style of the source code is messy.

In other languages:

[简体中文](https://github.com/lovexyn0827/MessMod/blob/master/README_zh_cn.md)

### Requirements

- Fabric Loader 0.7.4+. 
- The fabric-carpet by gnembon. (Strongly recommended, but not necessary) 
- Minecraft 1.16.4 or 1.16.5. 
- Everything Minecraft 1.16.x requires. 

### Commands

Names of arguments are wrapped by pointy semicolons, and optional components are wrapped by squared semicolons.

##### `/ensure <pos>` 

Obtain the information of block state and block entity data (if exists) at `<pos>` to check if the block is rendered wrongly or is a ghost block.

##### `/entityconfig <targets> enableStepHeight|disableStepHeight` 

Make `<targets>` can/cannot step on blocks directly.

##### `/entityconfig localPlayer enableStepHeight|disableStepHeight` 

Similar to the last, but the target is always the local player. 

##### `/entityfield <target> get <fieldName>` 

Get the value of `<field>` in the object which corresponds to `<target>`. Note that the names are intermediary names like `field_18276`, please use a mapping to translate them. 

##### `/entityfield <target> listAvailableFields` 

List all fields defined or inherited by the class of `<target>`. 

##### `/entityfield <target> set <fieldName> <newValue>` 

Set the value of `<field>` in the object which represents `<target>` to `<newValue>`.Supported types : int, float, double, boolean(may fail to be set now),String, Vec3d(use "," as delimiter between components).

##### `/entitylog sub <target>` 

Start recording the Motion, position, and listened fields of `<target>` every tick and save them to a CSV file. Note that some of the records will temporarily be saved to a buffer rather than being written to the file immediately, `/entity flush` could be used instead. 
Logs are saved to the entitylog folder in the world folder. 

##### `/entitylog unsub <target>` 

Stop monitoring `<target>` and save the records in the buffer to the disk.

##### `/entitylog listenfield <type> <field>` 

Mark `<field>` 'listened' so that its value will be recorded if any entity containing this field is subscribed by using `/entitylog sub <target>`, execution of this command will restart all recording processes. Currently, `<type>` wouldn't restrict the scope of influence of this command.

##### `/entitylog flush` 

Save the records in the buffer to the disk without stopping recording.

##### `/explode <pos> <power> <fire>` 

Create an explosion with the power of `<power>` at `<pos>`, and create fire if the optional argument `<fire>` is true. The power of explosions can be any single preciseness floating-point number, including Infinities and even `NaN`.

##### `/messcfg` 

Display the version and current config of the mod.

##### `/messcfg <option>` 

Get the current value of `<option>` and related helping information about it.

##### `/messcfg <option> <value>` 

Set the value of `<option>` to `<value>`. See the next section of the document.

##### `/messcfg reloadConfig` 

Update options from `mcwmem.prop`. 

##### `/modify <targets> <key> <val>` 

Change the value `<key>` in the entities with to `<val>`. Much simpler than using `/entityfield`. 

##### `/modify <target> remove` 

Remove `<target>` from the world. 

##### `/moventity <targets> <delta> entity self|piston|shulkerBox|player|shulker` 

Move the entity using `Entity.move()`. The displacement  is determined by `<delta>`. Usually, "self" should be used as the last argument. After the execution, the actual displacement of the entity is outputted.

##### `/moventity <targets> <delta> projectile`

Move the entity in the way projectile entities moves. The distance is determined by `<delta>`. The command seems to be buggy now.

##### `/poi get <pos>` 

Get the type of the POI at `<pos>`. 

##### `/poi getDistanceToNearestOccupied <pos>` 

Get the distance (Maybe the Manhattan distance, and the unit may be a subchunk) to the nearest working station.

##### `/poi scan <center> <radius> <type>` 

Find POIs with the given type in a sphere whose center and radius are given in the command.

##### `/poi scanCobic <corner1> <corner2> <type>` 

Find POIs with the given type in a Cube whose two opposite corners are given in the command. 

##### `/poi set <pos> <type> <replace>` 

Set the POI(Point of Interest) at `<pos>`to `<type>` if there is not a POI at `<pos>` or `<replace>` is true. 

##### `/raycast blocks <from> <to> [visual]` 

Check if the direction-ed segment whose two vertexes are given in the command is blocked by any block, just like the collision checking of projectiles and the raycasts in the calculations of exposure of explosions. After execution, the coordination of checked blocks will be output (there may be duplication), and if the line was blocked, the coordination of the block that blocked the line, the face that blocked the line, and the coordination of the point where the line was blocked is output. If visual is present, the process of the checking will be visualized, that is, all grids which checked blocks are in is displayed in light green, the colliding shape of the block which blocked the line is displayed in orange, while the grid the block is in is displayed in purple, the part of the unblocked line is displayed in magenta while the blocked part is displayed in red.

Here is an example: 

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/raycast-block.png)

##### `/raycast entities <from> <to> <expand> <excludeSender> [visual]` 

Check if the direction-ed line segment whose two vertexes are given in the command is blocked by any entity, just like the collision checking of projectiles. After the execution, if the line was blocked, the type or the custom name of the entity which blocked the line and the coordination of the point where the line was blocked will be output. The argument `<expand>` determines the range of checking, which should be one-half of the width of the bounding box of the projectile add 1 in projectile collision checking simulations.

Example: 

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/raycast-entity.png)

##### `/repeat <times> <feedbacks> ...` 

Repeat executing a command for given times, the argument can be used to indicate if the feedback of the command is output.

##### `/rng world setSeed <seed>` 

Get the current seed of the RNG of the dimension. 

##### `/rng world next int <bounds>` 

Get the next integer in the range [0, bounds) generated by the RNG of the dimension.

##### `/rng world next int|float|double|boolean|gaussian` 

Get the next value generated by the RNG of the dimension.

##### `/rng <target> ...` 

Do something on the RNG of `<target>` (some entities), just like the last two commands. 

##### `/setexplosionblock <blockState> <fireState>` 

Make explosions place `<blockState>` instead of air, `<fireState>` instead of fire. 

##### `/tileentity get <pos>` 

Get the information of the block entity at `<pos>`.

##### `/tileentity set <pos> <type> <tag>` 

Set the block entity at `<pos>` to `<type>`. Optionally, you can specify a `<tag>` as the initial data. (It may fail to execute when the block at `<pos>` is not suitable.) 

##### `/tileentity remove <pos>` 

Remove the block entity at <pos>.In the current version of the mod, if a block needs a block entity, the block entity will be reset after removing(that is a bug). 

### Options

The following options could be set with the command `/messcfg`, and the format of the command is `/messcfg <option> <value>`. For example, to enable the entity boundary box renderer, the command `/messcfg serverSyncedBox true` could be used.

##### `blockInfoRendererUpdateInFrozenTicks`

<font color=#FF0000>**[TODO]**</font>What the block information renderers will do in ticks frozen by the Carpet. Hope it works.

Possible values: NORMALLY|PAUSE|NO_REMOVAL

Default value: NORMALLY

##### `blockShapeToBeRendered` 

Specify the type of block shape rendered when `renderBlockShape` is enabled. The `COLLIDER` shape is the shape used to do calculations about collisions, while the `OUTLINE` shape is the shape used to determine which block the player is looking at.

Possible values: COLLIDER|OUTLINE|RAYSAST|SIDES|VISUAL

Default value: COLLIDER

##### `commandExecutionRequirment` 

Whether or not the execution of commands defined by this mod requires OP permission.

Possible values: true or false

Default value: false

##### `creativeUpwardsSpeed` 

Set the speed at which the player is flying upwards in the creative mode.

Possible values: Real number between 0 and 1024

Default value: 0.05

##### `debugStickSkipsInvaildState` 

Prevent debug sticks change blocks to an invalid state. By now, the option *doesn't work* in some cases, like changing the shape property of a rail can still turn the rail into an illegal state and get broken. 

Possible values: true or false

Default value: false

##### `disableExplosionExposureCalculation` 

Disable the calculation of explosion exposure to reduce the lag caused by stacked TNT explosions. This will also mean that blocks cannot prevent entities from being influenced by explosions.

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

Set how many ticks the rendered rays remain. 

Possible values: Any positive integer.

Default value: 300

##### `entityExplosionRaysVisiblity` 

Enable or disable explosion ray (used to calculate the exposure of entities) renderer. 

Possible values: true or false

Default value: false

##### `entityLogAutoArchiving`

Archive the entity log produced within a single session automatically.

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

Set the maximum number of ticks that can be processed within a single frame when the FPS is lower than 20, setting it to a low value may fix the bug which makes players cannot toggle the flying state when the FPS is too low. 

Possible values: Any positive integer.

Default value: 10

##### `maxEndEyeTpRadius` 

Set the maximum range of teleporting with `endEyeTeleport`. 

Possible values: Any positive real number.

Default value: 180

##### `mobFastKill` 

`/kill` removes mobs directly instead of damaging them. In other words, the death amination of mobs killed by the command will be disabled.

Possible values: true or false

Default value: false

##### `projectileChunkLoading` 

Projectiles load chunks in their processing. It may be helpful in testing pearl canons. Note that if a projectile flies at an extremely high speed when the option is set to true.

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

##### `railNoAutoConnection`

<font color=#FF0000>**[TODO]**</font>Prevent the shape of rails from being changed by surrounding blocks.

Possible values: true or false

Default value: false

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

<font color=#FF0000>**[TODO]**</font>wlujkgfdhlqcmyfdhj...Anyway, never turn this on!

Possible values: true or false

Default value: false

##### `tntChunkLoading` 

TNT entities load chunks in their processing. It may help make some kind of TNT canons. Note that if a TNT entity flies at an extremely high speed when the option is set to true.

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

### Key Binds

**F3 + E**: Toggle the HUD that displays the information of the entity which the player is looking at. 

**F3 + M** : Toggle the HUD that displays the information of the local player. 

**F3 + S**: Toggle the HUD that displays the information of the server-side player. 

### Renderers

***Entity information HUD***: Information about the entity that the player is looking at is got at the end of server ticks, and the information of the local player is got at the end of client ticks. More information about that is available below.

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/HUD-demo.png)

***Explosion rays***: It renders the lines between the center of an explosion and some chosen points in the hitbox of entities affected by the explosion, which determines how much damage the entities will take, and how much the velocity of the entity will change, etc. 

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/explosion-ray-demo.png)

***Server-side hitboxes***: Don't always believe the hitboxes provided by F3 + B because they are adjusted by the client to make movements smoother and don't "keep up with" the server-side when the entity is moving. Sometimes vanilla hitboxes could even be missing if the server has experienced a very long tick but the client hasn't.

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/server-synced-box-demo-0.png)

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/server-synced-box-demo-1.png)

***Data of fluid blocks***: Display the bounding box, height, level, and a vector describing the flowing direction of the targeted fluid block.

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/fluid-info-demo.png)

***Bounding boxes of blocks***: Display the bounding box (the collision box or OUTLINE Shape) of the targeted block.

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/block-box-demo.png)

***The output level of redstone gates***: Display the redstone signal level of the targeted redstone gate (i.e. repeaters and comparators).

![](https://raw.githubusercontent.com/wiki/lovexyn0827/MessMod/media/redstone-gate-demo.png)

### Tool Items 

***Brick***: Pause or continue ticking using /tick freeze provided by the Carpet

***Bone***: Continue ticking for some ticks when ticking is paused by /tick freeze. The number of the ticks equals the count of the stack of bones.

***Netherite Ingot***: Remove all non-player entities. 

### HUD Lines

#### Predefined Data

**`Header line`**: Include the ID(equals to the number of Entity instances created before the entity is loaded), name(custom name or the type in /summon), and age(ticks since the entity is loaded).  

**`Pos`**: The position of the entity.

**`Motion`**: The value of field motion (MCP), velocity(Yarn), or deltaMovement (Offical) in the entity's object. 

**`Delta`**: The distance the entity moved in the last update. It can be treated as velocity.

**`Yaw, Pitch`**: Nothing, but the facing of the entity.

**`Fall Distance`**: The distance the entity has fallen since the entity left the ground, but may be influenced by various factors like updating in lava.

**`General State`**: Some boolean states that all are available in all entities. More information can be found below.

**`Health`**: LivingEntity(including players, armor stands, and mobs)'s health.

**`Forward, Sideways, Upward`**: Some values related to mob's AI or player's input.

**`Speed`**: Two values representing how fast a LivingEntity moves.

**`Living State`**: Some boolean states that only exist in LivingEntity. More information can be found below.

**`Fuse`**: The length of a TNT's fuse, or the number of ticks before the explosion.

**`Power`**: The acceleration of a fireball. Note: A fireball has a drag of 0.05gt^-1, so the fireball won't accelerate forever.

**`Velocity Decay`**: The value of a boat's drag, which can be a different value when the boat is on different grounds. 

#### Shorten States

##### General

**`Gl`**: Rendered with a glowing outline.

**`Inv`**: Won't be affected by most damages.

**`Col`**: Can collide with blocks and other entities than has the state.

**`NG`**: Won't be influenced by gravity.

**`HC`**: Collided with a block horizontally.

**`VC`**: Collided with a block vertically.

**`Wet`**: Some parts of the entity are in water.

**`Sbm`**: The entity is seen as fully submerged in water by the game. <font color=#FF0000>**[WIP]**</font>

**`Sp`**: Sprinting.

**`Sn`**: Sneaking. 

**`De`**: Descending (like sneaking).

**`Sw`**: Swimming.

**`Og`**: On the ground.

##### Living Entity Only

**`Hurt`**: Represents the entity has taken any damage(including 0 and negative amounts) in the last tick.  

**`Fly`**: The entity is fall flying, using an elytra.

**`Slp`**: The entity is sleeping.

**`Dead`**: The entity's health is zero or lower, meaning the entity is dead. 

#### Listened Fields

<font color=#FF0000>**[WIP]**</font>

### Other Features

- Structure blocks can be seen when the player is very far from the block.

- Stacktrace will be printed when the Carpet Mod is not loaded. If the Carpet Mod is loaded, enabling the `superSecretSetting` has the same effect.

### Notice

- The mod is still in development, some feature is not available or buggy, please tell me if you find something wrong. 

- Dedicated servers are not supported currently, so only use the mod in single-player mode or a LAN server. 

- Some commands like /explode ~ ~ ~ 2147483647 true can freeze the server, be careful.