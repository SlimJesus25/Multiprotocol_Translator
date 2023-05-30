package eu.arrowhead.application.skeleton.consumer.classes.kafka;

import common.ConnectionDetails;
import common.IProducer;
import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaCustomProducer extends IProducer {

    private KafkaProducer<String, String> producer;

    private org.slf4j.Logger log = LoggerFactory.getLogger(KafkaCustomProducer.class);

    private KafkaProducerSettings settings;

    private ConsumerGroupMetadata metadata2;

    private int qos;

    private String topic;

    private int numberOfMessages = 0;

    long thing = System.currentTimeMillis();

    Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();

    private final Logger logger = LogManager.getLogger(KafkaCustomProducer.class);

    public KafkaCustomProducer(ConnectionDetails connectionDetails, Map<String, String> settings) {
        super(connectionDetails, settings);
        this.settings = new KafkaProducerSettings(settings);
        qos = this.settings.getQos();
        topic = this.settings.getTopic();
        metadata2 = new ConsumerGroupMetadata(this.settings.getClientId());
        createProducer();
    }

    @Override
    public void produce(String topic, String message) {
        final ProducerRecord<String, String> record = new ProducerRecord<>(this.topic, "key", message);

        if (numberOfMessages == 1) {
            thing = System.currentTimeMillis();
        }

        numberOfMessages++;



        try {
            Future<RecordMetadata> future = producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {

                    if (e != null) {
                        logger.error("Failed producing kafka message");
                        logger.error(e.toString());
                        throw new RuntimeException(e);
                    } else {
                        // logger.info("Produced kafka message to topic " + topic);
                    }


                }
            });
        } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
            logger.warn(e);
        }



        if (numberOfMessages == 100000f) {
            log.info("Messages per second + " + (100000f / ((System.currentTimeMillis() - thing) / 1000f)));
            numberOfMessages = 0;
        }
    }


    public void createProducer() {


        Properties config = new Properties();

            ConnectionDetails cd = this.getConnectionDetails();
            config.put("client.id", settings.getClientId());
            config.put("bootstrap.servers", cd.getAddress() + ":" + cd.getPort());

            switch (settings.getQos()) {
                case 0:
                    config.put("acks", "0");
                    break;
                case 2:
                    config.put("enable.idempotence", "true");
                    config.put("max.in.flight.requests.per.connection", "1");
                case 1:
                    config.put("acks","all");
                    break;
            }

            config.put("key.serializer",settings.getKeySerializer());
            config.put("value.serializer",settings.getValueSerializer());

            producer = new KafkaProducer<>(config);


            // logger.info("Kafka Producer connected to " + cd.getAddress() + ":" + cd.getPort() + " with topic " + settings.getTopic());

    }
}
