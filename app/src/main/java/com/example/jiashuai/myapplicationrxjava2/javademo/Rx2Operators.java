package com.example.jiashuai.myapplicationrxjava2.javademo;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.Timed;

import static java.lang.System.out;

/**
 * Created by JiaShuai on 2017/12/20.
 */

public class Rx2Operators {
    private String[] strings = {"a", "b", "c", "b", "a", "c", "d", "d", "a", "c"};

    /**********************************创建***********************************/

    private Observable<Long> intervalData() {
        return Observable.interval(1, TimeUnit.SECONDS, Schedulers.trampoline());
    }

    private Observable<Integer> intData() {
        return Observable.range(0, 10);
    }

    public void range() {
        intData().subscribe(out::println);
    }

    public void timer() {
        Observable.timer(2, TimeUnit.SECONDS, Schedulers.trampoline()).flatMap(new Function<Long, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Long aLong) throws Exception {
                return Observable.just("Hello");
            }
        }).subscribe(out::println);
    }

    /**********************************变换***********************************/

    public void cast(){
        Observable.just(1,2,3,"3.44",4,5).cast(Integer.class).subscribe(out::println);
    }
    public void buffer() {
        intData().buffer(2).subscribe(out::println);
    }

    public void groupBy() {
        intData().groupBy(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) throws Exception {
                return integer / 3;
            }
        }).flatMap(new Function<GroupedObservable<Integer, Integer>, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(GroupedObservable<Integer, Integer> integerIntegerGroupedObservable) throws Exception {
                out.println();
                out.println("Group: " + integerIntegerGroupedObservable.getKey());
                return integerIntegerGroupedObservable;
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                out.print(integer + ",");
            }
        });
    }

    public void scan() {
        intData().scan(new BiFunction<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        }).subscribe(i -> out.print(i + ","));
    }

    public void window() {
        intervalData().take(10).window(3, TimeUnit.SECONDS).flatMap(new Function<Observable<Long>, ObservableSource<Long>>() {
            @Override
            public ObservableSource<Long> apply(Observable<Long> longObservable) throws Exception {
                out.println(Thread.currentThread().getName() + "-------------------");
                return longObservable;
            }
        }).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                out.println(Thread.currentThread().getName() + "--" + aLong);
            }
        });
    }
    public void flatMapIterable(){
        Observable.just(2,3,4).flatMapIterable(new Function<Integer, Iterable<String>>() {
            @Override
            public Iterable<String> apply(Integer integer) throws Exception {
                return Arrays.asList(integer+"",integer*100+"");
            }
        }).subscribe(out::println);
    }
    public void concatMap(){
        Observable.just(2,3,4,5).concatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                return Observable.just(integer+"");
            }
        }).subscribe(out::println);
    }
    public void switchMap() {
        dataSource().switchMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                return Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> e) throws Exception {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ue) {
                            if (!e.isDisposed()) {
                                e.onError(ue);
                            }
                        }
                        e.onNext(s);
                        e.onComplete();
                    }
                }).subscribeOn(Schedulers.io());
            }
        }).observeOn(Schedulers.trampoline()).subscribe(out::println);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**********************************过滤***********************************/


    public void debounce() {

        intData().doOnNext(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Thread.sleep(integer * 1000);
            }
        })//小于五秒的会被过滤
                .debounce(5, TimeUnit.SECONDS).subscribe(out::println);

//
//        Observable.create(new ObservableOnSubscribe<Integer>() {
//            @Override
//            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
//                if (e.isDisposed()) {
//                    return;
//                }
//                for (int i = 0; i < 10; i++) {
//                    e.onNext(i);
//                    try {
//                        Thread.sleep(i * 1000);
//                    } catch (InterruptedException er) {
//                        er.printStackTrace();
//                    }
//                }
//                e.onComplete();
//
//            }
//        }).take(10).debounce(5, TimeUnit.SECONDS).subscribe(new Consumer<Integer>() {
//            @Override
//            public void accept(Integer aLong) throws Exception {
//                System.out.println(aLong);
//            }
//        });
    }

    private Observable<String> dataSource() {
        return Observable.fromArray(strings);
    }

    public void distinct() {
        dataSource().distinct(new Function<String, String>() {
            @Override
            public String apply(String s) throws Exception {
                out.println("去重前     " + s);
                return s;
            }
        }).subscribe(out::println);
    }

    public void elementAt() {
        dataSource().elementAt(4).subscribe(out::println);
    }

    public void filter() {
        dataSource().filter(new Predicate<String>() {
            @Override
            public boolean test(String s) throws Exception {
                return "a".equals(s);
            }
        }).subscribe(out::println);
    }

    public void ofType() {
        Observable.just(1, 2, 3, "Hello", 2.2).ofType(String.class).subscribe(out::println);
    }

    public void firstAndLast() {
        dataSource().first("haha").subscribe(out::println);
        dataSource().lastElement().subscribe(out::println);
    }

    public void ignoreElements() {
        dataSource().ignoreElements().subscribe(() -> out.println("完成"));
    }

    public void sample() {
        intervalData().take(10).sample(3, TimeUnit.SECONDS).subscribe(out::println);
    }

    public void skip() {
        intData().skip(3).subscribe(out::println);
        System.out.println("----------");
        intData().skipLast(4).subscribe(out::println);
    }

    public void take() {
        intData().take(3).subscribe(out::println);
        System.out.println("----------");
        intData().takeLast(4).subscribe(out::println);
    }



    /**********************************组合***********************************/
    public void combineLatest() {
        Observable<String> o1 = intervalData().map(aLong -> strings[Integer.parseInt(aLong.toString())]).take(strings.length).subscribeOn(Schedulers.newThread());
        Observable<String> o2 = intervalData().map(aLong -> strings[Integer.parseInt(aLong.toString())]).take(strings.length);


        Observable.combineLatest(o1, o2, new BiFunction<String, String, String>() {
            @Override
            public String apply(String integer, String integer2) throws Exception {
                return integer + " -- " + integer2;
            }
        }).subscribe(out::println);
    }

    public void merge() {
        Observable.merge(Observable.interval(1, TimeUnit.SECONDS).map(new Function<Long, String>() {
                    @Override
                    public String apply(Long aLong) throws Exception {
                        return strings[Integer.parseInt(aLong.toString())];
                    }
                }).take(strings.length),
                Observable.interval(2, TimeUnit.SECONDS, Schedulers.trampoline()).take(strings.length))
                .subscribe(out::println);
    }

    public void startWith() {
        dataSource().startWith("Hi").subscribe(out::println);
        dataSource().startWith(new ObservableSource<String>() {
            @Override
            public void subscribe(Observer<? super String> observer) {
                observer.onNext("Hi");
                observer.onNext("Hello");
                observer.onComplete();
            }
        }).subscribe(out::println);
//        dataSource().startWith(new Iterable<String>() {})//可迭代的，不会先放着，
    }

    public void concat() {
        Observable.concat(Observable.interval(1, TimeUnit.SECONDS).map(new Function<Long, String>() {
                    @Override
                    public String apply(Long aLong) throws Exception {
                        return strings[Integer.parseInt(aLong.toString())];
                    }
                }).take(strings.length),
                Observable.interval(2, TimeUnit.SECONDS, Schedulers.trampoline()).take(strings.length))
                .subscribe(out::println);
    }

    public void zip() {
        Observable.zip(dataSource(), intData(), new BiFunction<String, Integer, String>() {
            @Override
            public String apply(String s, Integer integer) throws Exception {
                return s + "--" + integer;
            }
        }).subscribe(out::println);
    }

    public void switchOnNext() {
        Observable.switchOnNext(new ObservableSource<ObservableSource<?>>() {
            @Override
            public void subscribe(Observer<? super ObservableSource<?>> observer) {
                /**
                 * 异步发射一个Observable
                 */
                observer.onNext(intervalData().subscribeOn(Schedulers.io()).map(new Function<Long, String>() {
                    @Override
                    public String apply(Long aLong) throws Exception {
                        return strings[Integer.parseInt(aLong.toString())];
                    }
                }).take(strings.length));
                try {
                    /**
                     * 听三秒
                     */
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /**
                 * 在发射一个
                 */
                observer.onNext(intData());
                observer.onComplete();

            }
        }).observeOn(Schedulers.trampoline()).subscribe(out::println);
        /**
         a
         b
         0
         1
         2
         3
         4
         5
         6
         7
         8
         9
         */
    }

    public void join() {
        intervalData().map(new Function<Long, String>() {
            @Override
            public String apply(Long aLong) throws Exception {
                return strings[Integer.parseInt(aLong.toString())];
            }
        }).take(strings.length).subscribeOn(Schedulers.io())

                .join(intervalData().subscribeOn(Schedulers.io()).take(strings.length), new Function<String, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(String s) throws Exception {
                        return Observable.interval(2, TimeUnit.SECONDS, Schedulers.io());
                    }
                }, new Function<Long, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(Long aLong) throws Exception {
                        return Observable.timer(0, TimeUnit.SECONDS, Schedulers.io());
                    }
                }, new BiFunction<String, Long, String>() {
                    @Override
                    public String apply(String s, Long aLong) throws Exception {
                        return s + "--" + aLong;
                    }

                })
                .subscribeOn(Schedulers.io()).observeOn(Schedulers.trampoline())
                .subscribe(out::println);


    }

    public void throttleFirst() {
        dataSource().delay(1, TimeUnit.SECONDS, Schedulers.trampoline()).subscribe(out::println);
    }

    /**********************************工具***********************************/

    public void delay() {

        intData().delay(2, TimeUnit.SECONDS, Schedulers.trampoline()).subscribe(out::println);
    }

    public void materializeAndDematerialize() {
        List<Notification<Integer>> list = new ArrayList<>();
        intData().materialize().subscribe(new Consumer<Notification<Integer>>() {
            @Override
            public void accept(Notification<Integer> integerNotification) throws Exception {
                list.add(integerNotification);
                out.println("通知。。。。。");
                if (integerNotification.isOnNext()) {
                    System.out.println(integerNotification.getValue());
                }
                if (integerNotification.isOnComplete()) {
                    out.println("完成");
                }
            }
        });

        Observable.fromIterable(list).dematerialize().subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                System.out.println(o.toString());
            }
        });
    }

    public void timestamp() {

        dataSource().timestamp(Schedulers.trampoline()).subscribe(new Consumer<Timed<String>>() {
            @Override
            public void accept(Timed<String> stringTimed) throws Exception {
                out.println(stringTimed.value() + "   ----------   " + stringTimed.time());
            }
        });
        out.println("******************************");
        intervalData().take(5).timestamp().subscribe(new Consumer<Timed<Long>>() {
            @Override
            public void accept(Timed<Long> longTimed) throws Exception {
                out.println(longTimed.value() + "   ----------   " + longTimed.time());
            }
        });
    }

    public void timeInterval() {
        intervalData().take(5).timeInterval().subscribe(new Consumer<Timed<Long>>() {
            @Override
            public void accept(Timed<Long> longTimed) throws Exception {
                out.println(longTimed.value() + "   ----------   " + longTimed.time());
            }
        });
    }

    public void serialize() {
        dataSource().serialize().subscribe(out::println);
    }

    public void cache() {

        Observable<String> observable = intervalData().take(5).map(new Function<Long, String>() {
            @Override
            public String apply(Long aLong) throws Exception {
                out.println("isCache  " + aLong);
                return "cache  " + aLong;
            }
        }).cache();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        observable.subscribe(out::println);
    }

    public void doOnEach() {
        dataSource().doOnEach(new Consumer<Notification<String>>() {
            @Override
            public void accept(Notification<String> stringNotification) throws Exception {
                out.println(stringNotification.getValue() + " next： " + stringNotification.isOnNext());
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                out.println(s);
            }
        });
    }

    public void doOnCompleted() {
        dataSource().doOnComplete(new Action() {
            @Override
            public void run() throws Exception {
                out.println("成功");
            }
        }).subscribe(out::println);
//dataSource().doOnError
    }


    public void doOnTerminate() {
        dataSource().doOnTerminate(new Action() {
            @Override
            public void run() throws Exception {
                out.println("doOnTerminate 完成，");
            }
        }).doAfterTerminate(new Action() {
            @Override
            public void run() throws Exception {
                out.println("doAfterTerminate 完成，");
            }
        }).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                out.println("doFinally 完成，");
            }
        }).subscribe(out::println);
    }

    public void doOnSubscribe() {
        Observable<Long> observable = intervalData().take(5).doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                out.println("订阅：" + disposable.isDisposed());
            }
        }).doOnDispose(new Action() {
            @Override
            public void run() throws Exception {
                out.println("取消订阅时调用");
            }
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        observable.subscribe(new Observer<Long>() {
            Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(Long aLong) {
                out.println(aLong);
                if (aLong == 3) {
                    disposable.dispose();
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

    }

    public void delaySubscription() {
        /**
         * 延时指定时间
         */
//        dataSource().delaySubscription(5,TimeUnit.SECONDS,Schedulers.trampoline()).subscribe(out::println);
        /**
         * 延时dataSource订阅；直到intervalData发出第一个元素或者执行完成
         */
        dataSource().delaySubscription(intervalData().take(10).doOnNext(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                out.println(aLong);
            }
        })).subscribe(out::println);
    }

    public void using() {
        Observable.using(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return null;
            }
        }, new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                return null;
            }
        }, new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {

            }
        });
    }

    public void single() {
        /**
         * java.lang.IllegalArgumentException: Sequence contains more than one element!
         */
        dataSource().single("默认值").subscribe(out::println);
    }


    /**********************************条件/布尔操作***********************************/

    public void all() {
        intData().all(new Predicate<Integer>() {
            @Override
            public boolean test(Integer integer) throws Exception {
                return integer >= 0;
            }
        }).subscribe(out::println);
    }

    public void contains() {
        dataSource().contains("c").subscribe(out::println);
    }

    public void sequenceEqual() {
        Observable.sequenceEqual(Observable.just(1, 2, 3, 4), Observable.just(1, 2, 3, 4).subscribeOn(Schedulers.io()))
                .observeOn(Schedulers.trampoline())
                .subscribe(out::println);

    }

    public void switchIfEmpty() {
        Observable.empty().switchIfEmpty(dataSource()).subscribe(out::println);
    }

    public void defaultIfEmpty() {
        Observable.empty().defaultIfEmpty("hahahaha").subscribe(out::println);
    }

    public void takeUntil() {
        /**
         * takeUntil不包含
         * takeWhile包含
         *
         */
        dataSource().takeUntil(new Predicate<String>() {
            @Override
            public boolean test(String s) throws Exception {
                return "d".equals(s);
            }
        }).subscribe(out::println);
    }

    /*********************聚合*******************************************/

    public void reduce() {
        intData().reduce(new BiFunction<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        }).subscribe(out::println);
    }

    public void collect() {
        dataSource().collect(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return new ArrayList<>();
            }
        }, new BiConsumer<List<String>, String>() {
            @Override
            public void accept(List<String> strings, String s) throws Exception {
                strings.add(s);
            }
        }).subscribe(new BiConsumer<List<String>, Throwable>() {
            @Override
            public void accept(List<String> strings, Throwable throwable) throws Exception {
                out.println(strings.size());


                Observable.fromIterable(strings).subscribe(out::println);
            }
        });
    }

    public void count() {
        dataSource().count().subscribe(out::println);
    }

    /***********************错误处理/重试机制*************************/

    public void onErrorResumeNext() {
        Observable.just(1, "2", 3).cast(Integer.class).onErrorResumeNext(Observable.just(1, 2, 3, 4)).subscribe(out::println);
    }
    public void onErrorReturn(){
        Observable.just(1, "2", 3).cast(Integer.class).onErrorReturnItem(4).subscribe(out::println);
    }
    public void retry(){
        Observable.just(1,"2",3).cast(Integer.class).retry(2).subscribe(out::println);
    }
    public void retryWhen(){
        Observable.just(1,"2",3).cast(Integer.class).retryWhen(new Function<Observable<Throwable>, ObservableSource<Long>>() {
            @Override
            public ObservableSource<Long> apply(Observable<Throwable> throwableObservable) throws Exception {
                return Observable.interval(2,TimeUnit.SECONDS, Schedulers.trampoline());
            }
        }).subscribe(out::println);
    }
    /********************************链接******************************/
    public void connectableObservable(){
        ConnectableObservable<String> co = dataSource().publish();
        co.subscribe(out::println);
        out.println("订阅");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        co.connect();
    }

    public static void main(String[] arg) {
        Rx2Operators rx = new Rx2Operators();

        //创建
//        rx.range();
//        rx.timer();

        //变换
//        rx.switchMap();
//        rx.buffer();
//        rx.groupBy();
//        rx.scan();
//        rx.window();
//        rx.cast();
//        rx.flatMapIterable();
//        rx.concatMap();

        //过滤
//        rx.debounce();
//        rx.distinct();
//        rx.elementAt();
//        rx.filter();
//        rx.ofType();
//        rx.firstAndLast();
//        rx.ignoreElements();
//        rx.sample();
//        rx.skip();
//        rx.take();
//        rx.throttleFirst();

        //组合
//        rx.combineLatest();
//        rx.merge();
//        rx.startWith();
//        rx.concat();
//        rx.zip();
//        rx.switchOnNext();
//        rx.join();

        //工具
//        rx.delay();
//        rx.materialize();
//        rx.timestamp();
//        rx.timeInterval();
//        rx.serialize();
//        rx.cache();
//        rx.doOnEach();
//        rx.doOnCompleted();
//        rx.doOnTerminate();
//        rx.doOnSubscribe();
//        rx.delaySubscription();
//        rx.single();

        //条件/布尔操作
//        rx.all();
//        rx.contains();
//        rx.sequenceEqual();
//        rx.switchIfEmpty();
//        rx.defaultIfEmpty();
//        rx.takeUntil();

        //聚合
//        rx.reduce();
//        rx.collect();
//        rx.count();

        //错误处理
//        rx.onErrorResumeNext();
//        rx.onErrorReturn();
//        rx.retry();
//        rx.retryWhen();

        //链接
        rx.connectableObservable();

    }


}
