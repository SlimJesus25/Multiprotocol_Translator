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
import eu.arrowhead.application.skeleton.consumer.classes.Constants;
import eu.arrowhead.application.skeleton.consumer.classes.PubSubSettings;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.omg.CORBA.*;
import org.omg.CORBA.Object;
import org.omg.CORBA.StringSeqHolder;

import java.util.Map;

/**
 * @author : Ricardo Venâncio - 1210828
 **/
public class DDSCustomProducer extends IProducer implements DataWriter {

    private static final int N_MSGS = 40;
    private final ProducerConfig producerConfig;
    private final PubSubSettings settings;
    private DataWriter dataWriter;
    private DomainParticipant domainParticipant;
    private DomainParticipantFactory domainParticipantFactory;
    private String[] args;
    private int count;

    public DDSCustomProducer(ConnectionDetails connectionDetails, Map<String, String> settings) {
        super(connectionDetails, settings);
        this.settings = new PubSubSettings(settings);
        this.producerConfig = new ProducerConfig(Constants.objectifyMap(settings));
        this.args = "-DCPSBit 0 - DCPSConfigFile tcp.ini -r -w -DCPSPendingTimeout 3".split(" ");
        count = 1;
    }

    private void createProducer(String topic){
        Map<String, java.lang.Object> config = producerConfig.originals();

        ConnectionDetails cd = this.getConnectionDetails();
        config.put("bootstrap.servers", cd.getAddress() + ":" + cd.getPort());

        this.domainParticipantFactory = TheParticipantFactory.WithArgs(new StringSeqHolder(this.args));
        if(this.domainParticipantFactory == null){
            System.out.println("Error");
            return;
        }

        this.domainParticipant = this.domainParticipantFactory.create_participant(this.count++, PARTICIPANT_QOS_DEFAULT.get(),
                null, DEFAULT_STATUS_MASK.value);
        if(this.domainParticipant == null){
            System.out.println("Error");
            return;
        }

        MessageTypeSupportImpl servant = new MessageTypeSupportImpl();
        if (servant.register_type(this.domainParticipant, "") != RETCODE_OK.value) {
            System.err.println("ERROR: register_type failed");
            return;
        }

        Topic top = this.domainParticipant.create_topic(topic,
                servant.get_type_name(),
                TOPIC_QOS_DEFAULT.get(),
                null,
                DEFAULT_STATUS_MASK.value);
        if (top == null) {
            System.err.println("ERROR: Topic creation failed");
            return;
        }

        Publisher pub = this.domainParticipant.create_publisher(PUBLISHER_QOS_DEFAULT.get(), null,
                DEFAULT_STATUS_MASK.value);
        if (pub == null) {
            System.err.println("ERROR: Publisher creation failed");
            return;
        }

        DataWriterQos dw_qos = new DataWriterQos();
        dw_qos.deadline = new DeadlineQosPolicy();

        /*
        TODO: Para já, o 'At least once' e o 'Exactly once' estão com as mesmas configurações, contudo ainda é preciso
          analisar se é possível fazer o mapeamento, e como.
         */

        boolean reliable = true;

        if(settings.getQos() == 0){
            dw_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._BEST_EFFORT_RELIABILITY_QOS);
            reliable = false;
        }else if(settings.getQos() == 1){
            dw_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._RELIABLE_RELIABILITY_QOS);
            dw_qos.deadline.period = new Duration_t(5, 0);
        }else{
            dw_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._RELIABLE_RELIABILITY_QOS);
            dw_qos.deadline.period = new Duration_t(5, 0);
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
        dw_qos.reliability = new ReliabilityQosPolicy();
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

        this.dataWriter = pub.create_datawriter(top,
                qosh.value,
                null,
                DEFAULT_STATUS_MASK.value);
        if (this.dataWriter == null) {
            System.err.println("ERROR: DataWriter creation failed");
            return;
        }
        System.out.println("Publisher Created DataWriter");

    }

    @Override
    public void produce(String topic, String message) {

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
        MessageDataWriter mdw = MessageDataWriterHelper.narrow(this.dataWriter);
        Message msg = new Message();
        msg.subject_id = 99;
        int handle = mdw.register_instance(msg);
        msg.from = "OpenDDS-Java";
        msg.subject = "Review";
        msg.text = message;
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
        this.domainParticipant.delete_contained_entities();
        this.domainParticipantFactory.delete_participant(this.domainParticipant);
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

    @Override
    public boolean _is_a(String s) {
        return false;
    }

    @Override
    public boolean _is_equivalent(Object object) {
        return false;
    }

    @Override
    public boolean _non_existent() {
        return false;
    }

    @Override
    public int _hash(int i) {
        return 0;
    }

    @Override
    public Object _duplicate() {
        return null;
    }

    @Override
    public void _release() {

    }

    @Override
    public Object _get_interface_def() {
        return null;
    }

    @Override
    public Request _request(String s) {
        return null;
    }

    @Override
    public Request _create_request(Context context, String s, NVList nvList, NamedValue namedValue) {
        return null;
    }

    @Override
    public Request _create_request(Context context, String s, NVList nvList, NamedValue namedValue, ExceptionList exceptionList, ContextList contextList) {
        return null;
    }

    @Override
    public Policy _get_policy(int i) {
        return null;
    }

    @Override
    public DomainManager[] _get_domain_managers() {
        return new DomainManager[0];
    }

    @Override
    public Object _set_policy_override(Policy[] policies, SetOverrideType setOverrideType) {
        return null;
    }
}
