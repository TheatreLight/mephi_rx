import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IOThreadScheduler implements MephiScheduler {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void execute(Runnable r) {
        executor.execute(r);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}
