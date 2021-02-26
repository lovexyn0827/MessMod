# Minecraft-World-Manipulation-Enchantment-Mod
**A Minecraft mod that provides extra ability (like creating explosions ect.) to manipulation a world.**  
## Requires:
1.A FabricLoader.  
2.The carpet-fabric by gem.
3.Minecraft 1.16.x(1.16.4 is the best).  
4.Everything Minecraft 1.16.x requires.  
## Commands:
**/biome get <\pos\> **:Get the name of the biome at \<pos\>.  
**~~/biome set \<pos\> \<biome\>~~(Will be available in future visions)** :Set the biome at \<pos\> to \<biome\>.  
**/entityfield \<\target\> get \<fieldName\> **:Get the value of \<field\> in the object which represents \<target\>.Note that the names are intermediary names like field_827,please use a mapping to translate them before the automatical mapping is added.  
**/entityfield \<target\> listAvailableFields **:List all fields defined or inherited by the class of\<target\>.  
**/entityfield \<target\> set \<fieldName\> \<newValue\> **:Set the value of \<field\> in the object which represents \<target\> to \<newValue\>.Supported types:int,float,double,boolean,String,Vec3d(use "," as delimiter)  
**/explode \<pos\> \<power\> \<fire\> **:Create an explosion with the power of \<power\> at \<pos\>,and create fire if the optional argument \<fire\> is true.  
**/mcwmem setDisplay \<location\> **:Move the HUDs to \<location\>.  
**/modify \<target\> \<key\> \<val\> **:Change the value \<key\> in the entity with \<id\> to \<val\>.  
**/setexplosionblock \<blockState\> \<fireState\> **:Make explosions place \<blockState\> instead of air,\<fireState\> instead of fire.  
**/setpoi \<pos\> \<type\> \<replace\> **:Set the POI(Point of Interest) at \<pos\> to\<type\> if there is not a POI at \<pos\> or \<replace\> is true.  
## Keybinds:
F3+E :Toggle the HUD that displays the information of the entity which the player is looking at.  
F3+M :Toggle the HUD that displays the information of the local player.  
## Configure Items
## HUD lines:
Header line :Include the ID(equals to the number of entities loaded before the entity is loaded),name(custom name or the type in /summon) and age(ticks since the entity is loaded).  
Pos :The position of the entity.  
Motion :The value of field motion(MCP),velocity(Yarn) or deltaMovement(Offical) in the entity's object,captured in the end of a server tick.   
Delta :The distance the entity moved in the last update.  
Yaw,Pitch :Nothing,but the facing of the entity.  
Fall Distance :The distance the entity has fallen since the entity leaved the ground,but may be influenced by various factors like updating
in lava.  
General State :Some boolean states that all is available in all entities.More information can be found below.  
Health :LivingEntity(include players,armor stands and mobs)'s health.  
Forward,Sideways,Upward :Some values related to mob's AI or player's input.  
Speed :Two values representing how fast a LivingEntity moves.  
Living State :Some boolean states that only exists in LivingEntity.More information can be found below.  
Fuse :The length of a TNT's fuse.The value is always 80 in a normal TNT.  
Power :The acceleration of a fireball._Note:A fireball has a drag of 0.05gt^-1,so the fireball won't accelerate forever.  
Velocity Decay :The value of a boat's drag,which can be different value when the boat in on different grounds.  
## Shorten States
### General
Gl :Entities that has the state is rendered with a glowing outline.  
Inv :Entities that has the state won't be affected by damage,explosions,etc.  
Col :Entities that has the state can collide with blocks and other entities than has the state.  
NG :Entities that has the state won't be influenced by gravity.  
HC :Represents the entity is collided with a block horizontally.  
VC :Represents the entity is collided with a block vertically.  
Wet :Represents the some parts of the entity is in water.  
Sp :Represents the entity is sprinting.  
Sn :Represents the entity is sneaking.
De :Represents the entity is descending(like sneak);
Sw :Represents the entity is swimming.  
### Living
Hurt :Represents the entity took any damage(include 0 and negative amounts) in the last tick.  
Fly :Represents the entity is fallflying,using a elytra.  
Slp :Represents the entity is sleeping.  
Dead :Represents the entity's health is zero or lower,meaning the entity is dead.  
## Something else:
1.The mod is still in development,some feature is not available or buggy,please tell me if you find something wrong.  
2.The local player is a ClientPlayerEntity instead of a ServerPlayerEntity beacuse most of the calculation of players' movement is calculated on clent side and values in ServerPlayerEntity can be zero and useless,meaning that if you are looking at a remote player,some values can be wrong.  
3.Dedicated server is not supported in current version,so only use the mod in single player mode or a LAN server.  
4.Some command like /explode \~ \~ \~ 2147483647 true can freeze the server,be careful.