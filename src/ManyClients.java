import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Creates and controls a number of clients to do the same thing at a time.
public class ManyClients {

    List<Project1Client> clients;
    public ManyClients(int count, InetAddress remoteAddress, int port) throws IOException
    {
        clients = new ArrayList<>();
        for (int i = 0; i < count; ++i)
        {
            clients.add(new Project1Client(remoteAddress, port));
        }
    }

    public BenchmarkResult[] timeOperation(Project1Client.Operation op)
    {
        TimeIt[] tests = new TimeIt[clients.size()];
        ExecutorService pool = Executors.newCachedThreadPool();
        // Start all tests.
        for (int i = 0; i < clients.size(); ++i)
        {
            Project1Client c = clients.get(i);
            pool.execute(new TimeIt(c, op));
        }

        // Wait for tests to finish and collect results.
        pool.shutdown();
        try {
            pool.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException ex)
        { return null; }

        BenchmarkResult[] results = new BenchmarkResult[clients.size()];
        for (int i = 0; i < clients.size(); ++i)
        {
            results[i] = tests[i].getResult();
        }

        return results;
    }


    class TimeIt extends Thread
    {
        private Project1Client client;
        private Project1Client.Operation op;
        private BenchmarkResult result = null;

        public TimeIt(Project1Client client, Project1Client.Operation operation)
        {
            this.client = client;
            op = operation;
        }

        public BenchmarkResult getResult()
        {
            return result;
        }

        @Override
        public void run()
        {
            try {
                long startTime = System.nanoTime();
                String resp = client.doRequest(op);
                long stopTime = System.nanoTime();

                result = new BenchmarkResult(true, resp.length(), stopTime - startTime);
                /*
                result.setResponseSize(resp.length());
                result.setSuccess(true);
                result.setDuration(time);
                */
            }
            catch (IOException ex)
            {
                result = new BenchmarkResult(false, 0, 0);
            }
        }
    }

}
