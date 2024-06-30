package eu.arrowhead.application.skeleton.consumer.exceptions;

public class ProducerNotFoundException extends RuntimeException {
    public ProducerNotFoundException(String producer) {
        super("Producer not found: \"" + producer + "\"");
    }
}
