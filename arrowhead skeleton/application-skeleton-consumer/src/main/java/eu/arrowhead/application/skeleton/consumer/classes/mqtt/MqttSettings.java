package eu.arrowhead.application.skeleton.consumer.classes.mqtt;

import java.util.Map;

public class MqttSettings {

    private String clientId = String.valueOf((int) (Math.random() * 1000 + 1));

    private boolean cleanSession = true;

    private int keepAliveInternal = 10;

    private int connectionTimeout = 30;

    private String topicOfConsumerWithoutPrefix = "";

    private int qos = 0;

    private String topic = "";

    public MqttSettings(Map<String,String> settings) {
        parseSettings(settings);
    }

    private void parseSettings(Map<String,String> settings) {

        String value = settings.get("cleanSession");
        if (value!=null) {
            cleanSession = Boolean.parseBoolean(value);
        }

        value = settings.get("clientId");
        if (value!=null) {
            clientId = value;
        }

        value = settings.get("keepAliveInternal");
        if (value!=null) {
            keepAliveInternal = Integer.parseInt(value);
        }

        value = settings.get("connectionTimeout");
        if (value!=null) {
            connectionTimeout = Integer.parseInt(value);
        }

        value = settings.get("topicOfConsumerWithoutPrefix");
        if (value!=null) {
            topicOfConsumerWithoutPrefix = value;
        }

        value = settings.get("qos");
        if (value!=null) {
            qos = Integer.parseInt(value);
        }

        value = settings.get("topic");
        if (value!=null) {
            topic = value;
        } else {
            throw new RuntimeException("Please define a topic for your MQTT instance");
        }
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getKeepAliveInternal() {
        return keepAliveInternal;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public String getTopicOfConsumerWithoutPrefix() {
        return topicOfConsumerWithoutPrefix;
    }

    public int getQos() {
        return qos;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public String toString() {
        return "Clean Session - " + cleanSession +
                "\nKeep Alive Internal - " + keepAliveInternal +
                "\nConnection Timeout - " + connectionTimeout +
                "\nP/C from topic of its pair, but removing: " + "\"" + topicOfConsumerWithoutPrefix + "\"" +
                "\nQOS - " + qos;
    }
}
