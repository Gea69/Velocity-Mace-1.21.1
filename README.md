


Changes to the base Mace item:

The smash attack now triggers and scales the bonus damage using the relative velocity between the attacker and target when the hit happens instead of the attacker's fall distance. Previously, the attacker had to fall a minimum of 1.5 blocks to trigger a smashing attack, and now the attacker and target need to be moving at least 6 relative blocks per second to trigger it, which can be done with tight timing by sprinting and attacking the moment after you start a jump, or right before you finish one for approximately 2.5x damage(15 for the vanilla mace).

The original damage calculation was "A successful smash attack causes a mace to deal 4HP extra damage for each of the first 3 blocks fallen, 2HP extra damage for each of the next 5 blocks fallen, and 1HP extra damage for each block fallen after that."
The new damage calculation is based on the mace in question's damage and has a piecewise function that from 0 b/s to 33.5 b/s does 0 to 40 damage(with a vanilla 6 damage mace, a mace that does 12 damage would do 80 by this point), and from 33.5 b/s to 100 b/s does 40 to 100 damage, and then past 100b/s scales at 1/6 weapon damage per b/s, giving 1 damage per b/s to the vanilla mace, or 2 to a modded mace that does 12.

The original function when landing a smash set your Y movement to 0.01, which stopped your falling but also stopped your upward motion too. Now it brings your Y movement to a minimum of 0.01 to stop falls, but will leave upward movement alone.


Mace enchantments:

Density (Mace) (Group:A) (I-V) (Small change):

Instead of giving 0.5 x lvl x FallDistance damage bonus, it now works with the relative velocity. It's still just a raw damage buff, but it doesn't scale with the weapon's base damage, making it less valuable on already high powered mod maces.
Mutually exclusive with Shockwave and Meteor Impact


Shockwave (Mace) (Group:A) (I-IV) (New):

Deals 25% x lvl of the smash attack's damage to everything in the smash knockback radius except the attacker and target.
Mutually exclusive with Density and Meteor Impact


Meteor Impact (Mace) (Group:A) (I-V) (New):

On a smash attack, create an explosion and take recoil, both scaled from relative velocity as a percentage of their max from 0 b/s to 100 b/s. The max explosion power is 6(end crystal or charged creeper), and the max damage is 300% of smash that triggered it. The explosion effect does not hit the attacker, but there is recoil damage, with a minimum of 1 damage, and a max at 100 b/s of 100% of the attacker's max health -1.
The first level of the Meteor Impact provides no recoil reduction, but levels 2 through 5 can give a max of 25% reduction for each level if you meet the defense cap.
Each level after the first provides a maximum of 6.25%, 12.5%, 18.75%, or 25% recoil mitigation for having 5, 10, 15, or 20 points of armor. It gives partial credit, so if you have a level 5 Meteor Impact, but only 17 armor, you still get 21.25% recoil mitigation.
Each level after the first provides a maximum of 18.75%, 37.5%, 56.25%, or 75% recoil mitigation for having 10, 20, 30, or 40 points worth of protection type enchantments, with the same partial credit system as the armor point check. The protection type enchantments contribute points towards the score based on which enchantment it is and how many levels of it are on your worn armor pieces. Blast Protection give 4 points per level, Fire Protection give 3 per level, Projectile Protection gives 2 per level, and (plain) Protection give 1 point per level.
Mutually exclusive with Density and Shockwave


Breach (Mace) (Group:B) (I-IV) (Big change):

Smash attacks ignore a percentage of enemy armor protection equal to the relative velocity capped by the enchantment's level at 33% at lvl 1, 66% at lvl 2 and 100% at lvl 3.
Level 4 of the enchantment retains the cap of 100% armor penetration, but also give a percentile damage bonus against armored targets calculated as [armor * 2 - armor toughness]


Wind Burst (Mace) (Group:B) (I-III) (Big change):

Wind Burst still gains the same amount of force with levels, but now has different functions gated at levels as well. At level 1, a smash attack while sprinting, riding, swimming, or crawling(just in case), the target of the attack is thrown away, separate and stacking with knockback. At level 2, the enchantment gains the vanilla attacker launch, but for it to activate the attacker must not be sprinting, riding, swimming, crawling, gliding, or flying. At level 3, the enchantment gains the ability to work while elytra gliding, splitting the force equally between straight up and the attacker's current movement direction(Try swooping down and hitting your target while moving parallel with the ground!).


Primed Strike (Mace) (Group:B) (I-V) (New):

Primed Strike holds the mace primed(and narrows the FoV) when attack button is held, and automatically swings when there is a valid target in range under the attacker's aim, and then causes all maces to go on a cooldown that blocks all Primed Strike maces from attacking. Levels 2 to 5 each also give +0.5 blocks of attack range each.


Yoink (Fishing Rod) (Group:) (I-III) (New):

When reeling in a hooked target, give them a bit of up force, then pull them to the wielder with a force based on enchantment level.


Yeet (Fishing Rod) (Group:) (I-III) (New):

When reeling in a bobber that hooked a target or is stationarity, the wielder is pulled towards it. This enchantment disables the ability to pull hooked targets unless the fishing rod also has the enchantment Yoink.


Long Cast (Fishing Rod) (Group:) (I-IV) (New):

Gives a 50% more force per level to casting the bobber, and 125% increased maximum line distance per level.


Sticky Bobber (Fishing Rod) (Group:) (I) (New)

The bobber sticks to blocks on contact.