package com.montini.expensesapp;

public class HelperClass3items {
    String mainItem, nextItem, xtraItem;

    public HelperClass3items(){}

    public HelperClass3items(String mainItem, String nextItem, String xtraItem) {
        this.mainItem = mainItem;
        this.nextItem = nextItem;
        this.xtraItem = xtraItem;
    }

    public String getMainItem() {
        return mainItem;
    }

    public String getNextItem() {
        return nextItem;
    }

    public String getXtraItem() {
        return xtraItem;
    }
}
