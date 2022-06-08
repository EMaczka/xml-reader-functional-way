package org.example.common;

public abstract class Stream<A> {

    private static Stream EMPTY = new Empty();

    public abstract Tuple<A, Stream<A>> head();

    public abstract Stream<A> tail();

    public abstract Stream<A> take(int n);


    private Stream() {}

    private static class Empty<A> extends Stream<A> {

        @Override
        public Stream<A> tail() {
            throw new IllegalStateException("tail called on empty");
        }

        @Override
        public Tuple<A, Stream<A>> head() {
            throw new IllegalStateException("head called on empty");
        }

        @Override
        public Stream<A> take(int n) {
            return this;
        }
    }

    private static class Cons<A> extends Stream<A> {

        private final Supplier<A> head;
        private final Result<A> h;
        private final Supplier<Stream<A>> tail;

        private Cons(Supplier<A> h, Supplier<Stream<A>> t) {
            head = h;
            tail = t;
            this.h = Result.empty();
        }

        private Cons(A h, Supplier<Stream<A>> t) {
            head = () -> h;
            tail = t;
            this.h = Result.success(h);
        }

        @Override
        public Stream<A> tail() {
            return tail.get();
        }

        @Override
        public Tuple<A, Stream<A>> head() {
            A a = h.getOrElse(head.get());
            return h.isEmpty()
                    ? new Tuple<>(a, new Cons<>(a, tail))
                    : new Tuple<>(a, this);
        }

        @Override
        public Stream<A> take(int n) {
            return n <= 0
                    ? empty()
                    : cons(head, () -> tail().take(n - 1));
        }
    }

    static <A> Stream<A> cons(Supplier<A> hd, Supplier<Stream<A>> tl) {
        return new Cons<>(hd, tl);
    }

    public static <A> Stream<A> empty() {
        return EMPTY;
    }
}