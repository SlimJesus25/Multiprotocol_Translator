package eu.arrowhead.application.skeleton.consumer.classes.dds;

import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;

import java.util.List;
import java.util.Map;

/**
 * @author : Ricardo Venâncio - 1210828
 **/
public class DDSCustomConsumer extends IConsumer {
    public DDSCustomConsumer(ConnectionDetails connectionDetails, List<IProducer> producer, Map<String, String> settings) {
        super(connectionDetails, producer, settings);
    }

    @Override
    public void run() {

    }
}
