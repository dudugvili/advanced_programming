package test.unit_tests;

import test.Agent;
import test.Message;
import test.Topic;
import test.TopicManagerSingleton;
import test.TopicManagerSingleton.TopicManager;
import test.ParallelAgent;
import java.util.Random;

public class ExtendedMainTrainEx2 {

    static String tn = null;

    public static class TestAgent1 implements Agent {
        double sum = 0;
        int count = 0;

        public void reset() {
            sum = 0;
            count = 0;
        }

        public void close() {}

        public String getName() {
            return getClass().getName();
        }

        @Override
        public void callback(String topic, Message msg) {
            tn = Thread.currentThread().getName();
            count++;
            sum += msg.asDouble;
            if (count % 5 == 0) {
                TopicManagerSingleton.get().getTopic("Sum").publish(new Message(sum));
                count = 0;
                sum = 0; // Reset sum after publishing
            }
        }

        public double getSum() {
            return sum;
        }

        public int getCount() {
            return count;
        }
    }

    public static class TestAgent2 implements Agent {
        public void reset() {}

        public void close() {}

        public String getName() {
            return getClass().getName();
        }

        @Override
        public void callback(String topic, Message msg) {
            if (Double.isNaN(msg.asDouble)) {
                System.out.println("Failure with the cause of: Received NaN value in TestAgent2 callback");
            }
        }
    }

    public static void main(String[] args) {
        TopicManager tm = TopicManagerSingleton.get();
        int tc = Thread.activeCount();
        ParallelAgent pa = new ParallelAgent(new TestAgent1(), 10);
        tm.getTopic("A").subscribe(pa);

        if (Thread.activeCount() != tc + 1) {
            System.out.println("your ParallelAgent does not open a thread (-10)");
        }
        else {
            System.out.println("your ParallelAgent opens a thread (+10)");
        }

        tm.getTopic("A").publish(new Message("a"));
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        if (tn == null) {
            System.out.println("your ParallelAgent didn't run the wrapped agent callback (-20)");
        } else {
            if (tn.equals(Thread.currentThread().getName())) {
                System.out.println("the ParallelAgent does not run the wrapped agent in a different thread (-10)");
            }
            else {
                System.out.println("the ParallelAgent runs the wrapped agent in a different thread (+10)");
            }

            String last = tn;
            tm.getTopic("A").publish(new Message("a"));
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            if (!last.equals(tn)) {
                System.out.println("all messages should be processed in the same thread of ParallelAgent (-10)");
            }
            else {
                System.out.println("all messages are processed in the same thread of ParallelAgent (+10)");
            }
        }

        // Additional tests for comprehensive coverage

        // Test message parsing with various values
        testMessageParsing();

        // Test agent subscription, unsubscription, reset, and close
        testAgentSubscription();

        // Test ParallelAgent with high-frequency messages and edge cases
        testParallelAgent();

        pa.close();
        System.out.println("done");
    }

    private static void testMessageParsing() {
        try {
            Message validDoubleMessage = new Message(123.45);
            if (validDoubleMessage.asDouble != 123.45) {
                System.out.println("Failure with the cause of: Valid double message parsing");
            } else {
                System.out.println("SUCCESS: Valid double message parsing");
            }

            Message invalidDoubleMessage = new Message("NotANumber");
            if (!Double.isNaN(invalidDoubleMessage.asDouble)) {
                System.out.println("Failure with the cause of: Invalid double message parsing");
            } else {
                System.out.println("SUCCESS: Invalid double message parsing");
            }

            Message nanDoubleMessage = new Message(Double.NaN);
            if (!Double.isNaN(nanDoubleMessage.asDouble)) {
                System.out.println("Failure with the cause of: NaN double message parsing");
            } else {
                System.out.println("SUCCESS: NaN double message parsing");
            }

            // Edge cases: extremely large and small double values
            Message largeDoubleMessage = new Message(Double.MAX_VALUE);
            if (largeDoubleMessage.asDouble != Double.MAX_VALUE) {
                System.out.println("Failure with the cause of: Large double message parsing");
            } else {
                System.out.println("SUCCESS: Large double message parsing");
            }

            Message smallDoubleMessage = new Message(Double.MIN_VALUE);
            if (smallDoubleMessage.asDouble != Double.MIN_VALUE) {
                System.out.println("Failure with the cause of: Small double message parsing");
            } else {
                System.out.println("SUCCESS: Small double message parsing");
            }

            Message negativeDoubleMessage = new Message(-123.45);
            if (negativeDoubleMessage.asDouble != -123.45) {
                System.out.println("Failure with the cause of: Negative double message parsing");
            } else {
                System.out.println("SUCCESS: Negative double message parsing");
            }
        } catch (Exception e) {
            System.out.println("Failure with the cause of: Exception in message parsing tests");
        }
    }

    private static void testAgentSubscription() {
        try {
            // Create agents
            TestAgent1 agent1 = new TestAgent1();
            TestAgent2 agent2 = new TestAgent2();

            // Get topic manager and topics
            TopicManager tm = TopicManagerSingleton.get();
            Topic numbersTopic = tm.getTopic("Numbers");
            Topic sumTopic = tm.getTopic("Sum");

            // Subscribe agent2 to Numbers and Sum topics
            numbersTopic.subscribe(agent2);
            sumTopic.subscribe(agent2);

            // Subscribe the same agent multiple times (should handle gracefully)
            numbersTopic.subscribe(agent2);

            // Publish some messages to "Numbers" topic
            for (int i = 0; i < 10; i++) {
                double value = new Random().nextDouble() * 10;
                numbersTopic.publish(new Message(value));
            }

            // Unsubscribe agent2 and publish more messages
            numbersTopic.unsubscribe(agent2);
            for (int i = 0; i < 5; i++) {
                double value = new Random().nextDouble() * 10;
                numbersTopic.publish(new Message(value));
            }

            // Unsubscribe an agent that was never subscribed (should handle gracefully)
            numbersTopic.unsubscribe(agent2);

            // Test reset method
            agent1.reset();
            if (agent1.getSum() != 0 || agent1.getCount() != 0) {
                System.out.println("Failure with the cause of: Agent reset method");
            }

            // Test close method
            agent1.close();
            try {
                agent1.close(); // Close an already closed agent (should handle gracefully)
            } catch (Exception e) {
                System.out.println("Failure with the cause of: Exception in agent close method");
            }

        } catch (Exception e) {
            System.out.println("Failure with the cause of: Exception in agent subscription tests");
        }
    }

    private static void testParallelAgent() {
        try {
            // Create agents
            TestAgent1 agent1 = new TestAgent1();
            ParallelAgent parallelAgent = new ParallelAgent(agent1, 10);

            // Get topic manager and topics
            TopicManager tm = TopicManagerSingleton.get();
            Topic numbersTopic = tm.getTopic("Numbers");

            // Subscribe parallel agent to Numbers topic
            numbersTopic.subscribe(parallelAgent);

            // Publish some messages to "Numbers" topic
            for (int i = 0; i < 10; i++) {
                double value = new Random().nextDouble() * 10;
                numbersTopic.publish(new Message(value));
            }

            // High-frequency message publishing
            for (int i = 0; i < 1000; i++) {
                double value = new Random().nextDouble() * 10;
                numbersTopic.publish(new Message(value));
            }

            // Ensure messages are processed in the correct order
            for (int i = 0; i < 10; i++) {
                double value = i * 1.0;
                numbersTopic.publish(new Message(value));
            }

            // Close parallel agent
            parallelAgent.close();

        } catch (Exception e) {
            System.out.println("Failure with the cause of: Exception in ParallelAgent tests");
        }
    }
}
