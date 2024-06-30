package eu.arrowhead.application.skeleton.consumer.exceptions;

/**
 * @author : Ricardo Venâncio - 1210828
 **/
public class InvalidQoSException extends RuntimeException {


    public InvalidQoSException (String qos) {
        super("Invalid QoS \"" + qos + "\". The supported QoS are 0, 1 or 2!");
    }

}
