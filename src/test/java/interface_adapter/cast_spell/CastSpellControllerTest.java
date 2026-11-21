package interface_adapter.cast_spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import use_case.cast_spell.CastSpellInputBoundary;
import use_case.cast_spell.CastSpellInputData;
import use_case.cast_spell.UpdateProjectilesInputBoundary;

/**
 * Checks request forwarding, tick ordering, and the casting thread handoff.
 */
class CastSpellControllerTest {
    private final List<CastSpellInputData> requests = new ArrayList<>();
    private final List<String> calls = new ArrayList<>();
    private final List<Thread> executionThreads = new ArrayList<>();

    private final CastSpellInputBoundary castSpell = request -> {
        requests.add(request);
        calls.add("cast:" + request.getSpellId());
        executionThreads.add(Thread.currentThread());
    };

    private final UpdateProjectilesInputBoundary updateProjectiles = () -> {
        calls.add("update");
        executionThreads.add(Thread.currentThread());
    };

    private final CastSpellController controller =
            new CastSpellController(castSpell, updateProjectiles);

    /** Queuing input must not mutate the game through either boundary. */
    @Test
    void queuingDoesNotExecuteUseCases() {
        controller.queueCast("fireball", 120.5, 80.25);

        assertTrue(requests.isEmpty());
        assertTrue(calls.isEmpty());
    }

    /** The controller preserves the identifier and world coordinates exactly. */
    @Test
    void tickForwardsRequestValuesUnchanged() {
        controller.queueCast("fireball", 120.5, 80.25);

        controller.tick();

        assertEquals(1, requests.size());
        CastSpellInputData request = requests.get(0);
        assertEquals("fireball", request.getSpellId());
        assertEquals(120.5, request.getTargetWorldX());
        assertEquals(80.25, request.getTargetWorldY());
        assertEquals(List.of("cast:fireball", "update"), calls);
    }

    /** Each tick advances simulation even when there are no new casting requests. */
    @Test
    void emptyTicksStillUpdateProjectilesOnceEach() {
        controller.tick();
        controller.tick();

        assertTrue(requests.isEmpty());
        assertEquals(List.of("update", "update"), calls);
    }

    /** Multiple queued requests are launched in order before the single update. */
    @Test
    void multipleRequestsRunInOrderBeforeUpdate() {
        controller.queueCast("first", 10, 20);
        controller.queueCast("second", 30, 40);

        controller.tick();

        assertEquals(2, requests.size());
        assertEquals(List.of("cast:first", "cast:second", "update"), calls);
        assertEquals(10, requests.get(0).getTargetWorldX());
        assertEquals(30, requests.get(1).getTargetWorldX());
    }

    /** Consumed requests are not launched again on subsequent ticks. */
    @Test
    void laterTicksDoNotRepeatConsumedRequests() {
        controller.queueCast("fireball", 100, 50);

        controller.tick();
        controller.tick();

        assertEquals(1, requests.size());
        assertEquals(List.of("cast:fireball", "update", "update"), calls);
    }

    /** Input queued after an earlier tick is processed on a later tick. */
    @Test
    void requestsCanArriveBetweenTicks() {
        controller.tick();
        controller.queueCast("fireball", 200, 75);
        assertTrue(requests.isEmpty());

        controller.tick();

        assertEquals(1, requests.size());
        assertEquals(List.of("update", "cast:fireball", "update"), calls);
    }

    /** Invalid input reaches the interactor, which owns validation and feedback. */
    @Test
    void invalidValuesAreForwardedForUseCaseValidation() {
        controller.queueCast(null, Double.NaN, Double.POSITIVE_INFINITY);

        controller.tick();

        assertEquals(1, requests.size());
        CastSpellInputData request = requests.get(0);
        assertEquals(null, request.getSpellId());
        assertTrue(Double.isNaN(request.getTargetWorldX()));
        assertEquals(Double.POSITIVE_INFINITY, request.getTargetWorldY());
        assertEquals(List.of("cast:null", "update"), calls);
    }

    /** A producer thread queues requests while execution stays on the tick caller. */
    @Test
    void workerInputRunsOnlyWhenTheGameThreadTicks() throws InterruptedException {
        Thread producer = new Thread(() -> {
            controller.queueCast("first", 10, 20);
            controller.queueCast("second", 30, 40);
        });
        producer.start();
        producer.join(2000);

        assertFalse(producer.isAlive());
        assertTrue(calls.isEmpty());

        Thread gameThread = Thread.currentThread();
        controller.tick();

        assertEquals(List.of("cast:first", "cast:second", "update"), calls);
        assertEquals(List.of(gameThread, gameThread, gameThread), executionThreads);
    }

    /** Both use-case boundaries must be provided when wiring the controller. */
    @Test
    void nullBoundariesAreRejected() {
        assertThrows(NullPointerException.class,
                () -> new CastSpellController(null, updateProjectiles));
        assertThrows(NullPointerException.class,
                () -> new CastSpellController(castSpell, null));
    }
}