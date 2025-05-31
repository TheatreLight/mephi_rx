public interface MephiScheduler {
    void execute(Runnable r);
    void shutdown();
}
