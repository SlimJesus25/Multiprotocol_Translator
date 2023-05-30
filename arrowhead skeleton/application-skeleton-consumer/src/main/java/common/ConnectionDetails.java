package common;

public class ConnectionDetails {

    private String address;

    private int port;

    public ConnectionDetails(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "Address - " + address + ":" + port;
    }



}
