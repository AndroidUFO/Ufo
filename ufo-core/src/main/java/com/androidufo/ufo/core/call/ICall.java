package com.androidufo.ufo.core.call;

import androidx.lifecycle.LifecycleOwner;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */
public interface ICall<R extends ICall<R>> {
    // 取消请求
    void cancel();
    // 当前是否可以取消请求：一般情况请求还为执行完成即可取消
    boolean canCancel();
    // 请求对应的tag组，当多个请求在同一个生命周期组件中执行时，
    // 会被分配成同一个组，方便生命周期结束时，取消当前组件中的所有请求
    String getGroupTag();
    // 绑定生命周期，绑定之后，Activity和Fragment销毁时会自动取消正在执行的请求
    R bindLifecycle(LifecycleOwner owner);
}
