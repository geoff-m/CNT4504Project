import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

// Creates and controls a number of clients to do the same thing at a time.
public class ManyClients {

    List<Project1Client> clients;
    public ManyClients(int count, InetAddress remoteAddress, int port) throws IOException
    {
        clients = new ArrayList<>();
        for (int i=0; i<count; ++i)
        {
            clients.add(new Project1Client(remoteAddress, port));
        }
    }

    public void benchmarkDateTime()
    {

    }

    public void disconnectAll()
    {
        clients.stream().forEach(c -> c.disconnect());
    }

}
