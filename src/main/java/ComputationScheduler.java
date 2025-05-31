import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComputationScheduler implements MephiScheduler {
    private final ExecutorService executor;
    private int cores;

    public ComputationScheduler() {
        cores = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cores);
    }

    @Override
    public void execute(Runnable r) {
        executor.execute(r);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}
