package common;

public class ProducerThread implements Runnable{

    private final IProducer producer;
    private final String topic;
    private final String message;

    public ProducerThread(IProducer producer, String topic, String message) {
        this.producer = producer;
        this.topic = topic;
        this.message = message;
    }


    @Override
    public void run() {
        producer.produce(topic, message);
    }
}
