package common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class IProducerTest {

    IProducer producer;

    @BeforeEach
    void setUp() {
        Map<String,String> settings = new HashMap<>();
        settings.put("consumerTopic","Remove/;/Remove");
        producer = new IProducer(new ConnectionDetails("",1),settings) {
            @Override
            public void produce(String topic, String message) {

            }
        };
    }

    @Test
    void topicCannotBeEmpty() {
        assertThrows(
                RuntimeException.class,
                () -> producer.topicFromConsumer("Remove/"),
                "Expected method to throw"
        );
    }

    @Test
    void topicIsRemoved() {
        assertEquals("temperature",producer.topicFromConsumer("Remove/temperature"));
    }

    @Test
    void multipleTopicAreRemoved() {
        assertEquals("temperature",producer.topicFromConsumer("Remove/temperature/Remove"));
    }
}


