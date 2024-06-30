package eu.arrowhead.application.skeleton.consumer.dto;

import java.util.List;

public class ConsumerRequestDTO {
    private String brokerAddress;
    private String brokerPort;
    private String topic;
    private int qos;
    private String protocol;
    private List<String> producers;

    public ConsumerRequestDTO(String brokerAddress, String brokerPort, String topic, int qos, String protocol, List<String> producers) {
        this.brokerAddress = brokerAddress;
        this.brokerPort = brokerPort;
        this.topic = topic;
        this.qos = qos;
        this.protocol = protocol;
        this.producers = producers;
    }

    public String getBrokerAddress() {
        return brokerAddress;
    }
    public void setBrokerAddress(String broker) {
        this.brokerAddress = broker;
    }
    public String getBrokerPort() {
        return brokerPort;
    }
    public void setBrokerPort(String broker) {
        this.brokerPort = broker;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public int getQos() {
        return qos;
    }
    public void setQos(int qos) {
        this.qos = qos;
    }
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    public List<String> getProducers() {
        return producers;
    }
    public void setProducers(List<String> producers) {
        this.producers = producers;
    }
}
