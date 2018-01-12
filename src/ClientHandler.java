
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
            StringBuilder msg = new StringBuilder();
            String clientSaid;
            do {
                //msg = new StringBuilder(); // Yes, we allocate a new StringBuilder every time. There appears to be no good way to simply clear it.
                do {
                    int bytesRead = read.read(rbuf);
                    if (bytesRead == -1) {
                        // This means client has disconnected.
                        return;
                    }
                    msg.append(new String(rbuf, 0, bytesRead, StandardCharsets.UTF_8));
                } while (read.available() > 0);

                clientSaid = msg.toString().trim();
                System.out.format("Client said: \"%s\"\n", clientSaid);
                msg.setLength(0); // clear buffer.
            } while (clientSaid.compareToIgnoreCase("quit") != 0 && clientSaid.compareToIgnoreCase("exit") != 0);

        } catch (IOException ex) {
            String exmsg = ex.getMessage();
            if (!exmsg.equals("Connection reset"))
                System.out.format("Error communicating with client: %s\n", exmsg);
        } finally {
            try {
                if (client != null)
                    client.close();
            } catch (Exception e) {} // Ignore exception when attempting to close client.

            for (Consumer<ClientHandler> listener : termListeners)
            {
                listener.accept(this);
            }
            System.out.format("Client disconnected: %s\n", client.getRemoteSocketAddress().toString());
        }
    }

}
