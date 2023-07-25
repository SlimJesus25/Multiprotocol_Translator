package eu.arrowhead.application.skeleton.consumer.classes;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final String TOPIC_IDENTIFIER = "topic";
    public static final String QOS_IDENTIFIER = "qos";
    public static final String CLIENT_ID_IDENTIFIER = "client.id";

    public static final int MAX_UNIQUE_MESSAGES = 5000000;

    public static Map<String,Object> objectifyMap(Map<String,String> stringMap) {
        // Convert Map<String, String> to Map<String, Object>
        Map<String, Object> objectMap = new HashMap<>();
        for (Map.Entry<String, String> entry : stringMap.entrySet()) {
            objectMap.put(entry.getKey(), entry.getValue());
        }

        return objectMap;
    }
}
