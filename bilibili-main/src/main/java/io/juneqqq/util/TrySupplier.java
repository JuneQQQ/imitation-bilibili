package io.juneqqq.util;

public interface TrySupplier<T>{
    T get() throws Throwable;
}
