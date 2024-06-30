package eu.arrowhead.application.skeleton.consumer.exceptions;

public class ConsumerNotFoundException extends RuntimeException{
    public ConsumerNotFoundException(String consumer) {
        super("Consumer not found: \"" + consumer + "\"");
    }
}
