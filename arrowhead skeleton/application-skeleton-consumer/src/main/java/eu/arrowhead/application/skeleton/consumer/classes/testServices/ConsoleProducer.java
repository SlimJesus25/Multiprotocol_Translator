package eu.arrowhead.application.skeleton.consumer.classes.testServices;

import common.ConnectionDetails;
import common.IProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class ConsoleProducer extends IProducer {

    private final Logger logger = LogManager.getLogger(ConsoleProducer.class);

    private final String topic;
    public ConsoleProducer(ConnectionDetails connectionDetails, Map<String, String> settings) {
        super(connectionDetails, settings);
        this.topic = settings.get("topic");
        logger.info("\nCreating new console producer... ");
        logger.info("\nConsole producer connected at: \n" + connectionDetails + "\n\n");
    }

    @Override
    public void produce(String topic, String message) {
        ConnectionDetails cd = getConnectionDetails();
        logger.info("Connected at " + cd.getAddress() + ":" + cd.getPort() + "\n");
        logger.info("Publishing to topic \"" + this.topic +"\"");
        logger.info("Message: " + message + "\n");
    }
}
