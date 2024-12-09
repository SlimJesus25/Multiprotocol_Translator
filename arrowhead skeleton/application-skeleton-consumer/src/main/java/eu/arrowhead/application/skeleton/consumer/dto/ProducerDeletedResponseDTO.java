package eu.arrowhead.application.skeleton.consumer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProducerDeletedResponseDTO {
        @JsonProperty("internalID")
        private String internalID;


        public ProducerDeletedResponseDTO(String internalID) {
            this.internalID = internalID;
        }

        public String getInternalID() {
            return internalID;
        }
        public void setInternalID(String internalID) {
            this.internalID = internalID;
        }
}
