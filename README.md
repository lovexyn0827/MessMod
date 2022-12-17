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
- Minecraft 1.16.4/1.16.5/1.17.1/1.18.2
- Everything the Minecraft requires. 

### Commands

Names of arguments are wrapped by pointy semicolons, and optional components are wrapped by squared semicolons.

##### `/ensure <pos>` 

Get the information of block state and block entity data (if exists) at `<pos>` to check if the block is rendered wrongly or is a ghost block.

##### `/entityconfig <targets> enableStepHeight|disableStepHeight` 

Make `<targets>` can/cannot step on blocks directly.

##### `/entityconfig localPlayer enableStepHeight|disableStepHeight` 

Similar to the last, but the target is always the local player. 

##### `/entityfield <target> get <fieldName> [<path>]` 

Get the value of `<field>` in the object which corresponds to `<target>`, an accessing path can be specified if needed. Note that if the mapping is not loaded, the names here are intermediary names like `field_18276`, which are hard to be understood, please use a mapping to translate them to readable names. 

##### `/entityfield <target> listAvailableFields` 

List all fields defined or inherited by the class of `<target>`. 

##### `/entityfield <target> set <fieldName> <newValue>` 

Set the value of `<field>` in the object which represents `<target>` to `<newValue>`.Supported types : int, float, double, boolean(may fail to be set now),String, Vec3d(use "," as delimiter between components, must be quoted).

##### `/entitylog sub <target>` 

Start recording the Motion, position, and listened fields of `<target>` every tick and save them to a CSV file. Note that some of the records will temporarily be saved to a buffer rather than being written to the file immediately, `/entity flush` could be used instead. 

Logs are saved to the `entitylog` folder in the world folder. 

##### `/entitylog unsub <target>` 

Stop monitoring `<target>` and save the records in the buffer to the disk.

##### `/entitylog listenfield <entityType> <field> [<name> [<path>]]` 

Mark `<field>` as 'listened' so that its value will be recorded if any entity containing this field is subscribed by using `/entitylog sub <target>`, execution of this command will restart all recording processes. Currently, `<type>` wouldn't restrict the scope of influence of this command. 

##### `/entitylog stopListenField <field>`

Stop listening a field.

##### `/entitylog listListenedFields`

List all listened fields.

##### `/entitylog autoSub <entityType>`

Start monitoring a kind of entities automatically.

##### `/entitylog stopAutoSub <entityType>`

Stop monitoring a kind of entities automatically.

##### `/entitylog autoSubName <name>`

Start monitoring entities with the given `<name>` automatically.

##### `/entitylog stopAutoSubName <name>`

Stop monitoring entities with the given `<name>` automatically.

##### `/entitylog flush` 

Save the records in the buffer to the disk without stopping recording.

##### `/entitysidebar add <target> <field> <name> [<whereToUpdate> [<path>]]`

Add a new line to the entity information sidebar. You can specify where the data get updated and an accessing path if necessary.

Supported ticking phases: 

- WEATHER_CYCLE: Just after the calculation of weather cycle was completed and the game time was updated.
- CHUNK: Just after most stuff related to chunks (including unloading, spawning, freezing , snowing, random ticks and many other tasks) get processed.
- SCHEDULED_TICK: Just after the calculating of scheduled tick finished.
- VILLAGE: Just after the states of raids got updated.
- BLOCK_EVENT: Just after all block events got processed.
- ENTITY: Just after all entities got processed.
- TILE_ENTITY: Just after all block entities got processed.
- TICKED_ALL_WORLDS: When all worlds got ticked and the asynchronized tasks like inputs of players haven't got processed.
- SERVER_TASKS: After all asynchronized  tasks got processed.

##### `/entitysidebar remove <name>`

Remove a line from the entity information sidebar.

##### `/explode <pos> <power> [<fire>`] 

Create an explosion with the power of `<power>` at `<pos>`, and create fire if the optional argument `<fire>` is true. The power of explosions can be any single preciseness floating-point number, including Infinities and even `NaN`.

##### `/freezentity freeze|resume <entities>`

Pause|continue processing selected entities.

##### `/hud subField target <entityType> <field> [<name> [<path>]]`

Mark `<field>` as 'listened' so that its value will be included in the looking at entity HUD. Currently, `<type>` make no difference to the execution but being used in resolution of the field name and providing suggestions.

##### `/hud subField client|server <field> [<name> [<path>]]`

Add a listened field to the client-side/server-side player information HUD. You can specift a custom name and an accessing path for the field if necessary.

##### `/hud unsubField target|client|server <name>`

Remove a listened field from a HUD.

##### `/hud setHudTarget <profile>`

Set the player used in getting the data in the server-side player information HUD and the looking at entity HUD in multiplayer games.

##### `/lag nanoseconds [<thread>]`

Make the a thread of the game sleep for a while. If the thread is not specified explicitly, the server thread will sleep.

##### `/logmovement sub <target>`

Subscribe some entities to see how they are pushed by pistons, shulker boxes and shulkers.

##### `/logmovement unsub <target>`

Unsubscribe the entities.

##### `/logpacket sub|unsub <type>`

Start|stop listening to packets between the server and the client(s). For some reason, the results are only printed in the log.

##### `/messcfg` 

Display the version and current config of the mod.

##### `/messcfg <option>` 

Get the current value of `<option>` and related helping information about it.

##### `/messcfg <option> <value>` 

Set the save-specific value of `<option>` to `<value>`. See the next section of the document.

##### `/messcfg reloadConfig` 

Read options from `mcwmem.prop`. 

##### `/messcfg setGlobal <option> <value>`

Set the global value (used as the default value of options for new saves) and the save-specific value of `<option>` to `<value>`.

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

##### `/ride <passengers> <vehicle> <force>`

Make `<passengers>` ride `<vehicle>`

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

##### `accessingPathInitStrategy`

There are there initializing strategies available: 

- Legacy strategy: Accessing paths are only initialized once for its first input, then the result, including the resolved `Member` instances and so on, will be used to handle all subsequent inputs.
- Standard strategy: Accessing paths are parsed for every different inputs, and the parsed copies are cached until the inputs are discarded by the garbage collector.
- Strict Strategy: Accessing paths are reinitialized each time they are used.

Available values: 

- `LEGACY`
- `STANDARD`
- `STRICT`

Default value: STANDARD

##### `antiHostCheating`

Enable anti-cheating for the host player in SP & LAN game.

Available values: true/false

Default value: false

##### `attackableTnt`

TNT entities can be killed by players' attacking.

Available values: true/false

Default value: false

##### `blockInfoRendererUpdateInFrozenTicks`

What should the block information renderers do in ticks frozen by the Carpet.

Available values: 

- `NORMALLY`
- `PAUSE`
- `NO_REMOVAL`

Default value: NORMALLY

##### `blockPlacementHistory`

Record what the players has placed recently so that you may undo or redo these operations later. Note that if the blocks are changed by something other than the player, undoing these related operations may result in unexpected behaviors.

Available values: true/false

Default value: false

##### `blockShapeToBeRendered`

The type of block shape rendered when `renderBlockShape` is enabled. The COLLIDER shape is the  shape used to do calculations about collisions, while the OUTLINE shape is the shape used to let the game know which block the player is looking at. See the wiki on Github for details.

Available values: 

- `OUTLINE`
- `SIDES`
- `VISUAL`
- `RAYCAST`
- `COLLISION`

Default value: COLLISION

##### `commandExecutionRequirment`

Whether or not execution of commands defined by this mod require OP permission.

Available values: true/false

Default value: false

##### `craftingTableBUD`

Detect the block updates received by crafting tables.

Available values: true/false

Default value: false

##### `creativeUpwardsSpeed`

Set the speed which the player is flying upwards at in the creative mode.

Available values: Any positive real number

Default value: 0.05

##### `debugStickSkipsInvaildState`

Prevent debug sticks from changing blocks to an invalid state. By now, the option doesn't work in many cases, for example, changing the `shape` property of a rail can still turn the rail in to an illegal state and have the rail broken. 

Available values: true/false

Default value: false

##### `disableChunkLoadingCheckInCommands`

As the name says, you can fill some blocks in unloaded chunks.

Available values: true/false

Default value: false

##### `disableExplosionExposureCalculation`

Disable the calculation of explosion exposure to reduce the lag caused by stacked TNT explosions, especially when the TNTs are at the same spot. This will also mean that blocks cannot prevent entities from be influenced by explosions. 
This feature may not work properly if the Lithium is loaded.

Available values: true/false

Default value: false

##### `disableProjectileRandomness`

Remove the random speed of projectiles. It could be used to test pearl cannons, but don't forget to disable it if not needed.

Available values: true/false

Default value: false

##### `enabledTools`

Item tools, which makes bone and bricks useful. Requires carpet-fabric.

- Bone: /tick step <countOfBones> 
- Brick: /tick freeze 
- Netherier Ingot: /kill @e[type!=player]

Available values: true/false

Default value: false

##### `endEyeTeleport`

When the player uses ender eyes, teleport it to where it looks at.

Available values: true/false

Default value: false

##### `entityExplosionInfluence`

Tell you how entities are affected by explosions. Remember to turn it off if you are going to test something like TNT compressors, or the game will be frozen.
Incompatible with Lithium.

Available values: true/false

Default value: false

##### `entityExplosionRaysLifetime`

The number of ticks the rendered rays remains. 

Available values: Any integer

Default value: 300

##### `entityExplosionRaysVisiblity`

Explosion ray (used in the calculation the exposure of entities) renderer. Remember to turn it off if you are going to test something like TNT compressors, or the game will be frozen.

Available values: true/false

Default value: false

##### `entityLogAutoArchiving`

Archive the entity log produced within a single session automatically. These archives can be found in folder `World Folder/entitylog/archives`.

Available values: true/false

Default value: true

##### `generateChunkGrid`

Generate a layer of glass on the ground to show the chunks.

Available values: true/false

Default value: false

##### `getEntityRangeExpansion`

[TODO]In the vanilla getEntities() method, only entities which are in subchunks whose Cheshev distances to the given AABB are smaller than 2 blocks are seen. Usually it doesn't matter, but when height of some of the entities is greater than 2 blocks or the width is greater than 4 blocks, it can lead to some problems, especially when the entity is close to the boundary of subchunks. Change it to a higher value may fix some bugs about interaction between entities and something else.

Available values: Any positive real number

Default value: 2.0

##### `hideSurvivalSaves`

Hide worlds that is likely to be survival saves to prevent it to be opened accidentally. Can only be set globally.

Available values: true/false

Default value: false

##### `hotbarLength`

The number of item stacks the hotbar can contain. Note that this feature is not finished currently, some features like saving hotbars and vanilla hotbat texture is not available.

Available values: Any integer between 1 and 36 (inclusive)

Default value: 9

##### `hudAlignMode`

Move the HUDs to a given location.

Available values: 

- `TOP_LEFT`
- `TOP_RIGHT`
- `BOTTIM_LEFT`
- `BOTTOM_RIGHT`

Default value: TOP_RIGHT

##### `hudStyles`

The style of the HUDs, containing zero or more flags below: 

- B: Render a gray background
- L: Align the headers on the left and the data on the right
- R: Change the color of headers to red

Available values: Any string

Default value: (BL)^2/(mR)

##### `hudTextSize`

Set the size of the text in the HUDs.

Available values: Any positive real number

Default value: 1.0

##### `interactableB36`

Allow players to break block-36s and place something against it.

Available values: true/false

Default value: false

##### `language`

The main language of the Mod.

Available values: - -FOLLOW_SYSTEM_SETTINGS-

- zh_cn
- zh_cn_FORCELOAD
- en_us
- en_us_FORCELOAD

Default value: -FOLLOW_SYSTEM_SETTINGS-

##### `maxClientTicksPerFrame`

The maximum number of ticks can be processed within a single frame when the FPS is lower than 20.

Available values: Any positive integer

Default value: 10

##### `maxEndEyeTpRadius`

Set the maximum range of teleportation with endEyeTeleport.

Available values: Any positive real number

Default value: 180

##### `minecartPlacementOnNonRailBlocks`

Allow players to place minecarts directly on the ground.

Available values: true/false

Default value: false

##### `mobFastKill`

/kill kill mobs by removes them directly instead of damaging them.

Available values: true/false

Default value: false

##### `projectileChunkLoading`

Allow projectiles to load chunks for themselves in their calculations, which maybe helpful in testing pearl canons.  Note that if a projectile flies at a extremely high speed when the option is set to true, the server may be lagged greatly.

Available values: true/false

Default value: false

##### `projectileChunkLoadingPermanence`

Projectiles load the chunks permanently when projectileChunkLoading is enabled.

Available values: true/false

Default value: false

##### `projectileChunkLoadingRange`

Set the radius of entity processing chunks loaded by projectileChunkLoading.

Available values: Any non-negative integer

Default value: 3

##### `projectileRandomnessScale`

The amount of the randomness of projectiles.

Available values: Any real number

Default value: 1.0

##### `quickMobMounting`

Placing mobs into vehicles.

Available values: true/false

Default value: false

##### `railNoAutoConnection`

Prevent the shape of rails from being changed by surrounding rails.

Available values: true/false

Default value: false

##### `rejectChunkTicket`

Prevent the chunks from being loaded in some ways.

Available values: Some of the following elements, separated by ',': 

- start
- dragon
- player
- forced
- light
- portal
- post_teleport
- unknown

Default value: []

##### `renderBlockShape`

Enhanced block outline renderer.

Available values: true/false

Default value: false

##### `renderFluidShape`

Display the outlines, heights, and vectors describing the flowing directions of the target fluid blocks.

Available values: true/false

Default value: false

##### `renderRedstoneGateInfo`

Display the output level of repeaters and comparators the player looks at.

Available values: true/false

Default value: false

##### `serverSyncedBox`

Enable or disable the server-side hitbox renderer.

Available values: true/false

Default value: false

##### `serverSyncedBoxRenderRange`

The maximum Cheshev distance between the player and the entities with their bounding boxes rendered. All non-positive values are considered as positive infinite.

Available values: Any real number

Default value: -1

##### `skipUnloadedChunkInRaycasting`

Ignore potential collisions in unloaded chunks in raycasts. Enabling it may speed up long distance raycasts.

Available values: true/false

Default value: false

##### `skippedGenerationStages`

Skip some stages in the world generation. Skipping stage `biome` and `full` is not supported, as the absense of them will make the server crash.

Available values: Some of the following elements, separated by ',': 

- empty
- structure_starts
- structure_references
- noise
- surface
- carvers
- liquid_carvers
- features
- light
- spawn
- heightmaps

Default value: []

##### `stableHudLocation`

Make the location of HUDs more stable when the length of lines change frequently.

Available values: true/false

Default value: true

##### `strictAccessingPathParsing`

Parse accessing paths in a more strict way, to make them more reliable. Currently the strictly checking system is not completed, so it is not recommended to enable it.

Available values: true/false

Default value: false

##### `superSuperSecretSetting`

wlujkgfdhlqcmyfdhj...Never turn it on!

Available values: true/false

Default value: false

##### `tntChunkLoading`

Allow TNT entities to load chunks for themselves in their ticking, which maybe helpful in designing some kinds of TNT canons.

Available values: true/false

Default value: false

##### `tntChunkLoadingPermanence`

TNT entities load the chunks permanently when tntChunkLoading is enabled.

Available values: true/false

Default value: false

##### `tntChunkLoadingRange`

The radius of entity processing chunks loaded by tntChunkLoading.

Available values: Any non-negative integer

Default value: 3

##### `vanillaDebugRenderers`

Enable some vanilla debugging renderers, some of which won't actually work.

Available values: Some of the following elements, separated by ',': 

- pathfindingDebugRenderer
- waterDebugRenderer
- chunkBorderDebugRenderer
- heightmapDebugRenderer
- collisionDebugRenderer
- neighborUpdateDebugRenderer
- caveDebugRenderer
- structureDebugRenderer
- skyLightDebugRenderer
- worldGenAttemptDebugRenderer
- blockOutlineDebugRenderer
- chunkLoadingDebugRenderer
- villageDebugRenderer
- villageSectionsDebugRenderer
- beeDebugRenderer
- raidCenterDebugRenderer
- goalSelectorDebugRenderer
- gameTestDebugRenderer

Default value: []

### Key Binds

**F3 + E**: Toggle the HUD that displays the information of the entity which the player is looking at. 

**F3 + M** : Toggle the HUD that displays the information of the local player. 

**F3 + S**: Toggle the HUD that displays the information of the server-side player. 

**Ctrl + Z**: Undo block placement or breaking. (Requires `blockPlacementHistory`)

**Ctrl+ Y**: Redo block placement or breaking. (Requires `blockPlacementHistory`)

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

### Accessing Paths

See the wiki.

### Options In Entity Selectors

##### `id`

Possible values: An integer or a range, just like the `level` option in the vanilla entity selector.

Select entities with given numberic ID (field `entityId`, `networkId`).

##### `side`

Possible values: `client` or `server`

Where the entities are selected from. Note that this feature is not thread-safe, so it should be used in some simple commands without side-effects (in other words, commands that only reads stuff but doesn't writes).

### Other Features

- Structure blocks can be seen when the player is very far from the block.

- Stacktrace will be printed when the Carpet Mod is not loaded. If the Carpet Mod is loaded, enabling the `superSecretSetting` has the same effect.

### Notice

- The mod is still in development, some feature is not available or buggy, please tell me if you find something wrong. 

- Dedicated servers are not supported well currently and there are many unsolved bugs related to the connection between the server and the client, so only use the mod in single-player mode or the host client of a LAN server. 

- Some commands like /explode ~ ~ ~ 2147483647 true can freeze the server, be careful.
