package test.unit_tests;

import test.Agent;
import test.Message;
import test.Topic;
import test.TopicManagerSingleton;
import test.TopicManagerSingleton.TopicManager;
import java.util.Random;

public class ExtendedMainTrainEx1 {

    public static void testMessage() {
        // Test String constructor
        String testString = "Hello";
        Message msgFromString = new Message(testString);
        if (!testString.equals(msgFromString.asText)) {
            System.out.println("Failure with the cause of: String constructor - asText does not match input string");
        }
        if (!java.util.Arrays.equals(testString.getBytes(), msgFromString.data)) {
            System.out.println("Failure with the cause of: String constructor - data does not match input string bytes");
        }
        if (!Double.isNaN(msgFromString.asDouble)) {
            System.out.println("Failure with the cause of: String constructor - asDouble should be NaN for non-numeric string");
        }
        if (msgFromString.date == null) {
            System.out.println("Failure with the cause of: String constructor - date should not be null");
        }
    }

    public static abstract class AAgent implements Agent {
        public void reset() {}
        public void close() {}
        public String getName() {
            return getClass().getName();
        }
    }

    public static class TestAgent1 extends AAgent {
        double sum = 0;
        int count = 0;
        TopicManager tm = TopicManagerSingleton.get();

        public TestAgent1() {
            tm.getTopic("Numbers").subscribe(this);
        }

        @Override
        public void callback(String topic, Message msg) {
            count++;
            sum += msg.asDouble;

            if (count % 5 == 0) {
                tm.getTopic("Sum").publish(new Message(sum));
                count = 0;
                sum = 0; // Reset sum after publishing
            }
        }
    }

    public static class TestAgent2 extends AAgent {
        public void callback(String topic, Message msg) {
            if (Double.isNaN(msg.asDouble)) {
                System.out.println("Failure with the cause of: Received NaN value in TestAgent2 callback");
            }
        }
    }

    public static void main(String[] args) {
        boolean allTestsPassed = true;

        // Test message parsing and construction
        testMessage();

        // Test with valid and invalid double values
        try {
            Message validDoubleMessage = new Message(123.45);
            if (validDoubleMessage.asDouble != 123.45) {
                System.out.println("Failure with the cause of: Valid double message parsing");
                allTestsPassed = false;
            }
            Message invalidDoubleMessage = new Message("NotANumber");
            if (!Double.isNaN(invalidDoubleMessage.asDouble)) {
                System.out.println("Failure with the cause of: Invalid double message parsing");
                allTestsPassed = false;
            }
            Message nanDoubleMessage = new Message(Double.NaN);
            if (!Double.isNaN(nanDoubleMessage.asDouble)) {
                System.out.println("Failure with the cause of: NaN double message parsing");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("Failure with the cause of: Exception in message parsing tests");
            allTestsPassed = false;
        }

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

        // Publishing some messages to "Numbers" topic
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            double value = rand.nextDouble() * 10;
            numbersTopic.publish(new Message(value));
        }

        // Edge case: Publishing to a topic with no subscribers
        try {
            Topic emptyTopic = tm.getTopic("Empty");
            emptyTopic.publish(new Message("This should not be received by anyone"));
        } catch (Exception e) {
            System.out.println("Failure with the cause of: Exception in empty topic publishing");
            allTestsPassed = false;
        }

        // Unsubscribe agent2 and publish more messages
        try {
            numbersTopic.unsubscribe(agent2);
            for (int i = 0; i < 5; i++) {
                double value = rand.nextDouble() * 10;
                numbersTopic.publish(new Message(value));
            }
        } catch (Exception e) {
            System.out.println("Failure with the cause of: Exception in unsubscribe publishing");
            allTestsPassed = false;
        }

        // Additional edge cases
        try {
            // Re-subscribe agent2 and publish more messages
            numbersTopic.subscribe(agent2);
            for (int i = 0; i < 5; i++) {
                double value = rand.nextDouble() * 10;
                numbersTopic.publish(new Message(value));
            }

            // Check multiple agents subscribing to the same topic
            TestAgent1 agent3 = new TestAgent1();
            numbersTopic.subscribe(agent3);
            for (int i = 0; i < 5; i++) {
                double value = rand.nextDouble() * 10;
                numbersTopic.publish(new Message(value));
            }
        } catch (Exception e) {
            System.out.println("Failure with the cause of: Exception in additional edge cases");
            allTestsPassed = false;
        }

        // Final output
        if (allTestsPassed) {
            System.out.println("Success");
        } else {
            System.out.println("Failure");
        }
    }
}
