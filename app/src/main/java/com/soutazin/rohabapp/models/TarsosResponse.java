package com.soutazin.rohabapp.models;

public class TarsosResponse {
    float _pitchInHertz;
    int _indexOfNearest;
    double _cents;

    public float get_pitchInHertz() {
        return _pitchInHertz;
    }

    public void set_pitchInHertz(float _pitchInHertz) {
        this._pitchInHertz = _pitchInHertz;
    }

    public int get_indexOfNearest() {
        return _indexOfNearest;
    }

    public void set_indexOfNearest(int _indexOfNearest) {
        this._indexOfNearest = _indexOfNearest;
    }

    public double get_cents() {
        return _cents;
    }

    public void set_cents(double _cents) {
        this._cents = _cents;
    }
}
