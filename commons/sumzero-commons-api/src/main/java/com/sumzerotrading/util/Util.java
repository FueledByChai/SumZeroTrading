/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.util;

import com.sumzerotrading.data.SumZeroException;

/**
 *
 * @author RobTerpilowski
 */
public class Util {

    public static int roundLot(int size, int minimumSize) {
        if (minimumSize == 0) {
            throw new SumZeroException("Cannot divide by zero");
        }
        int increments = (int) Math.round((double) size / (double) minimumSize);
        return increments * minimumSize;
    }

    public static String parseTicker(String ticker) {
        if (ticker.indexOf(".") != -1) {
            ticker = ticker.substring(0, ticker.indexOf("."));
        }
        if (ticker.startsWith("0")) {
            return parseTicker(ticker.substring(1));
        } else {
            return ticker;
        }
    }

}
