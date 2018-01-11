import java.io.IOException;
import java.net.*;

public class Project1Client {

    private Socket client;

    public Project1Client(String remoteAddress, int port) throws IOException
    {
        client = new Socket(InetAddress.getByName(remoteAddress), port);

    }

    private void showMenu()
    {
        System.out.println("----Operations------------------------");
        System.out.println("1. Say hi");
        System.out.println("2. Say blah");

    }

}
