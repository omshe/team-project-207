# Sprite credits

| File(s) | Source |
| --- | --- |
| `enemy/slime/slime-idle.png`, `enemy/slime/slime-walk.png`, `enemy/slime/slime-death.png` | *Tiny RPG Character Asset Pack v1.03b – Full 20 Characters*, `Characters(100x100)/Slime/Slime/Slime-Idle.png`, `Slime-Walk.png` and `Slime-Death.png`, copied unchanged and renamed to lowercase. |
| `wizard/Wizard-*.png` | The same pack's Wizard character sheets (Idle, Walk, Attack01, Attack02, Hurt, DEATH). The game uses Idle and Walk so far. |

Licence: the asset pack was purchased by Jordan Lin for the team, and its art is used under
that purchase's licence. The downloaded pack itself contains no licence or credit file.

Sheet layout (measured from the files): each sheet is one row of 100 x 100 px frames (slime: six
for Idle and Walk, four for Death; wizard: six for Idle, eight for Walk). The wizard's body fills x 43..59 with its feet on
row 59; the game lines those up with the player's box. The
slime itself only fills about 28 x 17 px near the bottom middle of each frame; the rest is
transparent padding. The game measures that visible area when it loads the sheets and scales
it to the enemy's hitbox.
