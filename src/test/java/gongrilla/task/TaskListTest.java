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
    void constructor_varargsArray_isCopiedInOrder() {
        Task first = new Todo("first");
        Task second = new Todo("second");
        Task[] initialTasks = {first, second};
        TaskList tasks = new TaskList(initialTasks);

        initialTasks[0] = new Todo("replacement");

        assertEquals(List.of(first, second), tasks.asList());
    }

    @Test
    void addAll_emptyAndMultipleArguments_preservesExistingTasksAndOrder() {
        Task first = new Todo("first");
        Task second = new Todo("second");
        Task third = new Todo("third");
        TaskList tasks = new TaskList(first);

        tasks.addAll();
        assertEquals(List.of(first), tasks.asList());
        tasks.addAll(second, third);

        assertEquals(List.of(first, second, third), tasks.asList());
    }

    @Test
    void addAndDelete_tasks_updatesListAndReturnsDeletedTask() {
        TaskList tasks = new TaskList();
        Todo first = new Todo("first");
        Todo second = new Todo("second");

        tasks.addAll(first, second);
        Task deleted = tasks.delete(0);

        assertEquals(first, deleted);
        assertEquals(1, tasks.size());
        assertEquals(second, tasks.get(0));
    }

    @Test
    void markAndUnmark_task_updatesCompletionState() {
        TaskList tasks = new TaskList(new Todo("read book"));

        assertTrue(tasks.mark(0).isDone());
        assertFalse(tasks.unmark(0).isDone());
    }

    @Test
    void asList_returnedListCannotModifyTaskList() {
        TaskList tasks = new TaskList(new Todo("read book"));

        assertThrows(UnsupportedOperationException.class, () ->
                tasks.asList().add(new Todo("write book")));
        assertEquals(1, tasks.size());
    }

    @Test
    void find_matchingKeyword_returnsMatchesIgnoringCaseInOriginalOrder() {
        Todo firstMatch = new Todo("Read Book");
        Todo nonMatch = new Todo("write essay");
        Todo secondMatch = new Todo("return textbook");
        TaskList tasks = new TaskList(firstMatch, nonMatch, secondMatch);

        assertEquals(List.of(firstMatch, secondMatch), tasks.find("BOOK"));
    }

    @Test
    void find_keywordWithoutMatches_returnsEmptyList() {
        TaskList tasks = new TaskList(new Todo("read book"));

        assertTrue(tasks.find("banana").isEmpty());
    }

    @Test
    void find_nullKeyword_throwsNullPointerException() {
        TaskList tasks = new TaskList();

        assertThrows(NullPointerException.class, () -> tasks.find(null));
    }

    @Test
    void find_blankKeyword_throwsIllegalArgumentException() {
        TaskList tasks = new TaskList();

        assertThrows(IllegalArgumentException.class, () -> tasks.find("   "));
    }
}
