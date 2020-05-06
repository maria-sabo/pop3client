import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RecvMessage {

    private ArrayList<HeaderPair> headers;
    private String body;
    private int number;
    private long size;
    private int id;
    private boolean markToDel;

    public RecvMessage(ArrayList<HeaderPair> headers, String body, int number, long size, int id) {
        this.headers = headers;
        this.body = body;
        this.number = number;
        this.size = size;
        this.id = id;
    }

    public RecvMessage(ArrayList<HeaderPair> headers, String body, int number) {
        this.headers = headers;
        this.body = body;
        this.number = number;
    }

    public RecvMessage(int number, long size) {
        this.number = number;
        this.size = size;
    }

    public RecvMessage(boolean markToDel) {
        this.markToDel = markToDel;
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

    public int getId() {
        return id;
    }

    public boolean getMarkToDel() {
        return markToDel;
    }

    public void setHeaders(ArrayList<HeaderPair> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMarkToDel(boolean markToDel) {
        this.markToDel = markToDel;
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