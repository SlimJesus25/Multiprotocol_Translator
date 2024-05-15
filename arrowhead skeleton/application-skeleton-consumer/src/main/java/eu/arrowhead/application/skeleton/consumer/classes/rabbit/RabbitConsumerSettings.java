package eu.arrowhead.application.skeleton.consumer.classes.rabbit;

import eu.arrowhead.application.skeleton.consumer.classes.Settings;
import java.util.Map;

public class RabbitConsumerSettings extends Settings {

    private final RabbitSettings rabbitSettings;
    private String queue = "";

    public RabbitConsumerSettings(Map<String, String> settingsMap) {
        super(settingsMap);
        defineQueue(settingsMap.get("queue"));
        rabbitSettings = new RabbitSettings(settingsMap);
    }

    private void defineQueue(String queueName) {
        if (queueName != null) {
            this.queue = queueName;
        }
    }

    public String getQueue() {
        return queue;
    }
    public RabbitSettings getRabbitSettings() {
        return rabbitSettings;
    }
}
