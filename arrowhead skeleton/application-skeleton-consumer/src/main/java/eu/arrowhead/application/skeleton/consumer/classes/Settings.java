package eu.arrowhead.application.skeleton.consumer.classes;

import eu.arrowhead.application.skeleton.consumer.exceptions.InvalidQoSException;
import java.util.Map;

public class Settings {

    private int qos = 0;
    private boolean randomId = true;

    public Settings(Map<String, String> settingsMap) {
        defineQos(settingsMap.get(Constants.QOS_IDENTIFIER));
        defineIdentifierGenerator(settingsMap.get("identifier.gen"));
    }

    private void defineQos(String qos) {
        if (qos == null)
            throw new InvalidQoSException("Qos is null");
        if (Integer.parseInt(qos) < 0 || Integer.parseInt(qos) > 2)
            throw new InvalidQoSException("Invalid QoS");

        this.qos = Integer.parseInt(qos);
    }

    private void defineIdentifierGenerator(String identifierGen) {
        if (identifierGen != null) {
            if (identifierGen.equals("random")) {
                randomId = true;
            }
            if (identifierGen.equals("messageBody")) {
                randomId = false;
            }
        }
    }

    public int getQos() {
        return qos;
    }

    public boolean isRandomId() {
        return randomId;
    }
}
