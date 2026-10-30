package nl.han.ica.datastructures;

public class HanLinkedList<T> implements IHANLinkedList<T> {

    private class Node {
        T value;
        Node next;

        Node(T value) {
            this.value = value;
            this.next = null;
        }
    }

    // Dummy header node to simplify edge cases (insert/delete at index 0)
    private Node header;
    private int size;

    public HanLinkedList() {
        this.header = new Node(null);
        this.size = 0;
    }

    @Override
    public void addFirst(T value) {
        Node newNode = new Node(value);
        newNode.next = header.next;
        header.next = newNode;
        size++;
    }

    @Override
    public void clear() {
        header.next = null;
        size = 0;
    }

    @Override
    public void insert(int index, T value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        Node current = header;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }

        Node newNode = new Node(value);
        newNode.next = current.next;
        current.next = newNode;
        size++;
    }

    @Override
    public void delete(int pos) {
        if (pos < 0 || pos >= size) {
            throw new IndexOutOfBoundsException("Position: " + pos + ", Size: " + size);
        }

        Node current = header;
        for (int i = 0; i < pos; i++) {
            current = current.next;
        }

        current.next = current.next.next;
        size--;
    }

    @Override
    public T get(int pos) {
        if (pos < 0 || pos >= size) {
            throw new IndexOutOfBoundsException("Position: " + pos + ", Size: " + size);
        }

        Node current = header.next;
        for (int i = 0; i < pos; i++) {
            current = current.next;
        }

        return current.value;
    }

    @Override
    public void removeFirst() {
        if (size == 0) {
            throw new IndexOutOfBoundsException("Cannot remove from an empty list.");
        }

        header.next = header.next.next;
        size--;
    }

    @Override
    public T getFirst() {
        if (size == 0) {
            throw new IndexOutOfBoundsException("The list is empty.");
        }

        return header.next.value;
    }

    @Override
    public int getSize() {
        return size;
    }
}