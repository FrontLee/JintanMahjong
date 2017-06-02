package com.vinson.mahjong.base;

public class Node<E> implements Cloneable{
    public E data;

    public Node<E> next;

    public Node<E> previous;

    public Node(E value) {
        this.data = value;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
