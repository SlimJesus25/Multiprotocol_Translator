package eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase;

public interface Repository {

    public boolean registerNewMessage(String messageId);

    public boolean messageExists(String messageId);

    public void deleteAll();
}
