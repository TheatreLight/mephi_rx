import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Predicate;

import java.util.ArrayList;
import java.util.List;

public class MephiObservable<T>  {
    interface OnSubscribe<T> {
        void call(MephiObserver<T> observer) throws InterruptedException;
    }

    private final OnSubscribe<T> source;

    public MephiObservable(OnSubscribe<T> source) {
        this.source = source;
    }

    public MephiDisposable subscribe(MephiObserver<T> observer) {
        MephiDisposable disposable = new MephiDisposable() {
            private boolean disposed = false;
            @Override
            public void dispose() {
                disposed = true;
            }

            @Override
            public boolean isDisposed() {
                return disposed;
            }
        };
        observer.onSubscribe(disposable);

        try {
            source.call(observer);
        } catch(Exception e) {
            observer.onError(e);
        }
        return disposable;
    }

    public static <T> MephiObservable<T> just(T... value) {
        return new MephiObservable<T>(observer -> {
            for (var i : value) {
                observer.onNext(i);
            }
            observer.onComplete();
        });
    }
    public MephiObservable<T> map(Function<T, T> mapper) {
        return new MephiObservable<T>(observer->this.subscribe(new MephiObserver<T>(){
            MephiDisposable d;
            @Override
            public void onSubscribe(MephiDisposable disposable) {
                d = disposable;
            }

            @Override
            public void onNext(T item) {
                try {
                    if (!d.isDisposed()) {
                        observer.onNext(mapper.apply(item));
                    }
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(Throwable e) {
                observer.onError(e);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        }));
    }
    public MephiObservable<T> filter(Predicate<T> predicate) {
        return new MephiObservable<T>(observer -> this.subscribe(new MephiObserver<T>() {
            MephiDisposable d;
            @Override
            public void onSubscribe(MephiDisposable disposable) {
                d = disposable;
            }

            @Override
            public void onNext(T item) {
                try {
                    if (d.isDisposed()) return;
                    if (predicate.test(item)) {
                        observer.onNext(item);
                    }
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(Throwable e) {
                observer.onError(e);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        }));
    }

    public MephiObservable<T> subscribeOn(MephiScheduler scheduler) {
        return new MephiObservable<>(observer -> {
            scheduler.execute(()->this.subscribe(observer));
        });
    }

    public MephiObservable<T> observeOn(MephiScheduler scheduler) {
        return new MephiObservable<>(observer -> this.subscribe(new MephiObserver<T>() {
            MephiDisposable d;

            @Override
            public void onSubscribe(MephiDisposable disposable) {
                d = disposable;
            }

            @Override
            public void onNext(T item) {
                if (d.isDisposed())  {
                    scheduler.shutdown();
                    return;
                }
                scheduler.execute(()->observer.onNext(item));
            }

            @Override
            public void onError(Throwable e) {
                scheduler.execute(()->observer.onError(e));
            }

            @Override
            public void onComplete() {
                scheduler.execute(observer::onComplete);
            }
        }));
    }

    public <R> MephiObservable<R> flatMap(Function<? super T, MephiObservable<R>> mapper) {
        return new MephiObservable<>(downstream ->
                MephiObservable.this.subscribe(new MephiObserver<T>() {
                    final List<MephiDisposable> innerDisposables = new ArrayList<>();

                    @Override
                    public void onSubscribe(MephiDisposable d) {
                        downstream.onSubscribe(d); // пробрасываем
                    }

                    @Override
                    public void onNext(T value) {
                        try {
                            MephiObservable<R> inner = mapper.apply(value);
                            inner.subscribe(new MephiObserver<R>() {
                                @Override
                                public void onSubscribe(MephiDisposable d) {
                                    innerDisposables.add(d);
                                }

                                @Override
                                public void onNext(R innerValue) {
                                    downstream.onNext(innerValue);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    downstream.onError(e);
                                }

                                @Override
                                public void onComplete() {
                                    // ничего не делаем: ждём всех
                                }
                            });
                        } catch (Throwable e) {
                            downstream.onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        downstream.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        downstream.onComplete(); // ⚠️ упростим: вызываем сразу
                    }
                })
        );
    }

}
