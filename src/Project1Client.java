import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Project1Client {

    private Socket client;
    final int READ_BUFFER_SIZE = 4096;

    public Project1Client(InetAddress remoteAddress, int port) throws IOException
    {
        client = new Socket(remoteAddress, port);

    }

    // Used for testing client->server communication.
    public void chatDemo()
    {
        System.out.println("Whatever you type will be sent to the server. Type 'exit' or 'quit' to disconnect.");
        Scanner read = new Scanner(System.in);
        while (client.isConnected())
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
        System.out.println("Disconnected from server.");
    }

    public void interact()
    {
        Scanner read = new Scanner(System.in);
        showMenu();

        while (true) {
            System.out.print("> ");
            String uin = read.nextLine().trim();
            if (equalsAnyIgnoreCase(uin, "quit", "stop", "exit"))
                return;

            Operation op;
            // Try parsing the choice as a number.
            Integer choice = tryParseInteger(uin.replaceAll("[\\p{Punct}]", ""));

            if (choice == null)
            {
                // Try parsing the choice as a word.
                List<Operation> matches = _operations.stream().filter(o -> o.matches(uin)).collect(Collectors.toCollection(ArrayList<Operation>::new));
                switch (matches.size())
                {
                    case 0:
                        // Search returned no results.
                        System.out.println("Invalid choice.");
                        showMenu();
                        continue;
                    case 1:
                        // Search returned exactly one result.
                        op = matches.get(0);
                        break;
                    default:
                        // There was some ambiguity when attempting to map the input to an operation.
                        // This will never happen in deployment if nicknames are mutually exclusive.
                        System.out.format("Did you mean %s?\n", orList(matches.stream().map(o -> String.valueOf(_operations.indexOf(o) + 1)).toArray(String[]::new)));
                        showMenu();
                        continue;
                }
            } else {
                if (choice == 0)
                    return; // 0 means quit.

                if (choice < 1 || choice > _operations.size()) {
                    System.out.println("Invalid choice.");
                    showMenu();
                    continue;
                }
                // Choice was in range.
                op = _operations.get(choice - 1);
            }

                //System.out.format("OPERATION SELECTED: %s\n", op.getDescription());

            // Send the command to the server.
            try {
                writeByte(op.code);

                // display response from server (as text)
                System.out.println(readAll(client.getInputStream()));

            } catch (IOException ex) {
                System.out.format("Error communicating with server: %s\n", ex.getMessage());
            }

        } // infinite loop.

    }

    private String readAll(InputStream is) throws IOException
    {
        StringBuilder sb = new StringBuilder(is.available());
        byte[] buf = new byte[READ_BUFFER_SIZE];
        int bytesRead;
        do
        {
            bytesRead = is.read(buf);
            sb.append(StandardCharsets.UTF_8.decode(ByteBuffer.wrap(buf)));
        } while (bytesRead > 0 && is.available() > 0);
        return sb.toString();
    }

    public void disconnect()
    {
        try {
            if (client != null)
                client.close();
        } catch (IOException ex) { }
    }

    private static boolean equalsAnyIgnoreCase(String comparand, String... choices)
    {
        for (String c : choices)
        {
            if (comparand.equalsIgnoreCase(c))
                return true;
        }
        return false;
    }

    // Formats a list of strings as a pretty string with commas and "or".
    private static String orList(String... items)
    {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < items.length - 1; ++i)
        {
            sb.append(items[i]);
            sb.append(", ");
        }
        sb.append("or ");
        sb.append(items[items.length - 1]);
        return sb.toString();
    }

    // Immediately sends a single byte to the socket.
    private void writeByte(byte b) throws IOException
    {
        client.getOutputStream().write(b);
        client.getOutputStream().flush();
    }

    private static Integer tryParseInteger(String s)
    {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }


    private static List<Operation> _operations;

    static {
        _operations = new ArrayList<>(6);
        // Server and client must agree on the byte ("code" parameter).
        _operations.add(new Operation("Get host date & time",  (byte)0x11,
                "date", "time"));
        _operations.add(new Operation("Get host uptime",  (byte)0x22,
                "uptime"));
        _operations.add(new Operation("Get host memory usage",  (byte)0x33,
                "memory", "mem", "free"));
        _operations.add(new Operation("Get host netstat output",  (byte)0x44,
                "netstat"));
        _operations.add(new Operation("Get host current users",  (byte)0x55,
                "users", "who"));
        _operations.add(new Operation("Get host running processes",  (byte)0x66,
                "process", "processes", "ps"));
    }

    static class Operation
    {
        private String descr;
        private String[] nicks;
        private byte code;

        /*
         * @param description   A description of the operation that will be shown to the user.
         * @param code          The byte that uniquely identifies this operation to the remote host.
         * @param nicknames     A collection of nicknames that this operation should match. Nicknames must be mutually exclusive among all operations.
         */
        public Operation(String description, byte code, String... nicknames)
        {
            descr = description;
            this.code = code;
            nicks = nicknames;
        }

        // Returns true iff the given text matches any of this Operation's nicknames.
        public boolean matches(String text)
        {
            return equalsAnyIgnoreCase(text, nicks);
        }

        public String getDescription()
        {
            return descr;
        }

        public byte getCode()
        {
            return code;
        }
    }

    private void showMenu()
    {
        System.out.format("---There are %d supported operations-------------\n", _operations.size());
        for (int i=0; i < _operations.size(); ++i)
        {
            System.out.format(" %d. %s\n", i + 1, _operations.get(i).getDescription());
        }
        System.out.println("Enter 0 to quit.");
        System.out.println();
    }

}
