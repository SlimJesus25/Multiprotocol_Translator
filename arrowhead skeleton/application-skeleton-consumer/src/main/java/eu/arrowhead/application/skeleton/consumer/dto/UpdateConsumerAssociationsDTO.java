package eu.arrowhead.application.skeleton.consumer.dto;

import java.util.List;

public class UpdateConsumerAssociationsDTO {

    private String consumerId;
    private List<String> newProducers;

    public UpdateConsumerAssociationsDTO(String consumerId, List<String> newProducers) {
        this.consumerId = consumerId;
        this.newProducers = newProducers;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public List<String> getNewProducers() {
        return newProducers;
    }

    public void setNewProducers(List<String> newProducers) {
        this.newProducers = newProducers;
    }
}
