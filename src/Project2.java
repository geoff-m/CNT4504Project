
import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

/*
 * This class contains the entry point for the application.
 * It parses the command-line arguments and starts execution in the specified mode.
 */
public class Project2 {

    private static void showUsage()
    {
        System.err.println("Usage: project1 [mode] [arguments]");

        System.err.println("[mode] - server (s), client (c), or many (m)");

        System.err.println("\nServer arguments: [port]");
        System.err.println("\t[port] - The port to listen on");

        System.err.println("Client arguments: [host address] [port]");
        System.err.println("\t[host address] - The remote host's IPv4 address");
        System.err.println("\t[port] - The remote port to connect");

        System.err.println("Many-client arguments: [host address] [port] [count] [operation]");
        System.err.println("\t[host address] - The remote host's IPv4 address");
        System.err.println("\t[port] - The remote port to connect");
        System.err.println("\t[count] - The number of clients to use for test.");
        System.err.println("\t[operation] - The type of request that will be performed.");

    }

    enum Mode
    {
        Unknown,
        Server,
        Client,
        ManyClients
    }

    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            showUsage();
            return;
        }

        Mode mode = parseMode(args[0].trim());

        switch (mode)
        {
            case Server:
                startAsServer(args);
                break;
            case Client:
                startAsClient(args);
                break;
            case ManyClients:
                startAsManyClients(args);
                break;
            default:
                System.err.println("Unknown mode.\n");
                showUsage();
        }
    }

    // Starts the application as a server.
    private static void startAsServer(String[] args)
    {
        if (args.length != 2)
        {
            System.err.println("Too many arguments.\n");
            showUsage();
            return;
        }
        Integer port = tryParseInteger(args[1]);
        if (port == null)
        {
            System.err.format("Cannot parse port number \"%s\"\n", args[1]);
            return;
        }
        // Start up server.
        Project2Server server = null;
        try {
            server = new Project2Server(port);
            server.start();
        } catch (IOException ex) {
            System.err.format("Error setting up server: %s\n", ex.getMessage());
            return;
        }

        Scanner read = new Scanner(System.in);
        String line;
        do {
            System.out.println("Type \"stop\" to stop server.");
            line = read.nextLine().trim();
        } while (!line.equalsIgnoreCase("stop"));
        System.out.println("Stopping server...");
        server.stop(2000);

        if (server.isRunning())
        {
            // Server is still running after waiting for it to stop.
            // Kill it.
            server.abort();
        }
	System.exit(0); // Testing shows this is necessary.
    }

    // Starts the application as an interactive client.
    private static void startAsClient(String[] args)
    {
        if (args.length != 3)
        {
            showUsage();
            return;
        }


        InetAddress addr;
        try {
            addr = InetAddress.getByName(args[1]);
        } catch (IOException ex) {
            System.err.format("Error: %s\n", ex.getMessage());
            return;
        }

        Integer port = tryParseInteger(args[2]);
        if (port == null)
        {
            System.err.format("Cannot parse port number \"%s\"\n", args[2]);
            return;
        }

        // Start up client.
        Project1Client client;
        client = new Project1Client(addr, port);
        client.interact();
    }

    private static void startAsManyClients(String[] args)
    {
        if (args.length != 5)
        {
            showUsage();
            return;
        }

        InetAddress addr;
        try {
            addr = InetAddress.getByName(args[1]);
        } catch (IOException ex) {
            System.err.format("Error: %s\n", ex.getMessage());
            return;
        }

        Integer port = tryParseInteger(args[2]);
        if (port == null)
        {
            System.err.format("Cannot parse port number \"%s\"\n", args[2]);
            return;
        }

        Integer count = tryParseInteger(args[3]);
        if (count == null)
        {
            System.err.format("Cannot parse count \"%s\"\n", args[3]);
            return;
        }

        Project1Client.Operation op = Project1Client.parseOperation(args[4]);
        if (op == null)
        {
            System.err.format("Invalid operation \"%s\"\n", args[4]);
            return;
        }

        // Start up clients.
        System.err.println("Running tests...");
        ManyClients clients;
        clients = new ManyClients(count, addr, port);
        BenchmarkResult[] results = null;
            results = clients.timeOperation(op);
        System.err.println("Test done.");

        // output results as CSV
        System.out.println("Test #,Operation,Success,Response Size (bytes),Duration (ms)");
        for (int i = 0; i < results.length; ++i)
        {
            BenchmarkResult r = results[i];
            System.out.format("%d,%s,%s,%d,%d\n",
                    i,                      // Test #
                    op.getShortName(),      // Operation
                    r.wasSuccess(),         // Whether request succeeded
                    r.getResponseSize(),    // Response size
                    r.getDuration());       // Transaction time
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
        switch (arg.toLowerCase())
        {
            case "client":
            case "c":
                return Mode.Client;
            case "server":
            case "s":
                return Mode.Server;
            case "many":
            case "m":
                return Mode.ManyClients;
        }
        return Mode.Unknown;
    }


}
