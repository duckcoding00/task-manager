package task.manager.backend.utils.exception.CustomException;

public class InsertException extends RuntimeException {
    public InsertException(String message) {
        super(message);
    }

    public InsertException(String message, Throwable cause) {
        super(message, cause);
    }
}
