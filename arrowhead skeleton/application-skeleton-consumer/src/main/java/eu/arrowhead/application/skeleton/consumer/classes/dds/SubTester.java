package eu.arrowhead.application.skeleton.consumer.classes.dds;

import DDS.*;
import Messenger.MessageTypeSupportImpl;
import OpenDDS.DCPS.DEFAULT_STATUS_MASK;
import OpenDDS.DCPS.TheParticipantFactory;
import OpenDDS.DCPS.TheServiceParticipant;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.omg.CORBA.StringSeqHolder;

import java.io.FileReader;

public class SubTester {

    public static void main(String[] args) throws Exception {

        args = new String[6];
        Object obj = new JSONParser().parse(new FileReader("arrowhead skeleton/application-skeleton-consumer" +
                "/src/main/java/eu/arrowhead/application/skeleton/consumer/classes/dds/arguments.json"));

        JSONObject jo = (JSONObject) obj;
        args[0] = "-DCPSBit";
        long dcpsBit = (long) jo.get("DCPSBit");
        args[1] = String.valueOf(dcpsBit);
        args[2] = "-DCPSConfigFile";
        args[3] = (String) jo.get("DCPSConfigFile");
        if(jo.get("r") != null)
            args[4] = "-r";
        if(jo.get("w") != null)
            args[5] = "-w";

        obj = new JSONParser().parse(new FileReader("arrowhead skeleton/application-skeleton-consumer" +
                "/src/main/java/eu/arrowhead/application/skeleton/consumer/classes/dds/pubSubConf.json"));

        jo = (JSONObject) obj;
        long qos = (long) jo.get("qos");
        String topic = (String) jo.get("topic");

        System.out.println("Start Subscriber");

        DomainParticipantFactory dpf =
                TheParticipantFactory.WithArgs(new StringSeqHolder(args));
        if (dpf == null) {
            System.err.println("ERROR: Domain Participant Factory not found");
            return;
        }
        DomainParticipant dp = dpf.create_participant(1,
                PARTICIPANT_QOS_DEFAULT.get(), null, DEFAULT_STATUS_MASK.value);
        if (dp == null) {
            System.err.println("ERROR: Domain Participant creation failed");
            return;
        }

        MessageTypeSupportImpl servant = new MessageTypeSupportImpl();
        if (servant.register_type(dp, "") != RETCODE_OK.value) {
            System.err.println("ERROR: register_type failed");
            return;
        }
        Topic top = dp.create_topic(topic,
                servant.get_type_name(),
                TOPIC_QOS_DEFAULT.get(),
                null,
                DEFAULT_STATUS_MASK.value);
        if (top == null) {
            System.err.println("ERROR: Topic creation failed");
            return;
        }

        Subscriber sub = dp.create_subscriber(SUBSCRIBER_QOS_DEFAULT.get(),
                null, DEFAULT_STATUS_MASK.value);
        if (sub == null) {
            System.err.println("ERROR: Subscriber creation failed");
            return;
        }

        DataReaderQos dr_qos = new DataReaderQos();

        boolean reliable = true;
        dr_qos.reliability = new ReliabilityQosPolicy();
        dr_qos.reliability.max_blocking_time = new Duration_t();
        dr_qos.deadline = new DeadlineQosPolicy();
        dr_qos.destination_order = new DestinationOrderQosPolicy();

        if(qos == 0){
            dr_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._BEST_EFFORT_RELIABILITY_QOS);
            reliable = false;
            dr_qos.deadline.period = new Duration_t(0, 500000000);
            dr_qos.destination_order.kind = DestinationOrderQosPolicyKind.from_int(DestinationOrderQosPolicyKind
                    ._BY_RECEPTION_TIMESTAMP_DESTINATIONORDER_QOS);
        }else if(qos == 1){
            dr_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._RELIABLE_RELIABILITY_QOS);
            dr_qos.deadline.period = new Duration_t(1, 0);
            dr_qos.destination_order.kind = DestinationOrderQosPolicyKind.from_int(DestinationOrderQosPolicyKind
                    ._BY_RECEPTION_TIMESTAMP_DESTINATIONORDER_QOS);
        }else{
            dr_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._RELIABLE_RELIABILITY_QOS);
            dr_qos.deadline.period = new Duration_t(1, 0);
            dr_qos.destination_order.kind = DestinationOrderQosPolicyKind.from_int(DestinationOrderQosPolicyKind
                    ._BY_SOURCE_TIMESTAMP_DESTINATIONORDER_QOS);
        }

        dr_qos.durability = new DurabilityQosPolicy();
        dr_qos.durability.kind = DurabilityQosPolicyKind.from_int(DurabilityQosPolicyKind._VOLATILE_DURABILITY_QOS);

        dr_qos.latency_budget = new LatencyBudgetQosPolicy();
        dr_qos.latency_budget.duration = new Duration_t();

        dr_qos.liveliness = new LivelinessQosPolicy();
        dr_qos.liveliness.kind = LivelinessQosPolicyKind.from_int(LivelinessQosPolicyKind._AUTOMATIC_LIVELINESS_QOS);
        dr_qos.liveliness.lease_duration = new Duration_t();

        dr_qos.reliability.max_blocking_time = new Duration_t();

        dr_qos.history = new HistoryQosPolicy();
        dr_qos.history.kind = HistoryQosPolicyKind.from_int(0);

        dr_qos.resource_limits = new ResourceLimitsQosPolicy();

        dr_qos.user_data = new UserDataQosPolicy();
        dr_qos.user_data.value = new byte[0];

        dr_qos.ownership = new OwnershipQosPolicy();
        dr_qos.ownership.kind = OwnershipQosPolicyKind.from_int(OwnershipQosPolicyKind._SHARED_OWNERSHIP_QOS);

        dr_qos.time_based_filter = new TimeBasedFilterQosPolicy();
        dr_qos.time_based_filter.minimum_separation = new Duration_t();

        dr_qos.reader_data_lifecycle = new ReaderDataLifecycleQosPolicy();
        dr_qos.reader_data_lifecycle.autopurge_nowriter_samples_delay = new Duration_t();
        dr_qos.reader_data_lifecycle.autopurge_disposed_samples_delay = new Duration_t();

        dr_qos.representation = new DataRepresentationQosPolicy();
        dr_qos.representation.value = new short[0];

        dr_qos.type_consistency = new TypeConsistencyEnforcementQosPolicy();
        dr_qos.type_consistency.kind = 2;
        dr_qos.type_consistency.ignore_member_names = false;
        dr_qos.type_consistency.force_type_validation = false;

        DataReaderQosHolder qosh = new DataReaderQosHolder(dr_qos);
        sub.get_default_datareader_qos(qosh);
        if (reliable) {
            qosh.value.reliability.kind =
                    ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
        }
        qosh.value.history.kind = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;

        DataReaderListenerImpl2 listener = new DataReaderListenerImpl2();

        GuardCondition gc = new GuardCondition();
        WaitSet ws = new WaitSet();
        ws.attach_condition(gc);
        listener.set_guard_condition(gc);

        DataReader dr = sub.create_datareader(top,
                qosh.value,
                listener,
                DEFAULT_STATUS_MASK.value);
        if (!reliable) {
            listener.set_expected_count(1);
        }

        if (dr == null) {
            System.err.println("ERROR: DataReader creation failed");
            return;
        }
        Duration_t timeout = new Duration_t(DURATION_INFINITE_SEC.value,
                DURATION_INFINITE_NSEC.value);

        ConditionSeqHolder cond = new ConditionSeqHolder(new Condition[]{});
        if (ws.wait(cond, timeout) != RETCODE_OK.value) {
            System.err.println("ERROR: wait() failed.");
            return;
        }
        System.out.println("Subscriber Report Validity");
        listener.report_validity();

        ws.detach_condition(gc);

        System.out.println("Stop Subscriber");

        dp.delete_contained_entities();
        dpf.delete_participant(dp);
        TheServiceParticipant.shutdown();

        System.out.println("Subscriber exiting");
    }
}
