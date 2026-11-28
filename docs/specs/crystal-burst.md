# Crystal Burst

Crystal Burst uses the approved Obsidian Lance artwork. One core splits into exactly three
shards at -25, 0 and +25 degrees relative to its incoming direction. Core speed is 8 pixels
per tick; shards use 10. Core/shard hitboxes are 16 by 16 / 8 by 8, independently of artwork.

## Travel and impact

The captured aim point determines the core velocity once. Shard velocities are calculated once
from the incoming core vector. Neither core nor shards store targets, home, or steer. Misses
continue until another living-enemy contact or world exit. The current one-hit defeat rule is
reused; the wave use case retains death timing and removal.

The existing swept collision check selects the earliest enemy contact before exit. Ties retain
stable enemy order. The collision centre is interpolated from the start hitbox; children are not
snapped to enemy centres or to the parent's end-of-tick position. Secondary spawns are added
after the original snapshot is updated. They appear in that tick's display but first move and
collide next tick. Shards use terminal behavior, so splitting cannot recurse.

## Development controls

Left mouse click continues to cast Fireball through `MouseCastInput`. A fresh E-key press
casts Crystal Burst toward the current mouse-cursor position relative to the game surface.
The surface currently uses world pixels directly. The input adapter captures coordinates
when E is pressed, not when the game loop later drains the existing casting queue.

`CrystalCastInput` is a view-layer key/focus listener. It translates E into one existing
`queueCast` request; `Main` only constructs and attaches it. Movement direction, player facing
and glyph angle never supply aim. OS key-repeat presses are ignored until an E release.

A null, inaccessible or outside pointer produces no request. That press still latches E, so
moving the pointer into the surface while holding E cannot trigger a delayed cast. The user
must release E and press again. Input while unfocused is ignored. Focus loss preserves a held
E latch, and focus gain never casts or clears it; release E once after returning to rearm it
if its release happened elsewhere. Already queued legitimate requests are not cancelled.
The casting interactor handles invalid/coincident aim using the existing failure output.
Movement and F3 bindings, the controller and all public boundaries stay unchanged.

## Architecture

- Entities: `ProjectileDefinition`, fixed-velocity `Projectile`, immutable `ProjectileSpawn`,
  `ShardFan`, and `CrystalBurstSpell`.
- Use case: `ProjectileImpactBehavior` has terminal and fan implementations. It receives
  immutable contact geometry and returns spawn values; it never mutates storage or presentation.
- Interactors: validate requests, coordinate entities/storage, apply hits, and publish once
  through the existing output boundaries. Public input boundaries still return `void`.
- Data access: the update interface includes insertion of newborn projectiles. The existing
  shared in-memory DAO already implements insertion.
- Presentation: spell identity and projectile type remain separate. Immutable data crosses
  outward, never live entities. Actual velocity determines a newborn's angle.
- View: `CrystalCastInput` captures E/cursor/focus state without game rules. One reusable
  `CrystalSprite` renders both kinds through the existing `ProjectileSprite`
  interface. Fixed (50, 50) cell origins preserve alignment during rotation.
- App: `Main` chooses and injects all concrete strategies, definitions and renderers.

The old Fireball casting constructor remains available to callers using shared settings. The
new constructor accepts per-spell definitions. Ordinary projectile construction also remains
compatible with existing callers. New simulation callers inject the impact-strategy map.

## Artwork

Four PNG strips under `sprites/spells/crystal_burst/` each contain four transparent 100 by 100
frames. Flight loops and impacts play once, at five simulation ticks per frame. Flight is drawn
at 2x source scale and impacts at 3x, matching the approved preview. Decorative shatter fragments
are visual only; the gameplay creates exactly three projectiles. The saved prompt is historical
provenance and does not override the three-shard implementation.

## Verification

Unit tests cover fan rotation and speeds, rejection, swept birth position, deferred movement,
non-recursive hits, enemy evasion, quiet world exit and same-tick defeated targets. Presentation
tests retain type information and impacts; off-screen rendering tests load the approved PNGs.
Input tests cover E press/release cycles, repeat suppression, pointer capture, no/outside/native
pointer failure, unrelated keys and focus loss/return. Wiring tests exercise simultaneous
left-click Fireball and E-key Crystal Burst through the real shared queue and storage, check
that E uses its captured cursor aim, and preserve coincident-aim failure output. The Crystal
wiring test also exercises simulation, presentation and rendering of the approved sprites.

Run `mvn -q -Djava.awt.headless=true test`, the AGENTS dependency checks, and
`mvn checkstyle:check`. Checkstyle warnings are reported without failing Maven in this repository,
so inspect its XML report as well. Java 11 compatibility is enforced by the Maven compiler.
