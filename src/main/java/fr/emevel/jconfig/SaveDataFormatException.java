package fr.emevel.jconfig;

/**
 * Exception thrown when the Class representing the data to save is not valid.
 * It can be because the class is not public, or because it has no default constructor.
 *
 */
public class SaveDataFormatException extends RuntimeException {

    public SaveDataFormatException(String message) {
        super(message);
    }

    public SaveDataFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
