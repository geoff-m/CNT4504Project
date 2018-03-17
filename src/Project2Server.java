/*
 * This server uses a monitor and an atomic int to achieve basically the same effect as
 * the "fixed thread pool" described in https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executors.html#newFixedThreadPool-int-
 * While the number of concurrent clients is at its allowed maximum, the server stops accepting (i.e. ignores) new ones.
 */


import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Project2Server {

    private ServerSocket svSock;

    private Listener listen;
    private Thread tListen;
    public Project2Server(int port) throws IOException
    {
        svSock = new ServerSocket(port);
        listen = new Listener(this);
        tListen = new Thread(listen);
    }

    public void start()
    {
        System.out.format("Starting server on %s.\n", svSock.getLocalSocketAddress().toString());
        tListen.start();
    }

    public boolean isRunning()
    {
        return tListen.isAlive();
    }

    public void stop(long timeoutMilliseconds)
    {
        listen.stop();
        try {
            tListen.join(timeoutMilliseconds);
        } catch (InterruptedException ex) { }
    }

    public void abort()
    {
        listen.stop();
        tListen.interrupt();
    }

    AtomicInteger runningClientHandlers = new AtomicInteger();
    private void onClientHandlerExit(ClientHandler sender)
    {
        System.out.format("Client disconnected: %s\n", sender.getRemoteSocketAddress());

        int current = runningClientHandlers.decrementAndGet();
        if (current == 1)
        {
            System.out.println("There is now 1 client connected.");
        } else {
            System.out.format("There are now %d clients connected.\n", current);
        }
    }

    class Listener implements Runnable
    {
        Project2Server server;
        public Listener(Project2Server server)
        {
            this.server = server;
        }

        boolean stop = false;

        public synchronized void stop()
        {
            stop = true;
        }

        @Override
        public void run()
        {
            while (!stop)
            {
                try {
                    // Await new client.
                    Socket client = svSock.accept();

                    // Launch a new ClientHandler.
                    ClientHandler handler = new ClientHandler(client);
                    runningClientHandlers.getAndIncrement();
                    handler.addTerminationListener(Project2Server.this::onClientHandlerExit);
                    handler.start();

                    System.out.format("Client connected: %s\n", client.getRemoteSocketAddress().toString());
                }
                catch (Exception ex)
                {
                    if (ex instanceof IOException)
                    {
                        System.out.format("Server error: %s\n", ex.getMessage());
                    }
                }
            }

        }
    }

}
