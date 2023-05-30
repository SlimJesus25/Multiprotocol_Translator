package eu.arrowhead.application.skeleton.consumer.classes.kafka;

import java.util.Map;

public class KafkaProducerSettings {

    private String clientId = "localProducer";

    private String bootstrapServers = "127.0.0.1:29092";

    private int qos = 0;

    private String topic = "";

    private Class<?> keySerializer = org.apache.kafka.common.serialization.StringSerializer.class;

    private Class<?> valueSerializer = org.apache.kafka.common.serialization.StringSerializer.class;

    public KafkaProducerSettings(Map<String,String> settings) {
        parseSettings(settings);
    }

    private void parseSettings(Map<String,String> settings) {

        String value = settings.get("client.id");
        if (value!=null) {
             clientId = (value);
        }

        value = settings.get("qos");
        if (value!=null) {
            qos = Integer.parseInt(value);
        }

        /*
        value = settings.get("key.serializer");
        if (value!=null) {
            keySerializer = value;
        }

        value = settings.get("value.serializer");
        if (value!=null) {
            valueSerializer = (value);
        }*/

        value = settings.get("topic");
        if (value!=null) {
            topic = value;
        } else {
            throw new RuntimeException("Please define a topic for your Kafka instance");
        }
    }

    public String getTopic() {
        return topic;
    }

    public Integer getQos() {
        return qos;
    }

    public String getClientId() {
        return clientId;
    }

    public Class<?> getKeySerializer() {
        return keySerializer;
    }

    public Class<?> getValueSerializer() {
        return valueSerializer;
    }
}
