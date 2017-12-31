package com.example.jiashuai.myapplicationrxjava2.javademo;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by JiaShuai on 2017/12/13.
 */

public class Rx2Demo1 {

    /**
     * 基本创建
     */
    public void observableOnsubscrib() {
        Observable.create(new ObservableOnSubscribe<String>() {
            /**
             *
             * @param e 发射
             * @throws Exception 抛异常
             */
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("Hello");
                e.onNext("Hi");
                e.onNext("不输出");
                e.onComplete();
            }
        }).subscribe(new Observer<String>() {
            Disposable disposable;

            /**
             *
             * @param d 用于停止发送
             */
            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(String s) {
                System.out.println(s);
                if ("Hi".equals(s)) {
                    disposable.dispose();
                    onComplete();
                }

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                System.out.println("完成");
            }
        });
    }

    /**
     * 不能为空
     */
    public void cantBeNulls() {

        /**
         * 不能发送空值
         * The item is null
         */
        Observable.just(null).subscribe(System.out::println);

    }

    /**
     * 发送自定义类型
     */
    public void customType() {
        Observable.fromArray(CustomType.values()).subscribe(new Consumer<CustomType>() {
            @Override
            public void accept(CustomType customType) throws Exception {
                System.out.println(customType.getValue());
            }
        });
    }

    enum CustomType {
        TYPE_HE("hehehe"),
        TYPE_HI("hi"),
        TYPE_HELLO("Hello");

        private String value;

        CustomType(String s) {
            value = s;
        }

        public String getValue() {
            return value;
        }
    }

    public void backpressure() {
        Flowable.interval(1, TimeUnit.MILLISECONDS).observeOn(Schedulers.newThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                Thread.sleep(2000);
                System.out.println(aLong);
            }
        });
    }


    public void flowable() {

        /**
         * 实例1
         */
        Flowable.range(0, 10)
                .subscribe(new Subscriber<Integer>() {
                    Subscription sub;

                    //当订阅后，会首先调用这个方法，其实就相当于onStart()，
                    //传入的Subscription s参数可以用于请求数据或者取消订阅
                    @Override
                    public void onSubscribe(Subscription s) {
                        System.out.println("onsubscribe start");
                        sub = s;
                        sub.request(1);
                        System.out.println("onsubscribe end");
                    }

                    @Override
                    public void onNext(Integer o) {
                        System.out.println("onNext--->" + o);
                        //证明o==3的时候向上游请求两个元素，4和5；4直接return，不继续发送仍然会接收到5，所以证明了  sub.request(2)；发送两个元素
                        if (o == 4) {
                            return;
                        }
                        if (o == 3) {
                            //向上游请求2个元素
                            sub.request(2);
                        } else {
                            if (o < 7) {
                                //向上游请求1个元素
                                sub.request(1);
                            } else {
                                /**
                                 * 停止发送数据，并且清理资源
                                 */
                                sub.cancel();
                            }

                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("onComplete");
                    }
                });

/**
 * 实例2
 */

        Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(FlowableEmitter<String> e) throws Exception {
                e.onNext("Hello1");
                e.onNext("Hello2");
                e.onNext("Hello3");
                e.onNext("Hello4");
                e.onComplete();
            }
            // //需要指定背压策略
        }, BackpressureStrategy.BUFFER)
                .subscribe(new Subscriber<String>() {
                    Subscription su;

                    //当订阅后，会首先调用这个方法，其实就相当于onStart()，
                    //传入的Subscription s参数可以用于请求数据或者取消订阅
                    @Override
                    public void onSubscribe(Subscription s) {
                        System.out.println("onSubscribe start");
                        su = s;
                        su.request(1);
                        System.out.println("onSubscribe end");
                    }

                    @Override
                    public void onNext(String s) {
                        System.out.println("onNext--->".concat(s));
                        su.request(1);
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();

                    }

                    @Override
                    public void onComplete() {
                        System.out.println("onComplete");
                    }
                });

    }

    public void single() {
        Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> e) throws Exception {
                //只能发送一个
                e.onSuccess("Hello");
            }
        }).subscribe(new SingleObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(String s) {

            }

            @Override
            public void onError(Throwable e) {

            }
        });
        /**
         * 只能发送一个
         */
        Single.just("Hello2").subscribe(new BiConsumer<String, Throwable>() {
            @Override
            public void accept(String s, Throwable throwable) throws Exception {
                System.out.println("accept  " + s);
            }
        });
    }

    public void completable() {
        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                Thread.sleep(2000);
                e.onComplete();
            }
        }).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("开始");
            }

            @Override
            public void onComplete() {
                System.out.println("完成");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("异常");
                e.printStackTrace();
            }
        });

        Completable.complete().subscribe(new Action() {
            @Override
            public void run() throws Exception {
                System.out.println(Thread.currentThread().getName() + "完成");
            }
        });
    }

    public void maybe() {
        Maybe.create(new MaybeOnSubscribe<String>() {
            @Override
            public void subscribe(MaybeEmitter<String> e) throws Exception {
                //只调用Observer.onComplete
//                e.onComplete();
                //值调用Observer.onSuccess
                e.onSuccess("hello");
            }
        }).subscribe(new MaybeObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("开始");
            }

            @Override
            public void onSuccess(String s) {
                System.out.println("成功" + s);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("异常");
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("完成");
            }
        });

        Maybe.just("Hello").subscribe(new MaybeObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("开始");
            }

            @Override
            public void onSuccess(String s) {
                System.out.println("成功" + s);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("异常");
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("完成");
            }
        });

        Maybe.empty().subscribe(new MaybeObserver<Object>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("开始");
            }

            @Override
            public void onSuccess(Object s) {
                System.out.println("成功" + s);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("异常");
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("完成");
            }
        });
    }

    public static void main(String[] arg) {
        Rx2Demo1 rx2 = new Rx2Demo1();

//        rx2.observableOnsubscrib();
//        rx2.cantBeNulls();
//        rx2.customType();
//        rx2.backpressure();
//        rx2.flowable();
//        rx2.single();
//        rx2.completable();
        rx2.maybe();
    }


}
