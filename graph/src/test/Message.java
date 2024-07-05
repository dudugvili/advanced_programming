package test;

import java.util.Date;

public class Message {
    public final byte[] data;
    public final String asText;
    public final double asDouble;
    public final Date date;

    public Message(byte[] data) {
        this.data = data;
        this.asText = new String(data);
        this.asDouble = toDouble(this.asText);
        this.date = new Date();
    }

    public Message(String text) {
        this(text.getBytes());
    }

    public Message(double value) {
        this(String.valueOf(value));
    }

    private double toDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
}


    


