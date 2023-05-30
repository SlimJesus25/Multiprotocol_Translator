package common;

import java.util.List;
import java.util.Map;

public abstract class IConsumer implements Runnable {

    public List<IProducer> producerList;

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

    public void OnMessageReceived(String topic, String message) {
        for (IProducer producer : producerList) {
            producer.produce(topic,message);
        }
    }

    public List<IProducer> getProducerList() {
        return producerList;
    }

    public void linkProducer(IProducer producer) {
        producerList.add(producer);
    }
}
