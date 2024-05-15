package eu.arrowhead.application.skeleton.consumer.classes.kafka;

import eu.arrowhead.application.skeleton.consumer.classes.Constants;
import eu.arrowhead.application.skeleton.consumer.classes.PubSubSettings;
import eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase.JavaRepository;
import eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase.Repository;
import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.time.Duration;
import java.util.*;

public class KafkaCustomConsumer extends IConsumer {

    private final Repository qosRepository = new JavaRepository();
    private KafkaConsumer<String, String> consumer;
    private final ConsumerConfig consumerConfig;
    private final PubSubSettings settings;
    Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
    private final Logger logger = LogManager.getLogger(KafkaCustomConsumer.class);

    public KafkaCustomConsumer(ConnectionDetails connectionDetails, List<IProducer> producer, Map<String, String> settings) {
        super(connectionDetails, producer, settings);
        settings.put("bootstrap.servers", connectionDetails.getAddress() + ":" + connectionDetails.getPort());
        loadDefaults(settings);
        this.settings = new PubSubSettings(settings);
        consumerConfig = new ConsumerConfig(Constants.objectifyMap(settings));
    }

    private void loadDefaults(Map<String,String> settings) {
        if (!settings.containsKey("key.deserializer"))
            settings.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");

        if (!settings.containsKey("value.deserializer"))
            settings.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");

        if (!settings.containsKey("group.id"))
            settings.put("group.id","middleware-group-"+UUID.randomUUID());
    }

    @Override
    public void run() {
        createConsumer();
        subscribe();
    }

    public void createConsumer() {
        consumer = new KafkaConsumer<>(consumerConfig.originals());
    }

    public void subscribe() {
        List<String> topicList = new ArrayList<>();
        topicList.add(settings.getTopic());
        consumer.subscribe(topicList);

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

            for (ConsumerRecord<String, String> record : records) {
                if (settings.getQos() == 0 || settings.getQos() == 1)
                    newMessage(record);
                else {
                    if (settings.getQos()==2) {
                        if (!qosRepository.messageExists(record.key())) {
                            newMessage(record);
                            qosRepository.registerNewMessage(record.key());
                        }
                    }

                    offsets.put(new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1)); // commit offset for next message
                    consumer.commitSync(offsets);
                }
            }
        }
    }

    public void newMessage(ConsumerRecord<String,String> record) {
        lastMessage = record.value();
        numberOfMessages++;

        this.OnMessageReceived(record.topic(), record.value());
    }

    public void stop() {
        consumer.close();
    }

}
