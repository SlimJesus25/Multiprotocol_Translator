package common;

import java.util.Map;

public abstract class IProducer {

    private ConnectionDetails connectionDetails;

    private String[] consumerTopic;


    public IProducer(ConnectionDetails connectionDetails, Map<String, String> settings) {
        this.connectionDetails = connectionDetails;
        if (settings.containsKey("consumerTopic")) {
            this.consumerTopic = settings.get("consumerTopic").split(";");
        }
    }

    public ConnectionDetails getConnectionDetails() {
        return connectionDetails;
    }

    public abstract void produce(String topic, String message);

    public String topicFromConsumer(String originalTopic) {
        String newTopic = originalTopic;

        if (consumerTopic==null) {
            return originalTopic;
        }

        for (int i = 0; i < consumerTopic.length; i++) {
            newTopic = newTopic.replaceAll(consumerTopic[i],"");
        }

        if (newTopic.equals("")) {
            throw new RuntimeException("Using consumer's topic resulted in an empty topic");
        }
        return newTopic;
    }
}
