package eu.arrowhead.application.skeleton.consumer.exceptions;

public class DDSException extends IllegalArgumentException {
    private String errorMessage;
    public DDSException (String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String errorMessage() {
        return errorMessage;
    }
}
