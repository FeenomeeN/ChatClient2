import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientConnection {
    private Socket socket;
    private BufferedReader in;// поток чтения из сокета
    private BufferedWriter out;// поток чтения в сокет
    private BufferedReader inputUser;
    private String addr;
    private int port;
    private String nickname;
    private Date time;
    private String dtime;
    private SimpleDateFormat dt1;

    public ClientConnection(String addr, int port) {
        this.addr = addr;
        this.port = port;
        try {
            this.socket = new Socket(addr, port);
        } catch (IOException e) {
            System.err.println("Exception while connecting to " + addr + " : " + port + e);
        }
        System.out.println("Successfully connected to server " + addr + ":" + port);
        try {
            // потоки чтения из сокета / записи в сокет, и чтения с консоли
            inputUser = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            this.pressNickname();
            new ReadMsg().start();// нить читающая сообщения из сокета в бесконечном цикле
            new WriteMsg().start();// нить пишущая сообщения в сокет приходящие с консоли в бесконечном цикл
        } catch (IOException e) {
            // Сокет должен быть закрыт при любой
            // ошибке, кроме ошибки конструктора сокета:
            System.err.println("Unable connect to server " + addr + ":" + port + e);
            ClientConnection.this.downService();
        }
    }
//закрытие сокета
    private void downService() {
        try {
            if(!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch (IOException e) {
            System.out.println("Connection closed.");
        }
    }

    private void pressNickname() {
        System.out.println("Press your nickname: ");
        try {
            nickname = inputUser.readLine();
            out.write("Hello " + nickname + "\n");
            out.flush();
        } catch (IOException e) {
            System.err.println("Exception while entering user nickname " + e);
        }
    }

    private class ReadMsg extends Thread {
        @Override
        public void run() {
            String str;
            try {
                while (true) {
                    str = in.readLine();// ждем сообщения с сервера
                    if (str.equals("stop")) {
                        ClientConnection.this.downService();
                        break;
                    }
                    System.out.println(str);
                }
            } catch (IOException e) {
                ClientConnection.this.downService();
            }
        }
    }
    //  отправляющая сообщения приходящие с консоли на сервер
    public class WriteMsg extends Thread {
        @Override
        public void run() {
            while (true) {
                String userWord;
                try {
                    time = new Date();
                    dt1 = new SimpleDateFormat("HH:mm:ss");
                    dtime = dt1.format(time);
                    userWord = inputUser.readLine();
                    if (userWord.equals("stop")) {
                        out.write("stop" + "\n");
                        ClientConnection.this.downService();
                        break;
                    } else {
                        out.write("(" + dtime + ")" + nickname + ": " + userWord + "\n");// отправляем на сервер
                    }
                    out.flush();// чистим
                } catch (IOException e) {
                    ClientConnection.this.downService();
                }
            }
        }
    }
}
