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

    /*
    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public void setResponseSize(long responseSize)
    {
        this.responseSize = responseSize;
    }

    public void setDuration(double duration)
    {
        this.duration = duration;
    }
*/
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
