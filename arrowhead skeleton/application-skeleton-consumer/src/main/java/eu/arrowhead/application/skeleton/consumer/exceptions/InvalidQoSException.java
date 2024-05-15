package eu.arrowhead.application.skeleton.consumer.exceptions;

/**
 * @author : Ricardo Venâncio - 1210828
 **/
public class InvalidQoSException extends IllegalArgumentException {

    private final static long serialVersionUID = 1L;
    private String errorMessage;
    public InvalidQoSException (String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String errorMessage() {
        return errorMessage;
    }
}
