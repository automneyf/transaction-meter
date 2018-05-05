package com.weiqi.util;

/**
 * Created by Weiqi on 5/4/2018.
 */
public class NumberUtil {

    public static double getMoneyAmount(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
}
