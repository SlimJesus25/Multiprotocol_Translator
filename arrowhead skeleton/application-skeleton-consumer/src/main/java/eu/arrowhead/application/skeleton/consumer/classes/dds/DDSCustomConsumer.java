package eu.arrowhead.application.skeleton.consumer.classes.dds;

import DDS.*;
import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;
import java.util.List;
import java.util.Map;

/**
 * @author : Ricardo Venâncio - 1210828
 **/
public class DDSCustomConsumer extends IConsumer implements DataReader {

    private ReadCondition readCondition;
    private QueryCondition queryCondition;
    private DataReaderQos qos;
    private DataReaderListener dataReaderListener;
    private TopicDescription topicDescription;
    private Subscriber subscriber;

    public DDSCustomConsumer(ConnectionDetails connectionDetails, List<IProducer> producer, Map<String, String> settings) {
        super(connectionDetails, producer, settings);
    }

    @Override
    public void run() {

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
}
