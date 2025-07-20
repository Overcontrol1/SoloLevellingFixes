# MCreator Memory Fix


Fixes a large memory allocation bug in MCreator for Forge 1.20.1.

## About

*Some light technical stuff ahead. Nothing too bad.*

### The Issue

MCreator mods (at least 1.20.1 Forge ones)... have some issues regarding player variables. Mainly the fact that whenever an MCreator mod accesses
its player variables, it creates a whole new default one no matter what, even if it doesn't actually need to.

Where's the problem? MCreator mods often use player variables... a lot. 
This results in a lot of unnecessary allocations. Like... a lot. 
This allocation scales in part with tick rate (which is normally 20tps),
and in other occasions (and the bigger issue): ***framerate***. Yes. Every frame, a ton of these useless
objects are getting created. This leads to high memory allocation rates for no reason at all.

### The Solution

#### The Main Solution

So... how about instead of creating a whole new variables object
every time we wanna just grab it from the player, we... just... don't? That's what this mod does.

**There is no functional difference between creating a whole
new object and just using my singleton.**

## Effect

Without this mod, some MCreator mods can allocate up to ~300MB of absolute useless trash before garbage collection.
Memory allocation is slow, and allocating that much memory for no gain is just... pointless.
I don't know how much it is with this mod, but it's nowhere even in that ballpark. And I'm not bothered to check properly.

I haven't actually tested, but this mod shouldn't cause many issues. It's pretty much free.

## Compatibility

No idea. Should probably be fine as long as no other mod is touching the same stuff this one touches (obviously).

This mod is a little obtrusive, it modifies a lot of the original mod's classes.

**Any mod that doesn't explicitly touch MCreator mods should be 100% compatible, always.**

## FAQ

**DISCLAIMER: Not a single individual has ever asked me any one of these questions.**

> How do I make it work for an MCreator mod?

*Upon running the game, a file in 
the config directory (.minecraft/config) will be created. 
After this file is created, you should restart the game to ensure the detected MCreator mods are changed.
After adding new MCreator mods, you should delete this file so the mod rescans your mods.
A message is logged when the mod scans for MCreator mods.*

**Modpack developers may safely redistribute this config file, preventing the end user from having to restart their game.**

> You seem really cool. Can you be my friend?

*No.*

> Can I use this in a modpack?

*Why do people even ask this? Yes.*

