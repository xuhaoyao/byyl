package com.oyyy.cfg;

import java.util.Objects;

/**
 * @description:
 * @author: oyyy
 * @date: 2021/11/23 11:11
 */
public class Node {
    Character nonter;  //非终结符
    Character ter;  //终结符

    public Node(Character nonter, Character ter) {
        this.nonter = nonter;
        this.ter = ter;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(nonter, node.nonter) && Objects.equals(ter, node.ter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nonter, ter);
    }
}
