public class BenchmarkResult {
    boolean success;
    long responseSize;
    long duration;

    public BenchmarkResult(boolean success, long responseSize, long duration)
    {
        this.success = success;
        this.responseSize = responseSize;
        this.duration = duration;
    }

    public boolean wasSuccess()
    {
        return success;
    }

    public long getResponseSize()
    {
        return responseSize;
    }

    public long getDuration()
    {
        return duration;
    }
}
