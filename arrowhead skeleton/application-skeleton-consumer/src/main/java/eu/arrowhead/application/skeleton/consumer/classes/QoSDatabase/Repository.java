package eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase;

public interface Repository {

    void registerNewMessage(String messageId);

    boolean messageExists(String messageId);

    int getSize();
}
