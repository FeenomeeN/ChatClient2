import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter ip");
        String ipAddr;
        do {
            ipAddr = in.nextLine();
        } while (ipAddr.matches(""));
        short port = 0;
        do {
            System.out.println("Enter port: ");
            if (in.hasNextShort())
                port = in.nextShort();
            else
                System.out.println("Incorrect input!");
            in.nextLine();
        } while (port <= 0);
        new ClientConnection(ipAddr, port);
    }
}