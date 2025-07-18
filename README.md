# Solo Levelling Fixes


Fixes bug(/s) with the [Solo Leveling Reawakening](https://www.curseforge.com/minecraft/mc-mods/solo-craft-reawakening)
mod for Forge 1.20.1

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

This mod transforms all the Solo Leveling classes to use my singleton default.

**There is no functional difference between creating a whole
new object and just using my singleton.**

***A little extra...***

Without this mod, the server creates and sends way too many sync packets.
This isn't as big an issue as the main one, but I solved* it anyway.
This issue scales linearly with the amount of players.

This mod limits player sync packets to once per tick, per entity. This results in far fewer packets being sent total.

The mod also bypasses a few redundant steps in the syncing of these packets, resulting in even less memory allocation.

I might've done this in an incompatible and definitely thread-unsafe way. I don't know, if I'm honest.

## Effect

Without this mod, the base mod can allocate up to ~300MB of absolute useless trash before garbage collection.
Memory allocation is slow, and allocating that much memory for no gain is just... pointless.
I don't know how much it is with this mod, but it's nowhere even in that ballpark. And I'm not bothered to check properly.

I haven't actually tested, but this mod shouldn't cause any issues. It's pretty much free.

## Compatibility

No idea. Should probably be fine as long as no other mod is touching the same stuff this one touches (obviously).

This mod is a little obtrusive, it modifies a lot of the original mod's classes.

**Any mod that doesn't explicitly touch Solo Leveling Reawakening should be 100% compatible, always.**

## FAQ

**DISCLAIMER: Not a single individual has ever asked me any one of these questions.**


> Why is the name of this mod spelled differently to the main mod?

*Because I spell it this way. And I don't want to hear another word about it. Leave me alone.*

> You seem really cool. Can you be my friend?

*No.*

> Can I use this in a modpack?

*Why do people even ask this? Yes.*
