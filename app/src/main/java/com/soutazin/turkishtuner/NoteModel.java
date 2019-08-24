package com.soutazin.turkishtuner;

class NoteModel {
    private int _indexOfNearest = 0;
    private int _cents = 0;
    private int _nearest = 0;
    private boolean _pitched = false;
    private int lastIndexFound = 0;
    private int countOfThisFrequency = 0;

    public int get_indexOfNearest() {
        return _indexOfNearest;
    }

    public void set_indexOfNearest(int _indexOfNearest) {
        this._indexOfNearest = _indexOfNearest;
    }

    public int get_cents() {
        return _cents;
    }

    public void set_cents(int _cents) {
        this._cents = _cents;
    }

    public int get_nearest() {
        return _nearest;
    }

    public void set_nearest(int _nearest) {
        this._nearest = _nearest;
    }

    public boolean is_pitched() {
        return _pitched;
    }

    public void set_pitched(boolean _pitched) {
        this._pitched = _pitched;
    }

    public int getLastIndexFound() {
        return lastIndexFound;
    }

    public void setLastIndexFound(int lastIndexFound) {
        this.lastIndexFound = lastIndexFound;
    }

    public int getCountOfThisFrequency() {
        return countOfThisFrequency;
    }

    public void setCountOfThisFrequency(int countOfThisFrequency) {
        this.countOfThisFrequency = countOfThisFrequency;
    }
}
