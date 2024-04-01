package eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase;

public class ExactlyOnceProducerHelper {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final JavaRepository idStore = new JavaRepository();

    public ExactlyOnceProducerHelper() {

    }

    public String generateIdentifier() {
        String alphanumeric = convertToAlphanumeric(idStore.getSize(),String.valueOf(idStore.getSize()).length() + 1);
        idStore.registerNewMessage(alphanumeric);
        return alphanumeric;
    }

    private String convertToAlphanumeric(int value, int length) {
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            int remainder = value % ALPHABET.length();
            char character = ALPHABET.charAt(remainder);
            sb.insert(0, character);
            value /= ALPHABET.length();
        }
        while (sb.length() < length) {
            sb.insert(0, ALPHABET.charAt(0));
        }
        return sb.toString();
    }
}
