package nl.han.ica.datastructures;

import java.util.EmptyStackException;

public class HanStack<T> implements IHANStack<T> {

    private static class Node<T> {
        private final T data;
        private Node<T> next;

        public Node(T data) {
            this.data = data;
        }
    }

    private Node<T> top;

    @Override
    public void push(T value) {
        Node<T> newNode = new Node<>(value);
        newNode.next = top;
        top = newNode;
    }

    @Override
    public T pop() {
        if (top == null) {
            throw new EmptyStackException();
        }
        T value = top.data;
        top = top.next;
        return value;
    }

    @Override
    public T peek() {
        if (top == null) {
            throw new EmptyStackException();
        }
        return top.data;
    }
}
