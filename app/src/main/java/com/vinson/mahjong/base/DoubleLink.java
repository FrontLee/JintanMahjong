package com.vinson.mahjong.base;

public class DoubleLink<E> implements Cloneable{
    public Node<E> first;

    public Node<E> last;

    public int size;

    public DoubleLink() {
        first = null;
        last = null;
        size = 0;
    }

    public void insertFirst(E value) {
        Node<E> newLink = new Node<E>(value);
        if (isEmpty())
            last = newLink;
        else
            first.previous = newLink;
        newLink.next = first;
        first = newLink;
        newLink.previous = null;
        size++;
    }

    public int getNotNullSize() {
        int count = 0;
        Node<E> node = first;
        while(node != null){
            count++;
            node = node.next;
        }
        return count;
    }

    public int insertNext(E value, Node<E> position) {
        if (position == null)
            return -1;

        if (!isExist(position))
            return 0;

        Node<E> newLink = new Node<E>(value);
        if (position.equals(last)) {
            last = newLink;
            newLink.next = null;
        } else {
            Node<E> next = position.next;
            next.previous = newLink;
            newLink.next = next;
        }
        newLink.previous = position;
        position.next = newLink;
        size++;
        return 1;

    }

    public int insertNext(Node<E> value, Node<E> position) {
        if (position == null)
            return -1;

        if (!isExist(position))
            return 0;

        if (position.equals(last)) {
            last = value;
            value.next = null;
        } else {
            Node<E> next = position.next;
            next.previous = value;
            value.next = next;
        }
        value.previous = position;
        position.next = value;
        size++;
        return 1;

    }

    public int insertPrevious(E value, Node<E> position) {
        if (position == null)
            return -1;

        if (!isExist(position))
            return 0;

        Node<E> newLink = new Node<E>(value);
        if (position.equals(first)) {
            first = newLink;
            newLink.previous = null;
        } else {
            Node<E> previous = position.previous;
            previous.next = newLink;
            newLink.previous = previous;
        }
        newLink.next = position;
        position.previous = newLink;
        size++;
        return 1;
    }

    public void insertLast(E value) {
        Node<E> newLink = new Node<E>(value);
        if (isEmpty())
            first = newLink;
        else {
            last.next = newLink;
            newLink.previous = last;
        }
        last = newLink;
        newLink.next = null;
        size++;
    }

    public void deleteFirst() {
        if (isEmpty()) {
            return;
        }
        if (size == 1) {
            first = null;
            last = null;
        } else {
            first = first.next;
            first.next.previous = null;
        }

        size--;
    }

    public int delete(Node<E> node) {
        if (node == null) {
            return -1;
        }
        if (!isExist(node)) {
            return 0;
        }
        if (first.next == null && node.equals(first)) {
            first = null;
            last = null;
            node = null;
            size--;
            return 1;
        }
        if (node.equals(first) && first.next != null) {
            first = node.next;
            node.next.previous = null;
            node = null;
            size--;
            return 2;
        }
        if (node.equals(last) && last.previous != null) {
            last = node.previous;
            node.previous.next = null;
            node = null;
            size--;
            return 3;
        }
        node.previous.next = node.next;
        node.next.previous = node.previous;
        node = null;
        size--;
        return 4;
    }

    public void deleteLast() {
        if (isEmpty()) {
            return;
        }
        if (size == 1) {
            first = null;
            last = null;
        } else {
            last = last.previous;
            last.next = null;
        }
        size--;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isExist(Node<E> position) {
        Node<E> current = first;
        boolean result = false;
        while (current != null) {
            if (current.equals(position)) {
                result = true;
                break;
            }
            current = current.next;
        }
        return result;
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
