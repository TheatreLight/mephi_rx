public class MephiShedulers {
    public static MephiScheduler io() {
        return new IOThreadScheduler();
    }

    public static MephiScheduler computation() {
        return new ComputationScheduler();
    }

    public static MephiScheduler single() {
        return new SingleThreadScheduler();
    }
}
