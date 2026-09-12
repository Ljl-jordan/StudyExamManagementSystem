package com.ljl.studyexammanagementsystem;

public class Test {
    public static String reserve(String str) {
        if (str == null || str.length() <= 1) {
            return str;
        }
        char[] arr = str.toCharArray();
        int left = 0;
        int right = str.length() - 1;
        while (left < right) {
            char temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }

        return new String(arr);

    }
    public static void main (String[]args){

        System.out.println(reserve("hello"));

    }
}