package com.github.ngeor;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public sealed interface Result<T, E> {

    static <T, E> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }

    static <T, E> Result<T, E> err(E err) {
        return new Err<>(err);
    }

    <U> Result<U, E> map(Function<T, U> f);

    <F> Result<T, F> mapErr(Function<E, F> f);

    <U> Result<U, E> flatMap(Function<T, Result<U, E>> f);

    Optional<T> toOk();

    Optional<E> toErr();

    record Ok<T, E>(T value) implements Result<T, E> {

        public Ok {
            Objects.requireNonNull(value);
        }

        @Override
        public <U> Result<U, E> map(Function<T, U> f) {
            return ok(f.apply(value));
        }

        @Override
        public <F> Result<T, F> mapErr(Function<E, F> f) {
            return ok(value);
        }

        @Override
        public <U> Result<U, E> flatMap(Function<T, Result<U, E>> f) {
            return f.apply(value);
        }

        @Override
        public Optional<T> toOk() {
            return Optional.of(value);
        }

        @Override
        public Optional<E> toErr() {
            return Optional.empty();
        }
    }

    record Err<T, E>(E error) implements Result<T, E> {

        public Err {
            Objects.requireNonNull(error);
        }

        @Override
        public <U> Result<U, E> map(Function<T, U> f) {
            return err(error);
        }

        @Override
        public <F> Result<T, F> mapErr(Function<E, F> f) {
            return err(f.apply(error));
        }

        @Override
        public <U> Result<U, E> flatMap(Function<T, Result<U, E>> f) {
            return err(error);
        }

        @Override
        public Optional<T> toOk() {
            return Optional.empty();
        }

        @Override
        public Optional<E> toErr() {
            return Optional.of(error);
        }
    }
}
