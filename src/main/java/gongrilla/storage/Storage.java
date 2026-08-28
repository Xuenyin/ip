package gongrilla.storage;

import gongrilla.task.Deadline;
import gongrilla.task.Event;
import gongrilla.task.Task;
import gongrilla.task.Todo;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists gongrilla.Gongrilla's tasks in an append-only journal on the local hard disk.
 */
public class Storage {
    private static final String FIELD_SEPARATOR = " | ";
    private static final DateTimeFormatter STORAGE_DATE_TIME_FORMAT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final Path filePath;


    /**
     * Creates storage that uses the given data file.
     *
     * @param filePath path of the task data file.
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
     * @param task task to record.
     * @throws IOException if the record cannot be appended.
     */
    public void appendAdd(Task task) throws IOException {
        if (task == null) {
            throw new IllegalArgumentException("gongrilla.Gongrilla need real task. Stop trolling.");
        }
        appendRecord("A" + FIELD_SEPARATOR + task.toDataString());
    }

    /**
     * Records a task deletion without rewriting existing records.
     *
     * @param index zero-based index of the task.
     * @throws IOException if the record cannot be appended.
     */
    public void appendDelete(int index) throws IOException {
        appendIndexRecord("X", index);
    }

    /**
     * Records that a task was marked complete.
     *
     * @param index zero-based index of the task.
     * @throws IOException if the record cannot be appended.
     */
    public void appendMark(int index) throws IOException {
        appendIndexRecord("M", index);
    }

    /**
     * Records that a task was marked incomplete.
     *
     * @param index zero-based index of the task.
     * @throws IOException if the record cannot be appended.
     */
    public void appendUnmark(int index) throws IOException {
        appendIndexRecord("U", index);
    }

    /**
     * Loads tasks by replaying the data file from top to bottom.
     * Legacy snapshot records from the previous implementation are also accepted.
     *
     * @return reconstructed tasks, or an empty list if the file does not exist.
     * @throws IOException if the file cannot be read or contains an invalid record.
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

    /**
     * Applies one journal or legacy snapshot record to the reconstructed task list.
     *
     * @param line record read from the data file.
     * @param tasks task list being reconstructed.
     * @throws IllegalArgumentException if the record type or fields are invalid.
     */
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

    /**
     * Recreates a task from decoded legacy fields or encoded version-two fields.
     *
     * @param fields task type, completion state, description, and optional dates.
     * @return reconstructed task.
     * @throws IllegalArgumentException if the record is incomplete or malformed.
     */
    private Task createTask(String[] fields) {
        if (fields.length < 3) {
            throw new IllegalArgumentException("Task record has too few fields");
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
                yield new Deadline(name, parseStoredDateTime(fields[3], encoded, "deadline"));
            }
            case "E" -> {
                requireFieldCount(fields, 5);
                yield new Event(name, parseStoredDateTime(fields[3], encoded, "event start"),
                        parseStoredDateTime(fields[4], encoded, "event end"));
            }
            default -> throw new IllegalArgumentException("unknown task type '" + type + "'");
        };
        if (isDone) {
            task.markDone();
        }
        return task;
    }

    /**
     * Converts the persisted completion flag into a boolean value.
     *
     * @param value persisted flag, either {@code 0} or {@code 1}.
     * @return whether the task is complete.
     * @throws IllegalArgumentException if the flag is not supported.
     */
    private boolean parseCompletion(String value) {
        return switch (value) {
            case "0" -> false;
            case "1" -> true;
            default -> throw new IllegalArgumentException("completion status must be 0 or 1");
        };
    }

    /**
     * Validates and reads a zero-based task index from a journal operation.
     *
     * @param fields operation type and stored index.
     * @param taskCount current number of reconstructed tasks.
     * @return valid zero-based task index.
     * @throws IllegalArgumentException if the index is malformed or out of range.
     */
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

    /**
     * Appends a journal operation that targets one task index.
     *
     * @param operation journal operation code.
     * @param index zero-based task index.
     * @throws IllegalArgumentException if the index is negative.
     * @throws IOException if the record cannot be appended.
     */
    private void appendIndexRecord(String operation, int index) throws IOException {
        if (index < 0) {
            throw new IllegalArgumentException("The task index cannot be negative.");
        }
        appendRecord(operation + FIELD_SEPARATOR + index);
    }

    /**
     * Creates the parent directory when necessary and appends one journal record.
     *
     * @param record record to append without a trailing line separator.
     * @throws IOException if the directory or data file cannot be written.
     */
    private void appendRecord(String record) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(filePath, record + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * Decodes a version-two field while preserving unencoded legacy text.
     *
     * @param value stored field value.
     * @param encoded whether the record uses version-two encoding.
     * @return decoded field value.
     */
    private String decodeIfNeeded(String value, boolean encoded) {
        return encoded ? URLDecoder.decode(value, StandardCharsets.UTF_8) : value;
    }

    /**
     * Parses a stored ISO date-time, accepting legacy date-only values as midnight.
     *
     * @param value stored date or date-time field.
     * @param encoded whether the field must first be decoded.
     * @param fieldName field description used in validation errors.
     * @return parsed date and time.
     * @throws IllegalArgumentException if the stored value is not a supported date format.
     */
    private LocalDateTime parseStoredDateTime(String value, boolean encoded, String fieldName) {
        String decodedValue = decodeIfNeeded(value, encoded);
        try {
            return LocalDateTime.parse(decodedValue, STORAGE_DATE_TIME_FORMAT);
        } catch (DateTimeParseException dateTimeException) {
            try {
                // Date-only records from the previous version are interpreted as midnight.
                return LocalDate.parse(decodedValue, DateTimeFormatter.ISO_LOCAL_DATE)
                        .atStartOfDay();
            } catch (DateTimeParseException dateException) {
                String guidance = encoded
                        ? "expected an ISO date-time"
                        : "legacy date must be changed to an ISO date or date-time";
                throw new IllegalArgumentException(
                        "invalid " + fieldName + " date-time '" + decodedValue + "'; "
                                + guidance,
                        dateTimeException);
            }
        }
    }

    /**
     * Verifies that a record contains exactly the required number of fields.
     *
     * @param fields record fields to inspect.
     * @param expected required field count.
     * @throws IllegalArgumentException if the field count differs.
     */
    private void requireFieldCount(String[] fields, int expected) {
        if (fields.length != expected) {
            throw new IllegalArgumentException("expected " + expected + " fields but found "
                    + fields.length);
        }
    }

    /**
     * Copies the suffix of an array beginning at the supplied index.
     *
     * @param values source values.
     * @param start index of the first value to copy.
     * @return copied suffix.
     */
    private String[] slice(String[] values, int start) {
        String[] result = new String[values.length - start];
        System.arraycopy(values, start, result, 0, result.length);
        return result;
    }
}
