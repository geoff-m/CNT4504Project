
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ClientHandler extends Thread {

    private Socket client;

    private final int READ_BUFFER_SIZE = 1024;
    private byte[] rbuf = new byte[READ_BUFFER_SIZE];

    public ClientHandler(Socket client)
    {
        this.client = client;

    }

    private Set<Consumer<ClientHandler>> termListeners = new HashSet<>();

    public void addTerminationListener(Consumer<ClientHandler> listener)
    {
        termListeners.add(listener);
    }

    @Override
    public void run()
    {
        try {
            InputStream read = client.getInputStream();
            while (client.isConnected()) {
                do {
                    int message = read.read();
                    if (message == -1) {
                        // This means client has disconnected.
                        return; // Jump to finally...
                    }
                    boolean success = handleMessage((byte) message);
                    if (!success) {
                        System.out.println("Message could not be handled successfully.");
                    }
                } while (read.available() > 0); // While buffer not empty.
            }

        } catch (IOException ex) {
            String exmsg = ex.getMessage();
            if (!exmsg.equals("Connection reset")) // todo: Is there a more robust way to detect client disconnect as the cause of the exception?
                System.out.format("Error communicating with client: %s\n", exmsg);
        } finally {
            try {
                if (client != null)
                    client.close();
            } catch (Exception e) {} // Ignore exception when attempting to close client.

            System.out.format("Client disconnected: %s\n", client.getRemoteSocketAddress().toString());

            for (Consumer<ClientHandler> listener : termListeners)
            {
                listener.accept(this);
            }
        }
    }

    // Handles the incoming message from the client and responds if necessary.
    // Returns true if the message was handled, otherwise false.
    private boolean handleMessage(byte code)
    {
        switch (code)
        {
            case (byte)11:
                handleDateAndTime();
                return true;
            case (byte)22:
                handleUptime();
                return true;
            case (byte)33:
                handleMemoryUsage();
                return true;
            case (byte)44:
                handleNetstat();
                return true;
            case (byte)55:
                handleUsers();
                return true;
            case (byte)66:
                handleProcesses();
                return true;
            default:
                System.out.printf("Unknown command: %02x\n", code);
                return false;
        }
    }

    private void handleDateAndTime()
    {
        try {
            client.getOutputStream().write(StandardCharsets.UTF_8.encode("it's about tree fiddy").array());
            client.getOutputStream().flush();
        } catch (IOException ex) {
            System.out.format("Error sending date and time: %s\n", ex.getMessage());
        }
    }

    private void handleUptime()
    {
        try {
            client.getOutputStream().write(StandardCharsets.UTF_8.encode("i've been up since dawn").array());
            client.getOutputStream().flush();
        } catch (IOException ex) {
            System.out.format("Error sending uptime: %s\n", ex.getMessage());
        }
    }
    private void handleMemoryUsage()
    {
        try {
            client.getOutputStream().write(StandardCharsets.UTF_8.encode("1234 MB in use").array());
            client.getOutputStream().flush();
        } catch (IOException ex) {
            System.out.format("Error sending memory usage: %s\n", ex.getMessage());
        }
    }

    private void handleNetstat()
    {
        try {
            client.getOutputStream().write(StandardCharsets.UTF_8.encode("all my interwebs").array());
            client.getOutputStream().flush();
        } catch (IOException ex) {
            System.out.format("Error sending netstat output: %s\n", ex.getMessage());
        }
    }

    private void handleUsers()
    {
        try {
            client.getOutputStream().write(StandardCharsets.UTF_8.encode("tom, dick and harry").array());
            client.getOutputStream().flush();
        } catch (IOException ex) {
            System.out.format("Error sending users: %s\n", ex.getMessage());
        }
    }

    private void handleProcesses()
    {
        try {
            client.getOutputStream().write(StandardCharsets.UTF_8.encode("i got 99 problems").array());
            client.getOutputStream().flush();
        } catch (IOException ex) {
            System.out.format("Error sending processes: %s\n", ex.getMessage());
        }
    }

}
