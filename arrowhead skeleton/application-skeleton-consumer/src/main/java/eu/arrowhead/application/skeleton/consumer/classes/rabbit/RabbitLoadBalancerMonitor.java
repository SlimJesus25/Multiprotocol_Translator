package eu.arrowhead.application.skeleton.consumer.classes.rabbit;

import com.rabbitmq.client.AMQP;

import java.util.ArrayList;
import java.util.List;

public class RabbitLoadBalancerMonitor implements Runnable {

    private List<RabbitCustomConsumer> consumerList = new ArrayList<>();

    private AMQP.Queue.DeclareOk queue;

    private List<Integer> ola;


    public RabbitLoadBalancerMonitor(AMQP.Queue.DeclareOk queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        checkStatus();
    }

    private void checkStatus() {
        try {
            Thread.sleep(5000);

            RabbitCustomConsumer a = consumerList.get(0);
            System.out.println("Real - " + queue.getConsumerCount());
            if (queue.getMessageCount() > 70000 && queue.getConsumerCount() < 5) {
                RabbitCustomConsumer b = new RabbitCustomConsumer(a.getConnectionDetails(),a.getProducerList(),a.getSettings(),false);
                new Thread(b).start();
                consumerList.add(b);
            }



            checkStatus();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public void addConsumer(RabbitCustomConsumer consumer) {
        consumerList.add(consumer);
    }
}
