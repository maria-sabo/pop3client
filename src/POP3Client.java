import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class POP3Client {
    private Socket socket;
    private boolean debug = false;

    private BufferedReader reader;
    private BufferedWriter writer;
    private ArrayList<RecvMessage> listOfMessages = new ArrayList<>();

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void connect(String host, int port) throws IOException {
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port));
            socket.setReceiveBufferSize(22000000);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            if (debug)
                System.out.println("Socket created. Connected to the host");
            readResponseLine();
        } catch (IOException ex) {
            System.out.println("Connection error");
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void disconnect() throws IOException {
        if (!isConnected())
            throw new IllegalStateException("Not connected to a host");
        socket.close();
        reader = null;
        writer = null;
        if (debug)
            System.out.println("Disconnected from the host");
    }

    protected String readResponseLine() throws IOException {
        String response = reader.readLine();

        if (debug) {
            System.out.println("server: " + response);
        }
        if (response.startsWith("-ERR")) {
            System.out.println("Server has returned an error: " + response.replaceFirst("-ERR ", ""));
        }
        return response;
    }

    protected String sendCommand(String command) throws IOException {
        if (debug) {
            System.out.println("send: " + command);
        }
        writer.write(command + "\n");
        writer.flush();
        return readResponseLine();
    }

    public void login(String login, String password) throws IOException {
        if (sendCommand("USER " + login).startsWith("Server has returned an error")
                || sendCommand("PASS " + password).startsWith("Server has returned an error"))
            System.out.println("Failed to log in");
        else System.out.println("Log in successful");
    }

    public void logout() throws IOException {
        if (sendCommand("QUIT").startsWith("Server has returned an error"))
            System.out.println("Failed to logout");
    }

    private int getNumberOfMsgs() throws IOException {
        return Integer.parseInt(sendCommand("STAT").split(" ")[1]);
    }

    public void cmdStat() throws IOException {
        String response = sendCommand("STAT");
        System.out.println("Number of messages in your mailbox: " + Integer.parseInt(response.split(" ")[1]));
        System.out.println("Total length: " + Integer.parseInt(response.split(" ")[2]));
    }

    private ArrayList<Integer> getListOfNumbers() throws IOException {
        ArrayList<Integer> list = new ArrayList<>();
        String response;
        sendCommand("LIST");
        while (!((response = readResponseLine()).equals("."))) {
            list.add(Integer.parseInt(response.split(" ")[0]));
        }
        return list;
    }

    public void cmdList() throws IOException {
        String response;
        sendCommand("LIST");
        while (!((response = readResponseLine()).equals("."))) {
            System.out.println(response);
        }
    }

    private RecvMessage retrieveMsg(int num) throws IOException {
        ArrayList<RecvMessage.HeaderPair> headers = new ArrayList<>();
        sendCommand("RETR " + num);
        String value = "", header = "", response;
        boolean body_f = false;
        StringBuilder body = new StringBuilder();

        while (!(response = readResponseLine()).equals(".")) {
            if (response.startsWith("  ") || response.startsWith("\t")) {
                value += response;
            }
            if (response.contains(":") && !(response.startsWith("  ") || response.startsWith("\t"))) {
                RecvMessage.HeaderPair hp = new RecvMessage.HeaderPair(header, value);
                headers.add(hp);
                int pos = response.indexOf(":");
                header = response.substring(0, pos);
                value = response.substring(pos + 1);
            }
            if (body_f) {
                body.append(response).append("\n");
            }
            if (response.startsWith("Content-Type: text/plain")) {
                body_f = true;
            }
        }
        RecvMessage.HeaderPair hp = new RecvMessage.HeaderPair(header, value);
        headers.add(hp);
        headers.remove(0);

        for (RecvMessage message : listOfMessages) {
            if (message.getNumber() == num) {
                message.setHeaders(headers);
                message.setBody(body.toString());
            } else {
                message = new RecvMessage(headers, body.toString(), num);
                listOfMessages.add(message);
            }
            return message;
        }
        RecvMessage msg;
        if (listOfMessages.size() == 0) {
            msg = new RecvMessage(headers, body.toString(), num);
            listOfMessages.add(msg);
            return msg;
        }
        return null;
    }

    private void printMsg(RecvMessage msg) {
        System.out.println("Message number: " + msg.getNumber());
        for (RecvMessage.HeaderPair hp : msg.getHeaders()) {
            System.out.println(hp.getHeader() + ":" + hp.getHeaderBody());
        }
        System.out.println(msg.getBody());
    }


    public void cmdRetrieve(BufferedReader br) throws IOException {
        int num, numAll;
        System.out.println("Enter message number: ");
        try {
            num = Integer.parseInt(br.readLine());
            numAll = getNumberOfMsgs();
            if (num > numAll || num < 0) {
                System.out.println("A message with this number does not exist\n");
            } else printMsg(retrieveMsg(num));
        } catch (
                NumberFormatException ex) {
            System.out.println("A wrong integer was entered\n");
        }
    }


    public void cmdTop(BufferedReader br) throws IOException {
        int num = 0, numAll, count = 0;
        String response;
        System.out.println("Enter message number: ");
        try {
            num = Integer.parseInt(br.readLine());
            numAll = getNumberOfMsgs();
            if (num > numAll || num < 1) {
                System.out.println("A message with this number does not exist\n");
            }
        } catch (NumberFormatException ex) {
            System.out.println("A wrong integer was entered\n");
        }
        System.out.println("Enter count of lines would you like: ");
        try {
            count = Integer.parseInt(br.readLine());
        } catch (NumberFormatException ex) {
            System.out.println("A wrong integer was entered\n");
        }
        sendCommand("TOP " + num + " " + count);
        while (!(response = readResponseLine()).equals(".")) {
            System.out.println(response);
        }
    }

    public void cmdUidl(BufferedReader br) throws IOException {
        int num = 0, numAll;
        String response;
        System.out.println("Enter message number (0 for get all identifiers): ");
        try {
            num = Integer.parseInt(br.readLine());
            numAll = getNumberOfMsgs();
            if (num > numAll || num < 0) {
                System.out.println("A message with this number does not exist\n");
            } else {
                if (num == 0) {
                    sendCommand("UIDL");
                    while (!(response = readResponseLine()).equals(".")) {
                        System.out.println(response);
                    }
                } else {
                    System.out.println(((sendCommand("UIDL " + num)).split(" "))[1] + " " + ((sendCommand("UIDL " + num)).split(" "))[2]);
                }
            }
        } catch (NumberFormatException ex) {
            System.out.println("A wrong integer was entered\n");
        }
    }

    public void listOfNewMessages(String host, int port, String login, String password) throws IOException {
        disconnect();
        connect(host, port);
        login(login, password);
        String response;
        sendCommand("LIST");
        ArrayList<ShortMessage> listOfNewMsg = new ArrayList<>();
        while (!(response = readResponseLine()).equals(".")) {
            int num = Integer.parseInt(response.split(" ")[0]);
            long size = Long.parseLong(response.split(" ")[1]);
            boolean found = false;
            for (RecvMessage el : listOfMessages) {
                if (((el.getNumber() == num) && (el.getSize() == size))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                ShortMessage msg = new ShortMessage(num, size);
                RecvMessage full_msg = new RecvMessage(num, size);
                listOfNewMsg.add(msg);
                listOfMessages.add(full_msg);
            }
        }
        long size_all = 0;
        for (ShortMessage el : listOfNewMsg) {
            System.out.println(el.getNumber() + " " + el.getSize());
            size_all += el.getSize();
        }
        if (listOfNewMsg.size() == 0)
            System.out.println("No new messages");
        System.out.println("Number of new messages: " + listOfNewMsg.size());
        System.out.println("Summary size: " + size_all);
    }


    public void cmdMarkMsgsDel(BufferedReader br) throws IOException {
        System.out.println("Enter the numbers of letters separated by a space to delete: ");
        String[] msgsForDelete = br.readLine().split(" ");
        ArrayList<Integer> nums = new ArrayList<>();
        try {
            for (String msg : msgsForDelete) {
                nums.add(Integer.parseInt(msg));
            }
            ArrayList<Integer> list = getListOfNumbers();
            for (int num : nums) {
                if (!list.contains(num)) {
                    System.out.println("You have no message number: " + num);
                } else sendCommand("DELE " + num);
            }
            System.out.println("You marked: " + nums);
        } catch (NumberFormatException ex) {
            System.out.println("A wrong integer was entered");
        }
    }

    public void cmdDeleteMarkedMsgs(String host, int port, String login, String password) throws IOException {
        logout();
        disconnect();
        connect(host, port);
        login(login, password);
        System.out.println("All marked messages were deleted");
    }

    public void cmdRsetDelete() throws IOException {
        String response = sendCommand("RSET");
        System.out.println(response);
    }
    public void wrF(int num){
        String response;
        try {
            // \Users\Maria\IdeaProjects\pop3cl
            File file = new File(String.valueOf(new File("C:\\file" + num)));
            BufferedWriter writerf = new BufferedWriter(new FileWriter(file));

            response =sendCommand("RETR " + num);
            while (!(response = readResponseLine()).equals(".")) {
                writerf.write(response + '\n');
                writer.flush();
                writerf.flush();
            }
            System.out.println("Successful");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    public void cmdRetrBig(BufferedReader br) {
        int num, numAll;
        String response;
        System.out.println("Enter message number: ");
        try {
            num = Integer.parseInt(br.readLine());
            numAll = getNumberOfMsgs();
            if (num > numAll || num < 0) {
                System.out.println("A message with this number does not exist\n");
            } else wrF(num);
        } catch (
                NumberFormatException | IOException ex) {
            System.out.println("A wrong integer was entered\n");
        }
    }
}

