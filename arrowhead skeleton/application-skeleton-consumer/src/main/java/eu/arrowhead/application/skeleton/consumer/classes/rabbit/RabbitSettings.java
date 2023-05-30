package eu.arrowhead.application.skeleton.consumer.classes.rabbit;

import java.util.Map;

public class RabbitSettings {

    private String exchange = "";

    private String queue = "";

    private String routingKey = "";

    private int qos = 0;

    private int loadBalancer = 0;

    public RabbitSettings(Map<String,String> settings) {
        parseSettings(settings);
    }

    public void parseSettings(Map<String, String> settings) {
        String value;

        value = settings.get("exchange");
        if (value!=null) {
            exchange = value;
        }

        value = settings.get("queue");
        if (value!=null) {
            queue = value;
        } /* else {
            throw new RuntimeException("Please define a queue for your Rabbit instance");
        } */

        value = settings.get("routing.key");
        if (value!=null) {
            routingKey = value;
        }

        value = settings.get("qos");
        if (value!=null) {
            qos = Integer.parseInt(value);
        }

        value = settings.get("load.balancing");
        if (value!=null) {
            loadBalancer = Integer.parseInt(value);
        }
    }

    public String getExchange() {
        return exchange;
    }

    public String getQueue() {
        return queue;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public int getQos() {
        return qos;
    }

    public int getLoadBalancer() {
        return loadBalancer;
    }

    public void doLoadBalancer() {
        this.loadBalancer = 0;
    }
}
