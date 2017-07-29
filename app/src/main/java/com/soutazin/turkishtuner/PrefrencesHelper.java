package com.soutazin.turkishtuner;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by pc on 8/11/2014.
 */
public class PrefrencesHelper {

    //این کلاس یک سینگلتون است که وظیفه ذخیره سازی تنظیمات کاربر روی دستگاه و واکشی آن ها در ابتدای برنامه را بر عهده دارد

    private static PrefrencesHelper INSTANCE;

    private static final String PREFS_NAME = "SOUTAZIN_DATA";

    private static final String BASE_NOTE = "basenote";
    private static final String BASE_BEMOL = "basebemol";
    private static final String BASE_FREQUENCY = "basefrequency";
    private static final String SAMPLE_RATE = "samplerate";
    private static final String VIEW_HELP = "helpvisible";







    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;

    private static String baseNote, baseBemol;
    private static float baseFrequency;
    private static long sampleRate;
    private int baseFrequesncy;
    private static boolean helpVisible = true;

    PrefrencesHelper() {
    }

    public synchronized static PrefrencesHelper getInstance() {
        if(INSTANCE == null){
            INSTANCE = new PrefrencesHelper();
        }
        return INSTANCE;
    }


    public synchronized void init(Context pContext) {

        // راه اندازی اولیه تنظیمات با مقادیر پیش فرض
        if (mSettings == null) {
            mSettings = pContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            mEditor = mSettings.edit();
            baseNote = mSettings.getString(BASE_NOTE, "C"); // C,
            baseBemol = mSettings.getString(BASE_BEMOL, "");
//            isSensitive = mSettings.getBoolean(IS_SENSITIVE, false);
            baseFrequency =mSettings.getFloat(BASE_FREQUENCY, 440.0f);
            sampleRate = mSettings.getLong(SAMPLE_RATE, 44100);
//            persian = mSettings.getBoolean(PERSIAN, false);
            helpVisible = mSettings.getBoolean(VIEW_HELP, true);
        }
    }

    public synchronized void clearAll()
    {
        mEditor.clear();
        mEditor.commit();
    }

    public synchronized void setHelpVisible(boolean b)
    {
        mEditor.putBoolean(VIEW_HELP, b);
        mEditor.commit();
        helpVisible = b;
    }

    public boolean isHelpVisible()
    {
        return helpVisible;
    }

//    public synchronized  void setPersian(boolean b){
//        mEditor.putBoolean(PERSIAN, b);
//        mEditor.commit();
//        persian = b;
//    }
//    public boolean isPersian()
//    {
//        return false;//  persian; this is th turkish version
//    }

    public synchronized void setBaseNote(String note)
    {

        mEditor.putString(BASE_NOTE, note);
        mEditor.commit();
        baseNote = note;
    }
    public synchronized void setBaseBemol(String bemol)
    {

        mEditor.putString(BASE_BEMOL, bemol);
        mEditor.commit();
        baseBemol   = bemol;
    }

    public synchronized void setBaseFrequency(float frequnecy)
    {

        baseFrequency = frequnecy;
        mEditor.putFloat(BASE_FREQUENCY, frequnecy);
        mEditor.commit();
    }
//    public synchronized  void setIsSensitive(boolean sensitive)
//    {
//        mEditor.putBoolean(IS_SENSITIVE, sensitive);
//        mEditor.commit();
//        isSensitive = sensitive;
//    }
    public synchronized  void setSampleRate(long sr)
    {
        sampleRate= sr;
        mEditor.putLong(SAMPLE_RATE, sampleRate);
        mEditor.commit();
    }

    public static String getBaseNote()
    {
        return  baseNote;
    }
    public static String getBaseBemol()
    {
        return  baseBemol;
    }
    public static float getBaseFrequency()
    {
        return baseFrequency;
    }
//    public static boolean isSensitive()
//    {
//        return isSensitive;
//    }
    public static long getSampleRate()
    {
        return  sampleRate;
    }

}
