/*
 * This server uses a monitor and an atomic int to achieve basically the same effect as
 * the "fixed thread pool" described in https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executors.html#newFixedThreadPool-int-
 * While the number of concurrent clients is at its allowed maximum, the server stops accepting (i.e. ignores) new ones.
 */


import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.*;

public class Project1Server {

    private ServerSocket svSock;

    private Listener listen;
    private Thread tListen;
    public Project1Server(int port) throws IOException
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

    class Listener implements Runnable
    {
        Project1Server server;
        public Listener(Project1Server server)
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
                    Socket client = svSock.accept();
                    System.out.format("Client connected: %s\n", client.getRemoteSocketAddress().toString());
                    ClientHandler handler = new ClientHandler(client);
                    handler.start();
                    handler.join();
                }
                catch (Exception ex)
                {
                    if (ex instanceof IOException)
                    {
                        System.out.format("Server error: %s\n", ex.getMessage());
                    }
                    if (ex instanceof InterruptedException)
                    {
                        // Break if we're interrupted.
                        return;
                    }
                }
            }

        }
    }

}
