package common;

import java.util.Map;

public abstract class IProducer {

    private ConnectionDetails connectionDetails;


    public IProducer(ConnectionDetails connectionDetails, Map<String, String> settings) {
        this.connectionDetails = connectionDetails;

    }

    public ConnectionDetails getConnectionDetails() {
        return connectionDetails;
    }

    public abstract void produce(String topic, String message);
}
