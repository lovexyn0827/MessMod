- # 0.10.0+v20251019-BETA

  > For 2024.10.19, a autumn night that is not as cold as seemed.

  ## New

  ### Options

  - `clayBlockPlacer`
    - Place any technical blocks (e.g. nether portals) with clay balls. 
    - Clay balls should be named to specify the block to place. For example, with a clay ball named `grass_block[snowy=true]`, you can place snowy grass blocks by right clicks.
  - `playerInputsWhenScreenOpened`
    - Allow players to move and turn around while typing commands, etc.
    - Only when a hotkey, `F6` by default, is pressed.
  - `playerInputsWhenScreenOpenedHotkey`
  - `loomWaveGenerator`
    - An option to completely turn loom wave generator off.

  ### Commands

  - `/tileentity add`
    - Legacy `/tileentity set`.
  - `/nameitem`
    - Name the item the player is holding, without needing an anvil.
  - `/drawshape`
    - Draw boxes, lines, texts, or pictures in-game, as a marker.

  ### Others

  - Oscilloscope can now detect edges actively, allowing signal level changes without block updates to be captured. Can be disabled if you want to.
  - Repetition of waveform stages in standard mode.
    - e.g. `100[(+0=>+0)L15 (+0=>+0)L0]`, which repeats `(+0=>+0)L15 (+0=>+0)L0` 100 times
    - Can be nested: `3[5[(+0=>+0)L15 (+0=>+0)L0]]`
    - Alternative syntax: `3*[(+0=>+0)L15 (+0=>+0)L0]`
  - Command suggestions for waveforms
    - A little bit "silly", though.

  ## Changes

  ### Commands

  - `/messcfg` no longer list all options at once.
    - Instead, a list of labels, like `REDSTONE` or `INTERACTION_TWEAKS`, which you can click to get related options, are listed.
  - Renamed `/tileentity set` to `/tileentity replace`

  ### Oscilloscope

  - A more compact GUI of channel list.
  - Non-visible channels no longer emit triggers

  ### Waveforms

  - Abbreviations of ticking phases can be used in waveform definitions.
    - e.g. `ENTITY` => `EU`、`BLOCK_EVENTS` => `BE`

  ### Other

  - Advanced mixins are disabled by default now.

  ## Fixes

  - Error messages send when trying to add custom columns to a running entity log are not translated.
  - Some commands has not feedback at all.
    - `/logdeath unsub`
    - `/hud setHudTarget`
    - `/hud setHudTarget`
  - Some commands may fail due to uncaught exceptions.
    - `/logdeath sub`, when the death predicate has syntax errors.
    - `/exportsave export`, when export fails due to a unexpected error.
    - `/rng`, when no entity is selected.
  - The Chinese feedback of `/partlykill` is not grammatically correct.
  - `/tileentity set` may fail to set block entities.
    - 4.5-years-old
  - Strong signals (level >= 15) are not captured properly by the oscilloscope.
  - There are "gaps" between waveform's cycles.
  - Check boxes in oscilloscope channels list doesn't match actual channel settings.
  - Shapes are not rendered in customs dimensions.
    - 4-years-old
  - Oscilloscope GUI may crash the client with `ConcurrentModificationException`.
  - New oscilloscope channels are added to the channel list twice.
  - Tooltips in oscilloscope GUI are sometimes rendered outside the screen.
  - Client crashes when loading replays of ReplayMod.
  - Arrays of arrays (instead of multidimensional arrays) cannot be created by array literals.
  - Subcommands of `/variable` is incorrectly registered.
  - Vertical scaling of analog wave areas of oscilloscope is inconsistent with horizontal
  - Shapes sent when players are in another dimension is not visible.
  - Trig level markers cannot be dragged twice.
  - Dragging trig level marks doesn't cause trig level text fields to update.
  - `misc.negativeid` is not translated in current language.

# 0.9.0+v20250827-BETA

## New

### Options

- `hayOscilloscope`
  - Make hay block a oscilloscope / logic analyzer.
- hayOscilloscopeChannelVisibilityBroadcast
- `smartCursorMode`
  - Allow cursors to stop at word delimiters other than spaces (e.g. commas in entity selectors) when editing commands with Ctrl + Arrow or Ctrl + Backspace.
- `smartCursorCustomWordDelimiters`
- `survivalStatusBarInCreativeMode`
- `survivalXpBarInCreativeMode`
- `disableClientChunkUnloading`

### Commands

- `/logdeath`
  - Record death causes of entities and get statistics.
- `/wavegen`
  - Wave generator (a.k.a. function generator) in Minecraft.
- `/touch`
  - Update a large range of blocks. Fix "abnormal" block states.
- `/setblockraw`
  - Set block with `setBlockState()` method directly

### Others

- Array literal

## Changes

- `<exploder>` argument in `/explode`.
  - To emulate powered creeper explosion / TNT looting.
- `/variable print <var> toString` can accpet an accessing path to apply to the variable before printing.
- Recording & Statistics in `/logpacket`.

## Fixes

- `flowerFieldRendererSingleLayer` has no description & crashes the client
- `flowerFieldRenderer` crashes the server with `ConcurrentModificationException` when generating new chunks of biome other than flower forest.
- `TranslatableExceptions` thrown when creating variable with literals are not handled properly, resulting in "unexpected error".
- Potential resource leakage when saving custom accessing path nodes.
- Compiled SimpleNodes (.x, .y, .identityHash, etc.) has incorrect output type, causing unexpected compilation failure.
- Class literals are not remapped.
- Older custom HUD line added with `/hud subField ... -THIS-` are incorrectly replaced by new lines referecing fields directly.
  - Hard to believe it has been there since May 2022!
- `class` node not doesn't present in suggentions.
- `/variable print <slot>` doesn't work

# 0.8.0+v20250311-BETA

## Options

- creativeNoVoidDamage
- resistanceReducesVoidDamage
- disableCreativeForcePickup
- quickStackedEntityKillingOneTypeOnly
- detailedChunkTaskLogging
- directChunkAccessForMessMod
- entityExplosionImpulseScale
- flowerFieldRenderer

## Commands

- /loadjavaagent
- /logdeath
- /stackentity
- /variable print
- /variable print <slot> dumpFields <depth>
- /variable set <obj> <path>

## Fix

- Incorrect ordinal of nodes.
- Focus command suggestions on the server.
- Ordinal of nodes duplicates in AccessingFailureException.
- Field type of generic-typed classes are not accurate.
- Class names are not mapped in class cast node.
- Collision restriction are not output currectly.
- TickingPhase are not suggested natually.
- Exporting maps doesn't work properly and has side-effect on original save.
- Memory leak in chunk behavior logger.
- `attackableTnt` & `quickStackedEntityKilling` doesn't cooperate well.
- `/variable set new ...` doesn't parse arguments properly.

# 0.7.2+v20240129-BETA

## Fixed

- Game crashes when rendering the name of an entity (Issue #8) (Affects MC1.19.4+)
- Removed unnecessary debug markers (Affects MC1.18.2+)
- The threshold on the TPS chart is displayed incorrectly. (Affects MC1.16.5-MC1.20.2)

# 0.7.1+v20240112-BETA

## Fix
- fix: Some entity poses aren't handled properly, causing mc to crash ([Issue #7](https://github.com/lovexyn0827/MessMod/issues/7))

# 0.7.0+v20231216-BETA

## New Options

- `serverSyncedBoxUpdateModeInFrozenTicks`
  - What the server-side hitbox renderer should do in ticks frozen by the Carpet.
- `tpsGraphScale`
  - Scale the TPS (MSPT) graph so that it can be held by the screen.

- `quickStackedEntityKilling`
  - Kill the entity being knocked by a brick, along with all entities being at the same position as it.
- A couple of options to get the information of entities more convenient:
  - `dumpTargetEntityDataWithPaper`
    - Output the data of the current target entity by right-clicking while holding a paper.
  - `dumpTargetEntityDataWithCtrlC`
    - Output the data of the current target entity with `Ctrl + C`.
  - `dumpTargetEntityNbt`
    - Output the NBT data of the targeted entity. 
  - `dumpTargetEntitySummonCommand`
    - Generate a command to summon the target entity. 
  - `dumpTargetEntityDataOnClient`
    - Output the information of the client-side entity instead of the one of the server-side one.
- `accessingPathDynamicAutoCompletion`
  - Support suggestions for fields and methods when auto-completing Accessing paths.
- `defaultSaveComponents`
  - Save components that are included in export saves by default.
- `independentEntityPickerForInfomation`
  - Pick crosshair-targeted entities for information providers (currently only the UUID suggestor) independently.
- `getEntityRangeExpansion`
  - In the vanilla `getEntities()` method, only entities that are in subchunks whose Cheshev distances to the given AABB are smaller than 2 blocks are seen. Usually, it doesn't matter, but when the height of some of the entities is greater than 2 blocks or the width is greater than 4 blocks, it can lead to some problems, especially when the entity is close to the boundary of subchunks. Changing it to a higher value may fix some bugs about the interaction between entities and something else.
- `disableItemUsageCooldown`
  - Disable item usage cooldown for ender pearls and chorus fruits, etc.
- `expandedStructureBlockRenderingRange`
  - Expand the maximum visible distance of structure blocks.

## New Commands

- `/variable`
  - Creates and manages variables that hold Java objects.
- `/logchunkbehavior addColumn|removeColumn`
  - Creates and manages custom columns for chunk behavior logger.
- `/messcfg list [<label>]`
  - List options with given labels.
- `/lag while & /lag once`
  - Freeze a thread of the game for once, or freeze the current game at a given phase for a given number of ticks.

## Other New Features

- Auto-completion of accessing paths.
- More chunk events for `/logchunkbehavior`.
- Advanced mixin selecting dialog.
- Ticking phase `DIM_REST`.
- Variable literals.
- Simple node `this`.
- Save component `GAMERULES`.
- Entity selector @t.
- Literal node in accessing path.
- Press F8 in the title screen to choose advanced mixins.

## Changes

- The dimension argument in `/export addRegion` can be omitted to use the current dimension more convenient.
- No longer expand the rendering range of structure blocks by default.
- No longer use scarpet for enabledTools.
- Removed original `/lag` command.

## Fixes

- The default value of `creativeUpwardsSpeed` doesn't match the vanilla mechanism perfectly (#4)
- Potential issues related to forking accessing paths.
- Entity names are not reflected properly in the name of logs produced by the entity data logger.
- Crashes caused by `ConcurrentModificationException` in entity logger.
- Invalid characters in the name of exported saves are not stripped.
- Update callbacks for chunk info renderer are not removed properly.
- Field name suggestion is not available when using some commands as a child of `/execute`.
- Custom line lists returned by `/hud listFields` aren't complete。
- Component nodes are not compiled properly.
- Some option parsing errors are not translated.
- Dedicated server crash caused by `EntitySelectorMixin`.
- Accessing paths are not uninitialized properly.
- Compiled nodes have incorrect output types.
- `generateChunkGrid` causes world generation glitches in basalt deltas.
- Argument types are registered on the client too late.
- Cannot use non-ASCII characters in the name of exported saves.
- Cannot use . in method & mapper node.
- Unicode escapes in string literals are not handled properly.
- `/lazyload` affects all dimensions.
- HUDs are rendered incorrectly when rendered on the left or bottom of the screen.
- Using already used names in new sidebar lines is reported as successful despite rejection.
- Header lines are sometimes rendered outside the screen.
- Not compatible with Fabric 0.15+.

# 0.6.1+v20230907-BETA

## Fixed

-  `/lazyload add|remove <corner1>` doesn't work properly.
- Game crashes if Carpet is not loaded.
- Dedicate server crash.
- Changes to blocks with inventories cannot be undone properly.
- Automatic mapping downloading doesn't work when using modern versions of TIS Carpet Additions.

# 0.6.0+v20230823-BETA

The biggest update since the mod was created, I guess.

## New Features

### Options
- `allowTargetingSpecialEntities`
   - Mainly to enable UUID suggestions for entities like ender pearls and items.
- `fletchingTablePulseDetector` 
   - Measures the length and exact starting and ending time (including ticking phases) of an redstone pulse.
   - By default, only positive pulses are detected, but you may enable detecting negative pulses with `fletchingTablePulseDetectingMode`.
- `fletchingTablePulseDetectingMode`
- `fillHistory`
   - Allow you to undo or redo `/fill`.
- `allowSelectingDeadEntities`
   - Allow entity selector `@e` to select dead entities.
- `disableEnchantCommandRestriction`
   - Remove the restriction on enchantment level and compatibility from `/enchant`.
- `blamingMode`
   - Used by the chunk event cause analyzer mentioned below.
- `blameThreshold`
   - Used by the chunk event cause analyzer mentioned below.

### Commands
- `/lazyload`
   - Simulates lazy loaded chunks.
   - Currently, only entity ticking is disabled within marked chunks, other properties of the chunks are not influenced.
   - Unlike `/forceload`, the states are not permanent.
- `/countentities`
   - Counts entities, especially stacked ones, in a easier way.

### Other Features
- Four kinds of entity selector options
   - `typeRegex`
      - Specify a regular expression matching the ID (including the namespace) of selected entities.
   - `nameRegex`
      - Specify a regular expression matching the name of selected entities.
   - `class`
      - Specify a regular expression matching the class (package name is optional) of selected entities.
   - `instanceof`
      - Specify a concrete class name of the super class or implemented (directly or indirectly) of the class of selected entities.
- Accessing path compiler
   - Compiles custom accessing path nodes into bytecode to enable higher performance.
   - Experimental feature.
- A chunk event cause analyzer
   - Allows the chunk behavior logger (`/logchunkbehavior`) analyze the cause of chunk events.
   - When enabled, you can select whether raw stacktrace or a few analyzed tags will be written to the file with option `blamingMode`.
- Storage policies for entity logger.
   - Previously, logs produced when both the server and the client ticks are saved.
   - But now, you can disable logs from one of these sides by specifing a policy with `/entitylog sub <entities> <policy>` or `/entitylog setDefaultStroagePolicy <policy>`.
   - By default, only logs from the server are saved.
   - Saving all logs in a single file is possible, but not recommended.
- A warning screen displayed when trying to load a survival save for the first time with MessMod.
- Server ticking phase `REST`.
   - Represents the point where all async tasks like player inputs are finished.
- Accessing path node `class`.
   - The `Class` object representing the class of the input.
- Support exporting more parts of saves in `/exportsave`.
   - It can be customized with `/exportsave addComponent <comp>` and `/exportsave removeComponent <comp>`.

## Changes

- Allow using wildcards in commands.
   - The format is similar to DOS filename wildcard.
   - It can be used in commands involving adding (subscribing) or removing (unsubscribing) some items, including: 
      - `/logchunkbehavior subscribe | unsubscribe <event>`
      - `/logpacket sub | unsub <type>`
- No longer permit using primitive types in class cast node.
   - Using them may lead to failure in compiled accessing paths.
   - And they are actually invalid as all direct input of all nodes are of refercence types.
- Methods nodes now references one of the deepest overriden method correctly, which makes them more adaptable.
   - For example, `Object.toString()` instead of `LivingEntity.toString()` is used.
- Mapper nodes now only recognize targets with exactly matching descriptors.
- (Un)subscribing fields won't restart entity information logger, instead, it fails if the logger isn't idle.
- No longer marked as conflicting with lithium.
- Ticking events are now triggered at the beginning of corresponding stages.
- Spilt `/logchunkbehavior setSubscribed` into 2 commands.
- More accurate elapsed time measuring in `/exportsave`.
- Updated channel version from 2 to 3.

## Fixes

- `/freezentity` prevents the chunk position of entities from updating.
- Certain movement restrictions are calculated twice in `/logmovement`.
- The error message sent when using a undefined network side in entity selectors is incorrect.
- Temproary files produced by `/exportsave` are not cleaned.
- Undo and redo are performed twice within a single click.
- When using key bindings `F3 + <Key>`, actions attached to `<Key>` are performed.
   - For example, when pressing `F3 + E`, the inventory screen is opened.
- Multiple targets can be found even if a descriptor is specified.
- Chunk event `TICKET_ADDITION` & `TICKET_REMOVAL` is not triggered correctly.
   - If the are documented before.
- Vanilla hotbar texture is not available when `hotbarLength` is modified.
   - Previously a simple grey background is used.
- Save specific options are still used after exiting from the save.
- Shapes for different players are not sent independently.
   - For example, previously the block outline renderer effectively only renders shapes for one player (possibly a bot), making the feature not available for other players.
- `/raycast entity` fails when the language is set to Chinese.
- Some error messages related to options setting or arguments in method and mapper nodes are not translated.
- Argument types are not serializable.
   - An consequence of the bug is that some warning messages are output when players log on and that arguments of these types are absent in `/help`.
- Listening to `-THIS-` in entity information logger without specifing an accessing path leads to crash.
- Class literal of primitive types cannot be used.
- Fields without a mapping couldn't be referenced in `/entityfield`.
- Block entity changes cannot be undone.
   - However, it is not completely fixed now, as inventories is still unable to get restored.
- Exported save folder doesn't match the given name.

# 0.5.1+v20230612-BETA

## Fixed

 - Not compatible with Pehkui (See Issue#4)
 - Not compatible with Java 8 & 9

# 0.5.0+v20230404-BETA

## Added
- Client ticking events, which can be used in entity logs and sidebars.
- Option optimizedEntityPushing
- Chunk behavior logger, and command /logchunkbehavior
## Fixed
- Some bugs caused by using overriding methods in mapper & method nodes.
- Misbehavior of mapper & method nodes, when literal E+ENUM is used.
- The feedback of /entitysidebar remove is not translated.
- The rendering distance of structure block is not enlarged when the carpet is not installed.
- Buggy exception handling in mapper & method nodes.
## Changed
- Remove black outline of chunk loading status markers

# 0.4.0+20221217-BETA

## Options

- minecartPlacementOnNonRailBlocks: Allow players to place minecarts directly on the ground
- quickMobMounting: Placing mobs into vehicles
- generateChunkGrid: Generate a layer of glass on the ground to show the chunks

## Commands

- /accessingpath: Manage custom nodes in accessing paths. (Some other features may be introduced in the future.)
- /exportsave: Export some given areas as a new save.

## Other Features

- Vec3d literal

## Fixes

- Changed hideSuvivalSave to hideSurvivalSave to correct a spelling mistake.
- Fixed some grammatical mistakes.
- Many commands have no feedback.
- In HUDs, listened fields get replaced when a newer line with the same name is created.
- Couldn't parse BlockPos literals.

# 0.3.0+20221001-mc1.16.x

**Experimental Snapshot**

 - /partlykill - Kill a part of selected entities
 - More literals (which has been used in method nodes in accessing paths) in /entityfield
 - Accessing paths and -THIS- in /entityfield modify
 - Fix: Negative number or numbers with suffix in literals couldn't be parsed

# 0.2.1-mc1.16.x+20220930

Fix: Not compatible with carpet 1.4.25 and above(Issue #3)

# 0.2.0+20220827-BETA

 - Ticking phase SERVER_TASKS.
 - Fix: The class name of packets are not deobfusciated.
 - Fix: The value of option vanillaDebugRenderers couldn't be saved.
 - Clients connecting the server with a different protocol version will receive a warning message in the chating bar.
 - Chunk status biomes & full can no longer be skipped, because the server will crash if they are not processed.
 - (1.17/1.18 only): Fixed some rendering glitches.

# 0.1.0+v20220823-BETA

## New Features

 - /logpacket <type>
 - Listen to the packets between the server and the client
 - /freezentity freeze|resume <entities>
 - Pause the calculation of some entities.
 - Option hotbarLength
 - Change the number of item slots the hotbar contains.
 - However, it is actually unfinished now, some vanilla features like loading and saving hotbars is not compatible with it.
 - Option projectileRandomnessScale
 - Change the amount of the random initial velocity of projectiles.
 - Option hideSuvivalSaves
 - Hide worlds that is likely to be suvivial saves to prevent it to be opened accidently.
 - Actually, this mod is basically designed for creative saves.
 - Option interactableB36
 - Allow you to break moving pistons and place blocks against it.
 - Option blockPlacementHistory
 - Record what the players has placed recently so that you may undo or redo these operations later (With Ctrl + Z or Ctrl + Y).
 - Entity selector option id
 - Select entities with their numberic IDs, which is much shorter than the UUIDs.
 - In the source code, the entity ID is the value of field entityId (or networkId in 1.17 and beyond)
 - Entity selector option side
 - Supports selecting entities from the client.
 - Only available in single player games.

## Changes

 - Rename SERVER_TASKS to TICKED_ALL_WORLDS, as the the previous one is hard to implement because of mod compatiblity.
 - -THIS- is now available in /entitylog, /hud and /entitysidebar
 - The dedicated server will send the options to the clients.
 - Suggestions is available when setting the option vanillaDebugRenderers now.
 - The version is now respects the SemVer standard.

## Bug Fixes

 - Parsing the shape data from dedicated server takes too long.
 - Some commands has effects when another world has been opened.
 - The argument phase in /entitylog has no effect.
 - The error message in /entityfield is not correct.
 - Versions that is compatible with MC 1.17 & 1.18 will be available in a few days.

# 1.16.x-20220810-BETA

## Fixed

 - Rendering glitches in texts
 - Overriding methods are counted as separate ones
 - Game crashes if the carped mod (1.4.32 or later) and this mod are installed at the same time.
 - dedicated server crashes
 - The description of option vanillaDebugRenderers is absenting.

## Commands

 - /lag
 - /ride

## Other Changes

 - Incompatiblity warnings
 - Added icon

# 1.16.4-20220802-BETA

## New Options

 - accessingPathInitStrategy
 - hudStyles
 - skippedGenerationStages
 - vanillaDebugRenderers

## Other New Features**

 - Move descriptions of options to language file and translated it to Simpleified Chinese
 - Support for arguments and more literals in AccessingPath
 - Entity sidebar
 - Support specifying the phase in which the lines in sidebar get updated

## Changes

 - Use @ to separate time and entity info in file names of entity logs
 - Renamed /hudfieldlistening to /hud

## Fixed

 - The game sometimes crashes when using AccessingPaths
 - Fail to clone a unloaded area to somewhere
 - Accessing path bug fixes
 - Removed unused accesswidener
 - Crashes caused by the entity sidebar
 - The title of entity sidebar is still rendered when there is no line available
 - Added suggestions to /entitysidebar remove
 -  - Entities are not unsubscribed when the server is closed
 - Some commands have no (translatable) feedback
 - Failure in setting a protected / private field via /entityfield modify

> ## Incompleted support for dedicated servers
>
> > When running on dedicated servers, there are a few (known) issues that must be addressed before the mod can be announced server-side-compatible:
>
>  - Shape handling process runs too slow to support the serverSyncedBox
>  - Styles and aligning mode of HUDs couldn't be set via commands
>  - The client side player information HUD doesn't work
>  - The HUD and shape data sender couldn't be configured for a specified player
>  - Connections sometimes gets timeout or lost
>  - Actually, there is still a kind of unsupported environment, that is, the guest client of a LAN game.
>  - As the running environment of this mod is mainly the single player game, most of the problems above will be solved after the 1.0 version is released.
>

# 1.16.x-220715-SNAPSHOT

> Fri Jul 15 09:41:45 2022 +0800

## New Options

 - antiHostCheating
 - craftingTableBUD
 - disableChunkLoadingCheckInCommands (WIP)
 - rejectChunkTicket
 - serverSyncedBoxRenderRange
 - skipUnloadedChunkInRaycasting (WIP)

## New Commands

 - /fixedentityhud (WIP)

## Other New Features

 - A new HUD data storage system
 - Forceload languages

## Fixed

 - vy could't be set in /modify
 - Structure block rendering distance overriding doesn't work outside development environment
 - Some options couldn't be set to negative values, even though they are vaild.
 - The game sometimes crashes when using AccessingPaths

# 1.16.4-20220502

> Mon May 2 03:27:05 2022 +0800

Fixed:

 - The number of (un)subscribed entities are not displayed correctly.
 - The block bounding information renderers work in a annoying way whenthe game is frozen via /tick freeze
 - Incomplete translations could be loaded.
 - Server-side entity hitbox renderer doesn't work in the nether and theend.

Changed Features:

 - Some details of errors in command executions are given in hoveringtexts.
 - Add brief descriptions of options in hovering texts of their names in/messcfg (just /messcfg, with no arguments).
 - Change the special field '-THIS' (which represents the entity itself)to '-THIS-' in /entityfield.
 - Make /rng fully compatible with /execute and something like that
 - Changed the default value of language to -FOLLOW_SYSTEM_SETTINGS-,which means the language the Mod uses will be the same as the one in the game setting.

Newly Available Options

 - strictAccessingPathParsing
 - railNoAutoConnection

# 1.16.4-20220501

> Sun May 1 02:19:55 2022 +0800

Commands:

 - /hudfieldlistening
 - /namentity

Options:

 - attackableTnt
 - language
 - stableHudLocation
 - entityLogAutoArchiving

Features:

 - Translation system
 - Support for adding custom fields to HUDs
 - Separated configuration for each saves and a global config
 - Accessing path in /entityfield and /entitylog
 - Automatic entity log archiving
 - Move most of the reflection operation to a seprated class
 - I18N (Translation) system

Fixed:

 - Getting null value from with /entityfield causes a failure in execution.
 - And many unfinished things...

> 1.16.4-20220408

> Fri Apr 8 13:43:08 2022 +0800

Added commands:

 - /ensure <pos>
 - /logmovement

Added options:

 - commandExecutionRequirment
 - disableExplosionExposureCalculation
 - projectileChunkLoadingPermanence
 - projectileChunkLoadingRange
 - renderRedstoneGateInfo
 - tntChunkLoadingPermanence
 - tntChunkLoadingRange

Renamed some options

 - setHudDisplay=>hudAlignMode
 - hudtextSize=>hudTextSize
 - Better /messcfg

 - The description of each options
 - Colored output
 - Better /entitylog
 - Made /entitylog listenField ... works properly
 - /entitylog saves logs to the entitylog folder in thr world folder
 - The type of logged entity is included in the name of the log file.
 - Pause /entitylog in frozen ticks (in other words, when the game is froze via /tick freeze).
 - /entitylog sub won't restart recording.
 - Health of mobs an players will be included in the log.
 - Corrected the number of entitits subscribed/unsubscribed via /entitylog.
 - Automatic mapping loading
 - Automatic download the Yarn mapping from the Maven.
 - Load mapping from Carpet TIS Extension.
 - Rewrote the option storage code
 - Hit block state & better coordation output in /raycast
 - Structure block visiblity
 - Server-side player info HUD. (Toggle it with F3 + S)
 - entityExplosionInfluence incompatiblity warning if the Lithium Mod is loaded.
 - Separated mapping loader.
 - Removed server_box.sc as it is no longer needed.
 - Updated the document.

# 1.16.4-20220330

> Wed Mar 30 18:26:29 2022 +0800

Added the ability to resize the text in HUDs

# v20220130

> Sun Jan 30 23:48:38 2022 +0800

Fixed a critial exception which leads to failure in / messcfg execution

# 1.16.4-v20220124

> Mon Jan 24 22:15:08 2022 +0800

Added Options:

 - disableProjectileRandomness
 - endEyeTeleport
 - maxEndEyeTpRadius
 - creativeUpwardsSpeed
 - Added Command:
 - /entitylog

Fixes & Changes:

 - Debfuscation is now available to be used in /entityfield
 - Projectile/TNT chunk loading can work correctly now

# 1.16.x-20210827

> Fri Aug 27 19:19:40 2021 +0800

1. Options:
    - tntChunkLoading //does not work so well
    - projectile //does not work so will
    - maxClientTicksPerFrame
    - debugStickSkipsInvaildState //does not work at all
2. Added a simple defuscator, to make /entityfield easier to use
3. Document Update

# 1.16.x-20210818

> Wed Aug 18 00:38:02 2021 +0800

- More types of block shape can be used when the option renderBlockShape is true.
- However, RAYCAST and SIDES shape may not be rendered correctly.
- Added /raycast entities & /raycast blocks.
- Feedbacks can be disabled in /repeat.
- Structure blocks can be renderer correctly when the carpet rule structureBlockLimit is set to a large number.
- Stacktrace will be printed when the Carpet Mod is not loaded.
- If the Carpet Mod is loaded, enabling the superSecretSetting has the same effect.
- Renamed the root package of the mod to lovexyn0827.mess, and renamed the mod to Just A Mess.
- Updated README.

# 1.16.x-20210817

> Tue Aug 17 01:46:01 2021 +0800

- Invaild numbers (NaN and Infinities) can be used as the power in /explode
- The color of the entity influence determining rays will be correct.
- Add the comment "Copied From The Fabric-Carpet" to some classes that are modified versions of classes in fabric-carpet.

# 1.16.x-v20210731

> Sat Jul 31 07:35:01 2021 +0800

- /raycast
- /repeat
- Fluid & Block shape renderer

# 0723

> Fri Jul 23 12:54:12 2021 +0800

- `/raycast`
- `/repeat`
- Fluid & Block shape renderer

# 1.16.x-20210227

> Sat Feb 27 01:39:46 2021 +0800

 - /rng : supports nextGaussian() now.
 - Now entity RNG can be used correctly.
 - Documents

# 1.16.x-20210226

> Fri Feb 26 11:26:21 2021 +0800

Added Commands:

 - /entityconfig
 - /moventity
 - /poi
 - /rng
 - /tileentity
 - Removed Commands:
 - /biome  It can be implemented using scarpet easier
 - /setpoi merged to /poi

Renders:

 - Explosion rays which determined how entity is affected
 - Better hitbox from the server

The version of MCWMEMod requires the Carpet.

# 1.16.x-20210212

> Fri Feb 12 03:03:46 2021 +0800

Bug Fixing:

Local Player HUD doesn't update after a player dies.

# 1.16.x-20210209

> Tue Feb 9 07:03:39 2021 +0800

Added some commands:

 - /biome
 - /entityfield
 - /mcwmem
 - /setexplosionblock
 - /setpoi


Enachant some commands:

 - /explode: Added optional argument <fire> to tell the game if the explosion causes fire.

Other:

 - Refactored the HUDs,removing most static methods.
 - A option file.
 - Many new info lines.
 - MCWMEMod.INSTANCE.???() insteadof MCWMEMod.???().
 - Maybe more.

# 1.16.x-20200205

> Fri Feb 5 00:19:37 2021 +0800

Add:
Client player info HUD
Hiding HUDs
Added /modify

# 20210204-1429

> Thu Feb 4 14:29:44 2021 +0800

Added looking at entity HUD.

Added `/explode`.