
import java.io.IOException;

public class Application {
    enum Mode
    {
        Unknown,
        Server,
        Client
    }


    private static Project1Server server;

    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            showUsage();
            return;
        }

        Mode mode = parseMode(args[0].trim());
        if (mode == Mode.Unknown)
        {
            System.out.println("Unknown mode.\n");
            showUsage();
            return;
        }

        if (mode == Mode.Server)
        {
            if (args.length != 2)
            {
                System.out.println("Too many arguments.\n");
                showUsage();
                return;
            }

            Integer port = tryParseInteger(args[1]);
            if (port == null)
            {
                System.out.format("Cannot parse port number \"%s\"\n", args[1]);
                return;
            }

            try {
                server = new Project1Server(port);
                server.start();
            } catch (IOException ex) {
                System.out.format("Error setting up server: %s\n", ex.getMessage());
                return;
            }

        }

        if (mode == Mode.Client)
        {

        }
    }

    // Returns the input parsed as an integer, or null on failure.
    private static Integer tryParseInteger(String s)
    {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    private static Mode parseMode(String arg)
    {
        if (arg.equalsIgnoreCase("client") || arg.equalsIgnoreCase("c"))
        {
            return Mode.Client;
        }
        if (arg.equalsIgnoreCase("server") || arg.equalsIgnoreCase("s"))
        {
            return Mode.Server;
        }
        return Mode.Unknown;
    }


    private static void showUsage()
    {
        System.out.println("Usage: project1 [mode] [arguments]");

        System.out.println("[mode] - Either client (c) or server (s)");

        System.out.println("Client arguments: [host address] [port]");
        System.out.println("[host address] - The remote host's IPv4 address");
        System.out.println("[port] - The remote port to connect");

        System.out.println("\nServer arguments:[port]");
        System.out.println("[port] - The port to listen on");
    }

}
