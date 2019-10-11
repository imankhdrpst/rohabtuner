package com.soutazin.rohabapp.models;

public class FoundIndex {
    private int index = -1;
    private int occurrence = 0;

    public FoundIndex(int index, int occurrence) {
        this.index = index;
        this.occurrence = occurrence;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(int occurrence) {
        this.occurrence = occurrence;
    }
}
