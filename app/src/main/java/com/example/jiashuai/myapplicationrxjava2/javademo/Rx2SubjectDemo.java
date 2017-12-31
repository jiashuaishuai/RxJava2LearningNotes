package com.example.jiashuai.myapplicationrxjava2.javademo;

import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

/**
 * http://www.jianshu.com/p/1257c8ba7c0c
 * Subject:注意的是一定要用Subcect.create()的方式创建并使用，不要用just(T)、from(T)、create(T)创建，否则会导致失效...
 * RxJava中常见的Subject有4种，分别是** AsyncSubject、 BehaviorSubject、 PublishSubject、 ReplaySubject**。
 *
 * @author JiaShuai
 * @date 2017/12/21
 */

public class Rx2SubjectDemo {
    /**
     * AsyncSubject：永远只输出最后一个参数
     * 但是如果因为发生了错误而终止，AsyncSubject将不会发射任何数据，只是简单的向前传递这个错误通知。
     */
    public void asyncSubject() {
        AsyncSubject<String> subject = AsyncSubject.create();
        subject.onNext("a");
        subject.onNext("b");
        subject.onNext("c");
        subject.onNext("d");
        subject.subscribe(System.out::println);
        subject.onComplete();
    }

    /**
     * BehaviorSubject:会发送离订阅最近的上一个值和订阅后的所有值；没有上一个值得时候会发送默认值。
     * 如果遇到错误会直接
     */
    public void behaviorSubject() {
        BehaviorSubject<String> subject = BehaviorSubject.createDefault("xx");
//        subject.subscribe(System.out::println);//输出xx,a,b,c,d
        subject.onNext("a");
        subject.onNext("b");
//        subject.subscribe(System.out::println);        //这里订阅 输出：b,c,d
        subject.onNext("c");
        subject.onNext("d");
//        subject.subscribe(System.out::println);        //这里订阅 输出：d
        subject.onComplete();
        //这里订阅 没有回调
        subject.subscribe(System.out::println);
    }

    /**
     * 从那里订阅就从那里开始发送数据。
     */
    public void publishSubject() {
        PublishSubject<String> subject = PublishSubject.create();
        //a,b,c,d
        subject.onNext("a");
        subject.onNext("b");
        //c，d
        subject.onNext("c");
        subject.onNext("d");
        subject.onComplete();
        //无效
        subject.subscribe(System.out::println);

    }

    /**
     * 无论在哪里订阅全部发送
     */
    public void replaySubject(){
        ReplaySubject<String> subject = ReplaySubject.create();
        subject.onNext("a");
        subject.onNext("b");
        subject.onNext("c");
        subject.onNext("d");
        subject.onComplete();
        subject.subscribe(System.out::println);
    }

    public static void main(String[] arg) {
        Rx2SubjectDemo demo = new Rx2SubjectDemo();
//        demo.asyncSubject();
//        demo.behaviorSubject();
//        demo.publishSubject();
//        demo.replaySubject();
    }
}
