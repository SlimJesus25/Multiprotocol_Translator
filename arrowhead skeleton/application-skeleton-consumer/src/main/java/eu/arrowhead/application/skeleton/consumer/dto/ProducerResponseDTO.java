package eu.arrowhead.application.skeleton.consumer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProducerResponseDTO {
    @JsonProperty("internalID")
    private String internalID;
    @JsonProperty("brokerAddress")
    private String brokerAddress;
    @JsonProperty("brokerPort")
    private String brokerPort;
    @JsonProperty("topic")
    private String topic;
    @JsonProperty("qos")
    private int qos;
    @JsonProperty("protocol")
    private String protocol;

    public ProducerResponseDTO(String internalID, String brokerAddress, String brokerPort, String topic, int qos, String protocol) {
        this.internalID = internalID;
        this.brokerAddress = brokerAddress;
        this.brokerPort = brokerPort;
        this.topic = topic;
        this.qos = qos;
        this.protocol = protocol;
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
}
