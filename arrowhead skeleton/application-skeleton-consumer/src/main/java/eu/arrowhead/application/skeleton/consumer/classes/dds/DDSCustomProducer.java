package eu.arrowhead.application.skeleton.consumer.classes.dds;

import DDS.*;
import Messenger.Message;
import Messenger.MessageDataWriter;
import Messenger.MessageDataWriterHelper;
import Messenger.MessageTypeSupportImpl;
import OpenDDS.DCPS.DEFAULT_STATUS_MASK;
import OpenDDS.DCPS.TheParticipantFactory;
import common.ConnectionDetails;
import common.IProducer;
import eu.arrowhead.application.skeleton.consumer.classes.PubSubSettings;
import eu.arrowhead.application.skeleton.consumer.classes.Utils;
import org.omg.CORBA.StringSeqHolder;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author : Ricardo Venâncio - 1210828
 **/
public class DDSCustomProducer extends IProducer {

    private static int amount = 2;
    private final PubSubSettings settings;
    private DataWriter dataWriter;
    private final org.slf4j.Logger log = LoggerFactory.getLogger(DDSCustomProducer.class);
    private long utilsID;
    private int numberOfMessages = 0;
    private final String[] args;
    private final String topic;
    private int count;
    private final List<Message> messageBatch = new ArrayList<>();
    private long lastSentTime = System.currentTimeMillis();
    private boolean first = false;
    private boolean quarter = false;
    private boolean half = false;
    private boolean threeQuarters = false;
    private final boolean[] arr = new boolean[3];

    private void addToBatch(Message message, MessageDataWriter mdw, int handle){
        messageBatch.add(message);
        int batchSize = 100;
        int batchTimeout = 2000;
        if(messageBatch.size() >= batchSize || (System.currentTimeMillis() - lastSentTime) >= batchTimeout){
            sendBatch(mdw, handle);
            lastSentTime = System.currentTimeMillis();
            messageBatch.clear();
        }
    }

    private void sendBatch(MessageDataWriter mdw, int handle){
        for(Message msg : messageBatch){
            mdw.write(msg, handle);
        }
    }

    public DDSCustomProducer(ConnectionDetails connectionDetails, Map<String, String> settings) {
        super(connectionDetails, settings);
        this.settings = new PubSubSettings(settings);
        count = 1;

        Arrays.fill(arr, true);

        args = new String[8];
        args[0] = "-DCPSBit";
        args[1] = "0";
        args[2] = "-DCPSConfigFile";
        args[3] = "/home/arrowhead/IdeaProjects/Multiprotocol_Translator/arrowhead skeleton/application-skeleton-consumer" +
                "/src/main/java/eu/arrowhead/application/skeleton/consumer/classes/dds/tcp2.ini";
        args[4] = "-r";
        args[5] = "-w";
        args[6] = "-DCPSPendingTimeout";
        args[7] = "3";
        this.topic = settings.get("topic");
        createProducer(settings.get("topic"));
    }

    private void createProducer(String topic){

        DomainParticipantFactory domainParticipantFactory = TheParticipantFactory.WithArgs(new StringSeqHolder(this.args));
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
        dw_qos.deadline.period = new Duration_t();

        if(settings.getQos() == 0){
            dw_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._BEST_EFFORT_RELIABILITY_QOS);
            reliable = false;
        }else if(settings.getQos() == 1){
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

        DataWriterListenerImpl dwListener = new DataWriterListenerImpl(log);

        this.dataWriter = pub.create_datawriter(top,
                qosh.value,
                dwListener,
                DEFAULT_STATUS_MASK.value);
        if (this.dataWriter == null) {
            System.err.println("ERROR: DataWriter creation failed");
        }

    }

    /*
         This needs to be synchronized because of race conditions!
         Imagine the following scenario: There is one producer, MQTT for instance. And there are three consumers,
         all of them DDS. If
    */
    @Override
    public void produce(String topic, String message) {

            if (numberOfMessages == 1 || first) {
                utilsID = Utils.initializeCounting();
                first = false;
            }

            numberOfMessages++;

            topic = this.topic;
            StatusCondition sc = this.dataWriter.get_statuscondition();
            sc.set_enabled_statuses(PUBLICATION_MATCHED_STATUS.value);
            WaitSet ws = new WaitSet();
            ws.attach_condition(sc);
            PublicationMatchedStatusHolder matched =
                    new PublicationMatchedStatusHolder(new PublicationMatchedStatus());
            Duration_t timeout = new Duration_t(DURATION_INFINITE_SEC.value,
                    DURATION_INFINITE_NSEC.value);


            while (true) {
                final int result = this.dataWriter.get_publication_matched_status(matched);
                if (result != RETCODE_OK.value) {
                    System.err.println("ERROR: get_publication_matched_status()" +
                            "failed.");
                    return;
                }

                if (matched.value.current_count >= 1) {
                    // System.out.println("Publisher Matched");
                    break;
                }

                ConditionSeqHolder cond = new ConditionSeqHolder(new Condition[]{});
                if (ws.wait(cond, timeout) != RETCODE_OK.value) {
                    System.err.println("ERROR: wait() failed.");
                    return;
                }
            }

            ws.detach_condition(sc);
            MessageDataWriter mdw = MessageDataWriterHelper.narrow(this.dataWriter);
            Message msg = new Message();
            msg.subject_id = this.count;
            int handle = mdw.register_instance(msg);
            msg.from = this.settings.getClientId();
            msg.subject = topic;
            msg.text = message;
            msg.count = 1;
            this.count = this.numberOfMessages;
            int ret = RETCODE_TIMEOUT.value;


            addToBatch(msg, mdw, handle);
            /*
            for (; msg.count < amount; ++msg.count) {
                while ((ret = mdw.write(msg, handle)) == RETCODE_TIMEOUT.value) {
                }
                if (ret != RETCODE_OK.value) {
                    System.err.println("ERROR " + msg.count +
                            " write() returned " + ret);
                }
            }
             */

        Utils.checkValue(this.numberOfMessages, quarter, half, threeQuarters, arr, utilsID);

        quarter = arr[0];
        half = arr[1];
        threeQuarters = arr[2];

        if (numberOfMessages >= 100000) {
            Utils.pointReached(utilsID, log);
            this.numberOfMessages -= 100000;
            first = true;
            quarter = true;
            half = true;
            threeQuarters = true;
            Arrays.fill(arr, true);
        }
    }
}
