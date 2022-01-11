package com.oyyy.cfg;

import java.util.ArrayList;

/**
 * @description:产生式
 * @author: oyyy
 * @date: 2021/11/19 19:14
 */
public class Rules {
    char left;  //左部符号
    ArrayList<StringBuffer>right;  //右部
    boolean isAppear;  //求间接左公因子要用到

    public Rules(char left) {
        this.left = left;
    }

    public Rules(char left, ArrayList<StringBuffer> right) {
        this.left = left;
        this.right=right;
        this.isAppear=false;
    }

    @Override
    public String toString() {
        return "Rules{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}
