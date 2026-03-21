package com.github.ngeor;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Result<T, E extends RuntimeException> {

    static <T, E extends RuntimeException> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }

    static <T, E extends RuntimeException> Result<T, E> err(E err) {
        return new Err<>(err);
    }

    <U> Result<U, E> map(Function<T, U> f);

    <F extends RuntimeException> Result<T, F> mapErr(Function<E, F> f);

    <U> Result<U, E> flatMap(Function<T, Result<U, E>> f);

    T get();

    <U> Result<U, E> andThen(Supplier<Result<U, E>> supplier);

    record Ok<T, E extends RuntimeException>(T value) implements Result<T, E> {

        public Ok {
            Objects.requireNonNull(value);
        }

        @Override
        public <U> Result<U, E> map(Function<T, U> f) {
            return ok(f.apply(value));
        }

        @Override
        public <F extends RuntimeException> Result<T, F> mapErr(Function<E, F> f) {
            return ok(value);
        }

        @Override
        public <U> Result<U, E> flatMap(Function<T, Result<U, E>> f) {
            return f.apply(value);
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public <U> Result<U, E> andThen(Supplier<Result<U, E>> supplier) {
            return supplier.get();
        }
    }

    record Err<T, E extends RuntimeException>(E error) implements Result<T, E> {

        public Err {
            Objects.requireNonNull(error);
        }

        @Override
        public <U> Result<U, E> map(Function<T, U> f) {
            return err(error);
        }

        @Override
        public <F extends RuntimeException> Result<T, F> mapErr(Function<E, F> f) {
            return err(f.apply(error));
        }

        @Override
        public <U> Result<U, E> flatMap(Function<T, Result<U, E>> f) {
            return err(error);
        }

        @Override
        public T get() {
            throw error;
        }

        @Override
        public <U> Result<U, E> andThen(Supplier<Result<U, E>> supplier) {
            return err(error);
        }
    }
}
