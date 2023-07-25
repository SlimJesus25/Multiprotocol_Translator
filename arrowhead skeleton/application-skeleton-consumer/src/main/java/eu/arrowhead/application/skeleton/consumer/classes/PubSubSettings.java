package eu.arrowhead.application.skeleton.consumer.classes;

import java.util.Map;

public class PubSubSettings extends Settings {

    private String topic = "";

    private String clientId = "Middleware-Client-" + (int) (Math.random() * 1000000);

    public PubSubSettings(Map<String,String> settingsMap) {
        super(settingsMap);
        defineTopic(settingsMap.get(Constants.TOPIC_IDENTIFIER));
        defineClientId(settingsMap.get(Constants.CLIENT_ID_IDENTIFIER));
    }

    private void defineTopic(String topic) {
        // Implement topic validation logic here
        if (topic!=null) {
            this.topic = topic;
        }
    }

    private void defineClientId(String clientId) {
        // Implement Client ID validation logic here
        if (clientId!=null) {
            this.clientId = clientId;
        }
    }

    public String getTopic() {
        return topic;
    }

    public String getClientId() {
        return clientId;
    }
}
