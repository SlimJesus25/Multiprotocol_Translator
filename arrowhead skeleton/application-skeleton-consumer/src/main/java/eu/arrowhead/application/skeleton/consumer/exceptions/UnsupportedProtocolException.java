package eu.arrowhead.application.skeleton.consumer.exceptions;

public class UnsupportedProtocolException extends RuntimeException {

    public UnsupportedProtocolException(String protocol) {
        super("Unsupported protocol: \"" + protocol + "\"");
    }
}
