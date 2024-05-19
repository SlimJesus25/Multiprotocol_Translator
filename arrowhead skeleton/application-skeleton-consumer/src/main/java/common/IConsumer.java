package common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class IConsumer implements Runnable {

    public List<IProducer> producerList;

    protected int numberOfMessages = 0;

    protected String lastMessage = "None";

    private ConnectionDetails connectionDetails;

    public IConsumer(ConnectionDetails connectionDetails, List<IProducer> producer, Map<String, String> settings) {

        this.connectionDetails = connectionDetails;
        this.producerList = producer;

    }

    @Override
    public abstract void run();

    public ConnectionDetails getConnectionDetails() {
        return connectionDetails;
    }

    // It needs to be analyzed if LatchCount needs to be used in order to make the main thread wait for all threads do the work (- efficiency, + secure)
    // Or if it's ok to create threads and moving on with processing the messages from the external publisher (+ efficiency, - secure)
    public void OnMessageReceived(String topic, String message) {

        List<Thread> producersThreads = new ArrayList<>();

        for (IProducer producer : producerList) {
            producersThreads.add(new Thread(new ProducerThread(producer, topic, message)));
            producersThreads.get(producersThreads.size() - 1).start();
        }

        try {
            for(Thread producer : producersThreads)
                producer.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<IProducer> getProducerList() {
        return producerList;
    }

    public void linkProducer(IProducer producer) {
        producerList.add(producer);
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}
