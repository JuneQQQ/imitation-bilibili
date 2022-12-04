package com.juneqqq.util;

public interface TryMapFunction<T, R> {
    R apply(T t) throws Throwable;
}