package gongrilla.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests task-list ownership and mutation operations.
 */
class TaskListTest {
    @Test
    void addAndDelete_tasks_updatesListAndReturnsDeletedTask() {
        TaskList tasks = new TaskList();
        Todo first = new Todo("first");
        Todo second = new Todo("second");

        tasks.add(first);
        tasks.add(second);
        Task deleted = tasks.delete(0);

        assertEquals(first, deleted);
        assertEquals(1, tasks.size());
        assertEquals(second, tasks.get(0));
    }

    @Test
    void markAndUnmark_task_updatesCompletionState() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));

        assertTrue(tasks.mark(0).isDone());
        assertFalse(tasks.unmark(0).isDone());
    }

    @Test
    void asList_returnedListCannotModifyTaskList() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));

        assertThrows(UnsupportedOperationException.class,
                () -> tasks.asList().add(new Todo("write book")));
        assertEquals(1, tasks.size());
    }
}
