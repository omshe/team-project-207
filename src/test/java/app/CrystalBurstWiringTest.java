package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import data_access.InMemoryCastSpellDataAccessObject;
import entity.CrystalBurstSpell;
import entity.Enemy;
import entity.EnemyWave;
import entity.FireballSpell;
import entity.Player;
import entity.Projectile;
import entity.ProjectileDefinition;
import entity.ShardFan;
import interface_adapter.cast_spell.CastSpellController;
import interface_adapter.cast_spell.CastSpellPresenter;
import interface_adapter.cast_spell.CastSpellViewModel;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import use_case.cast_spell.CastSpellInteractor;
import use_case.cast_spell.ShatterIntoFanBehavior;
import use_case.cast_spell.TerminalImpactBehavior;
import use_case.cast_spell.UpdateProjectilesInteractor;
import view.CrystalCastInput;
import view.CrystalSprite;
import view.MouseCastInput;
import view.ProjectileLayer;

/** Connects both inputs to real storage, simulation, presentation and approved resources. */
class CrystalBurstWiringTest {
    private static final String ROOT = "/sprites/spells/crystal_burst/";

    @Test
    void keyPressQueuesOneCastAndCoreImpactPublishesRenderableShards() throws Exception {
        Player player = new Player(20, 80, 8, 4);
        EnemyWave wave = new EnemyWave();
        Enemy victim = new Enemy(70, 80, 10, 10, 0);
        wave.startNextWave(List.of(victim));
        InMemoryCastSpellDataAccessObject data =
                new InMemoryCastSpellDataAccessObject(player, wave);
        CastSpellViewModel model = new CastSpellViewModel();
        CastSpellController controller = casting(data, model);
        SwingUtilities.invokeAndWait(() -> {
            PointerSurface surface = new PointerSurface(180, 84);
            CrystalCastInput input = new CrystalCastInput(controller, surface, "crystal_burst");
            input.keyPressed(keyPress(surface));
            input.keyPressed(keyPress(surface));
        });
        assertTrue(data.getProjectiles().isEmpty());
        for (int tick = 0; tick < 5; tick++) {
            controller.tick();
        }
        assertTrue(victim.isDefeated());
        assertEquals(3, data.getProjectiles().size());
        assertEquals("Cast Crystal Burst.", model.getState().getStatusMessage());
        SwingUtilities.invokeAndWait(() -> {
            ProjectileLayer layer = new ProjectileLayer(model, Map.of(
                    "crystal_core", new CrystalSprite(ROOT + "crystal-core-flight.png",
                            ROOT + "crystal-core-shatter.png"),
                    "crystal_shard", new CrystalSprite(ROOT + "crystal-shard-flight.png",
                            ROOT + "crystal-shard-impact.png")));
            BufferedImage image = new BufferedImage(300, 220, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            try {
                layer.paint(graphics, 220);
            } finally {
                graphics.dispose();
            }
            assertTrue(model.drainImpacts().isEmpty());
            assertTrue((image.getRGB(62, 84) >>> 24) != 0);
        });
    }

    @Test
    void leftClickKeepsFireballWhileCrystalCapturesCursorAim() throws Exception {
        InMemoryCastSpellDataAccessObject data = new InMemoryCastSpellDataAccessObject(
                new Player(20, 80, 8, 4), new EnemyWave());
        CastSpellViewModel model = new CastSpellViewModel();
        CastSpellController controller = casting(data, model);
        SwingUtilities.invokeAndWait(() -> {
            PointerSurface surface = new PointerSurface(180, 130);
            MouseCastInput mouse = new MouseCastInput(controller, "fireball");
            CrystalCastInput crystal = new CrystalCastInput(controller, surface, "crystal_burst");
            mouse.mousePressed(new MouseEvent(surface, MouseEvent.MOUSE_PRESSED,
                    0, 0, 180, 84, 1, false, MouseEvent.BUTTON1));
            crystal.keyPressed(keyPress(surface));
            surface.pointer.setLocation(50, 180);
            crystal.keyPressed(keyPress(surface));
        });
        assertTrue(data.getProjectiles().isEmpty());
        controller.tick();
        List<Projectile> projectiles = data.getProjectiles();
        assertEquals(2, projectiles.size());
        Projectile fireball = projectiles.get(0);
        final Projectile crystal = projectiles.get(1);
        assertEquals("fireball", fireball.getSpell().getId());
        assertEquals("fireball", fireball.getProjectileTypeId());
        assertEquals(24, fireball.getHitbox().getWidth());
        assertEquals(8, fireball.getVelocityX());
        assertEquals(0, fireball.getVelocityY());
        assertEquals("crystal_burst", crystal.getSpell().getId());
        assertEquals("crystal_core", crystal.getProjectileTypeId());
        assertEquals(16, crystal.getHitbox().getWidth());
        double length = Math.hypot(156, 46);
        assertEquals(8 * 156 / length, crystal.getVelocityX(), 1e-9);
        assertEquals(8 * 46 / length, crystal.getVelocityY(), 1e-9);
        controller.tick();
        assertEquals(2, data.getProjectiles().size());
        assertEquals(8 * 46 / length, crystal.getVelocityY(), 1e-9);
    }

    @Test
    void coincidentCursorUsesExistingFailureOutputWithoutLaunching() throws Exception {
        InMemoryCastSpellDataAccessObject data = new InMemoryCastSpellDataAccessObject(
                new Player(20, 80, 8, 4), new EnemyWave());
        CastSpellViewModel model = new CastSpellViewModel();
        CastSpellController controller = casting(data, model);
        SwingUtilities.invokeAndWait(() -> {
            PointerSurface surface = new PointerSurface(24, 84);
            CrystalCastInput input = new CrystalCastInput(controller, surface, "crystal_burst");
            input.keyPressed(keyPress(surface));
        });
        controller.tick();
        assertTrue(data.getProjectiles().isEmpty());
        assertEquals("Cannot determine a direction for that aim point.",
                model.getState().getStatusMessage());
    }

    private static CastSpellController casting(InMemoryCastSpellDataAccessObject data,
                                               CastSpellViewModel model) {
        CrystalBurstSpell spell = new CrystalBurstSpell();
        ProjectileDefinition core = new ProjectileDefinition(spell, "crystal_core", 8, 16, 16);
        ProjectileDefinition shard = new ProjectileDefinition(spell, "crystal_shard", 10, 8, 8);
        ProjectileDefinition fireball = new ProjectileDefinition(
                new FireballSpell(), "fireball", 8, 24, 16);
        CastSpellPresenter presenter = new CastSpellPresenter(model);
        TerminalImpactBehavior terminal = new TerminalImpactBehavior();
        return new CastSpellController(
                new CastSpellInteractor(data, presenter,
                        Map.of("fireball", fireball, spell.getId(), core)),
                new UpdateProjectilesInteractor(data, presenter, 300, 220, Map.of(
                        "fireball", terminal,
                        "crystal_core", new ShatterIntoFanBehavior(
                                new ShardFan(shard, List.of(-25.0, 0.0, 25.0))),
                        "crystal_shard", terminal)));
    }

    private static KeyEvent keyPress(PointerSurface surface) {
        return new KeyEvent(surface, KeyEvent.KEY_PRESSED,
                0, 0, KeyEvent.VK_E, KeyEvent.CHAR_UNDEFINED);
    }

    /** Keeps pointer capture deterministic while using the production E-key adapter. */
    private static final class PointerSurface extends JPanel {
        private final Point pointer;

        private PointerSurface(int x, int y) {
            pointer = new Point(x, y);
            setSize(300, 220);
        }

        @Override
        public Point getMousePosition() {
            return pointer;
        }

        @Override
        public boolean isFocusOwner() {
            return true;
        }
    }
}
