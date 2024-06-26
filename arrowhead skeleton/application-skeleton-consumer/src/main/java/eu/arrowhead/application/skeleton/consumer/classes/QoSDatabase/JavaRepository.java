package eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase;

import eu.arrowhead.application.skeleton.consumer.classes.Constants;

import java.util.Map;
import java.util.TreeMap;

public class JavaRepository implements Repository {

    private Map<String, Integer> database = new TreeMap<>();
    private int count = 0;

    @Override
    public void registerNewMessage(String messageId) {

        // This won't be a huge problem, but it needs to be solved!
        if (database.size() > Constants.MAX_UNIQUE_MESSAGES) {
            database.remove(0);
        }
        database.put(messageId, count++);
    }

    @Override
    public boolean messageExists(String messageId) {
        if(messageId != null)
            return database.containsKey(messageId);
        return true;
    }

    @Override
    public int getSize() {
        return database.size();
    }
}
