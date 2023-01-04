import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Server {

    // stores a list of all clients in the memory
    static ArrayList<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        ServerSocket ss = new ServerSocket(1234);
        String ipAddress = InetAddress.getLocalHost().toString();
        System.out.println("Server is up on " + ipAddress + " port 1234");
        Socket s;
        int i = 0;
        while (true) {

            s = ss.accept();

            System.out.println("client " + i + " is connected to server!");

            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            // main class to handle every new client
            ClientHandler handler = new ClientHandler(s, dis, dos, "client "+i);
            Thread t = new Thread(handler);

            clients.add(handler);
            t.start();
            i++;
        }
    }

}


class ClientHandler implements Runnable {

    // username and password
    private String username;
    private String password = "";

    // is true after signing up
    private boolean isSignedUp = false;

    // client which sent connection request to you
    public ClientHandler reqFrom = null;
    // client which you sent connection request to
    public ClientHandler reqTo = null;
    // client which you are connected to
    public ClientHandler recipient = null;

    private class Decoder {

        private final String msg;
        private final String command;
        private final String payload;

        Decoder(String msg) {
            this.msg = msg;
            this.command = msg.substring(1,msg.indexOf("]"));
            this.payload = msg.substring(msg.indexOf("]") + 2);
        }

        public String getCommand() {
            return this.command;
        }

        public String[] getUserPass() {
            String[] sep = this.payload.split(" ");
            return sep;
        }

        public String getMessage() {
            return this.payload;
        }
    }

    Socket s;

    // this says if client is online or not
    boolean isLoggedIn;

    final DataInputStream dis;
    final DataOutputStream dos;

    ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, String username) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.isLoggedIn = true;
        this.username = username;
    }

    @Override
    public void run() {

        try {

            while (true) {

                // read client input
                String received = dis.readUTF();
                Decoder decoder = new Decoder(received);
                String command = decoder.getCommand();

                if (command.equals("login")) {
                    String[] userPass = decoder.getUserPass();
                    authentication(userPass[0], userPass[1]);
                }
                // client goes offline
                else if (command.equals("logout")) {
                    System.out.println("User " + username + " logged out.");
                    if (this.recipient != null) {
                        this.recipient.recipient = null;
                        this.recipient.dos.writeUTF("[offline] " + this.username);
                    }
                    this.recipient = null;
                    this.isLoggedIn = false;
                    //this.s.close();
                    break;
                }
                // print the list of all clients on the server
                else if (received.equals(("list")) || received.equals(("list on")) || received.equals(("list off"))) {
                    for (ClientHandler h : Server.clients) {
                        if (h.isSignedUp)
                            if (h.isLoggedIn) {
                                if (!received.equals(("list off")))
                                    dos.writeUTF("[list off] " + h.username);
                            }
                            else {
                                if (!received.equals(("list on")))
                                    dos.writeUTF("[list on] " + h.username);
                            }
                    }
                }
                // connect two clients
                else if (command.equals("connect")) {
                    sendConnectionRequest(decoder.getMessage());
                }
                else if (command.equals("message")) {
                    // invalid command
                    if (this.recipient == null) {

                    }
                    // chat
                    else {
                        System.out.println("\"" + decoder.getMessage() + "\" said " + this.username + " to " + this.recipient.username);
                        this.recipient.dos.writeUTF( "[message] " + decoder.getMessage());
                    }
                }
                else {

                }
            }
        }
        // handling client disconnection
        catch (IOException e) {
            try {
                this.isLoggedIn = false;
                if (this.recipient != null) {
                    this.recipient.recipient = null;
                    this.recipient.dos.writeUTF(this.username + " is disconnected");
                }
                this.recipient = null;
                this.s.close();
                System.out.println("User " + username + " disconnected.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        // close data streams
        finally {
            try {
                this.dis.close();
                this.dos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    private void authentication(String username_, String password_) throws IOException {
        // This method gets the username and password of client

        String signInStatus = usernameVerification(username_);
        boolean success = false;
        if (signInStatus.equals("declined")) {
            dos.writeUTF("[userTaken]");
            return;
        }
        else if (signInStatus.equals("signup")) {
            success = passwordSignIn(password_, "Up");
        }
        else {
            success = passwordSignIn(password_, "In");
            Server.clients.remove(getPreviousInstance(username_));
        }
        if (success) {
            username = username_;
            isSignedUp = true;
            System.out.println("User " + username + " is logged in");
        }
    }

    private ClientHandler getPreviousInstance(String received) {
        for (ClientHandler h: Server.clients) {
            if (h.username.equals(received)) {
                return h;
            }
        }
        return null;
    }

    private String usernameVerification(String received) throws IOException{

        for (ClientHandler h: Server.clients) {
            if (h.username.equals(received)) {
                if (h.isLoggedIn && h.isSignedUp) {
                    // the user is online
                    return "declined";
                }
                else if (!h.isLoggedIn && h.isSignedUp) {
                    // log in
                    password = h.password;
                    return "login";
                }
                else {
                    // sign up
                    return "signup";
                }
            }
        }
        return "signup";
    }

    private boolean passwordSignIn(String received, String inOrUp) throws IOException {

        if (inOrUp.equals("Up")) {
            // password strength check
            if (!passwordStrengthCheck(received)) {
                dos.writeUTF("[weakPass]");
                return false;
            }
        }

        // password hashing
        String temp = passwordHashing(received);
        if (temp != null) {
            if (inOrUp.equals("Up")) {
                dos.writeUTF("[trueUp]");
                password = temp;
                return true;
            }
            else {
                // verifying password for authentication
                if (temp.equals(password)) {
                    dos.writeUTF("[trueIn]");
                    return true;
                }
                else {
                    dos.writeUTF("[wrongPass]");
                    return false;
                }
            }
        }
        else {
            dos.writeUTF("[!]");
            password = "";
        }
        return false;
    }


    private boolean passwordStrengthCheck(String password) {
        if (password.length() < 8)
            return false;

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasNumber = false;
        char[] ch = password.toCharArray();
        for (char c: ch) {
            if (Character.isUpperCase(c))
                hasUpperCase = true;
            else if (Character.isLowerCase(c))
                hasLowerCase = true;
            else if (Character.isDigit(c))
                hasNumber = true;

            if (hasLowerCase && hasUpperCase && hasNumber)
                return true;
        }
        return false;
    }

    private String passwordHashing(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
            messageDigest.update(password.getBytes());
            byte[] resultByteArray = messageDigest.digest();
            StringBuilder sb = new StringBuilder();

            for (byte b : resultByteArray) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private void sendConnectionRequest(String received) throws IOException{
        // this method sends the connection request to other client

        if (received.equals(this.username)) {
            dos.writeUTF("[ConnectFailSelf]");
            return;
        }

        // check which client has the specified username
        for (ClientHandler h: Server.clients) {
            if (h.username.equals(received) && h.isLoggedIn) {
                //dos.writeUTF("Sending request to " + h.username);
                this.reqTo = h;
                // current client request is sent to the specified client
                h.reqFrom = this;
                h.reqFrom.receiveAccepted(true);
                //h.dos.writeUTF(username + " would like to chat with you. (Y/n)?");
                return;
            }
        }
        dos.writeUTF("[ConnectFailUserNotFound]");
    }

    public void receiveAccepted(boolean accepted) throws IOException{
        // after the reply to connection request, this method is called for final arrangement

        if (accepted) {
            // if request is accepted

            // the previous connections should close for current client
            if (this.recipient != null) {
                System.out.println(username + " is disconnected from " + this.recipient.username);
                dos.writeUTF("[DisconnectedFrom] " + this.recipient.username);
                this.recipient.dos.writeUTF("[DisconnectedFrom] " + this.username);
                this.recipient.recipient = null;
            }

            // the previous connections should close for target client
            if (this.reqTo.recipient != null) {
                System.out.println(this.reqTo.username + " is disconnected from " + this.reqTo.recipient.username);
                this.reqTo.dos.writeUTF("[DisconnectedFrom] " + this.reqTo.recipient.username);
                this.reqTo.recipient.dos.writeUTF("[DisconnectedFrom] " + this.reqTo.username);
                this.reqTo.recipient.recipient = null;
            }

            // connection is established
            System.out.println(username + " is connected to " + reqTo.username);
            reqTo.dos.writeUTF("[ConnectionFrom] " + username);
            dos.writeUTF("[ConnectedTo] " + reqTo.username);
            this.recipient = reqTo;
            reqTo.recipient = this;
            this.reqTo = null;

        }
        // request rejected
        else {
            dos.writeUTF("Request rejected! Try again later!");
        }
    }
}