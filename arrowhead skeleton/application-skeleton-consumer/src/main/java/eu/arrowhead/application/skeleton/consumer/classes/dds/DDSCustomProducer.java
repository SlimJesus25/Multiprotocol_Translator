package eu.arrowhead.application.skeleton.consumer.classes.dds;

import DDS.*;
import Messenger.Message;
import Messenger.MessageDataWriter;
import Messenger.MessageDataWriterHelper;
import Messenger.MessageTypeSupportImpl;
import OpenDDS.DCPS.DEFAULT_STATUS_MASK;
import OpenDDS.DCPS.TheParticipantFactory;
import OpenDDS.DCPS.TheServiceParticipant;
import common.ConnectionDetails;
import common.IProducer;

import java.util.Map;

/**
 * @author : Ricardo Venâncio - 1210828
 **/
public class DDSCustomProducer extends IProducer implements DataWriter {

    private static int count = 0;
    private static final int N_MSGS = 40;
    private boolean reliable;
    public DDSCustomProducer(ConnectionDetails connectionDetails, Map<String, String> settings) {
        super(connectionDetails, settings);
    }

    @Override
    public void produce(String topic, String message) {

        String[] args = new String[1];

        // TODO: Ver o motivo pelo qual o new StringSeqHolder não é aceite.
        DomainParticipantFactory dpf = TheParticipantFactory.WithArgs(null);
        if(dpf == null){
            System.out.println("Error");
            return;
        }

        DomainParticipant dp = dpf.create_participant(4, PARTICIPANT_QOS_DEFAULT.get(), null, DEFAULT_STATUS_MASK.value);
        if(dp == null){
            System.out.println("Error");
            return;
        }

        MessageTypeSupportImpl servant = new MessageTypeSupportImpl();
        if (servant.register_type(dp, "") != RETCODE_OK.value) {
            System.err.println("ERROR: register_type failed");
            return;
        }
        Topic top = dp.create_topic("Movie Discussion List",
                servant.get_type_name(),
                TOPIC_QOS_DEFAULT.get(),
                null,
                DEFAULT_STATUS_MASK.value);
        if (top == null) {
            System.err.println("ERROR: Topic creation failed");
            return;
        }

        Publisher pub = dp.create_publisher(PUBLISHER_QOS_DEFAULT.get(), null,
                DEFAULT_STATUS_MASK.value);
        if (pub == null) {
            System.err.println("ERROR: Publisher creation failed");
            return;
        }

        // Use the default transport configuration (do nothing)

        DataWriterQos dw_qos = new DataWriterQos();
        dw_qos.durability = new DurabilityQosPolicy();
        dw_qos.durability.kind = DurabilityQosPolicyKind.from_int(0);
        dw_qos.durability_service = new DurabilityServiceQosPolicy();
        dw_qos.durability_service.history_kind = HistoryQosPolicyKind.from_int(0);
        dw_qos.durability_service.service_cleanup_delay = new Duration_t();
        dw_qos.deadline = new DeadlineQosPolicy();
        dw_qos.deadline.period = new Duration_t();
        dw_qos.latency_budget = new LatencyBudgetQosPolicy();
        dw_qos.latency_budget.duration = new Duration_t();
        dw_qos.liveliness = new LivelinessQosPolicy();
        dw_qos.liveliness.kind = LivelinessQosPolicyKind.from_int(0);
        dw_qos.liveliness.lease_duration = new Duration_t();
        dw_qos.reliability = new ReliabilityQosPolicy();
        dw_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(0);
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
        DataWriter dw = pub.create_datawriter(top,
                qosh.value,
                null,
                DEFAULT_STATUS_MASK.value);
        if (dw == null) {
            System.err.println("ERROR: DataWriter creation failed");
            return;
        }
        System.out.println("Publisher Created DataWriter");

        StatusCondition sc = dw.get_statuscondition();
        sc.set_enabled_statuses(PUBLICATION_MATCHED_STATUS.value);
        WaitSet ws = new WaitSet();
        ws.attach_condition(sc);
        PublicationMatchedStatusHolder matched =
                new PublicationMatchedStatusHolder(new PublicationMatchedStatus());
        Duration_t timeout = new Duration_t(DURATION_INFINITE_SEC.value,
                DURATION_INFINITE_NSEC.value);

        while (true) {
            final int result = dw.get_publication_matched_status(matched);
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
        // TODO: Ver o motivo pelo qual dw não é aceite.
        MessageDataWriter mdw = MessageDataWriterHelper.narrow(null);
        Message msg = new Message();
        msg.subject_id = 99;
        int handle = mdw.register_instance(msg);
        msg.from = "OpenDDS-Java";
        msg.subject = "Review";
        msg.text = "Worst. Movie. Ever.";
        msg.count = 0;
        int ret = RETCODE_TIMEOUT.value;
        for (; msg.count < N_MSGS; ++msg.count) {
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

        while (matched.value.current_count != 0) {
            final int result = mdw.get_publication_matched_status(matched);
            try {
                Thread.sleep(100);
            } catch(InterruptedException ie) {
            }
        }

        System.out.println("Stop Publisher");

        // Clean up
        dp.delete_contained_entities();
        dpf.delete_participant(dp);
        TheServiceParticipant.shutdown();

        System.out.println("Publisher exiting");

    }

    @Override
    public int set_qos(DataWriterQos dataWriterQos) {
        return 0;
    }

    @Override
    public int get_qos(DataWriterQosHolder dataWriterQosHolder) {
        return 0;
    }

    @Override
    public int set_listener(DataWriterListener dataWriterListener, int i) {
        return 0;
    }

    @Override
    public DataWriterListener get_listener() {
        return null;
    }

    @Override
    public Topic get_topic() {
        return null;
    }

    @Override
    public Publisher get_publisher() {
        return null;
    }

    @Override
    public int wait_for_acknowledgments(Duration_t durationT) {
        return 0;
    }

    @Override
    public int get_liveliness_lost_status(LivelinessLostStatusHolder livelinessLostStatusHolder) {
        return 0;
    }

    @Override
    public int get_offered_deadline_missed_status(OfferedDeadlineMissedStatusHolder offeredDeadlineMissedStatusHolder) {
        return 0;
    }

    @Override
    public int get_offered_incompatible_qos_status(OfferedIncompatibleQosStatusHolder offeredIncompatibleQosStatusHolder) {
        return 0;
    }

    @Override
    public int get_publication_matched_status(PublicationMatchedStatusHolder publicationMatchedStatusHolder) {
        return 0;
    }

    @Override
    public int assert_liveliness() {
        return 0;
    }

    @Override
    public int get_matched_subscriptions(InstanceHandleSeqHolder instanceHandleSeqHolder) {
        return 0;
    }

    @Override
    public int get_matched_subscription_data(SubscriptionBuiltinTopicDataHolder subscriptionBuiltinTopicDataHolder, int i) {
        return 0;
    }

    @Override
    public int enable() {
        return 0;
    }

    @Override
    public StatusCondition get_statuscondition() {
        return null;
    }

    @Override
    public int get_status_changes() {
        return 0;
    }

    @Override
    public int get_instance_handle() {
        return 0;
    }
}
