package eu.arrowhead.application.skeleton.consumer.classes.dds;

import common.ConnectionDetails;
import common.IProducer;

import java.util.Map;

/**
 * @author : Ricardo Venâncio - 1210828
 **/
public class DDSCustomProducer extends IProducer {
    public DDSCustomProducer(ConnectionDetails connectionDetails, Map<String, String> settings) {
        super(connectionDetails, settings);
    }

    @Override
    public void produce(String topic, String message) {

    }
}
