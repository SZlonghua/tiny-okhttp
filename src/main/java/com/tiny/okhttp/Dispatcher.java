package com.tiny.okhttp;

import com.sun.istack.internal.Nullable;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.tiny.okhttp.RealCall.AsyncCall;
import com.tiny.okhttp.internal.Util;

import java.util.concurrent.atomic.AtomicInteger;

public final class Dispatcher {

    private int maxRequests = 64;
    private int maxRequestsPerHost = 5;
    private @Nullable Runnable idleCallback;

    private @Nullable ExecutorService executorService;

    private final Deque<AsyncCall> readyAsyncCalls = new ArrayDeque<>();

    private final Deque<AsyncCall> runningAsyncCalls = new ArrayDeque<>();

    private final Deque<RealCall> runningSyncCalls = new ArrayDeque<>();

    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<>(), Util.threadFactory("OkHttp Dispatcher", false));
        }
        return executorService;
    }

    void enqueue(AsyncCall call) {
        synchronized (this) {
            readyAsyncCalls.add(call);

            // Mutate the AsyncCall so that it shares the AtomicInteger of an existing running call to
            // the same host.
            AsyncCall existingCall = findExistingCallWithHost(call.host());
            if (existingCall != null) call.reuseCallsPerHostFrom(existingCall);
        }
        promoteAndExecute();
    }

    private boolean promoteAndExecute() {

        List<AsyncCall> executableCalls = new ArrayList<>();
        boolean isRunning;
        synchronized (this) {
            for (Iterator<AsyncCall> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
                AsyncCall asyncCall = i.next();

                if (runningAsyncCalls.size() >= maxRequests) break; // Max capacity.
                if (asyncCall.callsPerHost().get() >= maxRequestsPerHost) continue; // Host max capacity.

                i.remove();
                asyncCall.callsPerHost().incrementAndGet();
                executableCalls.add(asyncCall);
                runningAsyncCalls.add(asyncCall);
            }
            isRunning = runningCallsCount() > 0;
        }

        for (int i = 0, size = executableCalls.size(); i < size; i++) {
            AsyncCall asyncCall = executableCalls.get(i);
            asyncCall.executeOn(executorService());
        }

        return isRunning;
    }

    @Nullable
    private AsyncCall findExistingCallWithHost(String host) {
        for (AsyncCall existingCall : runningAsyncCalls) {
            if (existingCall.host().equals(host)) return existingCall;
        }
        for (AsyncCall existingCall : readyAsyncCalls) {
            if (existingCall.host().equals(host)) return existingCall;
        }
        return null;
    }

    void finished(AsyncCall call) {
        call.callsPerHost().decrementAndGet();
        finished(runningAsyncCalls, call);
    }

    private <T> void finished(Deque<T> calls, T call) {
        Runnable idleCallback;
        synchronized (this) {
            if (!calls.remove(call)) throw new AssertionError("Call wasn't in-flight!");
            idleCallback = this.idleCallback;
        }

        boolean isRunning = promoteAndExecute();

        if (!isRunning && idleCallback != null) {
            idleCallback.run();
        }
    }

    public synchronized int runningCallsCount() {
        return runningAsyncCalls.size() + runningSyncCalls.size();
    }

    synchronized void executed(RealCall call) {
        runningSyncCalls.add(call);
    }

    void finished(RealCall call) {
        finished(runningSyncCalls, call);
    }
}
