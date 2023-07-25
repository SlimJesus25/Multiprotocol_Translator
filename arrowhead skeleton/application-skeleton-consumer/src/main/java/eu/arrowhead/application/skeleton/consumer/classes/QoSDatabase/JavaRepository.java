package eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase;

import eu.arrowhead.application.skeleton.consumer.classes.Constants;

import java.util.ArrayList;
import java.util.List;

public class JavaRepository implements Repository {

    List<String> database = new ArrayList<>();

    @Override
    public boolean registerNewMessage(String messageId) {
        if (database.size() > Constants.MAX_UNIQUE_MESSAGES) {
            database.remove(0);
        }
        return database.add(messageId);
    }

    @Override
    public boolean messageExists(String messageId) {
        return database.contains(messageId);
    }

    @Override
    public int getSize() {
        return database.size();
    }
}
