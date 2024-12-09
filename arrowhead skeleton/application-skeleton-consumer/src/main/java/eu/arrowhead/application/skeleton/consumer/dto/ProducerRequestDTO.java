package eu.arrowhead.application.skeleton.consumer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author : Ricardo Venâncio - 1210828
 **/
public class ProducerRequestDTO {
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

    public ProducerRequestDTO(String brokerAddress, String brokerPort, String topic, int qos, String protocol) {
        this.brokerAddress = brokerAddress;
        this.brokerPort = brokerPort;
        this.topic = topic;
        this.qos = qos;
        this.protocol = protocol;
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
