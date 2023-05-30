package eu.arrowhead.application.skeleton.consumer.classes.kafka;

import java.util.Map;

public class KafkaConsumerSettings {

    private String clientId = "localConsumer";

    private String groupId = "foo";

    private String topic = "";

    private int qos = 0;

    public KafkaConsumerSettings(Map<String, String> settings) {
        parseSettings(settings);
    }

    private void parseSettings(Map<String, String> settings) {

        String value = settings.get("client.id");
        if (value!=null) {
            clientId = (value);
        }

        value = settings.get("group.id");
        if (value!=null) {
            groupId = (value);
        }

        value = settings.get("topic");
        if (value!=null) {
            topic = value;
        } else {
            throw new RuntimeException("Please define a topic for your Kafka instance");
        }


        value = settings.get("qos");
        if (value!=null) {
            qos = Integer.parseInt(value);
        }
    }

    public String getTopic() {
        return topic;
    }

    public String getClientId() {
        return clientId;
    }

    public String getGroupId() {
        return groupId;
    }

    public int getQos() {
        return qos;
    }
}
