import io.reactivex.rxjava3.annotations.NonNull;

public interface MephiObserver<T> {
    void onSubscribe(MephiDisposable disposable);
    void onNext(@NonNull T item);
    void onError(@NonNull Throwable e);
    void onComplete();
}
