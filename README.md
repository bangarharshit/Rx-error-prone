# Rx-error-prone: Catch common Rx-Java errors at compile time.

It is a set of checks for RxJava code. The code below 
will emit the value on `computation` and not the `main` thread like one might think.

```java
public class SampleVerifier {
  public static void main(String[] args) {
    Observable observable = Observable.just(1).observeOn(Schedulers.io()).delay(1, TimeUnit.SECONDS);
  }
}
```

```
$ ./gradlew clean :sample:build
> Task :sample:compileJava FAILED
/Users/harshitbangar/Desktop/RIBs/rx/sample/src/main/java/SampleVerifier.java:7: error: [DefaultSchedulerCheck] Using a default scheduler
    Observable observable = Observable.just(1).observeOn(Schedulers.io()).delay(1, TimeUnit.SECONDS);
                                                                               ^
    (see https://bitbucket.org/littlerobots/rxlint/)
1 error
```

## How to use
Follow the setup instruction in the [gradle error-prone repo](https://github.com/tbroyer/gradle-errorprone-plugin) and just add to your `build.gradle`
```gradle
apt "com.github.bangarharshit:rxbase:0.0.3"
```

By default all the checks are enabled. To disable check for code/unit-tests use the snippet below.
```gradle 
tasks.withType(JavaCompile) {
    if (!name.toLowerCase().contains("test")) {
        // For actual code.
        options.compilerArgs += ["-Xep:DefaultSchedulerCheck:OFF"]
    } else {
        // For unit tests.
        options.compilerArgs += [ '-Xep:DefaultSchedulerCheck:OFF', '-Xep:DanglingSubscriptionCheck:OFF']
    }
}
```

The list of checks
```gradle
1. DefaultSchedulerCheck
2. DanglingSubscriptionCheck
3. OnCreateCheck (for Rx1 create methods without backpressure)
4. CacheCheck
5. SubscribeOnErrorMissingCheck
6. SubscriptionInConstructorCheck
```


### SubscribeOnErrorMissingCheck
Check if the subscriber is handling the `onError()` callback. 

Every observable can report errors. Not implementing onError will throw an exception at runtime,
 which can be hard to debug when the error is thrown on a Scheduler that is not the invoking thread.

If a subscriber doesn't handle the error and the Observable emits an error, 
it is wrapped into an OnErrorNotImplementedException and routed to the RxJavaPlugins.onError handler.

Another advantage of implementing `onError` is a better stack-trace. You can include tags to enrich the trace.

### DefaultSchedulerCheck
Operators like `delay` or `interval` run on `Schedulers.computation()` by default. It can be extremely confusing
for the scenarios like - `someObservable.observeOn(AndroidSchedulers.MainThread()).interval(2, TimeUnit.Seconds)`
which will emit the value on `computation` and not the `main` thread like one might think.

An overloaded version is available where a custom scheduler can be provided and should be used. 

### DanglingSubscriptionCheck
Check that all the subscriptions are assigned to disposables. 
Any unassigned subscription means the subscription can't be `disposed()` or `unSubscribe()` 
which can lead to memory leaks and crashes in runtime.


### CacheCheck
cache() is a hard to understand operator. Use replay() instead. cache() is same as replay().autoConnect(). 
replay() is much more flexible, less confusing and can be combined with different multi-casting operations.
Check Dan Lew's [talk](https://youtu.be/QdmkXL7XikQ?t=19m21s) on the same.

### OnCreateCheck (for Rx1)
`<T>Observable.create(rx.Observable.OnSubscribe<T>)` and `unSafeCreate` doesn't handle backpressure.
These operators construct an Observable in an unsafe manner, that is, unsubscription and backpressure handling 
is the responsibility of the OnSubscribe implementation.

### SubscriptionInConstructorCheck
Subscription in constructor leads to unsafe object creation and is similar to starting a thread in the constructor.
Check the [safe construction technique article](https://www.ibm.com/developerworks/library/j-jtp0618/index.html) 
for more details.

Use any of the other overloaded `create` methods or `just/fromCallable` any other generator.

## Rxlint
[Rxlint](https://bitbucket.org/littlerobots/rxlint) is a similar tool and can be used to detect these errors.

## Contributing
Feel free to help with issues, and PR.
Go through the issues with tag `help wanted` and just send a PR. Comment on the issue that you are interested 
to avoid duplication.

Tutorials:
1. [How to write a new check](https://github.com/uber/NullAway)

Google code style is automatically applied as a part of git hook so no explicit steps are required.

## Acknowledgement
[Uber/Nullaway](https://github.com/uber/NullAway) for steps related to publishing.

## License
```
MIT License

Copyright (c) 2017 bangarharshit

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 ```
