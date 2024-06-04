package eu.arrowhead.application.skeleton.consumer.classes.dds;

import DDS.*;
import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
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

        this.args = new String[6];
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("arguments.json");
            JSONObject jo = new JSONObject(new JSONTokener(inputStream));

            int dcpsBit = jo.getInt("DCPSBit");
            String dcpsConfigFile = jo.getString("DCPSConfigFile");

            args[0] = "-DCPSBit";
            args[1] = String.valueOf(dcpsBit);
            args[2] = "-DCPSConfigFile";
            args[3] = dcpsConfigFile;
            args[4] = "r";
            args[5] = "w";

        }catch(NullPointerException ignored){

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
