import taskTypes.Deadline;
import taskTypes.Event;
import taskTypes.Task;
import taskTypes.Todo;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists Gongrilla's tasks in an append-only journal on the local hard disk.
 */
public class Storage {
    private static final String FIELD_SEPARATOR = " | ";
    private final Path filePath;

    /**
     * Creates storage that uses the given data file.
     *
     * @param filePath path of the task data file
     */
    public Storage(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("The storage path cannot be null.");
        }
        this.filePath = filePath;
    }

    /**
     * Records a task addition without rewriting existing records.
     *
     * @param task task to record
     * @throws IOException if the record cannot be appended
     */
    public void appendAdd(Task task) throws IOException {
        if (task == null) {
            throw new IllegalArgumentException("The task cannot be null.");
        }
        appendRecord("A" + FIELD_SEPARATOR + task.toDataString());
    }

    /**
     * Records a task deletion without rewriting existing records.
     *
     * @param index zero-based index of the task
     * @throws IOException if the record cannot be appended
     */
    public void appendDelete(int index) throws IOException {
        appendIndexRecord("X", index);
    }

    /**
     * Records that a task was marked complete.
     *
     * @param index zero-based index of the task
     * @throws IOException if the record cannot be appended
     */
    public void appendMark(int index) throws IOException {
        appendIndexRecord("M", index);
    }

    /**
     * Records that a task was marked incomplete.
     *
     * @param index zero-based index of the task
     * @throws IOException if the record cannot be appended
     */
    public void appendUnmark(int index) throws IOException {
        appendIndexRecord("U", index);
    }

    /**
     * Loads tasks by replaying the data file from top to bottom.
     * Legacy snapshot records from the previous implementation are also accepted.
     *
     * @return reconstructed tasks, or an empty list if the file does not exist
     * @throws IOException if the file cannot be read or contains an invalid record
     */
    public ArrayList<Task> load() throws IOException {
        ArrayList<Task> tasks = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return tasks;
        }
        if (!Files.isRegularFile(filePath)) {
            throw new IOException("The data path is not a regular file: " + filePath);
        }

        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        for (int i = 1; i <= lines.size(); i++) {
            String line = lines.get(i - 1);
            if (line.isBlank()) {
                continue;
            }
            try {
                replayRecord(line, tasks);
            } catch (IllegalArgumentException | IndexOutOfBoundsException exception) {
                throw new IOException("Invalid data on line " + i + ": "
                        + exception.getMessage(), exception);
            }
        }
        return tasks;
    }

    private void replayRecord(String line, ArrayList<Task> tasks) {
        String[] fields = line.split(" \\| ", -1);
        switch (fields[0]) {
        case "A" -> tasks.add(createTask(slice(fields, 1)));
        case "X" -> tasks.remove(readIndex(fields, tasks.size()));
        case "M" -> tasks.get(readIndex(fields, tasks.size())).markDone();
        case "U" -> tasks.get(readIndex(fields, tasks.size())).unmarkDone();
        case "T", "D", "E", "T2", "D2", "E2" -> tasks.add(createTask(fields));
        default -> throw new IllegalArgumentException("unknown record type '" + fields[0] + "'");
        }
    }

    private Task createTask(String[] fields) {
        if (fields.length < 3) {
            throw new IllegalArgumentException("task record has too few fields");
        }
        boolean encoded = fields[0].endsWith("2");
        String type = encoded ? fields[0].substring(0, 1) : fields[0];
        boolean isDone = parseCompletion(fields[1]);
        String name = decodeIfNeeded(fields[2], encoded);
        Task task = switch (type) {
        case "T" -> {
            requireFieldCount(fields, 3);
            yield new Todo(name);
        }
        case "D" -> {
            requireFieldCount(fields, 4);
            yield new Deadline(name, decodeIfNeeded(fields[3], encoded));
        }
        case "E" -> {
            requireFieldCount(fields, 5);
            yield new Event(name, decodeIfNeeded(fields[3], encoded),
                    decodeIfNeeded(fields[4], encoded));
        }
        default -> throw new IllegalArgumentException("unknown task type '" + type + "'");
        };
        if (isDone) {
            task.markDone();
        }
        return task;
    }

    private boolean parseCompletion(String value) {
        return switch (value) {
        case "0" -> false;
        case "1" -> true;
        default -> throw new IllegalArgumentException("completion status must be 0 or 1");
        };
    }

    private int readIndex(String[] fields, int taskCount) {
        requireFieldCount(fields, 2);
        int index;
        try {
            index = Integer.parseInt(fields[1]);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("task index is not an integer", exception);
        }
        if (index < 0 || index >= taskCount) {
            throw new IllegalArgumentException("task index " + index + " is out of range");
        }
        return index;
    }

    private void appendIndexRecord(String operation, int index) throws IOException {
        if (index < 0) {
            throw new IllegalArgumentException("The task index cannot be negative.");
        }
        appendRecord(operation + FIELD_SEPARATOR + index);
    }

    private void appendRecord(String record) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(filePath, record + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private String decodeIfNeeded(String value, boolean encoded) {
        return encoded ? URLDecoder.decode(value, StandardCharsets.UTF_8) : value;
    }

    private void requireFieldCount(String[] fields, int expected) {
        if (fields.length != expected) {
            throw new IllegalArgumentException("expected " + expected + " fields but found "
                    + fields.length);
        }
    }

    private String[] slice(String[] values, int start) {
        String[] result = new String[values.length - start];
        System.arraycopy(values, start, result, 0, result.length);
        return result;
    }
}
