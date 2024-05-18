package eu.arrowhead.application.skeleton.consumer.classes.dds;

import DDS.*;
import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;
import eu.arrowhead.application.skeleton.consumer.classes.Utils;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author : Ricardo Venâncio - 1210828
 **/
public class DDSCustomConsumer extends IConsumer {

    private final String topic;
    private final int qos;
    private final String[] args;
    private final org.slf4j.Logger log = LoggerFactory.getLogger(DDSCustomConsumer.class);

    public DDSCustomConsumer(ConnectionDetails connectionDetails, List<IProducer> producer, Map<String, String> settings) {
        super(connectionDetails, producer, settings);
        this.topic = settings.get("topic");
        this.qos = Integer.parseInt(settings.get("qos"));

        String pathToJSON = "arrowhead skeleton/application-skeleton-consumer" +
                "/src/main/java/eu/arrowhead/application/skeleton/consumer/classes/dds/arguments.json";

        String[] conf = new String[5];
        conf[0] = "-DCPSBit";
        conf[1] = "-DCPSConfigFile";
        conf[2] = "-r";
        conf[3] = "-w";

        try {
            this.args = Utils.parseJSON(pathToJSON, conf);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private void createConsumer(){

        Map<String, ?> ret = DataInstantiation.instantiateDataReader(this.args, this.topic, this.qos, this.log, this);

        Duration_t timeout = new Duration_t(DURATION_INFINITE_SEC.value,
                DURATION_INFINITE_NSEC.value);

        ConditionSeqHolder cond = new ConditionSeqHolder(new Condition[]{});

        WaitSet ws = (WaitSet) ret.get("ws");
        GuardCondition gc = (GuardCondition) ret.get("gc");

        if (ws.wait(cond, timeout) != RETCODE_OK.value) {
            System.err.println("ERROR: wait() failed.");
            return;
        }

        ws.detach_condition(gc);
    }


    @Override
    public void run() {
        createConsumer();
    }


}
