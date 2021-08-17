# Minecraft-World-Manipulation-Enchantment-Mod
`A Minecraft mod that provides extra ability to manipulation a world and some enachantments (not something can be applied to items).`  

Just like what the name says, the Mod contains functions in many fields, therefore the mod seems to be messy, even the style of the source code is messy.

In other languages:  
[简体中文](/README_zh_cn.md)

## Requires:
1.Fabric Loader 0.7.4+.  
2.The fabric-carpet by gnembon.(Strongly recommended, but not necessary)  
3.Minecraft 1.16.4 or 1.16.5.  
4.Everything Minecraft 1.16.x requires.  

## Commands: 
Names of agruments are wrapped by pointy semicolons, and optional components are wrapped by squared semicolons.
`/entityconfig <targets> enableStepHeight|disableStepHeight`:Make \<targets\> can/cannot step on blocks directly.
`/entityconfig localPlayer enableStepHeight|disableStepHeight`:Similar to the last,but the target is always the local player.  
`/entityfield <target> get <fieldName>`:Get the value of \<field\> in the object which represents \<target\>.Note that the names are intermediary names like field_827,please use a mapping to translate them before the automatical mapping is added.  
`/entityfield <target> listAvailableFields`:List all fields defined or inherited by the class of\<target\>.  
`/entityfield <target> set <fieldName> <newValue>`:Set the value of \<field\> in the object which represents <target> to \<newValue\>.Supported types : int,float,double,boolean(may fail to be set now),String,Vec3d(use "," as delimiter).  
`/explode <pos> <power> <fire>`:Create an explosion with the power of \<power\> at \<pos\>,and create fire if the optional argument <fire> is true.  The power of explosions can be any single preciseness floationg-point number, including Infinities and even NaN.
`/messcfg`:Display the version and current config of the mod  
`/messcfg blockShapeToBeRendered  COLLIDER|OUTLINE|RAYSAST|SIDES|VISUAL`:Choose which shape of a block will be rendered when the option renderBlockShape is true. COLLIDER corrsponds the colliding shape, OUTLINE  corrsponds the box which is drawn when the player look at a block. Note that RAYCAST and SIDES shape may be rendered not well.
`/messcfg enabledTools <bool>`:Enable or disable item tools(See below).  
`/messcfg entityExplosionInfluence <bool>`:Make the game send the infomation of entities affected by explosions or nothing.Not available in the version.(There is some bugs in non-development environments now)  
`/messcfg entityExplosionRaysVisiblity <bool>`:Enable or disable explosion ray (used to calculate the exposure of entities) renderer.  
`/messcfg entityExplosionRaysVisiblity setLifetime <ticks>`:Set how many ticks the rendered rays remains.   
`/messcfg mobFastKill <bool>`:Enable or disable fastMobKill,which makes /kill removes mobs directly instead of damage them. 
`/messcfg reloadConfig`:Update options from mcwmem.prop.  
`/messcfg renderBlockShape <bool>`:Enable or disable block outline rendering.
`/messcfg renderFluidShape <bool>`:Enable or disable the renderer of oulines, heights, and vectors of flowing directions of  target fluids.
`/messcfg serverSyncedBox <bool>`:Enable or disable the server-side hitbox renderer. `/messcfg setHudDisplay bottomLeft|bottomRight|topLeft|TopRight`:Move the HUDs to the given location.   
`/modify <targets> <key> <val>`:Change the value \<key\> in the entities with to \<val\>.Much simpler than using /entityfield.  
`/modify <target> remove`:Remove \<target\> from the world.  
`/moventity <targets> <delta> entity self|piston|shulkerBox|player|shulker`:Move the entity using Entity.move().The distance is determined by \<delta\>.Usually,we should use "self" as the last argument.  After the execution, the actual displacement of the entity is outputted.
`/moventity <targets> <delta> projectile`:Move the entity in the way projectile entities moves.The distance is determined by \<delta\>.The command seems to be buggy now.
`/poi get <pos>`:Get the name of the POI at \<pos\>.  
`/poi getDistanceToNearestOccupied <pos>`:Get the distance (Maybe the Manhattan distance, and the unit may be a subchunk) to the nestest working station.
`/poi scan <center> <radius> <type>`:Find POIs with the given type in a sphere whose center and radius is given in the command.
`/poi scanCobic <corner1> <corner2> <type>`:Find POIs with the given type in a Cube whose two opposite corners are given in the command.
`/poi set <pos> <type> <replace>`:Set the POI(Point of Interest) at \<pos\> to \<type\> if there is not a POI at \<pos\> or <replace> is true.  
`/raycast blocks <from> <to> [visual]`:Check if the directioned line segument whose two vertexes are given in the command is blocked by any block, just like the collision checking of projectiles and the raycasts in the calculations of exposure of explosions. After execution, the coordination of checked blocks will be output (there may be duplication) , and if the line was blocked, the coordination of the block who blocked the line, the face  blocked the line and the coordination of the point  where the line was blocked are output. If visual is present, the process of the checking will be visualized, that is , all grids which checked blocks is in is displayed in light green, the colliding shapeof the block which blocked the line is displayed in orange, while the grid the block is in is displayed in purple, the part of the unblocked line is displayed in magenta while the blocked part is displayed in red.
`/raycast entities <from> <to> <expand> <excludeSender> [visual]`:Check if the directioned line segment whose two vertexes are given in the command is blocked by any entity, just like the collision checking of projectiles. After the execution, if the line was blocked, the type or the custom name of the entity which blocked the line and the coordination of the point where the line was blocked will be output.The argument expand determines the range of checking, should be the half of the width of the bounding box of the projectile add 1 in projectile collision checking simulation.
`/repeat <times> <feedbacks> ...`:Repeat executing a command for given times, the argument /<feedback/> can be used to indicate if the feedback of the command is output.
`/rng world setSeed <seed>`:Get the current seed of the RNG of the dimension.  
`/rng world next int <bounds>`:Get the next integer in the range [0, bounds) generated by the RNG of the dimension.
`/rng world next int|float|double|boolean`:Get the next value generated by the RNG of the dimension.
`/rng <target> ...`:Do something on the RNG of \<target\>，just like the last two commands.  
`/setexplosionblock <blockState> <fireState>`:Make explosions place \<blockState\> instead of air,\<fireState\> instead of fire.  
`/tileentity get <pos>`:Get the information of the block entity at \<pos\>.
`/tileentity set <pos> <type> <tag>`:Set the block entity at \<pos\> to \<type\>.~Optionally,you can specify a <tag> as the initial data.~(It may fail to execute when the block at \<pos\> is not suitable.)  
`/tileentity remove <pos>`:Remove the block entity at \<pos\>.In current version of the mod,if there is a block which needs a block entity,the block entity will be reset after removing(that is a bug).  

## Keybinds:
`F3+E` :Toggle the HUD that displays the information of the entity which the player is looking at.  
`F3+M` :Toggle the HUD that displays the information of the local player.  

## Renderers: 
`Entity information HUD`:See below.Information of the entity that the player is looking at is got at the end of server ticks,and the information of local player is got at the end of client ticks.  
`Explosion rays`:It renders the lines between the center of a explosion and some chose points in the hit box of entities affected by the explosion,which determines how much damage the entities will take,how much the velocity of the entity will change,etc.  
`Server-side hitboxes`:Don't always believe the hitboxes provided by F3+B because they are adjusted by the client to make movements smoother and don't "keep up with" the server-side when the entity is moving.
## Item Tools: 
`Brick`:Pause or continue ticking using /tick freeze provided by the Carpet  
`Bone`:Continue ticking for some ticks when ticking is paused by /tick freeze.The number of the ticks equals to the count of the stack of bone.  
`Netherite Ingot`:Remove all non-player entity.  

## HUD lines:
`Header line` :Include the ID(equals to the number of Entity instance created before the entity is loaded),name(custom name or the type in /summon) and age(ticks since the entity is loaded).  
`Pos` :The position of the entity.  
`Motion` :The value of field motion(MCP),velocity(Yarn) or deltaMovement(Offical) in the entity's object.
`Delta` :The distance the entity moved in the last update.Itcan be treated as velocity.  
`Yaw,Pitch` :Nothing,but the facing of the entity.  
`Fall Distance` :The distance the entity has fallen since the entity leaved the ground,but may be influenced by various factors like updating in lava.  
`General State` :Some boolean states that all is available in all entities.More information can be found below.  
`Health` :LivingEntity(include players,armor stands and mobs)'s health.  
`Forward,Sideways,Upward` :Some values related to mob's AI or player's input.  
`Speed` :Two values representing how fast a LivingEntity moves.  
`Living State` :Some boolean states that only exists in LivingEntity.More information can be found below.  
`Fuse` :The length of a TNT's fuse.The value is always 80 in a normal TNT.  
`Power` :The acceleration of a fireball._Note:A fireball has a drag of 0.05gt^-1,so the fireball won't accelerate forever.  
`Velocity Decay` :The value of a boat's drag,which can be different value when the boat in on different grounds.  
## Shorten States
### General
`Gl` :Entities that has the state is rendered with a glowing outline.  
`Inv` :Entities that has the state won't be affected by most damages.  
`Col` :Entities that has the state can collide with blocks and other entities than has the state.  
`NG` :Entities that has the state won't be influenced by gravity.  
`HC` :Represents the entity is collided with a block horizontally.  
`VC` :Represents the entity is collided with a block vertically.  
`Wet` :Represents the some parts of the entity is in water.  
`Sp` :Represents the entity is sprinting.  
`Sn` :Represents the entity is sneaking.
`De` :Represents the entity is descending(like sneak);
`Sw` :Represents the entity is swimming.  
`Og`:Represents the entity is on the ground.
### Living
`Hurt` :Represents the entity took any damage(include 0 and negative amounts) in the last tick.  
`Fly` :Represents the entity is fallflying,using a elytra.  
`Slp` :Represents the entity is sleeping.  
`Dead` :Represents the entity's health is zero or lower,meaning the entity is dead.  
##Other Features
1.Structure blocks can be seen when the player is very far from the block.
2.Stacktrace will be printed when the Carpet Mod is not loaded.If the Carpet Mod is loaded, enabling the superSecretSetting has the same effect.

## Notice:
1.The mod is still in development,some feature is not available or buggy,please tell me if you find something wrong.  
2.The local player is a ClientPlayerEntity instead of a ServerPlayerEntity beacuse most of the calculation of players' movement is calculated on clent side and values in ServerPlayerEntity can be zero and useless,meaning that if you are looking at a remote player,some values can be wrong.  
3.Dedicated server is not supported in current version,so only use the mod in single player mode or a LAN server.  
4.Some command like /explode \~ \~ \~ 2147483647 true can freeze the server,be careful.