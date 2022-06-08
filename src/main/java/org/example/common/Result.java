package org.example.common;

import java.io.Serializable;
import java.util.function.Function;

public abstract class Result<T> implements Serializable {

    private Result() {
    }

    public abstract Boolean isEmpty();
    public abstract T getOrElse(final T defaultValue);
    public abstract T successValue();
    public abstract Exception failureValue();
    public abstract void forEach(Effect<T> c);
    public abstract void forEachOrThrow(Effect<T> c);
    public abstract Result<T> filter(Function<T, Boolean> f);
    public abstract Result<T> filter(Function<T, Boolean> p, String message);
    public abstract <U> Result<U> map(Function<T, U> f);
    public abstract <U> Result<U> flatMap(Function<T, Result<U>> f);

    public static <T, U> Result<T> failure(Failure<U> failure) {
        return new Failure<>(failure.exception);
    }
    public static <T> Result<T> failure(String message) {
        return new Failure<>(message);
    }

    public static <T> Result<T> failure(String message, Exception e) {
        return new Failure<>(new IllegalStateException(message, e));
    }

    public static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    public static <T> Result<T> empty() {
        return new Empty<>();
    }

    private static class Failure<T> extends Empty<T> {

        private final RuntimeException exception;

        private Failure(String message) {
            super();
            this.exception = new IllegalStateException(message);
        }

        private Failure(RuntimeException e) {
            super();
            this.exception = e;
        }

        @Override
        public T getOrElse(final T defaultValue) {
            return defaultValue;
        }

        @Override
        public T successValue() {
            throw new IllegalStateException("Method successValue() called on a Failure instance");
        }

        @Override
        public RuntimeException failureValue() {
            return this.exception;
        }

        @Override
        public void forEachOrThrow(Effect<T> c) {
            throw exception;
        }

        @Override
        public Result<T> filter(Function<T, Boolean> f) {
            return failure(this);
        }

        @Override
        public Result<T> filter(Function<T, Boolean> p, String message) {
            return failure(this);
        }

        @Override
        public <U> Result<U> map(Function<T, U> f) {
            return failure(this);
        }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> f) {
            return failure(exception.getMessage(), exception);
        }

        @Override
        public String toString() {
            return String.format("Failure(%s)", failureValue());
        }
    }

    private static class Empty<T> extends Result<T> {
        public Empty() {
            super();
        }

        @Override
        public Boolean isEmpty() {
            return true;
        }

        @Override
        public T getOrElse(final T defaultValue) {
            return defaultValue;
        }

        @Override
        public T successValue() {
            throw new IllegalStateException("Method successValue() called on a Empty instance");
        }

        @Override
        public RuntimeException failureValue() {
            throw new IllegalStateException("Method failureMessage() called on a Empty instance");
        }

        @Override
        public void forEach(Effect<T> c) {
            /* Empty. Do nothing. */
        }

        @Override
        public void forEachOrThrow(Effect<T> c) {
            /* Do nothing */
        }

        @Override
        public Result<T> filter(Function<T, Boolean> f) {
            return empty();
        }

        @Override
        public Result<T> filter(Function<T, Boolean> p, String message) {
            return empty();
        }

        @Override
        public <U> Result<U> map(Function<T, U> f) {
            return empty();
        }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> f) {
            return empty();
        }

        @Override
        public String toString() {
            return "Empty()";
        }

    }

    private static class Success<T> extends Result<T> {

        private final T value;

        public Success(T value) {
            super();
            this.value = value;
        }

        @Override
        public Boolean isEmpty() {
            return false;
        }

        @Override
        public T getOrElse(final T defaultValue) {
            return successValue();
        }

        @Override
        public T successValue() {
            return this.value;
        }

        @Override
        public RuntimeException failureValue() {
            throw new IllegalStateException("Method failureValue() called on a Success instance");
        }

        @Override
        public void forEach(Effect<T> e) {
            e.apply(this.value);
        }

        @Override
        public void forEachOrThrow(Effect<T> e) {
            e.apply(this.value);
        }

        @Override
        public Result<T> filter(Function<T, Boolean> p) {
            return filter(p, "Unmatched predicate with no error message provided.");
        }

        @Override
        public Result<T> filter(Function<T, Boolean> p, String message) {
            try {
                return p.apply(successValue())
                        ? this
                        : failure(message);
            } catch (Exception e) {
                return failure(e.getMessage(), e);
            }
        }

        @Override
        public <U> Result<U> map(Function<T, U> f) {
            try {
                return success(f.apply(successValue()));
            } catch (Exception e) {
                return failure(e.getMessage(), e);
            }
        }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> f) {
            try {
                return f.apply(successValue());
            } catch (Exception e) {
                return failure(e.getMessage());
            }
        }

        @Override
        public String toString() {
            return String.format("Success(%s)", successValue().toString());
        }
    }

    public static <T> Result<T> of(final Function<T, Boolean> predicate,
                                   final T value,
                                   final String message) {
        try {
            return predicate.apply(value)
                    ? Result.success(value)
                    : Result.failure(String.format(message, value));
        } catch (Exception e) {
            String errMessage = String.format("Exception while getting predicate: %s", String.format(message, value));
            return Result.failure(errMessage, e);
        }
    }

    public static <T> Result<T> of(final T value) {
        return value != null
                ? success(value)
                : Result.failure("Null value");
    }

    public static <A, B, C> Function<Result<A>, Function<Result<B>, Result<C>>> lift2(final Function<A, Function<B, C>> f) {
        return a -> b -> a.map(f).flatMap(b::map);
    }

    public static <A, B, C> Result<C> map2(final Result<A> a,
                                           final Result<B> b,
                                           final Function<A, Function<B, C>> f) {
        return lift2(f).apply(a).apply(b);
    }
}