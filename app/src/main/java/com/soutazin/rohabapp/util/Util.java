package com.soutazin.rohabapp.util;

import java.util.Arrays;

/**
 * Created by iman on 10/2/2015.
 */

public class Util{

    // این کلاس برای کمک به خواندن فرکانس ها و پیدا کردن نزدیک ترین اندیس به فرکانس مربوطه ایجاد شده است
    public static Integer nearInclusive(final float[] array, final float value) {
        Integer i = null;
        int idx = binarySearch(array, value);
        if (idx < 0) {
            idx = -(idx) - 1;
            if (idx == 0 || idx >= array.length) {
                // Do nothing. This point is outside the array bounds return value will be null
            }
            else {
                // Find nearest point
                double d0 = Math.abs(array[idx - 1] - value);
                double d1 = Math.abs(array[idx] - value);

                i = (d0 <= d1) ? idx - 1 : idx;
            }
        }
        else {
            i = idx;
        }
        return i;
    }

    // متود های جستجو در بین فرکانس ها

    public static int binarySearch(float[] a, float key) {
        int index = -1;
        if (a[0] < a[1]) {
            index = Arrays.binarySearch(a, key);
        }
        else {
            index = binarySearch(a, key, 0, a.length - 1);
        }
        return index;
    }

    private static int binarySearch(float[] a, float key, int low, int high) {
        while (low <= high) {
            int mid = (low + high) / 2;
            double midVal = a[mid];

            int cmp;
            if (midVal > key) {
                cmp = -1; // Neither val is NaN, thisVal is smaller
            }
            else if (midVal < key) {
                cmp = 1; // Neither val is NaN, thisVal is larger
            }
            else {
                long midBits = Double.doubleToLongBits(midVal);
                long keyBits = Double.doubleToLongBits(key);
                cmp = (midBits == keyBits ? 0 : (midBits < keyBits ? -1 : 1)); // (0.0, -0.0) or (NaN, !NaN)
            }

            if (cmp < 0) {
                low = mid + 1;
            }
            else if (cmp > 0) {
                high = mid - 1;
            }
            else {
                return mid; // key found
            }
        }
        return -(low + 1); // key not found.
    }
}
