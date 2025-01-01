package com.example.brainwave;

public class LocalDataSet {
    public static final String [] statues = {
        "Sleepy", "Awake"
    };

    static int label2index(String label) {
        int index;
        switch (label) {
            case "Sleepy":
                index = 0;
                break;
            case "Awake":
                index = 1;
                break;
            default:
                index = -1;
        }
        return index;
    }
}
