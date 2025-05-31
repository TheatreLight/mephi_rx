import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class Tests {
    @Test
    void testMap() {
        TestObserver<Integer> observer = new TestObserver<>();
        MephiObservable.just(1, 2, 3)
                .map(x -> x * 10)
                .subscribe(observer);

        assertEquals(List.of(10, 20, 30), observer.values);
        assertTrue(observer.complete);
    }

    @Test
    void testFilter() {
        TestObserver<Integer> observer = new TestObserver<>();
        MephiObservable.just(1, 2, 3, 4)
                .filter(x -> x % 2 == 0)
                .subscribe(observer);

        assertEquals(List.of(2, 4), observer.values);
        assertTrue(observer.complete);
    }

    @Test
    void testFlatMap() {
        TestObserver<String> observer = new TestObserver<>();
        MephiObservable.just("A", "B")
                .flatMap(ch -> MephiObservable.just(ch + "1", ch + "2"))
                .subscribe(observer);

        assertEquals(List.of("A1", "A2", "B1", "B2"), observer.values);
        assertTrue(observer.complete);
    }

    @Test
    void testDisposeStopsReceiving() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        MephiObservable<Integer> observable = new MephiObservable<>(emitter -> {
            new Thread(()-> {
                try {
                    emitter.onNext(1);
                    Thread.sleep(2000);
                    emitter.onNext(2);
                    Thread.sleep(2000);
                    emitter.onNext(3);
                    Thread.sleep(2000);
                    emitter.onComplete();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        });

        observable.subscribe(new MephiObserver<>() {
            MephiDisposable d;

            @Override
            public void onSubscribe(MephiDisposable d) {
                this.d = d;
            }

            @Override
            public void onNext(Integer value) {
                if (d.isDisposed()) return;
                result.add(value);
                d.dispose(); // отписка после первого значения
            }

            @Override public void onError(Throwable e) {}
            @Override public void onComplete() {}
        });
        latch.await(3, TimeUnit.SECONDS);
        List<Integer> expected = new ArrayList<>();
        expected.add(1);
        assertEquals(expected, result);
    }

    @Test
    void testLifecycleEvents() {
        TestObserver<String> observer = new TestObserver<>();

        MephiObservable<String> observable = new MephiObservable<>(emitter -> {
            emitter.onNext("OK");
            emitter.onComplete();
        });

        observable.subscribe(observer);

        assertTrue(observer.subscribed);
        assertEquals(List.of("OK"), observer.values);
        assertNull(observer.error);
        assertTrue(observer.complete);
    }

    @Test
    void testFixedThreadPoolScheduler() throws InterruptedException {
        MephiScheduler scheduler = MephiShedulers.computation();
        CountDownLatch latch = new CountDownLatch(1);
        List<String> output = new ArrayList<>();

        scheduler.execute(() -> {
            output.add("done");
            latch.countDown();
        });

        latch.await(1, TimeUnit.SECONDS);
        assertEquals(List.of("done"), output);
    }

    @Test
    void testCachedThreadPoolScheduler() throws InterruptedException {
        MephiScheduler scheduler = MephiShedulers.io();
        CountDownLatch latch = new CountDownLatch(1);

        scheduler.execute(() -> {
            latch.countDown();
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    @Test
    void testSingleThreadSchedulerSequentialExecution() throws InterruptedException {
        MephiScheduler scheduler = MephiShedulers.single();
        List<Integer> result = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(2);

        scheduler.execute(() -> {
            result.add(1);
            latch.countDown();
        });

        scheduler.execute(() -> {
            result.add(2);
            latch.countDown();
        });

        latch.await(1, TimeUnit.SECONDS);
        assertEquals(List.of(1, 2), result); // должен быть строгий порядок
    }


}
