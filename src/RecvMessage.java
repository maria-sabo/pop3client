import java.util.ArrayList;

public class RecvMessage {

    private ArrayList<HeaderPair> headers;
    private String body;
    private int number;
    private long size;

    public RecvMessage(ArrayList<HeaderPair> headers, String body, int number) {
        this.headers = headers;
        this.body = body;
        this.number = number;
    }

    public RecvMessage(int number, long size) {
        this.number = number;
        this.size = size;
    }

    public ArrayList<HeaderPair> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public int getNumber() {
        return number;
    }

    public long getSize() {
        return size;
    }

    public void setHeaders(ArrayList<HeaderPair> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public static class HeaderPair {
        private String header;
        private String headerBody;

        public HeaderPair(String header, String headerBody) {
            this.header = header;
            this.headerBody = headerBody;
        }

        public String getHeader() {
            return header;
        }

        public String getHeaderBody() {
            return headerBody;
        }
    }
}