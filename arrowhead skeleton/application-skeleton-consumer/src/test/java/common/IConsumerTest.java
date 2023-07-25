package common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IConsumerTest {

    IProducerException exceptionProducer;

    IProducerNormal emptyProducer;

    IConsumer emptyConsumer;

    @BeforeEach
    void setUp() {
        Map<String,String> emptySettings = new HashMap<>();

        exceptionProducer = new IProducerException(new ConnectionDetails("none",0),emptySettings);
        emptyProducer = new IProducerNormal(new ConnectionDetails("none",0),emptySettings);

        List<IProducer> producerList = new ArrayList<>();
        producerList.add(exceptionProducer);
        producerList.add(1,emptyProducer);

        emptyConsumer = new IConsumerNormal(new ConnectionDetails("none",0),producerList,emptySettings);
    }

    @Test
    void onMessageReceived() {
        emptyConsumer.OnMessageReceived("topic","message");
        assertEquals(1,emptyProducer.producedMessages);
    }

    private class IProducerException extends IProducer {

        public IProducerException(ConnectionDetails connectionDetails, Map<String, String> settings) {
            super(connectionDetails, settings);
        }

        @Override
        public void produce(String topic, String message) {
            try {
                throw new Exception("Error while producing the message");
            } catch (Exception e) {
                System.out.println("Exception");
            }
        }
    }

    private class IProducerNormal extends IProducer {

        public int producedMessages = 0;
        public IProducerNormal(ConnectionDetails connectionDetails, Map<String, String> settings) {
            super(connectionDetails, settings);
        }

        @Override
        public void produce(String topic, String message) {
            producedMessages++;
            System.out.println("hello");
        }
    }

    private class IConsumerNormal extends IConsumer {

        public IConsumerNormal(ConnectionDetails connectionDetails, List<IProducer> producer, Map<String, String> settings) {
            super(connectionDetails, producer, settings);
        }

        @Override
        public void run() {

        }
    }
}