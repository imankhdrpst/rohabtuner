package com.soutazin.rohabapp.models;

public class NoteModel {
    private float frequency = 0;
    private int index = 0;
    private double cents = 0;
    private boolean _pitched = false;

    public NoteModel(float frequency,int index, double cents, boolean pitched) {
        this.frequency = frequency;
        this.cents = cents;
        this._pitched = pitched;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getCents() {
        return cents;
    }

    public void setCents(double cents) {
        this.cents = cents;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public void set_cents(int _cents) {
        this.cents = _cents;
    }

    public boolean is_pitched() {
        return _pitched;
    }

    public void set_pitched(boolean _pitched) {
        this._pitched = _pitched;
    }

}
