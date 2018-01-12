import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Project1Client {

    private Socket client;

    public Project1Client(InetAddress remoteAddress, int port) throws IOException
    {
        client = new Socket(remoteAddress, port);

    }

    public void interact()
    {
        Scanner read = new Scanner(System.in);
        while (true)
        {
            System.out.print(">> ");
            String line = read.nextLine();
            try {
                client.getOutputStream().write(StandardCharsets.UTF_8.encode(line).array());
            } catch (IOException ex) {
                System.out.format("\nError! %s\n", ex.getMessage());
                return;
            }
        }
    }

    private void showMenu()
    {
        System.out.println("----Operations------------------------");
        System.out.println("1. Say hi");
        System.out.println("2. Say blah");

    }

}
