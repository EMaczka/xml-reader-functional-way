package org.example.common;

import java.util.*;
import java.util.function.Consumer;

import static org.example.common.TailCall.ret;
import static org.example.common.TailCall.sus;

public abstract class List<A> {

    protected abstract A head();
    protected abstract List<A> tail();
    protected abstract List<A> take(int n);
    public abstract boolean isEmpty();
    public abstract List<A> reverse();
    public abstract int length();
    public abstract <B> B foldLeft(B identity, Function<B, Function<A, B>> f);
    public abstract <B> B foldRight(B identity, Function<A, Function<B, B>> f);
    public abstract <B> List<B> map(Function<A, B> f);
    public abstract List<A> filter(Function<A, Boolean> f);
    public abstract <B> List<B> flatMap(Function<A, List<B>> f);
    public abstract List<A> takeWhile(Function<A, Boolean> p);
    public abstract List<List<A>> subLists();
    public abstract List<List<A>> interleave(A a);
    public abstract List<List<A>> perms();
    public abstract List<Tuple<List<A>, List<A>>> split();

    public abstract Stream<A> toStream();

    public List<A> cons(A a) {
        return new Cons<>(a, this);
    }

    public List<A> concat(List<A> list) {
        return concat(this, list);
    }

    public void forEach(Consumer<A> effect) {
        List<A> workList = this;
        while (!workList.isEmpty()) {
            effect.accept(workList.head());
            workList = workList.tail();
        }
    }

    @SuppressWarnings("rawtypes")
    public static final List NIL = new Nil();

    private List() {}

    private static class Nil<A> extends List<A> {
        private Nil() {}

        public A head() {
            throw new IllegalStateException("head called on an empty list");
        }

        public List<A> tail() {
            throw new IllegalStateException("tail called on an empty list");
        }

        public boolean isEmpty() {
            return true;
        }

        public String toString() {
            return "[NIL]";
        }

        @Override
        public List<A> reverse() {
            return this;
        }

        @Override
        public int length() {
            return 0;
        }

        @Override
        public <B> B foldLeft(B identity, Function<B, Function<A, B>> f) {
            return identity;
        }

        @Override
        public <B> B foldRight(B identity, Function<A, Function<B, B>> f) {
            return identity;
        }

        @Override
        public <B> List<B> map(Function<A, B> f) {
            return list();
        }

        @Override
        public List<A> filter(Function<A, Boolean> f) {
            return this;
        }

        @Override
        public <B> List<B> flatMap(Function<A, List<B>> f) {
            return list();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Nil;
        }

        @Override
        protected List<A> take(int n) {
            throw new IllegalStateException("take called on an empty list");
        }

        @Override
        public List<A> takeWhile(Function<A, Boolean> p) {
            return this;
        }

        @Override
        public List<List<A>> subLists() {
            return list(list());
        }

        @Override
        public List<List<A>> interleave(A a) {
            return list(list(a));
        }

        @Override
        public List<List<A>> perms() {
            return list(list());
        }

        @Override
        public List<Tuple<List<A>, List<A>>> split() {
            return list();
        }

        @Override
        public Stream<A> toStream() {
            return Stream.empty();
        }
    }

    private static class Cons<A> extends List<A> {

        private final A head;
        private final List<A> tail;
        private final int length;

        private Cons(A head, List<A> tail) {
            this.head = head;
            this.tail = tail;
            this.length = tail.length() + 1;
        }

        public A head() {
            return head;
        }

        public List<A> tail() {
            return tail;
        }

        public boolean isEmpty() {
            return false;
        }

        public String toString() {
            return String.format("[%sNIL]", toString(new StringBuilder(), this).eval());
        }

        private TailCall<StringBuilder> toString(StringBuilder acc, List<A> list) {
            return list.isEmpty()
                    ? ret(acc)
                    : sus(() -> toString(acc.append(list.head()).append(", "), list.tail()));
        }

        @Override
        public List<A> reverse() {
            return reverse_(list(), this).eval();
        }

        private TailCall<List<A>> reverse_(List<A> acc, List<A> list) {
            return list.isEmpty()
                    ? ret(acc)
                    : sus(() -> reverse_(new Cons<>(list.head(), acc), list.tail()));
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public <B> B foldLeft(B identity, Function<B, Function<A, B>> f) {
            return foldLeft(identity, this, f).eval();
        }

        private <B> TailCall<B> foldLeft(B acc, List<A> list, Function<B, Function<A, B>> f) {
            return list.isEmpty()
                    ? ret(acc)
                    : sus(() -> foldLeft(f.apply(acc).apply(list.head()), list.tail(), f));
        }

        @Override
        public <B> B foldRight(B identity, Function<A, Function<B, B>> f) {
            return reverse().foldLeft(identity, x -> y -> f.apply(y).apply(x));
        }

        @Override
        public <B> List<B> map(Function<A, B> f) {
            return foldRight(list(), h -> t -> new Cons<>(f.apply(h),t));
        }

        @Override
        public List<A> filter(Function<A, Boolean> f) {
            return foldRight(list(), h -> t -> f.apply(h) ? new Cons<>(h,t) : t);
        }

        @Override
        public <B> List<B> flatMap(Function<A, List<B>> f) {
            return foldRight(list(), h -> t -> concat(f.apply(h), t));
        }

        @Override
        protected List<A> take(int n) {
            return this.isEmpty()
                    ? this
                    : n > 1
                    ? new Cons<>(head(), tail().take(n - 1))
                    : new Cons<>(head(), list());
        }

        @Override
        public List<A> takeWhile(Function<A, Boolean> p) {
            return isEmpty()
                    ? this
                    : p.apply(head())
                    ? new Cons<>(head(), tail().takeWhile(p))
                    : list();
        }

        @Override
        public List<List<A>> subLists() {
            List<List<A>> yss = tail.subLists();
            return yss.concat(yss.map(subList -> subList.cons(head)));
        }

        @Override
        public List<List<A>> interleave(A a) {
            List<List<A>> yss = tail.interleave(a);
            return yss.map(lst -> lst.cons(head)).cons(this.cons(a));
        }

        @Override
        public List<List<A>> perms() {
            return tail.perms().flatMap(lst -> lst.interleave(head));
        }

        @Override
        public List<Tuple<List<A>, List<A>>> split() {
            return tail.isEmpty()
                    ? list()
                    : split_(tail);
        }

        @Override
        public Stream<A> toStream() {
            return Stream.cons(() -> head, tail::toStream);
        }

        private List<Tuple<List<A>, List<A>>> split_(List<A> list) {
            List<Tuple<List<A>, List<A>>> yss = list.tail().split();
            return yss.map(t -> new Tuple<>(t._1.cons(head), t._2)).cons(new Tuple<>(list(head), tail));
        }
    }

    @SuppressWarnings("unchecked")
    public static <A> List<A> list() {
        return NIL;
    }

    @SafeVarargs
    public static <A> List<A> list(A... a) {
        List<A> n = list();
        for (int i = a.length - 1; i >= 0; i--) {
            n = new Cons<>(a[i], n);
        }
        return n;
    }

    public static <A, B> B foldRight(List<A> list, B n, Function<A, Function<B, B>> f ) {
        return list.foldRight(n, f);
    }


    public static <A> List<A> concat(List<A> list1, List<A> list2) {
        return foldRight(list1, list2, x -> y -> new Cons<>(x, y));
    }

    public static <A, B> Result<List<B>> traverse(List<A> list, Function<A, Result<B>> f) {
        return list.foldRight(Result.success(List.list()), x -> y -> Result.map2(f.apply(x), y, a -> b -> b.cons(a)));
    }

    public static <A> Result<List<A>> sequence(List<Result<A>> list) {
        return traverse(list, x -> x);
    }

    public static <T> List<T> fromCollection(Collection<T> ct) {
        List<T> lt = list();
        for (T t : ct) {
            lt = lt.cons(t);
        }
        return lt.reverse();
    }

    public java.util.List<A> toJavaList() {
        java.util.List<A> s = new ArrayList<>();
        List<A> workList = this;
        while (!workList.isEmpty()) {
            s.add(workList.head());
            workList = workList.tail();
        }
        return s;
    }
}