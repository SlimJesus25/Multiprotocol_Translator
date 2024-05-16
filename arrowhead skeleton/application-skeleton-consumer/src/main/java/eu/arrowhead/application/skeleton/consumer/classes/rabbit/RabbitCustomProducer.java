package eu.arrowhead.application.skeleton.consumer.classes.rabbit;

import com.rabbitmq.client.*;
import common.ConnectionDetails;
import common.IProducer;
import eu.arrowhead.application.skeleton.consumer.classes.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeoutException;


public class RabbitCustomProducer extends IProducer {

    private final Logger log = LoggerFactory.getLogger(RabbitCustomProducer.class);
    private Channel channel;
    private final RabbitConsumerSettings settings;
    private int numberOfMessages = 1;
    private long utilsID;
    private boolean first = false;
    private final ConcurrentNavigableMap<Long, String> outstandingConfirms = new ConcurrentSkipListMap<>();
    private boolean quarter = false;
    private boolean half = false;
    private boolean threeQuarters = false;

    public RabbitCustomProducer(ConnectionDetails connectionDetails, Map<String,String> settings) {
        super(connectionDetails, settings);
        this.settings = new RabbitConsumerSettings(settings);
        connect();
    }

    private void connect() {
        ConnectionFactory factory = settings.getRabbitSettings().getConnectionFactory();
        factory.setHost(this.getConnectionDetails().getAddress());

        // TODO needs to change the way.
        factory.setUsername("admin");
        factory.setPassword("admin");

        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();

            // 0 is unlimited for both configurations.
            int prefetchSize = 0;
            int prefetchCount = 0;
            boolean global = true;

            ConfirmCallback confirmCallback = (sequenceNumber, multiple) -> {
                if(multiple){
                    int amount = 0;
                    Iterator<Map.Entry<Long, String>> it = outstandingConfirms.descendingMap().entrySet().iterator();
                    while(it.hasNext() || outstandingConfirms.containsKey(sequenceNumber)){
                        if(it.next().getKey() <= sequenceNumber) {
                            it.remove();
                            amount++;
                        }
                    }
                    numberOfMessages += amount;
                }else{
                    outstandingConfirms.remove(sequenceNumber);
                    numberOfMessages++;
                }
            };

            ConfirmCallback nackedConfirmCallback = (sequenceNumber, multiple) -> {
                    if(multiple){
                        ConcurrentNavigableMap<Long, String> nAcked = outstandingConfirms.headMap(sequenceNumber, true);
                        nAcked.forEach((id, message) -> produceMessage(settings.getQueue(), message));
                    }else{
                        produceMessage(settings.getQueue(), outstandingConfirms.get(sequenceNumber));
                    }
                };

            if(settings.getQos() == 0){

                channel.addConfirmListener(confirmCallback, (sequence, multiple) -> {

                });
                channel.basicQos(prefetchSize, prefetchCount, global);
            }else if(settings.getQos() == 1){

                channel.confirmSelect();

                channel.addConfirmListener(confirmCallback, // message was confirmed.
                        nackedConfirmCallback); // message was nack-ed.

                prefetchCount = 10;
                channel.basicQos(prefetchSize, prefetchCount, global);
            }else if(settings.getQos() == 2){

                channel.confirmSelect();

                // Sequence Number -> number that identifies the confirmed or nack-ed messages.
                // Multiple -> if false, only one message is confirmed/nack-ed. if true, all messages with a lower or
                // equal sequence number are confirmed/nack-ed.
                channel.addConfirmListener(confirmCallback, nackedConfirmCallback);

                prefetchCount = 1;
                channel.basicQos(prefetchSize, prefetchCount, global);
            }

        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void produce(String topic, String message) {

        // Starts counting.
        if (numberOfMessages == 1 && !first) {
            utilsID = Utils.initializeCounting();
            first = true;
        }

        String queueName;

        // In case queue name is not referred, it will be produced to the same name as topic/queue as the producer.
        if (settings.getQueue().isEmpty()) {
            queueName = topicFromConsumer(topic);
        } else {
            queueName = settings.getQueue();
        }

        produceMessage(queueName, message);

        if(settings.getQos() == 0) {
            numberOfMessages++;
        }

        // At 25%, 50% and 75% this collects information about memory, threads and CPU...
        boolean[] arr = new boolean[3];
        Utils.checkValue(this.numberOfMessages, quarter, half, threeQuarters, arr, utilsID);

        quarter = arr[0];
        half = arr[1];
        threeQuarters = arr[2];

        if (numberOfMessages >= 100000) {
            Utils.pointReached(utilsID, log);
            this.numberOfMessages -= 100000;
            first = true;
            quarter = true;
            half = true;
            threeQuarters = true;
        }
    }

    private void produceMessage(String queueName, String message) {
        try {
            outstandingConfirms.put(channel.getNextPublishSeqNo(), message);
            AMQP.BasicProperties basicProperties = new AMQP.BasicProperties().builder()
                    .messageId(UUID.randomUUID().toString())
                    .build();
            channel.basicPublish("", queueName, basicProperties, message.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }
}
