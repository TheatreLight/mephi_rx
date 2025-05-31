import java.util.ArrayList;
import java.util.List;

class TestObserver<T> implements MephiObserver<T> {
    List<T> values = new ArrayList<>();
    Throwable error;
    boolean complete = false;
    boolean subscribed = false;

    @Override
    public void onSubscribe(MephiDisposable d) {
        subscribed = true;
    }

    @Override
    public void onNext(T value) {
        values.add(value);
    }

    @Override
    public void onError(Throwable e) {
        error = e;
    }

    @Override
    public void onComplete() {
        complete = true;
    }
}

