package eu.arrowhead.application.skeleton.consumer.classes.rabbit;

import eu.arrowhead.application.skeleton.consumer.classes.Settings;

import java.util.Map;

public class RabbitProducerSettings extends Settings {

    private RabbitSettings rabbitSettings;

    private String exchange = "";

    private String routingKey = "default";

    public RabbitProducerSettings(Map<String, String> settingsMap) {
        super(settingsMap);
        defineExchange(settingsMap.get("exchange"));
        defineRoutingKey(settingsMap.get("routing.key"));
        rabbitSettings = new RabbitSettings(settingsMap);
    }

    private void defineExchange(String exchange) {
        if (exchange!=null) {
            this.exchange = exchange;
        }
    }

    private void defineRoutingKey(String routingKey) {
        if (routingKey!=null) {
            this.routingKey = routingKey;
        }
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public RabbitSettings getRabbitSettings() {
        return rabbitSettings;
    }
}
