package eu.arrowhead.application.skeleton.consumer.dto;

import java.util.List;

public class ConsumerResponseDTO {
    private String internalID;
    private String brokerAddress;
    private String brokerPort;
    private String topic;
    private int qos;
    private String protocol;
    private List<String> producers;

    public ConsumerResponseDTO(String internalID, String brokerAddress, String brokerPort, String topic, int qos, String protocol, List<String> producers) {
        this.internalID = internalID;
        this.brokerAddress = brokerAddress;
        this.brokerPort = brokerPort;
        this.topic = topic;
        this.qos = qos;
        this.protocol = protocol;
        this.producers = producers;
    }

    public String getInternalID() {
        return internalID;
    }
    public void setInternalID(String internalID) {
        this.internalID = internalID;
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
