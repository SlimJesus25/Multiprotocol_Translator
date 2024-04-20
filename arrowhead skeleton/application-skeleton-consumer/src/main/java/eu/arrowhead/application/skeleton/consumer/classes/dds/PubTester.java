package eu.arrowhead.application.skeleton.consumer.classes.dds;

import DDS.*;
import Messenger.Message;
import Messenger.MessageDataWriter;
import Messenger.MessageDataWriterHelper;
import Messenger.MessageTypeSupportImpl;
import OpenDDS.DCPS.DEFAULT_STATUS_MASK;
import OpenDDS.DCPS.TheParticipantFactory;
import org.omg.CORBA.StringSeqHolder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PubTester {

    private static final String topic = "cards2";
    private static final int qos = 2;
    private static int amount = 2;
    private static final String message = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

    public static void main(String[] args){

        args = new String[8];
        args[0] = "-DCPSBit";
        args[1] = "0";
        args[2] = "-DCPSConfigFile";
        args[3] = "/home/ricardo/IdeaProjects/Multiprotocol_Translator/arrowhead skeleton/application-skeleton-consumer/src/main/java/eu/arrowhead/application/skeleton/consumer/classes/dds/tcp2.ini";
        args[4] = "-r";
        args[5] = "-w";
        args[6] = "-DCPSPendingTimeout";
        args[7] = "3";

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

        boolean reliable = true;
        dw_qos.reliability = new ReliabilityQosPolicy();
        dw_qos.deadline = new DeadlineQosPolicy();
        if(qos == 0){
            dw_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._BEST_EFFORT_RELIABILITY_QOS);
            dw_qos.deadline.period = new Duration_t();
            reliable = false;
        }else if(qos == 1){
            dw_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._RELIABLE_RELIABILITY_QOS);
            dw_qos.deadline.period = new Duration_t();
            amount = 5;
        }else{
            dw_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._RELIABLE_RELIABILITY_QOS);
            dw_qos.deadline.period = new Duration_t();
        }

        dw_qos.durability = new DurabilityQosPolicy();
        dw_qos.durability.kind = DurabilityQosPolicyKind.from_int(0);
        dw_qos.durability_service = new DurabilityServiceQosPolicy();
        dw_qos.durability_service.history_kind = HistoryQosPolicyKind.from_int(0);
        dw_qos.durability_service.service_cleanup_delay = new Duration_t();
        dw_qos.latency_budget = new LatencyBudgetQosPolicy();
        dw_qos.latency_budget.duration = new Duration_t();
        dw_qos.liveliness = new LivelinessQosPolicy();
        dw_qos.liveliness.kind = LivelinessQosPolicyKind.from_int(0);
        dw_qos.liveliness.lease_duration = new Duration_t();
        //dw_qos.reliability = new ReliabilityQosPolicy();
        dw_qos.reliability.max_blocking_time = new Duration_t();
        dw_qos.destination_order = new DestinationOrderQosPolicy();
        dw_qos.destination_order.kind = DestinationOrderQosPolicyKind.from_int(0);
        dw_qos.history = new HistoryQosPolicy();
        dw_qos.history.kind = HistoryQosPolicyKind.from_int(0);
        dw_qos.resource_limits = new ResourceLimitsQosPolicy();
        dw_qos.transport_priority = new TransportPriorityQosPolicy();
        dw_qos.lifespan = new LifespanQosPolicy();
        dw_qos.lifespan.duration = new Duration_t();
        dw_qos.user_data = new UserDataQosPolicy();
        dw_qos.user_data.value = new byte[0];
        dw_qos.ownership = new OwnershipQosPolicy();
        dw_qos.ownership.kind = OwnershipQosPolicyKind.from_int(0);
        dw_qos.ownership_strength = new OwnershipStrengthQosPolicy();
        dw_qos.writer_data_lifecycle = new WriterDataLifecycleQosPolicy();
        dw_qos.representation = new DataRepresentationQosPolicy();
        dw_qos.representation.value = new short[0];

        DataWriterQosHolder qosh = new DataWriterQosHolder(dw_qos);
        pub.get_default_datawriter_qos(qosh);
        qosh.value.history.kind = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;
        if (reliable) {
            qosh.value.reliability.kind =
                    ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
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
        for (; msg.count < amount; ++msg.count) {
            while ((ret = mdw.write(msg, handle)) == RETCODE_TIMEOUT.value) {
            }
            if (ret != RETCODE_OK.value) {
                System.err.println("ERROR " + msg.count +
                        " write() returned " + ret);
            }
            try {
                Thread.sleep(100);
            } catch(InterruptedException ie) {
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

        System.out.println("Stop Publisher");
    }
}
