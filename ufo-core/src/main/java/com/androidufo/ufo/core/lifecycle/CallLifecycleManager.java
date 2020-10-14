package com.androidufo.ufo.core.lifecycle;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import com.androidufo.ufo.core.call.ICall;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CallLifecycleManager {

    private final Map<String, List<ICall>> callCaches = new ConcurrentHashMap<>();
    // 一个groupTag对应一个生命周期观察者
    private final Map<String, LifecycleEventObserver> observerMap = new ConcurrentHashMap<>();

    private CallLifecycleManager() {
    }

    public static CallLifecycleManager getManager() {
        return Holder.INSTANCE;
    }

    public String addCall(ICall call, LifecycleOwner owner) {
        if (call == null || owner == null) {
            return null;
        }
        String groupTag = owner.toString();
        if (!observerMap.containsKey(groupTag)) {
            LifecycleEventObserver observer = new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    if (Lifecycle.Event.ON_DESTROY == event) {
                        String lifecycleTag = source.toString();
                        cancelCalls(lifecycleTag);
                        observerMap.remove(lifecycleTag);
                        source.getLifecycle().removeObserver(this);
                    }
                }
            };
            owner.getLifecycle().addObserver(observer);
            observerMap.put(groupTag, observer);
        }
        addCall(call, groupTag);
        return groupTag;
    }

    private void addCall(ICall call, String groupTag) {
        if (callCaches.containsKey(groupTag)) {
            List<ICall> list = callCaches.get(groupTag);
            if (list != null && !list.contains(call)) {
                list.add(call);
            }
        } else {
            List<ICall> list = new ArrayList<>();
            list.add(call);
            callCaches.put(groupTag, list);
        }
    }

    public void removeCall(ICall call, String groupTag) {
        if (callCaches.containsKey(groupTag)) {
            List<ICall> list = callCaches.get(groupTag);
            if (list != null) {
                list.remove(call);
            }
        }
    }

    public void cancelCalls(String groupTag) {
        if (callCaches.containsKey(groupTag)) {
            List<ICall> calls = callCaches.remove(groupTag);
            if (calls != null) {
                for (ICall call : calls) {
                    if (call != null) {
                        call.cancel();
                    }
                }
            }
        }
    }

    private static class Holder {
        private static final CallLifecycleManager INSTANCE = new CallLifecycleManager();
    }
}
