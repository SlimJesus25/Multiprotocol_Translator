package eu.arrowhead.application.skeleton.consumer.classes.dds;

import DDS.*;
import OpenDDS.DCPS.TheServiceParticipant;
import eu.arrowhead.application.skeleton.consumer.classes.Utils;
import java.util.Map;

public class SubTester {

    public static void main(String[] args) throws Exception {

        args = new String[4];
        args[0] = "-DCPSBit";
        args[1] = "-DCPSConfigFile";
        args[2] = "-r";
        args[3] = "-w";
        args = Utils.parseJSON("arrowhead skeleton/application-skeleton-consumer"
                + "/src/main/java/eu/arrowhead/application/skeleton/consumer/classes/dds/arguments.json", args);

        String[] values = new String[2];
        values[0] = "qos";
        values[1] = "topic";
        String[] finalValues = Utils.parseJSON("arrowhead skeleton/application-skeleton-consumer/src/main/java/eu/arrowhead/" +
                "application/skeleton/consumer/classes/dds/pubSubConf.json", values);

        long qos = Long.parseLong(finalValues[1]);
        String topic = finalValues[3];

        Map<String, ?> ret = DataInstantiation.instantiateDataReader(args, topic, (int) qos, null,  null);

        WaitSet ws = (WaitSet) ret.get("ws");
        GuardCondition gc = (GuardCondition) ret.get("gc");

        System.out.println("Start Subscriber");

        Duration_t timeout = new Duration_t(DURATION_INFINITE_SEC.value,
                DURATION_INFINITE_NSEC.value);

        ConditionSeqHolder cond = new ConditionSeqHolder(new Condition[]{});
        if (ws.wait(cond, timeout) != RETCODE_OK.value) {
            System.err.println("ERROR: wait() failed.");
            return;
        }

        ws.detach_condition(gc);
        System.out.println("Stop Subscriber");
        TheServiceParticipant.shutdown();
        System.out.println("Subscriber exiting");
    }
}
