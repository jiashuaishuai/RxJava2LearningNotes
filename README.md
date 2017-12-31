# RxJava2
RxJava 2.0 已经按照Reactive-Streams specification规范完全的重写了。2.0 已经独立于RxJava 1.x而存在；因为Reactive-Streams有一个不同的构架，它改变了一些以往RxJava的用法。

## 为什么要使用ReactiveX
1. **函数式编程：** 对可观察数据流使用无副作用的输入输出函数，避免了程序里错综复杂的状态
2. **精简代码：** Rx的操作符经常可以将复杂的难题简化为很少的代码
3. **更好的处理异步任务** 传统的try/catch对于异步计算过程中的错误无能为力，但是Rx提供了更好的错误处理机制。
4. **轻松处理并发：** Rx的Observable和Scheduler让开发者可以摆脱底层的线程同步和各种并发问题

## 事件源关键类
1. Publisher：
2. Flowable：支持背压
3. Observable：不支持背压
4. Single：一个只包含一个item或error的流（只有一个数据，且只有成功和失败）
5. Completable：一个不包含任何item只包含completion或error信号的流（没有任何数据流，只有成功或失败）
6. Maybe：一个只包含一个maybe value或者error的流

### Publisher

```java
/**
*一个可能无限数量的序列元素的提供者，根据从其Subscriber接收到的请求发送他们，
*Publisher可以在不同的时间点动态的服务多个通过subscribe(Subscriber)方法订阅的Subscriber
*/
public interface Publisher<T> {
    /**
    *请求Publisher开始数据流，
    *这是一个可以被多次调用的‘工厂方法’但每次都会开始一个新的Subsription。
    *每个Subscription将只会为一个Subscriber工作
    *一个Subscriber应该只能向一个Publisher订阅一次
    *如果Publisher拒绝了订阅尝试或者订阅失败了，它将通过Subscriber#onError发送error信号
    */
    public void subscribe(Subscriber<? super T> s);
}
```

调用Publisher.subscribe(Subscriber)后Subscriber的响应可能是这样的方法调用序列：

`onSubscribe onNext*(onError|onComplete)?`

### io.reactivex.Flowable 和 io.reactivex.Observable

1. **Flowable:** 可以发送0...N个流，支持Reactive-Streams和背压(backpressure)
2. **Observable** 可以发送0...N个流，不支持背压

![](media/15131465248020/15136654674509.png)

Flowable实例：

```java
 Flowable.range(0,10)
                .subscribe(new Subscriber<Integer>() {
                    Subscription sub;
                    /**
                 * 在调用Publisher#subscribe(Subscriber)之后调用
                 * 直到Subscription#request(long)被调用才开始数据流
                 * 每当需要更多数据时，这个Subscriber实例都有责任调用Subscription#request(long)
                 * Publisher只有在Subscription#request(long)被调用后才会发送通知
                 * @param s 可以通过Subscription#request(long)方法请求数据的Subscription
                 */
                    @Override
                    public void onSubscribe(Subscription s) {
                        System.out.println("onsubscribe start");
                        sub=s;
                        sub.request(1);
                        System.out.println("onsubscribe end");
                    }

                    /**
                    *Publisher发送的数据通知，以响应Subscription#request(long)的请求
                    */
                    @Override
                    public void onNext(Integer o) {
                        System.out.println("onNext--->"+o);
                        /**
                        *证明o==3的时候向上游请求两个元素，4和5；
                        *4直接return，不继续发送仍然会接收到5，
                        *所以证明了  sub.request(2)；发送两个元素
                        */
                        if (o==4) {
                            return ;
                        }
                        if (o==3) {
                            //向上游请求2个元素
                            sub.request(2);
                        }else {
                            if (o<7) {
                                //向上游请求1个元素
                                sub.request(1);
                            }else {
                                /**
                                 * 停止发送数据，并且清理资源
                                 */
                                sub.cancel();
                            }

                        }
                    }
                    /**
                    * 失败的结束状态
                    * 即使再次调用Subscription#request(long)也不会再发送进一步事件
                    */
                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }
                    /**
                    * 成功的结束状态
                    * 即使再次调用Subscription#request(long)也不会再发送进一步事件
                    */
                    @Override
                    public void onComplete() {
                        System.out.println("onComplete");
                    }
                });
```
#### Subscriber:

```java
/**
 * 将Subscriber的一个实例传给Publisher#subscribe(Subscriber)方法后，将会收到一次onSubscribe(Subscription)方法调用
 * 在Subscription#request(long)被调用之前不会受到进一步通知
 * 在发送请求信号后:
 * 调用一到多次onNext(Object)方法，最多调用Subscription#request(long)中定义的次数
 * 调用一次onError(Throwable)或onComplete()方法表明终止状态，之后不会发送进一步事件
 * 只要Subscriber实例还能够处理，就可以通过Subscription#request(long)发请求信号
 *
 * @param <T> 信号元素类型
 */
public interface Subscriber<T> {
    /**
     * 在调用Publisher#subscribe(Subscriber)之后调用
     * 直到Subscription#request(long)被调用才开始数据流
     * 每当需要更多数据时，这个Subscriber实例都有责任调用Subscription#request(long)
     * Publisher只有在Subscription#request(long)被调用后才会发送通知
     *
     * @param s 可以通过Subscription#request(long)方法请求数据的Subscription
     */
    public void onSubscribe(Subscription s);

    /**
     * Publisher发送的数据通知，以响应Subscription#request(long)的请求
     *
     * @param t the element signaled
     */
    public void onNext(T t);

    /**
     * 失败的结束状态
     * 即使再次调用Subscription#request(long)也不会再发送进一步事件
     *
     * @param t the throwable signaled
     */
    public void onError(Throwable t);

    /**
     * 成功的结束状态
     * 即使再次调用Subscription#request(long)也不会再发送进一步事件
     */
    public void onComplete();
}
```


#### Subscription：

```java
package org.reactivestreams;

/**
 * Subscription 和 Subscriber一对一生命周期。
 * 只能由一个{@link Subscriber}使用一次。
 * 用于表示对数据的需求和取消需求（并允许资源清理）。
 */
public interface Subscription {
    /**
     * 在通过此方法发送需求之前，不会有任何事件由{@link Publisher}发送。
     * 不管何时需要，它都可以被调用，但未完成的累积需求不得超过Long.MAX_VALUE。
     * Long.MAX_VALUE的未完成累积需求可能会被{@link Publisher}视为“有效无限”。
     * 如果流结束，则发送者可以发送少于请求次数的请求，但是必须发出{@link Subscriber＃onError（Throwable）}或{@link Subscriber＃onComplete（）}。
     * 
     * @param 向上游请求的元素的严格正数{@link Publisher}
     */
    public void request(long n);

    /**
     *要求{@link Publisher}停止发送数据并清理资源。
     *呼叫取消后，仍然可能会发送数据以满足先前发出的信号
     */
    public void cancel();
}
```


#### BackpressureStrategy 背压策略
1. **MISSING** 表明由下游处理事件溢出问题，一般用于自定义参数的onBackpressureXXX操作符场景，
2. **ERROR** 表明如果下游跟不上上游的流速就抛出MissingBackpressureException，
3. **BUFFER** 表明缓冲所有的onNext值直到下游消费，
4. **DROP** 表明如果下游跟不上就丢弃最近的onNext值，
5. **LATEST** 表明如果下游跟不上就只保留最近的onNext值，覆盖之前的值

Flowable也提供了工厂方法

```java
Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(FlowableEmitter<String> e) throws Exception {
                e.onNext("Hello1");
                e.onNext("Hello2");
                e.onComplete();
            }
            // //需要指定背压策略
        }, BackpressureStrategy.BUFFER)
        
```

#### Observable
observable可以看做是Flowable的阉割版，只是不支持背压逻辑，其他逻辑和方法与Flowable基本一样

```java
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
```
#### 何时使用Observable
* 最多1000个元素的数据流，应用几乎没有机会OOME。
* 处理诸如鼠标移动或触摸事件之类的GUI事件：这些事件很少会合理地背压，也并不频繁。频率小于1000Hz
* 同步的

#### 何时使用Flowable
* 处理以某种方式生成的10K+元素，处理链可以告诉源限制元素生成的数量。
* 磁盘读取(解析)文件，本质上是阻塞式和基于pull的，
* 通过JDBC从数据库读取，也是阻塞式和基于pull的，
* 网络（流）IO
* 一些阻塞式或基于pull的数据源，最终会被一个非阻塞相应式的API/driver使用





### Single
Single与Observable类似，但只能发射一个onSuccess或者onError给他的消费者SingleObservable<T>

```java
public interface SingleObservable<T>{
    void onSubscribe(Disposable d);
    void onSuccess(T value);
    void onError(Throwable e);
}
```
响应流的事件模式为：onSubscribe(onSuccess|onError)

### Completable
用来表示一个延迟计算且不关心任何数据只关心完成和异常，只能发射一个onComplete或onError给它的消费者CompletableObservable

```java
public interface CompletableObservable{
    void onSubscribe(Disposable d);
    void onComplete();
    void onError(Throwable e);
}
```
响应流的事件模式为：onSubscribe(onComplete|onError)

### Maybe
从概念上讲是Single和Completable的结合，提供了一种捕获一些响应源发射0或1个item或一个error的发射模式的手段，用来表示一个延迟计算。Maybe以MaybeSource作为基本接口，以MaybeObserver<T>作为信号接收接口。由于最多只能发射一个元素Maybe就没打算支持背压处理，这就意味着，在onSubscribe(Disposable)被调用后可能接着调用一个其它onXXX方法，与Flowable不同的是，如果只有一个值信号，只有onSuccess被调用而不是onComplete，使用这种新的基本响应类型与其他类型几乎是一样的，因为它提供了一个适用于0或1个item序列的Flowable操作符子集。

```java
public interface MaybeObserver{
    void onSubscribe(Disposable d);
    void onSuccess(T value);
    void onComplete();
    void onError(Throwable e);
}
```

响应流的事件模式为：onSubscribe (onSuccess | onError | onComplete)?



# 操作符

## 创建

1. **Create：** 创建
2. **Defer：** 延迟创建；等到有一个Observer订阅才会生成一个Observable
3. **From：** 把其他对象或数据结构转成Observable；fromArray、fromCallable、fromFuture、fromIterable、fromPublisher等。
4. **Interval：** 创建一个，给定时间间隔发射一个递增整数(0,1,2.....N)的Observable
5. **Just：** 把一个item转换成发射这个item的Observable。
6. **Range：** 创建发射给定范围的连续整数的Observable; range(n,m);print(n,n+1,n+2....n+m-1)
7. **Timer：** 创建一个给定延迟后发射一个item(0L)的Observable

## 变换

1. **Map：** 变换
2. **FlatMap：** 变换为Observable
3. **Buffer：** 从源ObservableSource收集的项目的缓冲区，每个缓冲区都包含{count}个项目；Observable.range(1,5).buffer(2)；println([1, 2],[3, 4],[5])。
4. **GroupBy：** 分组
5. **Scan：** 累加器  
6. **Window：** 在时间间隔内缓存结果，类似于buffer缓存一个list集合，区别在于window将这个结果集合封装成了Observable
7. **case：** 强制转换,如果类型不匹配会报异常
8. **flatMapIterable：** 和flatMap作用一样，区别：flatMapIterable生成的是Iterable而不是Observable
9. **SwitchMap：** 当源Observable发射一个新的数据项时，如果旧数据项订阅还未完成，就取消旧订阅数据和停止监视那个数据项产生的Observable,开始监视新的数据项；注意是并发数据项。（喜新厌旧）http://blog.csdn.net/axuanqq/article/details/50738464
10. **concatMap：** 类似于flatMap，由于内部使用concat合并，所以是按顺序连续发射。

## 过滤

1. **Debounce：** 译“防抖动”，过滤小于规定时间内产生的结果，大于等于规定时间内的结果交给订阅者，例如：搜索框请求网络，
2. **Distinct：** 译“不同”，去重，还有带参数的`Function<String, String>`
3. **ElementAt：** 获取指定索引的结果;注意不能越界:否则将会回调onError()方法 IndexOutOfBoundsException
4. **Filter：** 过滤；通过只发出满足指定谓词的项来过滤由ObservableSource发出的项目。
5. **First：** 第一；顾名思义取第一个，也就是ElementAt(0L);first(defaultItem)
6. **Last：** 最后一个
7. **Sample：** 定期采样
8. **Skip：** 跳过前n个；SkipLast：跳过最后n个
9. **Take：** 取前n个；TaskLast：取后n个
10. **IgnoreElements：** 译“忽略元素”；忽略源ObservableSource发出的所有项目，只调用{@code onComplete}或{@code onError}。
11. **ofType：** 只有指定类型的数据才能通过
12. **Timeout：** 源Observable过了指定时间没有发射任何数据，就发射一个异常或者备用Observable

## 组合

https://segmentfault.com/a/1190000008157872

1. **And/Then/When：** 需要导入join包
2. **CombineLatest：** 用于将两个Observale最近发射的数据已经BiFunction函数的规则进展组合。
3. **Join：** join的四个参数
    * Observable:源Observable需要组合的Observable。
    * 第一个Function：接收源Observable发射来的数据，并返回一个ObservableSource，生命周期决定了源Observable发射出来数据的有效期；
    * 第二个Function：接收目标Observable发射来的数据，并返回一个ObservableSource，生命周期决定目标Observable发射出来数据的有效期
    * BiFunction：接收从源Observable和目标Observable发射出来的数据，并将这两个数据自由组合返回；
    * 语法：`onservableA.join(observableB, 控制observableA发射数据有效期的函数， 控制observableB发射数据有效期的函数，两个observable发射数据的合并规则)`
    
    join操作符的效果类似于排列组合，把第一个数据源A作为基座窗口，他根据自己的节奏不断发射数据元素，第二个数据源B，每发射一个数据，我们都把它和第一个数据源A中已经发射的数据进行一对一匹配；举例来说，如果某一时刻B发射了一个数据“B”,此时A已经发射了0，1，2，3共四个数据，那么我们的合并操作就会把“B”依次与0,1,2,3配对，得到四组数据： [0, B] [1, B] [2, B] [3, B]

4. **Merge：** 将两个Observable发射的事件序列组合并成一个事件序列，就像是一个Observable发射的一样。你可以简单的将它理解为两个Obsrvable合并成了一个Observable，合并后的数据是无序的。
5. **StartWith：** 用于在源Observable发射的数据前插入数据。使用startWith(Iterable<T>)我们还可以在源Observable发射的数据前插入Iterable
6. **Concat：** 用于将多个observable发射的数据进行合并发射，concat严格按照顺序发射数据，前一个Observable没发射完是不会发射后一个Observable的数据的。它可merge、startWitch相似，不同之处在于：
    * merge：合并后发射的数据是无序的；
    * startWitch：只能在源Observable发射的数据前插入数据。

7. **Zip：** 用来合并两个Observable发射的数据项，根据BiFunction函数生成一个新的值发射出去。当其中一个Observable发送数据结束或者异常出现后。另一个Observable也将停止发射数据。
8. **SwitchOnNext：** 用来将一个发射多个小Observable的源Observable转换为一个Observable，然后发射多个小Observable所发射的数据。如果一个小的Observable正在发射数据的时候，源Observable又发射出一个新的小Observable，则前一个Observable还未发射的数据会被抛弃。直接发射新的小Observable所发射的数据。




## 错误处理

1. **onErrorResumeNext：** 当原始Observable遇到错误时，使用备用Observable
2. **onExceptionResumeNext：** 当源Observable遇到错误时，使用备用Observable，与onErrorResumeNext类似，区别在于onErrorResumeNext可以处理所有的错误，onExceptionResumeNext只能处理异常。
3. **onErrorReturn：** 当源Observable在遇到错误时发射一个特定的数据
4. **retry：** 当源Observable在遇到错误时进行重试。
5. **retryWhen：** 当源Observable在遇到错误时，将错误传递给另一个Observable来决定是否要重新订阅这个Observable，

## Observable工具

1. **Delay：** 延迟发送（每个数据项目）;其中 Boolean delayError 如果为真，则在所有前面的正常元素之后，用给定的延迟信号通知上游异常;如果为false，则立即通知上游异常
2. **observerOn、subscribeOn：** 线程调度器，RxJava中有解释
3. **Materialize/Dematerialize：** 将Observable转换成一个通知(Notification)列表。/将通知逆转回一个Observable。
4. **Timestamp：** 给Observable发射的每个数据项添加一个时间  戳；
5. **TimeInterval** 给Observable发射的两个数据项添加一个时间  差，
6. **Serialize：** 强制Observable序列化
7. **Cache：** 缓存ObservableSource发射的数据，只有当订阅者订阅后才开始发送数据
8. **doOnNext：** 允许我们在每次输出一个元素之前做一些额外的事情；还有一个doAfterNext官方没有详细解释
9. **doOnEach：** 修改源ObservableSource(Notification)，为发出的每项目调用一个操作。
10. **doOnCompleted：** 对正常完成的Observable调用一个操作
11. **doOnError：** 对错误的Observable调用一个操作
12. **doOnTerminate、doAfterTerminate、doFinally** 对完成的Observable注册一个操作，无论是否发生错误；三个的区别暂时还不清楚
13. **doOnSubscribe：** Observable订阅时注册一个操作；
14. **doOnDispose：** Observable取消订阅时注册一个操作；在发射完成或者出现错误后调用dispose不会执行这个操作
15. **delaySubscription：** 指定延时处理订阅请求；or 直到另一个Observable发出一个元素或者正常完成。
16. **using：** 创建一个只在Observable生命周期存在的资源，当Observable终止时这个资源会被自动释放。using(资源工厂，Observable，释放资源动作)
17. **single、singleOrDefaullt：** 强制返回单个数据，否则抛出异常或默认数据






## 条件及布尔判断

1. **All：** 判断所有的数据项是否满足某个条件，内部通过OperatorAll实现。
2. **Exists：** 判断是否存在数据项满足某个条件。RxJava2取消
3. **Contains：** 是否包含指定数据
4. **SequenceEqual：** 两个Observable发射的数据是否相同（数据，发射顺序，终止状态）。
5. **isEmpty：** 判断Observable发射完毕时，有没有发射数据。有数据false，如果只收到了onComplete通知则为true
6. **Amb：** 给定多个Observable，只让第一个发射数据的Observable发射全部数据，其他Observable将会被忽略
7. **switchIfEmpty：** 如果原始Observable正常终止后仍然没有发射任何数据，就是用备用的Observable。
8. **defaultIfEmpty：** 源Observable正常终止后没有发射任何数据，就发射一个默认值，内部调用的switchIfEmpty。
9. **takeUntil：** 当发射的某个数据满足指定条件时（包含该数据）终止发送数据
10. **takeWhile：** 当发射的某个数据满足指定条件时（不包含该数据）终止发送数据。


## 聚合操作

1. **reduce：** 发射聚合的最终结果，
2. **collect：** 收集数据到一个可变的数据结构
3. **count：** 计算发射的数量，

## 数学及集合操作符

## 阻塞操作



## 转换Observable

1. **toList：** 收集源Observable发射的所有数据到一个列表然后返回这个列表
2. **toSortedList：** 收集源Observable发射的所有数据到一个有序列表，然后返回这个列表。
3. **toMap：** 转为Map
4. **toMultiMap：** 类似于toMap，不同的地方在map的value时一个集合


## ConnectableObservable链接操作

与普通的Observable差不多，但是可链接的Observable在被订阅时并不开始发射数据，只有在它的connect()被调用时才开始。用这种方法，你可以等所有的潜在订阅者都订阅了这个Observable之后才开始发射数据。

* ConnectableObservable.connect()指示一个可连续的Observable开始发射数据。
* Observable.publish()将一个Observable转换为一个可以连续的Observable
* Observable.replay()确保所有的订阅者看到相同的数据序列的ConnectableObservable，即使他们在Observable开始发射数据之后才订阅。
* ConnectableObservable.refCount()让一个可连续的Observable表现的像一个普通的Observable。










## 参考博客：

RxJava2基础理论：

1. http://blog.csdn.net/column/details/17715.html
2. http://www.jianshu.com/p/220955eefc1f

关于Subject：
http://www.jianshu.com/p/1257c8ba7c0c

RxJava和RxJava2区别：
http://blog.csdn.net/qq_35064774/article/details/53045298

操作符：
http://blog.csdn.net/maplejaw_/article/details/52396175



