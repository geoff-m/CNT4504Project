
import java.net.*;
import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;

public class ClientHandler extends Thread {

    private Socket client;
    private OutputStream write;

    public ClientHandler(Socket client) throws IOException {
        this.client = client;
        write = client.getOutputStream();
    }

    private Set<Consumer<ClientHandler>> termListeners = new HashSet<>();
    public void addTerminationListener(Consumer<ClientHandler> listener) {
        termListeners.add(listener);
    }

    @Override
    public void run() {
        try {
            InputStream read = client.getInputStream();
            int message = read.read();
            if (message == -1) {
                // This means client has disconnected.
                return; // Jump to finally...
            }
            boolean success = handleMessage((byte) message);
            if (!success) {
                System.out.println("Message could not be handled successfully.");
            }

        } catch (IOException ex) {
            String exmsg = ex.getMessage();
            if (!exmsg.equals("Connection reset")) // todo: Is there a more robust way to detect client disconnect as the cause of the exception?
                System.out.format("Error communicating with client: %s\n", exmsg);
        } finally {
            try {
                if (client != null)
                    client.close();
            } catch (IOException e) {
            } // Ignore exception when attempting to close client.

            for (Consumer<ClientHandler> listener : termListeners) {
                listener.accept(this);
            }
        }
    }

    // Handles the incoming message from the client and responds if necessary.
    // Returns true if the message was handled, otherwise false.
    private boolean handleMessage(byte code) {
        try {
            switch (code) {
                case 0x11:
                    handleDateAndTime();
                    return true;
                case 0x22:
                    handleUptime();
                    return true;
                case 0x33:
                    handleMemoryUsage();
                    return true;
                case 0x44:
                    handleNetstat();
                    return true;
                case 0x55:
                    handleUsers();
                    return true;
                case 0x66:
                    handleProcesses();
                    return true;
                default:
                    System.out.printf("Unknown command: %02x\n", code);
                    return false;
            }
        } catch (IOException ioe) {
            System.out.printf("Error handling message! code = %02x\n", code);
            return false;
        }
    }

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy, HH:mm:ss");

    private void handleDateAndTime() throws IOException {
        write.write(StandardCharsets.UTF_8.encode(LocalDateTime.now().format(dtf)).array());
        write.flush();
    }

    // Check we're on Linux.
    private static boolean haveUnix;

    static {
	    String osname = System.getProperty("os.name").toLowerCase();
	    System.out.format("osname: %s\n", osname);
        haveUnix = osname.contains("nux") || osname.contains("nix");
    }

    private void handleUptime() throws IOException
    {
        String msg;
        if (haveUnix) {
            int upSeconds;
            Scanner read = null;
            try {
                read = new Scanner(new FileInputStream("/proc/uptime"));
                upSeconds = (int)read.nextDouble();
            } catch (IOException e) {
                System.out.format("Error getting uptime: %s\n", e.getMessage());
                upSeconds = -1;
            } finally {
                if (read != null)
                    read.close();
            }
            if (upSeconds == -1) {
                msg = "Error reading uptime";
            } else {
                int upMinutes = upSeconds / 60;
                int upHours = upMinutes / 60;
                int upDays = upHours / 24;
		upHours %= upDays * 24;
		upMinutes %= upHours * 60;
		upSeconds %= 60;

                if (upDays > 0) {
                    msg = String.format("%dd %dh %02dm %02ds", upDays, upHours, upMinutes, upSeconds);
                } else {
                    msg = String.format("%dh %02dm %02ds", upHours, upMinutes, upSeconds);
                }
            }
        } else {
            msg = "Uptime not supported (linux not detected)";
        }
        write.write(StandardCharsets.UTF_8.encode(msg).array());
        write.flush();
    }

    private void handleMemoryUsage() throws IOException // Done.
    {
        String msg;
        if (haveUnix) {
            try {
                ProcessBuilder psb = new ProcessBuilder();
                psb.command("free");
                Process p = psb.start();
                msg = readAll(p.getInputStream());
            } catch (IOException e) {
                msg = "Error reading memory usage";
            }
        } else {
            msg = "Memory usage not supported (linux not detected)";
        }
        write.write(StandardCharsets.UTF_8.encode(msg).array());
        write.flush();
    }

    private void handleNetstat() throws IOException // Done.
    {
        String msg;
        if (haveUnix) {
            try {
                ProcessBuilder psb = new ProcessBuilder();
                psb.command("netstat");
                Process p = psb.start();
                msg = readAll(p.getInputStream());
            } catch (IOException e) {
                msg = "Error reading netstat";
            }
        } else {
            msg = "Netstat not supported (linux not detected)";
        }
        write.write(StandardCharsets.UTF_8.encode(msg).array());
        write.flush();
    }

    private void handleUsers() throws IOException
    {
        String msg;
        if (haveUnix) {
            try {
                ProcessBuilder psb = new ProcessBuilder();
                psb.command("who");
                Process p = psb.start();
                msg = readAll(p.getInputStream());
            } catch (IOException e) {
                msg = "Error reading users";
            }
        } else {
            msg = "Users not supported (linux not detected)";
        }
        write.write(StandardCharsets.UTF_8.encode(msg).array());
        write.flush();
    }

    private void handleProcesses() throws IOException // Sends ps command + shows running processes
    {
        String msg;
        if (haveUnix) {
            try {
                ProcessBuilder psb = new ProcessBuilder();
                psb.command("ps", "-e");
                Process p = psb.start();
                msg = readAll(p.getInputStream());
            } catch (IOException e) {
                msg = "Error reading processes";
            }
        } else {
            msg = "Processes not supported (linux not detected)";
        }
        write.write(StandardCharsets.UTF_8.encode(msg).array());
        write.flush();
    }

    static String readAll(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public SocketAddress getRemoteSocketAddress()
    {
        return client.getRemoteSocketAddress();
    }

}
