package eu.arrowhead.application.skeleton.consumer.classes.testServices;

import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import java.util.Map;

public class PeriodicConsumer extends IConsumer {

    private final Logger logger = LogManager.getLogger(PeriodicConsumer.class);

    private final String topic;

    public PeriodicConsumer(ConnectionDetails connectionDetails, List<IProducer> producer, String topic, Map<String,
            String> settings) {
        super(connectionDetails, producer, settings);
        this.topic = topic;
        logger.info("Creating new periodic consumer... ");
        logger.info("Periodic consumer (2s) connected at: " + connectionDetails);
    }

    @Override
    public void run() {
        start();
    }

    private void start() {
        logger.info("Initializing message simulation. Receiving a new message every 2 seconds");
        int i = 0;
        while (true) {
            logger.info("Message arrived on teste broker");
            i++;
            OnMessageReceived(topic,String.valueOf(i));


            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
