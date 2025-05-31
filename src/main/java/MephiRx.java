import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Flow;

public class MephiRx {
    public static void main(String[] args) {
        MephiObservable<String> mObservable = MephiObservable.just("Sziasztok!", "Szia", "Jo napot");
        MephiObserver<String> mObserver = new MephiObserver<String>() {
            MephiDisposable d;
            @Override
            public void onSubscribe(MephiDisposable disposable) {
                d = disposable;
            }

            @Override
            public void onNext(String item) {
                if (d.isDisposed()) return;
                System.out.println("Received: " + item);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Error: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                if (d.isDisposed()) return;
                System.out.println("Completed");
            }
        };
        var d = mObservable.subscribe(mObserver);
    }
}
