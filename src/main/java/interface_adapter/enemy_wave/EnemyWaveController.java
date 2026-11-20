package interface_adapter.enemy_wave;

import use_case.generate_mob_waves.GenerateMobWavesInputBoundary;
import use_case.generate_mob_waves.GenerateMobWavesInputData;

/**
 * Turns the held movement keys into a Generate Mob Waves request. Called once per tick by the
 * game loop, after the player's controller.
 *
 * <p>The key-to-direction conversion matches {@code GameController}: right/down are +1,
 * left/up are -1, and opposite keys cancel to 0. It is repeated here rather than shared so
 * the player-movement code stays untouched; if the team later extracts a shared helper, both
 * controllers can use it. Deciding whether that direction starts a wave is the interactor's
 * job, not this class's.
 */
public class EnemyWaveController {
    private final GenerateMobWavesInputBoundary generateMobWaves;

    /**
     * Creates the controller.
     *
     * @param generateMobWaves the use case's input boundary
     */
    public EnemyWaveController(GenerateMobWavesInputBoundary generateMobWaves) {
        this.generateMobWaves = generateMobWaves;
    }

    /**
     * Sends this tick's movement request to the use case.
     *
     * @param up    whether an up key is held
     * @param down  whether a down key is held
     * @param left  whether a left key is held
     * @param right whether a right key is held
     */
    public void tick(boolean up, boolean down, boolean left, boolean right) {
        int moveX = 0;
        if (right) {
            moveX = moveX + 1;
        }
        if (left) {
            moveX = moveX - 1;
        }

        int moveY = 0;
        if (down) {
            moveY = moveY + 1;   // screen y grows downward, so down is +1
        }
        if (up) {
            moveY = moveY - 1;
        }

        generateMobWaves.execute(new GenerateMobWavesInputData(moveX, moveY));
    }
}
