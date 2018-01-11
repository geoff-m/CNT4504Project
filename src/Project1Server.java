
import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.*;

public class Project1Server {

    private ServerSocket svSock;
    public Project1Server(int port) throws IOException
    {
        svSock = new ServerSocket(port);
        listen = new Thread(new Listener(this));
    }

    private Socket client;
    private Thread listen;
    public void start() throws IOException
    {
        System.out.format("Starting server on port %d.\n", svSock.getLocalPort());
        listen.start();
    }

    public void stop()
    {
        listen.interrupt(); // untested. this may not accomplish what we want.
    }

    private AtomicInteger runningHandlers = new AtomicInteger();

    class Listener implements Runnable
    {
        Project1Server server;
        public Listener(Project1Server server)
        {
            this.server = server;
        }

        @Override
        public void run()
        {
            try {
                System.out.format("Waiting for connection on %s...\n", svSock.getLocalSocketAddress().toString());
                client = svSock.accept();
                System.out.format("Client connected: %s\n", client.getRemoteSocketAddress().toString());
                runningHandlers.incrementAndGet();
                ClientHandler handler = new ClientHandler(client);
                handler.addTerminationListener(server::onHandlerTerminated);

            } catch (IOException ex) {
                System.out.format("Server error: %s\n", ex.getMessage());
            }
        }
    }

    private void onHandlerTerminated(ClientHandler source)
    {
        runningHandlers.decrementAndGet();
    }
}
