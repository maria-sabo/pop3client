import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        POP3Client client = new POP3Client();
        client.setDebug(true);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Enter host: ");
        String host = br.readLine();
        System.out.println("Enter port to connect the host: ");
        int port = Integer.parseInt(br.readLine());
        client.connect(host, port);

        System.out.println("Enter your login: ");
        String login = br.readLine();
        System.out.println("Enter your password: ");
        String password = br.readLine();
        client.login(login, password);

        System.out.println(
                "You can use commands: \n" +
                        "\tSTAT - to get the mailbox status\n" +
                        "\tLIST - to get the number of messages in mailbox\n" +
                        "\tRETR - to retrieve the message\n" +
                        "\tTOP - to get first N lines of the message\n" +
                        "\tUIDL - to get the unique identifier of messages\n" +
                        "\tNLIST - to get the number of new messages, their total length and short info \n" +
                        "\tMARKDELE - to mark the list of messages\n" +
                        "\tDELETEMARKED - to delete marked messages\n" +
                        "\tRSETQ - to quit the application without deleting marked messages\n" +
                        "\tQUIT - to quit\n");
        int quit = 0;
        do {
            String x = br.readLine();
            switch (x) {
                case "STAT":
                    client.cmdStat();
                    break;
                case "LIST":
                    client.cmdList();
                    break;
                case "RETR":
                    client.cmdRetrieve(br, client.listOfNewMessages(host, port, login, password));
                    break;
                case "TOP":
                    client.cmdTop(br);
                    break;
                case "UIDL":
                    client.cmdUidl(br);
                    break;
                case "NLIST":
                    client.listOfNewMessages(host, port, login, password);
                    break;
                case "MARKDELE":
                    client.cmdMarkMsgsDel(br);
                    break;
                case "DELETEMARKED":
                    client.cmdDeleteMarkedMsgs("mail.ngs.ru", 110, "masha129087@ngs.ru", "kek755");
                    break;
                case "RSETQ":
                    client.cmdRsetDelete();
                    quit = 1;
                    break;
                case "QUIT":
                    quit = 1;
                    break;
                default:
                    System.out.println("Unexpected command: " + x);
            }
        } while (quit != 1);
        client.logout();
        client.disconnect();
    }
}
