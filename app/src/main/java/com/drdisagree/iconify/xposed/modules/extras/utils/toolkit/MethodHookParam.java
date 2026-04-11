package com.drdisagree.iconify.xposed.modules.extras.utils.toolkit;

import java.lang.reflect.Executable;

public class MethodHookParam {

    public Object thisObject;
    public Executable method;
    public Object[] args = new Object[0];

    private Object result;
    private Throwable throwable;
    private boolean returnEarly = false;

    public Object getResult() {
        return result;
    }

    public void setResult(Object value) {
        this.throwable = null;
        this.returnEarly = true;
        this.result = value;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable value) {
        this.returnEarly = true;
        this.result = null;
        this.throwable = value;
    }

    public boolean isReturnEarly() {
        return returnEarly;
    }

    public boolean hasThrowable() {
        return throwable != null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getArg(int index) {
        return (T) args[index];
    }
}