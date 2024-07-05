package test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ParallelAgent implements Agent {
    private Agent agent;
    private BlockingQueue<Message> messageQueue;
    private Thread workerThread;

    public ParallelAgent(Agent agent, int capacity) {
        this.agent = agent;
        this.messageQueue = new ArrayBlockingQueue<>(capacity);
        workerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Message message = messageQueue.take();
                    agent.callback(message.getTopic(), message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        workerThread.start();
    }

    @Override
    public void callback(String topic, Message msg) {
        msg.setTopic(topic);
        try {
            messageQueue.put(msg);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void reset() {
        agent.reset();
    }

    @Override
    public String getName() {
        return agent.getName();
    }

    @Override
    public void close() {
        workerThread.interrupt();
        try {
            workerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
