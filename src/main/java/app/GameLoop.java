package app;

import interface_adapter.enemy_wave.EnemyWaveController;
import interface_adapter.game.GameController;
import javax.swing.JPanel;
import view.KeyboardInput;

/**
 * Runs the game at a fixed 60 ticks per second on its own thread.
 * Each tick: read input -> update the game (use case) -> ask Swing to repaint.
 * It contains no game rules; it only decides when they run.
 */
public class GameLoop implements Runnable {
    private static final int TICKS_PER_SECOND = 60;

    private final GameController controller;
    private final EnemyWaveController enemyWaveController;
    private final KeyboardInput keyboard;
    private final JPanel screen;
    private volatile boolean running;
    private Thread thread;

    /**
     * Creates the loop. It does not run until {@link #start()} is called.
     *
     * @param controller          the player's controller, ticked first
     * @param enemyWaveController the mob-wave controller, ticked after the player
     * @param keyboard            where the held keys are read from each tick
     * @param screen              the panel to repaint after each tick
     */
    public GameLoop(GameController controller, EnemyWaveController enemyWaveController,
                    KeyboardInput keyboard, JPanel screen) {
        this.controller = controller;
        this.enemyWaveController = enemyWaveController;
        this.keyboard = keyboard;
        this.screen = screen;
    }

    /** Starts the loop on a new daemon thread. Does nothing if it is already running. */
    public void start() {
        if (running) {
            return;
        }
        running = true;
        thread = new Thread(this, "game-loop");
        thread.setDaemon(true);   // don't keep the program alive after the window closes
        thread.start();
    }

    /** Asks the loop to stop; it finishes its current pass and then ends. */
    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        final double nanosPerTick = 1_000_000_000.0 / TICKS_PER_SECOND;
        double pendingTicks = 0;
        long last = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            pendingTicks += (now - last) / nanosPerTick;
            last = now;

            if (pendingTicks >= 1) {
                // 1. input: read the keys once so every controller sees the same input this tick
                boolean up = keyboard.isUp();
                boolean down = keyboard.isDown();
                boolean left = keyboard.isLeft();
                boolean right = keyboard.isRight();
                // 2. update: the player moves first, then the mobs chase the player's new position
                controller.tick(up, down, left, right);
                enemyWaveController.tick(up, down, left, right);
                // 3. render: Swing calls GamePanel.paintComponent on its own thread
                screen.repaint();
                pendingTicks--;
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
