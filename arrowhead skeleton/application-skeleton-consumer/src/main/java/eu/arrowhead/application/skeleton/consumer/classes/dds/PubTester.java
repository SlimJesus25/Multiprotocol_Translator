package eu.arrowhead.application.skeleton.consumer.classes.dds;

import DDS.*;
import Messenger.Message;
import Messenger.MessageDataWriter;
import Messenger.MessageDataWriterHelper;
import Messenger.MessageTypeSupportImpl;
import OpenDDS.DCPS.DEFAULT_STATUS_MASK;
import OpenDDS.DCPS.TheParticipantFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.omg.CORBA.StringSeqHolder;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PubTester {

    private static final List<Message> messageBatch = new ArrayList<>();
    private static long lastSentTime = System.currentTimeMillis();

    private static int addToBatch(Message message, MessageDataWriter mdw, int handle){
        messageBatch.add(message);
        int size = 0;
        int batchSize = 1000;
        int batchTimeout = 2000;
        if(messageBatch.size() >= batchSize || (System.currentTimeMillis() - lastSentTime) >= batchTimeout){
            sendBatch(mdw, handle);
            lastSentTime = System.currentTimeMillis();
            size = messageBatch.size();
            messageBatch.clear();
        }
        return size;
    }

    private static void sendBatch(MessageDataWriter mdw, int handle){
        for(Message msg : messageBatch){

            int ret = RETCODE_TIMEOUT.value;
            while(ret == RETCODE_TIMEOUT.value) {
                ret = mdw.write(msg, handle);
            }
            if (ret != RETCODE_OK.value) {
                System.err.println("ERROR " + msg.count +
                        " write() returned " + ret);
            }
        }
    }

    public static void main(String[] args) throws IOException, ParseException {

        args = new String[8];
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
        args[6] = "-DCPSPendingTimeout";
        long dcpsPendingTimeout = (long) jo.get("DCPSPendingTimeout");
        args[7] = String.valueOf(dcpsPendingTimeout);

        obj = new JSONParser().parse(new FileReader("arrowhead skeleton/application-skeleton-consumer" +
                "/src/main/java/eu/arrowhead/application/skeleton/consumer/classes/dds/pubSubConf.json"));

        jo = (JSONObject) obj;
        long qos = (long) jo.get("qos");
        long amount = (long) jo.get("amount");
        String topic = (String) jo.get("topic");
        String message = (String) jo.get("message");

        DomainParticipantFactory domainParticipantFactory = TheParticipantFactory.WithArgs(new StringSeqHolder(args));
        if(domainParticipantFactory == null){
            System.out.println("Error");
            return;
        }

        DomainParticipant domainParticipant = domainParticipantFactory.create_participant(1, PARTICIPANT_QOS_DEFAULT.get(),
                null, DEFAULT_STATUS_MASK.value);
        if(domainParticipant == null){
            System.out.println("Error");
            return;
        }

        MessageTypeSupportImpl servant = new MessageTypeSupportImpl();
        if (servant.register_type(domainParticipant, "") != RETCODE_OK.value) {
            System.err.println("ERROR: register_type failed");
            return;
        }

        Topic top = domainParticipant.create_topic(topic,
                servant.get_type_name(),
                TOPIC_QOS_DEFAULT.get(),
                null,
                DEFAULT_STATUS_MASK.value);
        if (top == null) {
            System.err.println("ERROR: Topic creation failed");
            return;
        }

        Publisher pub = domainParticipant.create_publisher(PUBLISHER_QOS_DEFAULT.get(), null,
                DEFAULT_STATUS_MASK.value);
        if (pub == null) {
            System.err.println("ERROR: Publisher creation failed");
            return;
        }

        DataWriterQos dw_qos = new DataWriterQos();

        dw_qos.deadline = new DeadlineQosPolicy();
        dw_qos.destination_order = new DestinationOrderQosPolicy();

        boolean reliable = true;
        dw_qos.reliability = new ReliabilityQosPolicy();
        dw_qos.deadline = new DeadlineQosPolicy();
        if(qos == 0){
            dw_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._BEST_EFFORT_RELIABILITY_QOS);
            dw_qos.deadline.period = new Duration_t(0, 500000000);
            dw_qos.destination_order.kind = DestinationOrderQosPolicyKind.from_int(DestinationOrderQosPolicyKind
                    ._BY_RECEPTION_TIMESTAMP_DESTINATIONORDER_QOS);
            reliable = false;
        }else if(qos == 1){
            dw_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._RELIABLE_RELIABILITY_QOS);
            dw_qos.deadline.period = new Duration_t(1, 0);
            dw_qos.destination_order.kind = DestinationOrderQosPolicyKind.from_int(DestinationOrderQosPolicyKind
                    ._BY_RECEPTION_TIMESTAMP_DESTINATIONORDER_QOS);

            // amount = 5;
        }else{
            dw_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._RELIABLE_RELIABILITY_QOS);
            dw_qos.deadline.period = new Duration_t(1, 0);
            dw_qos.destination_order.kind = DestinationOrderQosPolicyKind.from_int(DestinationOrderQosPolicyKind
                    ._BY_SOURCE_TIMESTAMP_DESTINATIONORDER_QOS);

        }

        dw_qos.durability = new DurabilityQosPolicy();
        dw_qos.durability.kind = DurabilityQosPolicyKind.from_int(DurabilityQosPolicyKind._VOLATILE_DURABILITY_QOS);
        dw_qos.durability_service = new DurabilityServiceQosPolicy();
        dw_qos.durability_service.history_kind = HistoryQosPolicyKind.from_int(HistoryQosPolicyKind._KEEP_LAST_HISTORY_QOS);
        dw_qos.durability_service.service_cleanup_delay = new Duration_t();

        dw_qos.latency_budget = new LatencyBudgetQosPolicy();
        dw_qos.latency_budget.duration = new Duration_t();

        dw_qos.liveliness = new LivelinessQosPolicy();
        dw_qos.liveliness.kind = LivelinessQosPolicyKind.from_int(LivelinessQosPolicyKind._AUTOMATIC_LIVELINESS_QOS);
        dw_qos.liveliness.lease_duration = new Duration_t(5, 0);

        dw_qos.reliability.max_blocking_time = new Duration_t();

        dw_qos.history = new HistoryQosPolicy();
        dw_qos.history.kind = HistoryQosPolicyKind.from_int(0);

        dw_qos.resource_limits = new ResourceLimitsQosPolicy();
        dw_qos.resource_limits.max_samples = 10000;
        dw_qos.resource_limits.max_samples_per_instance = 100;
        dw_qos.resource_limits.max_instances = 1000;

        dw_qos.transport_priority = new TransportPriorityQosPolicy();

        dw_qos.lifespan = new LifespanQosPolicy();
        dw_qos.lifespan.duration = new Duration_t();

        dw_qos.user_data = new UserDataQosPolicy();
        dw_qos.user_data.value = new byte[0];

        dw_qos.ownership = new OwnershipQosPolicy();
        dw_qos.ownership.kind = OwnershipQosPolicyKind.from_int(OwnershipQosPolicyKind._SHARED_OWNERSHIP_QOS);
        dw_qos.ownership_strength = new OwnershipStrengthQosPolicy();

        dw_qos.writer_data_lifecycle = new WriterDataLifecycleQosPolicy();

        dw_qos.representation = new DataRepresentationQosPolicy();
        dw_qos.representation.value = new short[0];

        DataWriterQosHolder qosh = new DataWriterQosHolder(dw_qos);
        pub.get_default_datawriter_qos(qosh);
        qosh.value.history.kind = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;
        qosh.value.resource_limits.max_samples = 200000;
        qosh.value.resource_limits.max_instances = 10;
        qosh.value.resource_limits.max_samples_per_instance = 10000;
        qosh.value.reliability.max_blocking_time.nanosec = 0;

        if (reliable) {
            qosh.value.reliability.kind =
                    ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
        }else{
            qosh.value.reliability.kind =
                    ReliabilityQosPolicyKind.BEST_EFFORT_RELIABILITY_QOS;
        }

        DataWriter dataWriter = pub.create_datawriter(top,
                qosh.value,
                null,
                DEFAULT_STATUS_MASK.value);
        if (dataWriter == null) {
            System.err.println("ERROR: DataWriter creation failed");
            return;
        }

        System.out.println("Publisher Created DataWriter");

        StatusCondition sc = dataWriter.get_statuscondition();
        sc.set_enabled_statuses(PUBLICATION_MATCHED_STATUS.value);
        WaitSet ws = new WaitSet();
        ws.attach_condition(sc);
        PublicationMatchedStatusHolder matched =
                new PublicationMatchedStatusHolder(new PublicationMatchedStatus());
        Duration_t timeout = new Duration_t(DURATION_INFINITE_SEC.value,
                DURATION_INFINITE_NSEC.value);


        while (true) {
            final int result = dataWriter.get_publication_matched_status(matched);
            if (result != RETCODE_OK.value) {
                System.err.println("ERROR: get_publication_matched_status()" +
                        "failed.");
                return;
            }

            if (matched.value.current_count >= 1) {
                System.out.println("Publisher Matched");
                break;
            }

            ConditionSeqHolder cond = new ConditionSeqHolder(new Condition[]{});
            if (ws.wait(cond, timeout) != RETCODE_OK.value) {
                System.err.println("ERROR: wait() failed.");
                return;
            }
        }

        ws.detach_condition(sc);
        MessageDataWriter mdw = MessageDataWriterHelper.narrow(dataWriter);
        Message msg = new Message();
        msg.subject_id = 1;
        int handle = mdw.register_instance(msg);
        msg.from = topic;
        msg.subject = topic;
        msg.text = message;
        msg.count = 1;
        int ret = RETCODE_TIMEOUT.value;



        if(qos > 0) {
            while (msg.count < amount) {
                if(msg.count % 100000 == 0)
                    System.out.println("Done %");
                msg.count += addToBatch(msg, mdw, handle);
            }
        }else{
            for (; msg.count < amount; ++msg.count) {
                while ((ret = mdw.write(msg, handle)) == RETCODE_TIMEOUT.value) {
                }
                if (ret != RETCODE_OK.value) {
                    System.err.println("ERROR " + msg.count +
                            " write() returned " + ret);
                }
            }
        }



        /*
        while (matched.value.current_count != 0) {
            final int result = mdw.get_publication_matched_status(matched);
            try {
                Thread.sleep(100);
            } catch(InterruptedException ie) {
            }
        }
        */

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Stop Publisher");
    }
}
