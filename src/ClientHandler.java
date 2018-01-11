import com.sun.deploy.util.SessionState;

import java.net.*;
import java.io.*;
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
            while (read.available() > 0)
            {
                int bytesRead = read.read(rbuf);
                if (bytesRead == 0)
                {
                    // this means client disconnected, right?
                    System.out.println("read returned zero bytes!");
                    break;
                }
            }
        } catch (IOException ex) {
            System.out.format("Error communicating with client: %s\n", ex.getMessage());
        } finally {
            try {
                if (client != null)
                    client.close();
            } catch (Exception e) {}

            for (Consumer<ClientHandler> listener : termListeners)
            {
                listener.accept(this);
            }
        }

        System.out.format("Client disconnected: %s\n", client.getRemoteSocketAddress().toString());
    }

}
