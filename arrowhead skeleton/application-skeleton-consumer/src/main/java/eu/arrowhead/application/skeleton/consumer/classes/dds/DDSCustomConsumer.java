package eu.arrowhead.application.skeleton.consumer.classes.dds;

import DDS.*;
import Messenger.MessageTypeSupportImpl;
import OpenDDS.DCPS.DEFAULT_STATUS_MASK;
import OpenDDS.DCPS.TheParticipantFactory;
import OpenDDS.DCPS.TheServiceParticipant;
import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;
import org.omg.CORBA.*;
import org.omg.CORBA.Object;
import org.omg.CORBA.StringSeqHolder;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author : Ricardo Venâncio - 1210828
 **/
public class DDSCustomConsumer extends IConsumer implements DataReader {


    private final String topic;
    private final int qos;
    private int count;
    private String[] args;

    public DDSCustomConsumer(ConnectionDetails connectionDetails, List<IProducer> producer, Map<String, String> settings) {
        super(connectionDetails, producer, settings);
        this.topic = settings.get("topic");
        this.qos = Integer.parseInt(settings.get("qos"));
        args = new String[6];
        args[0] = "-DCPSBit";
        args[1] = "0";
        args[2] = "-DCPSConfigFile";
        args[3] = "/home/ricardo/IdeaProjects/Multiprotocol_Translator/arrowhead skeleton/application-skeleton-consumer/src/main/java/eu/arrowhead/application/skeleton/consumer/classes/dds/tcp.ini";
        args[4] = "-r";
        args[5] = "-w";
        //args = "-DCPSBit 0 -DCPSConfigFile /home/ricardo/IdeaProjects/Multiprotocol_Translator/arrowhead skeleton/application-skeleton-consumer/src/main/java/eu/arrowhead/application/skeleton/consumer/classes/dds/tcp.ini -r -w".split("^d ^s");
        count = 1;
    }


    private void createConsumer(String topic){

        System.setProperty("java.library.path", "/home/ricardo/Downloads/OpenDDS-3.27/java/tests/messenger/messenger_idl");
        System.loadLibrary("OpenDDS_DCPS_Java");

        boolean reliable = this.qos != 0;

        DomainParticipantFactory dpf =
                TheParticipantFactory.WithArgs(new StringSeqHolder(this.args));
        if (dpf == null) {
            System.err.println("ERROR: Domain Participant Factory not found");
            return;
        }

        DomainParticipant dp = dpf.create_participant(this.count++,
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
        Topic top = dp.create_topic(this.topic,
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

        // Use the default transport (do nothing)

        DataReaderQos dr_qos = new DataReaderQos();
        dr_qos.durability = new DurabilityQosPolicy();
        dr_qos.durability.kind = DurabilityQosPolicyKind.from_int(0);
        dr_qos.deadline = new DeadlineQosPolicy();
        dr_qos.deadline.period = new Duration_t();
        dr_qos.latency_budget = new LatencyBudgetQosPolicy();
        dr_qos.latency_budget.duration = new Duration_t();
        dr_qos.liveliness = new LivelinessQosPolicy();
        dr_qos.liveliness.kind = LivelinessQosPolicyKind.from_int(0);
        dr_qos.liveliness.lease_duration = new Duration_t();
        dr_qos.reliability = new ReliabilityQosPolicy();
        dr_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(0);
        dr_qos.reliability.max_blocking_time = new Duration_t();
        dr_qos.destination_order = new DestinationOrderQosPolicy();
        dr_qos.destination_order.kind = DestinationOrderQosPolicyKind.from_int(0);
        dr_qos.history = new HistoryQosPolicy();
        dr_qos.history.kind = HistoryQosPolicyKind.from_int(0);
        dr_qos.resource_limits = new ResourceLimitsQosPolicy();
        dr_qos.user_data = new UserDataQosPolicy();
        dr_qos.user_data.value = new byte[0];
        dr_qos.ownership = new OwnershipQosPolicy();
        dr_qos.ownership.kind = OwnershipQosPolicyKind.from_int(0);
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

        DataReaderListenerImpl listener = new DataReaderListenerImpl(this);

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
        /*
        System.out.println("Stop Subscriber");

        dp.delete_contained_entities();
        dpf.delete_participant(dp);
        TheServiceParticipant.shutdown();

        System.out.println("Subscriber exiting");
         */

    }


    @Override
    public void run() {
        createConsumer(this.topic);
    }

    @Override
    public ReadCondition create_readcondition(int i, int i1, int i2) {
        return null;
    }

    @Override
    public QueryCondition create_querycondition(int i, int i1, int i2, String s, String[] strings) {
        return null;
    }

    @Override
    public int delete_readcondition(ReadCondition readCondition) {
        return 0;
    }

    @Override
    public int delete_contained_entities() {
        return 0;
    }

    @Override
    public int set_qos(DataReaderQos dataReaderQos) {
        return 0;
    }

    @Override
    public int get_qos(DataReaderQosHolder dataReaderQosHolder) {
        return 0;
    }

    @Override
    public int set_listener(DataReaderListener dataReaderListener, int i) {
        return 0;
    }

    @Override
    public DataReaderListener get_listener() {
        return null;
    }

    @Override
    public TopicDescription get_topicdescription() {
        return null;
    }

    @Override
    public Subscriber get_subscriber() {
        return null;
    }

    @Override
    public int get_sample_rejected_status(SampleRejectedStatusHolder sampleRejectedStatusHolder) {
        return 0;
    }

    @Override
    public int get_liveliness_changed_status(LivelinessChangedStatusHolder livelinessChangedStatusHolder) {
        return 0;
    }

    @Override
    public int get_requested_deadline_missed_status(RequestedDeadlineMissedStatusHolder requestedDeadlineMissedStatusHolder) {
        return 0;
    }

    @Override
    public int get_requested_incompatible_qos_status(RequestedIncompatibleQosStatusHolder requestedIncompatibleQosStatusHolder) {
        return 0;
    }

    @Override
    public int get_subscription_matched_status(SubscriptionMatchedStatusHolder subscriptionMatchedStatusHolder) {
        return 0;
    }

    @Override
    public int get_sample_lost_status(SampleLostStatusHolder sampleLostStatusHolder) {
        return 0;
    }

    @Override
    public int wait_for_historical_data(Duration_t durationT) {
        return 0;
    }

    @Override
    public int get_matched_publications(InstanceHandleSeqHolder instanceHandleSeqHolder) {
        return 0;
    }

    @Override
    public int get_matched_publication_data(PublicationBuiltinTopicDataHolder publicationBuiltinTopicDataHolder, int i) {
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
