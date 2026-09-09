package gongrilla.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Tests scheduling boundaries without depending on the current date or time zone. */
class ScheduleClashTest {
    private static final LocalDateTime START = LocalDateTime.of(2019, 12, 2, 10, 0);

    @Test
    void findClashingIndices_overlappingRanges_detectsBothDirections() {
        Event existing = event(0, 60);
        List<Event> candidates = List.of(event(-30, 30), event(30, 90), event(15, 45),
                event(-30, 90), event(0, 60));
        for (Event candidate : candidates) {
            assertEquals(List.of(0), new TaskList(existing).findClashingIndices(candidate));
            assertEquals(List.of(0), new TaskList(candidate).findClashingIndices(existing));
        }
    }

    @Test
    void findClashingIndices_disjointTouchingAndZeroLengthRanges_returnsEmpty() {
        Event existing = event(0, 60);
        List<Event> candidates = List.of(event(-60, -30), event(90, 120), event(-60, 0),
                event(60, 120), event(0, 0), event(30, 30), event(60, 60));
        for (Event candidate : candidates) {
            assertTrue(new TaskList(existing).findClashingIndices(candidate).isEmpty());
            assertTrue(new TaskList(candidate).findClashingIndices(existing).isEmpty());
        }
    }

    @Test
    void findClashingIndices_mixedTasks_preservesFullListIndicesAndExcludesCompletedTasks() {
        Event completed = event(0, 60);
        completed.markDone();
        TaskList tasks = new TaskList(new Todo("read"), event(0, 60),
                new Deadline("due", START), completed, event(0, 60));

        assertEquals(List.of(1, 4), tasks.findClashingIndices(event(0, 60)));
        assertTrue(tasks.findClashingIndices(new Todo("read")).isEmpty());
        assertTrue(tasks.findClashingIndices(new Deadline("due", START)).isEmpty());
        assertTrue(tasks.findClashingIndices(completed).isEmpty());
        assertEquals(5, tasks.size());
    }

    @Test
    void findClashingIndices_overnightMultidayAndSubminuteRanges_usesExactTimestamps() {
        TaskList tasks = new TaskList(event(0, 60));
        assertEquals(List.of(0), tasks.findClashingIndices(event(-1440, 1440)));
        assertEquals(List.of(0), tasks.findClashingIndices(new Event("seconds",
                START.plusMinutes(60).minusSeconds(1), START.plusMinutes(90))));

        LocalDateTime midnight = START.toLocalDate().atStartOfDay();
        Event overnight = new Event("overnight", midnight.minusHours(1), midnight.plusHours(1));
        assertEquals(List.of(0), new TaskList(overnight).findClashingIndices(
                new Event("day", midnight, midnight.plusDays(1))));
        assertTrue(new TaskList().findClashingIndices(overnight).isEmpty());
    }

    private Event event(int fromMinutes, int toMinutes) {
        return new Event("same name", START.plusMinutes(fromMinutes), START.plusMinutes(toMinutes));
    }
}
