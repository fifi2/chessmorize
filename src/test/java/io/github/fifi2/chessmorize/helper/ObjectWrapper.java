package io.github.fifi2.chessmorize.helper;

public class ObjectWrapper<T> {

    private T value;

    public void set(T value) {

        this.value = value;
    }

    public T get() {

        return this.value;
    }

}
