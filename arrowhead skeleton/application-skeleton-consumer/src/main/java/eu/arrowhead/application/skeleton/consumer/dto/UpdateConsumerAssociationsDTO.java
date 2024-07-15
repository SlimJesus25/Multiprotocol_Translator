package eu.arrowhead.application.skeleton.consumer.dto;

import java.util.List;

public class UpdateConsumerAssociationsDTO {

    private String consumerId;
    private List<String> newProducers;
    private Boolean keepOldProducers;

    public UpdateConsumerAssociationsDTO(String consumerId, List<String> newProducers, Boolean keepOldProducers) {
        this.consumerId = consumerId;
        this.newProducers = newProducers;
        this.keepOldProducers = keepOldProducers;
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

    public boolean keepOldProducers() {
        return keepOldProducers;
    }

    public void keepOldProducers(Boolean keepOldProducers) {
        this.keepOldProducers = keepOldProducers;
    }
}
