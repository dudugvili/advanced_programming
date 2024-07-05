package test.unit_tests;

import java.util.Arrays;
import test.Message;

public class MessageUnitTest {
    public static void main(String[] args) {
        testMessageWithData();
        testMessageWithText();
        testMessageWithValue();
    }

    public static void testMessageWithData() {
        byte[] data = { 65, 66, 67 };
        Message message = new Message(data);

        // Verify data
        assert Arrays.equals(message.data, data);

        // Verify asText
        assert message.asText.equals("ABC");

        // Verify asDouble
        assert message.asDouble == Double.NaN;

        // Verify date
        assert message.date != null;

        System.out.println("testMessageWithData passed");
    }

    public static void testMessageWithText() {
        String text = "Hello, World!";
        Message message = new Message(text);

        // Verify data
        assert Arrays.equals(message.data, text.getBytes());

        // Verify asText
        assert message.asText.equals(text);

        // Verify asDouble
        assert message.asDouble == Double.NaN;

        // Verify date
        assert message.date != null;

        System.out.println("testMessageWithText passed");
    }

    public static void testMessageWithValue() {
        double value = 3.14;
        Message message = new Message(value);

        // Verify data
        assert Arrays.equals(message.data, String.valueOf(value).getBytes());

        // Verify asText
        assert message.asText.equals(String.valueOf(value));

        // Verify asDouble
        assert message.asDouble == value;

        // Verify date
        assert message.date != null;

        System.out.println("testMessageWithValue passed");
    }
}