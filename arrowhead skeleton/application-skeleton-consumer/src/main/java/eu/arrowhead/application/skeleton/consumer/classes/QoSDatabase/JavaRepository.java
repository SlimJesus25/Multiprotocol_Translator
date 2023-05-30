package eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase;

import java.util.ArrayList;
import java.util.List;

public class JavaRepository implements Repository {

    List<String> database = new ArrayList<>();

    @Override
    public boolean registerNewMessage(String messageId) {
        /*
        boolean ret = database.add(messageId);

        if (database.size() > 1000) {
            deleteAll();
        }

        return ret;
         */

        return database.add(messageId);
    }

    @Override
    public boolean messageExists(String messageId) {
        return database.contains(messageId);
    }

    @Override
    public void deleteAll() {
        database = new ArrayList<>();
    }
}
