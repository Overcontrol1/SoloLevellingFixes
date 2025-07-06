# Solo Levelling Fixes

Fixes bug(/s) with the [Solo Leveling Reawakening](https://www.curseforge.com/minecraft/mc-mods/solo-craft-reawakening)
mod for Forge 1.20.1

## About

Right now, the mod only fixes an issue with how many player sync packets are sent. 
Without this mod, the server creates and sends way too many sync packets,
resulting in high amounts of memory allocation and putting unnecessary stress on the network.
This issue scales linearly with the amount of players.

This mod limits player sync packets to once per tick, per entity. This results in far fewer packets being sent total.

The mod also bypasses a few redundant steps in the syncing of these packets, resulting in even less memory allocation.

## Effect

Without this mod, the base mod can allocate up to ~300MB of absolute useless trash before garbage collection.
Memory allocation is slow, and allocating that much memory for no gain is just... pointless.
I don't know how much it is with this mod, but it's nowhere even in that ballpark. And I'm not bothered to check properly.

I haven't actually tested, but this mod shouldn't cause any issues. It's pretty much free.

## Compatibility

No idea. Should probably be fine as long as no other mod is touching the same stuff this one touches (obviously).

This mod is completely unobtrusive other than the few small changes to the player variables syncing code.

**Any mod that doesn't explicitly touch Solo Leveling Reawakening should be 100% compatible, always.**

## FAQ
**DISCLAIMER: Not a single individual has ever asked me any of these questions.**


> Why is the name of this mod spelled differently to the main mod?

*Because I spell it this way. And I don't want to hear another word about it.*

> You seem really cool. Can you be my friend?

*No.*

> Can I use this in a modpack?

*Why do people even ask this? Yes.*