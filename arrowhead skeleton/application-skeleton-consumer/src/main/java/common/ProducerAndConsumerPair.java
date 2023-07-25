package common;

public class ProducerAndConsumerPair {

    public IProducer producer;

    public IConsumer consumer;

    public ProducerAndConsumerPair(IProducer producer, IConsumer consumer) {
        this.producer = producer;
        this.consumer = consumer;
    }
}
