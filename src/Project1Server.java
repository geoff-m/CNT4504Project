
import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.*;

public class Project1Server {

    private ServerSocket svSock;

    public Project1Server(int port, int maximumClients) throws IOException
    {
        maxClients = maximumClients;

        svSock = new ServerSocket(port);
        listen = new Thread(new Listener(this));
    }


    private Thread listen;
    public void start()
    {
        System.out.format("Starting server on port %d.\n", svSock.getLocalPort());
        listen.start();
    }

    public void stop()
    {
        listen.interrupt(); // untested. this may not accomplish what we want.
    }

    class Listener implements Runnable
    {
        Project1Server server;
        public Listener(Project1Server server)
        {
            this.server = server;
        }

        boolean stop = false;

        public synchronized void Stop()
        {
            stop = true;
            notify();
        }

        @Override
        public void run()
        {
            try {

                while (!stop)
                {
                    System.out.format("Waiting for connections on %s...\n", svSock.getLocalSocketAddress().toString());
                    
                    while (runningHandlers.get() < maxClients)
                    {
                        Socket client = svSock.accept();
                        System.out.format("Client connected: %s\n", client.getRemoteSocketAddress().toString());
                        ClientHandler handler = new ClientHandler(client);
                        System.out.format("The number of clients is now %d.\n", runningHandlers.incrementAndGet());
                        handler.addTerminationListener(server::onHandlerTerminated);
                        handler.start();
                    }

                    // Wait for runningHandlers to fall below maximum. Then start accepting clients again.
                    synchronized (server)
                    {
                        //System.out.println("Waiting for clients to leave...");
                        try {
                            server.wait();
                        } catch (InterruptedException ie) { }
                        //System.out.println("Woken from wait!");
                    }
                }

            } catch (IOException ex) {
                System.out.format("Server error: %s\n", ex.getMessage());
            }
        }
    }

    private int maxClients;
    private AtomicInteger runningHandlers = new AtomicInteger(); // current number of clients we have.

    //
    private synchronized void onHandlerTerminated(ClientHandler source)
    {
        int currentClients = runningHandlers.decrementAndGet();
        System.out.format("The number of clients is now %d.\n", currentClients);
        notify();
    }


}
