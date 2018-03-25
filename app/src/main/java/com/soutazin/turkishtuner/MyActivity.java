package com.soutazin.turkishtuner;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import ru.katso.livebutton.LiveButton;

public class MyActivity extends Activity {


    public static boolean baseFreqChanged = true;
    int TG_OCTAVE = 0;  // نگه دارنده اکتاو در دیاپازون
    int currentIndexOfThisOctave = 0;
    private final int duration = 3; // زمان به صدا درآمدن دیاپازون
    private final int sampleRate = 8000; // ویژه دیاپازون
    private final int numSamples = duration * sampleRate; // ویژه دیاپازون
    private final double sample[] = new double[numSamples]; // ویژه دیاپازون

    private final byte generatedSnd[] = new byte[2 * numSamples];

    Handler handler = new Handler();
    //    static float[] allFrequencies = new float[289]; // تمام فرکانس ها در این آرایه قرار می گیرند
    static float[] freqInThisRefrence = new float[289];
    private static int MAX_TOKEN_RECOGNIZED = 3; // میزان دقت تیونر در تصمیم گیری ... هرچه بزرگتر باشد تصمیم دقیق تر است و البته نتیجه گیری سخت تر
    private static int countOfThisFrequency = 0;
    private static int lastIndexFound = 0;
    private ImageView imgGauge; // اندیکاتر
    NumberPicker refrencePicker; // تغییر فرکانس پایه در تنظیمات
    LinearLayout laySelectBemol; // تغییر بمل در تنظیمات
    LinearLayout laySelectBaseNote; // تغییر نت پایه در تنظیمات
    TextView txtBaseNote;
    ImageView imgBemol1;
    ImageView imgBemol2;
    ImageView imgBemol3;
    ImageView imgBemol4;

    private boolean lock = false;

    private static final int OCTAVE = 24;

    // علامت ها و نت ها در 24 حالت اساسی

    private static final String notes[] =
            {"C", "D", "E", "F", "G", "A", "B"};
    private static int tg_note_index = 0;

    private static final String notes_C[] =
            {"C", "C", "C", "D", "D", "D",
                    "D", "E", "E", "E", "F", "F", "F", "G", "G", "G", "G", "A", "A", "A", "A", "B", "B", "B"};
    private static final String sharps_C[] =
            {"", "♯³", "♯", "♭²", "", "♯³",
                    "♯", "♭²", "", "♯³", "", "♯³", "♯", "♭²", "", "♯³", "♯", "♭²"
                    , "", "♯³", "♯", "♭²", "", "♯³"};

    private static final String notes_Bb[] =
            {"D", "D", "D", "E", "E", "E",
                    "F", "F", "F", "G", "G", "G", "G", "A", "A", "A", "A", "B", "B", "B", "C", "C", "C", "C"};
    private static final String sharps_Bb[] =
            {"", "♯³", "\u266F", "\u266D²", "", "\u266F³",
                    "", "♯³", "♯", "\u266F³", "", "\u266F³", "\u266F", "\u266D²", "", "\u266F³", "\u266F", "\u266D²"
                    , "", "\u266F³", "", "♯³", "♯", "♭²"};

    private static final String notes_F[] =
            {"G", "G", "G", "A", "A", "A",
                    "A", "B", "B", "B", "C", "C", "C", "D", "D", "D", "D", "E", "E", "E", "F", "F", "F", "G"};
    private static final String sharps_F[] =
            {"", "\u266F³", "\u266F", "\u266D²", "", "\u266F³",
                    "\u266F", "\u266D²", "", "\u266F³", "", "\u266F³", "\u266F", "\u266D²", "", "\u266F³", "\u266F", "\u266D²"
                    , "", "\u266F³", "", "♯³", "♯", "\u266F³"};


    private static final String notes_Eb[] =
            {"A", "A", "A", "B", "B", "B",
                    "C", "C", "C", "D", "D", "D", "D", "E", "E", "E", "F", "F", "F", "G", "G", "G", "G", "A"};
    private static final String sharps_Eb[] =
            {"", "\u266F³", "\u266F", "\u266D²", "", "\u266F³",
                    "", "♯³", "♯", "♭²", "", "\u266F³", "\u266F", "\u266D²", "", "\u266F³", "", "♯³"
                    , "♯", "\u266F³", "", "♯³", "♯", "\u266F³"};

    int width, height;
    private float _pitchInHertz;
    private int _indexOfNearest;
    private boolean _pitched;
    private float _nearest;
    private double _cents;
    private int gaugeSpinInteger;
    private Thread mainThread = null;
    private CheckBox chkPersian;
    private float tg_frequency = 0.0f;
    private Dialog dlgHelpView = null;
    private TextView txtOctaveViewer;
    private TextView txtCentsViewer;
    private TextView txtNoteViewer;
    private ImageView imgSignViewer;
    private LinearLayout layCentsViewer;
    private ImageView imgcorrectnessView;
    private Dialog demoDialog = null;
    private TextView txtFreqViewer;
    private LiveButton btnSetting;
    private LiveButton btnToneGenerator;

    @Override
    public void onResume() {
        super.onResume();
        if (baseFreqChanged)
        {
            initFloats();
        }
        startTuner();
    }

    private void startTuner()
    {

        // راه اندازی اولیه نخ بند مربوط به تیونر
        AudioDispatcher dispatcher = AudioDispatcherFactory
                .fromDefaultMicrophone((int) PrefrencesHelper.getInstance().getSampleRate(), 2048, 0);

// این هندلر برای دریافت فرکانس ورودی راه اندازی می شود
        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(final PitchDetectionResult result, AudioEvent e) {
                try {

                    _pitchInHertz = result.getPitch();

                    if (_pitchInHertz == -1.0f) { // یعنی هیچ فرکانسی را دریافت نکرده است
                        _pitchInHertz = 0;
                        _indexOfNearest = 0;
                        _cents = 0;
                        _nearest = 0;
                        _pitched = false;
                        lastIndexFound = 0;
                        countOfThisFrequency = 0;
                    } else {
                        // به کمک کلاس های مربطوه به جتجوی فرکانس در میان نت های رهاب بپرداز

                        _indexOfNearest = Util.nearInclusive(freqInThisRefrence, /*audio.frequency*/ _pitchInHertz);
                        if (_indexOfNearest == lastIndexFound) { // اگر آخرین نتی که پیدا کردی تکراری است
                            countOfThisFrequency++;
                            Log.d("SATUNER", "pitch foiund : " + _pitchInHertz + " , nearest index found : " + _indexOfNearest);
                            if (countOfThisFrequency >= MAX_TOKEN_RECOGNIZED) { // اگر این نت بیش از 3 بار تکرار شده است پس درست است
                                _nearest = /*allFrequencies*/freqInThisRefrence[_indexOfNearest]/*[(int) PrefrencesHelper.getInstance().getBaseFrequency() - 414]*/;
                                Log.d("SATUNER", " nearest pitch found : " + _nearest);
                                // سنت را محاسبه کن
                                _cents = -1200.0f * (Math.log10(_nearest / _pitchInHertz/*frequency*/) / Math.log10(2.0));  // -12.0 * log2(nearest / frequency) * 10.0;
                                _pitched = _cents <= 5.0 && _cents >= -5.0;

                            } else {
                                _pitchInHertz = 0;
                                _indexOfNearest = 0;
                                _cents = 0;
                                _nearest = 0;
                                _pitched = false;
                            }
                        } else {
                            countOfThisFrequency = 0;
                            lastIndexFound = _indexOfNearest;
                            _indexOfNearest = 0;
                            _cents = 0;
                        }

                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!lock && _pitchInHertz > 0 && _indexOfNearest > 0) {
                                // از آنجایی که نت درست پیدا شده و تصمیم گیری انجام شده است باید این تصمیم اجرا شود
                                startDecision(_pitchInHertz, _indexOfNearest, _cents);
                            }
                        }
                    });


                } catch (Exception ex) {
                    int a = 0;
                    a++;
                }

            }
        };
        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                PrefrencesHelper.getInstance().getSampleRate(), 2048, pdh);
        dispatcher.addAudioProcessor(p);

        mainThread = new Thread(dispatcher, "audioRecorder");
        // به محض اتمام راه اندازی های اولیه ، ماشین تصمیم گیری را شروع کن
        mainThread.start();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // تنظیمات اولیه تیونر
//        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getActionBar().hide();
//        getActionBar().setTitle(getResources().getString(R.string.app_name));
        PrefrencesHelper.getInstance().init(getApplicationContext());


        gaugeSpinInteger = (int) getResources().getDimension(R.dimen.gauge_spin);

        android.view.Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        // فرکانس ها را بخوان و در آرایه بریز
        initFloats();

//        initCurrentBaseFrequesncyArray();


        txtOctaveViewer = (TextView) findViewById(R.id.txtOctaveViewer);
        txtCentsViewer = (TextView) findViewById(R.id.txtCentsViewer);
        txtNoteViewer = (TextView) findViewById(R.id.txtNoteViewer);
        txtFreqViewer = (TextView) findViewById(R.id.txtFreqViewer);
        btnToneGenerator = (LiveButton) findViewById(R.id.btnToneGenerator);
        btnSetting = (LiveButton) findViewById(R.id.btnSettings);
        imgSignViewer = (ImageView) findViewById(R.id.imgSignViewer);
        layCentsViewer = (LinearLayout) findViewById(R.id.layCentsViewer);
        imgcorrectnessView = (ImageView) findViewById(R.id.imgCorrectnessViewer);


        btnToneGenerator.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                createToneGeneratorDialog();

            }
        });

        btnSetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                createSettingDialog();
                Intent intent = new Intent(MyActivity.this, SettingsActivity.class);
                startActivity(intent);

            }
        });

        imgGauge = (ImageView) findViewById(R.id.imggauge);





        // بررسی کن اگر راهنما باید نشان داده شود
//        if (PrefrencesHelper.getInstance().isHelpVisible()) {
//            (new Handler()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
////                    createHelpdialog();
//                }
//            }, 500);
//        }

    }

//    private void initCurrentBaseFrequesncyArray() {
//        for (int j = 0; j < 289; j++) {
//            freqInThisRefrence[j] = /*allFrequencies*/freqInThisRefrence/*[j]*/[(int) PrefrencesHelper.getInstance().getBaseFrequency() - 414];
//        }
//
//    }

    @Override
    public void onDestroy() { // اگر از برنامه خارج شد
        super.onDestroy();
        try {
            mainThread.interrupt();
            mainThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getResourceBySharpSign(String sharp) { // برای تبدیل اندیس ها به علامت ها در دیاپازون استفاده می شود
        int sharpResourceId = 0;
        if (sharp.equals("♯³")) {
            sharpResourceId = R.drawable.diese3;
        } else if (sharp.equals("♯")) {
            sharpResourceId = R.drawable.diese;
        } else if (sharp.equals("♭²")) {
            sharpResourceId = R.drawable.bemol2;
        } else if (sharp.equals("♭")) {
            sharpResourceId = R.drawable.bemol1;
        }
        return sharpResourceId;
    }

    private void createToneGeneratorDialog() {
        Dialog dlg = new Dialog(MyActivity.this, R.style.TransparentProgressDialog);
        // ظاهر دیاپازون از این لایوت خوانده می شود
        dlg.setContentView(R.layout.tone_generator);
        dlg.setCancelable(true);

        TG_OCTAVE = 5;
        currentIndexOfThisOctave = 0;
        tg_note_index = 0;

        final NumberPicker octavePicker = (NumberPicker) dlg.findViewById(R.id.num_picker_octave);
        final TextView tgTxtNoteToneViewer = (TextView) dlg.findViewById(R.id.txtNoteViewer);
        final TextView tgTxtOctaveViewer = (TextView) dlg.findViewById(R.id.txtOctaveViewer);
        final ImageView tgImgSignViewer = (ImageView) dlg.findViewById(R.id.imgSignViewer);

        ImageButton btnDiesNote = (ImageButton) dlg.findViewById(R.id.btn_diese_tone);
        ImageButton btnBemolNote = (ImageButton) dlg.findViewById(R.id.btn_bemol_tone);
        Button btnPlaySound = (Button) dlg.findViewById(R.id.btn_play_tone);


        final int baseFrequency = (int) PrefrencesHelper.getInstance().getBaseFrequency();

// نمایش حالت انتخاب شده برای تولید صدا در دیاپازون
        tgTxtNoteToneViewer.setText(notes_C[currentIndexOfThisOctave]);
        tgTxtOctaveViewer.setText(String.valueOf(TG_OCTAVE));
        tgImgSignViewer.setImageResource(getResourceBySharpSign(sharps_C[currentIndexOfThisOctave]));


        btnPlaySound.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tg_frequency = /*allFrequencies*/freqInThisRefrence[TG_OCTAVE * 24 + currentIndexOfThisOctave]/*[baseFrequency - 414]*/;
// راه اندازی ابتدائی دیاپازون
                initToneGenerator(tg_frequency);

            }
        });

        btnBemolNote.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // یک ربع پرده پایین برو
                if (currentIndexOfThisOctave > 0) {
                    currentIndexOfThisOctave--;
                } else {
                    if (tg_note_index > 0) {
                        tg_note_index--;
                        currentIndexOfThisOctave = 23;
                    } else {
                        if (TG_OCTAVE > 0) {
                            TG_OCTAVE--;
                            tg_note_index = 6;
                            currentIndexOfThisOctave = 23;
                        }
                    }
                }

                tgTxtNoteToneViewer.setText(notes_C[currentIndexOfThisOctave]);
                tgTxtOctaveViewer.setText(String.valueOf(TG_OCTAVE));
                tgImgSignViewer.setImageResource(getResourceBySharpSign(sharps_C[currentIndexOfThisOctave]));
                octavePicker.setValue(TG_OCTAVE);

                tg_frequency = /*allFrequencies*/freqInThisRefrence[TG_OCTAVE * 24 + currentIndexOfThisOctave]/*[baseFrequency - 414]*/;

            }
        });

        btnDiesNote.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // یک ربع پرده بالا برو
                if (currentIndexOfThisOctave < 23) {
                    currentIndexOfThisOctave++;
                } else {
                    if (tg_note_index < 6) {
                        tg_note_index++;
                        currentIndexOfThisOctave = 0;
                    } else {
                        if (TG_OCTAVE < 11) {
                            TG_OCTAVE++;
                            tg_note_index = 0;
                            currentIndexOfThisOctave = 0;
                        }
                    }
                }
                tgTxtNoteToneViewer.setText(notes_C[currentIndexOfThisOctave]);
                tgTxtOctaveViewer.setText(String.valueOf(TG_OCTAVE));
                tgImgSignViewer.setImageResource(getResourceBySharpSign(sharps_C[currentIndexOfThisOctave]));
                octavePicker.setValue(TG_OCTAVE);
                tg_frequency = /*allFrequencies*/freqInThisRefrence[TG_OCTAVE * 24 + currentIndexOfThisOctave]/*[baseFrequency - 414]*/;

            }
        });

        tgTxtNoteToneViewer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // اگر روی نت تپ شد ، صدا را تولید کن
                tgTxtNoteToneViewer.setText(notes_C[currentIndexOfThisOctave]);
                tgTxtOctaveViewer.setText(String.valueOf(TG_OCTAVE));
                tgImgSignViewer.setImageResource(getResourceBySharpSign(sharps_C[currentIndexOfThisOctave]));
                tg_frequency = /*allFrequencies*/freqInThisRefrence[TG_OCTAVE * 24 + currentIndexOfThisOctave]/*[baseFrequency - 414]*/;

            }
        });

        octavePicker.setMaxValue(11);
        octavePicker.setMinValue(0);
        octavePicker.setValue(5);
        octavePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                // اگر اکتاو دیاپازون تغییر کرد اجرا می شود
                TG_OCTAVE = i1;
                tgTxtNoteToneViewer.setText(notes_C[currentIndexOfThisOctave]);
                tgTxtOctaveViewer.setText(String.valueOf(TG_OCTAVE));
                tgImgSignViewer.setImageResource(getResourceBySharpSign(sharps_C[currentIndexOfThisOctave]));
                tg_frequency = /*allFrequencies*/freqInThisRefrence[TG_OCTAVE * 24 + currentIndexOfThisOctave]/*[baseFrequency - 414]*/;

            }
        });


        dlg.show();


    }


    void initToneGenerator(final double frqOfTone) {
        // نخ بند مربوط به دیاپازون تعریف می شود
        Thread thread = new Thread(new Runnable() {
            public void run() {
                // در حین اجرا صدا مربوطه توسط متود زیر تولید می شود
                genTone(frqOfTone);
                handler.post(new Runnable() {

                    public void run() {
                        try {
                            // صدای تولید شده در مزحله قبلی در اینجا پخش می شود
                            playSound();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        // پس از تعریف نخ بند دیاپازون آن را اجرا کن
        thread.start();

    }

    void genTone(double freqOfTone) {
        // صدا های لازم را در آرایه بریز - بصورت فرکانس
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
        }
// صدا های را بصورت 16 بیتی نرمال می کند
        int idx = 0;
        for (double dVal : sample) {
            short val = (short) (dVal * 32767);
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
    }

    void playSound() throws IllegalStateException {
        // یک شی اندرویدی که حاوی صدای انالوگ است را اجرا کن
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, numSamples);
        // صدای انالوگ را پخش کن
        audioTrack.play();
    }

    private Dialog createAndShowDemoDialog() {// برای نمایش دیالوگ راهنمای برنامه است
        final Dialog dlg = new Dialog(MyActivity.this, R.style.TransparentProgressDialog);
        dlg.setContentView(R.layout.help_view);
        dlg.setCancelable(true);

        LinearLayout mainLayotu = (LinearLayout) dlg.findViewById(R.id.main_help_view);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(2 * width / 3, 2 * height / 3);
        mainLayotu.setLayoutParams(params);


        TextView txtHelp = (TextView) dlg.findViewById(R.id.tt_help_view);
        Button btnHelpSkip = (Button) dlg.findViewById(R.id.btn_help_skip);

        txtHelp.setText("This is a demonstration of " + BuildConfig.NATIONALITY + " Rohab , this version is limited to detect these notes : C, D, E, F \n\n\n \nHow to use Tuner:\nPlace your device in front of the sound source, play your intended note gently and wait for the tuner to detect the note.\nSetting:\nTo set up the tuner, Tap on the indicator once. You can set up the tuner here; tap on the first option to adjust the frequency range of standard “A” tone from 410 Hz. to 450 Hz. The second option is used to enable flat modes: b, bb, bbb, bbbb. Tap on the third option to select tone mode. Select C, Bb, F, Eb for woodwind musical instruments and select “C” for other musical instruments. Switch between Persian quarter-tone signs or Turkish sings to use them.\n\n\nTone generator:\nFirst tap and then hold on the indicator to use tone generator. You can choose your intended octave here. Tap on “a quarter tone higher” and “a quarter tone lower” key to make it continuous and circular tone ranging between 2c-8c.\n");

        btnHelpSkip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                final String appPackageName = BuildConfig.PAID_VERSION; // getPackageName() from Context or Activity object
                if (BuildConfig.PLATFORM.equals("play")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                else
                {
                    Intent intent = new Intent(Intent.ACTION_EDIT);
                    intent.setData(Uri.parse("bazaar://details?id=" + appPackageName));
                    intent.setPackage("com.farsitel.bazaar");
                    startActivity(intent);
                }


                // اگر درخواست عدم نمایش راهنما زده شود

//                PrefrencesHelper.getInstance().setHelpVisible(false);
                dlg.dismiss();
            }
        });


        return dlg;
    }


//    private void createSettingDialog() {
//
//        // دیالوگ تنظیمات در اینا ساخته می شود
//        Dialog dlg = new Dialog(MyActivity.this, R.style.MyDialogTheme);
//        dlg.setContentView(R.layout.settings_activity);
//        dlg.setCancelable(true);
//
//
//        RippleView rippleFB = (RippleView) dlg.findViewById(R.id.fb_ripple);
//        RippleView rippleText = (RippleView) dlg.findViewById(R.id.text_ripple);
//
//
//        refrencePicker = (NumberPicker) dlg.findViewById(R.id.numRefrenceFrequency);
//        refrencePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
//        refrencePicker.setMinValue(414);
//        refrencePicker.setMaxValue(454);
//        refrencePicker.setValue((int) PrefrencesHelper.getInstance().getBaseFrequency());
//        refrencePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
//            @Override
//            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//                // فرکانس انتخابی را در دستگاه ذخیره کن
//                PrefrencesHelper.getInstance().setBaseFrequency(newVal);
//                initFloats();
////                initCurrentBaseFrequesncyArray();
//
//            }
//        });
//
//        txtBaseNote = (TextView) dlg.findViewById(R.id.txt_base_not_view);
//        imgBemol1 = (ImageView) dlg.findViewById(R.id.bemol1);
//        imgBemol2 = (ImageView) dlg.findViewById(R.id.bemol2);
//        imgBemol3 = (ImageView) dlg.findViewById(R.id.bemol3);
//        imgBemol4 = (ImageView) dlg.findViewById(R.id.bemol4);
//        // تنظیم نیم گرده ها
//        if (PrefrencesHelper.getInstance().getBaseBemol().length() > 4) {
//            PrefrencesHelper.getInstance().setBaseBemol("");
//
//        }
//        if (PrefrencesHelper.getInstance().getBaseBemol().length() == 0) {
//            imgBemol1.setVisibility(View.INVISIBLE);
//            imgBemol2.setVisibility(View.INVISIBLE);
//            imgBemol3.setVisibility(View.INVISIBLE);
//            imgBemol4.setVisibility(View.INVISIBLE);
//        } else if (PrefrencesHelper.getInstance().getBaseBemol().length() == 1) {
//            imgBemol1.setVisibility(View.VISIBLE);
//            imgBemol2.setVisibility(View.INVISIBLE);
//            imgBemol3.setVisibility(View.INVISIBLE);
//            imgBemol4.setVisibility(View.INVISIBLE);
//        } else if (PrefrencesHelper.getInstance().getBaseBemol().length() == 2) {
//            imgBemol1.setVisibility(View.VISIBLE);
//            imgBemol2.setVisibility(View.VISIBLE);
//            imgBemol3.setVisibility(View.INVISIBLE);
//            imgBemol4.setVisibility(View.INVISIBLE);
//        } else if (PrefrencesHelper.getInstance().getBaseBemol().length() == 3) {
//            imgBemol1.setVisibility(View.VISIBLE);
//            imgBemol2.setVisibility(View.VISIBLE);
//            imgBemol3.setVisibility(View.VISIBLE);
//            imgBemol4.setVisibility(View.INVISIBLE);
//        } else if (PrefrencesHelper.getInstance().getBaseBemol().length() == 4) {
//            imgBemol1.setVisibility(View.VISIBLE);
//            imgBemol2.setVisibility(View.VISIBLE);
//            imgBemol3.setVisibility(View.VISIBLE);
//            imgBemol4.setVisibility(View.VISIBLE);
//        }
//
//
//        laySelectBemol = (LinearLayout) dlg.findViewById(R.id.layBemolSelection);
//        laySelectBemol.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // بصورت چرخشی ربع پرده را تغییر بده
//                PrefrencesHelper.getInstance().setBaseBemol("b" + PrefrencesHelper.getInstance().getBaseBemol());
//                if (PrefrencesHelper.getInstance().getBaseBemol().length() > 4) {
//                    PrefrencesHelper.getInstance().setBaseBemol("");
//
//                }
//                if (PrefrencesHelper.getInstance().getBaseBemol().length() == 0) {
//                    imgBemol1.setVisibility(View.INVISIBLE);
//                    imgBemol2.setVisibility(View.INVISIBLE);
//                    imgBemol3.setVisibility(View.INVISIBLE);
//                    imgBemol4.setVisibility(View.INVISIBLE);
//                } else if (PrefrencesHelper.getInstance().getBaseBemol().length() == 1) {
//                    imgBemol1.setVisibility(View.VISIBLE);
//                    imgBemol2.setVisibility(View.INVISIBLE);
//                    imgBemol3.setVisibility(View.INVISIBLE);
//                    imgBemol4.setVisibility(View.INVISIBLE);
//                } else if (PrefrencesHelper.getInstance().getBaseBemol().length() == 2) {
//                    imgBemol1.setVisibility(View.VISIBLE);
//                    imgBemol2.setVisibility(View.VISIBLE);
//                    imgBemol3.setVisibility(View.INVISIBLE);
//                    imgBemol4.setVisibility(View.INVISIBLE);
//                } else if (PrefrencesHelper.getInstance().getBaseBemol().length() == 3) {
//                    imgBemol1.setVisibility(View.VISIBLE);
//                    imgBemol2.setVisibility(View.VISIBLE);
//                    imgBemol3.setVisibility(View.VISIBLE);
//                    imgBemol4.setVisibility(View.INVISIBLE);
//                } else if (PrefrencesHelper.getInstance().getBaseBemol().length() == 4) {
//                    imgBemol1.setVisibility(View.VISIBLE);
//                    imgBemol2.setVisibility(View.VISIBLE);
//                    imgBemol3.setVisibility(View.VISIBLE);
//                    imgBemol4.setVisibility(View.VISIBLE);
//                }
//            }
//        });
//        txtBaseNote.setText(PrefrencesHelper.getBaseNote());
//
//        laySelectBaseNote = (LinearLayout) dlg.findViewById(R.id.lay_base_not);
//        laySelectBaseNote.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // نت پایه را تغییر بده و در دستگاه ذخیره کن
//                String bN = PrefrencesHelper.getInstance().getBaseNote();
//                String nextBaseNote = "";
//                if (bN.equals("C")) {
//                    nextBaseNote = "B♭";
//                } else if (bN.endsWith("B♭")) {
//                    nextBaseNote = "F";
//                } else if (bN.equals("F")) {
//                    nextBaseNote = "E♭";
//                } else if (bN.equals("E♭")) {
//                    nextBaseNote = "C";
//                }
//                PrefrencesHelper.getInstance().setBaseNote(nextBaseNote);
//                txtBaseNote.setText(nextBaseNote);
//            }
//        });
//
//        // تنظیمات به اشتراک گذاری در فیسبوک در اینجا انجام می شود
//        callbackManager = CallbackManager.Factory.create();
//        shareDialog = new ShareDialog(this);
//        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
//
//            @Override
//            public void onSuccess(Sharer.Result result) {
//
//            }
//
//            @Override
//            public void onCancel() {
//
//            }
//
//            @Override
//            public void onError(FacebookException e) {
//
//            }
//        });
//        // محتوای به اشتراک گذاری در فیسبوک :
//        ShareLinkContent content = new ShareLinkContent.Builder()
//                .setContentUrl(Uri.parse("https://www.google.com"))
//                .setContentTitle("Sout Azin Quarter Tone Tuner")
//                .setImageUrl(Uri.parse("http://www.soutazin.ir/images/hdr-SA-logo.gif"))
//                .setContentDescription("Download From Play Store")
//                .build();
//
//        ShareButton shareButton = (ShareButton) dlg.findViewById(R.id.facebook_share_button);
//        shareButton.setShareContent(content);
//
//
//// تنظیمات به اشتراک گذاری متنی در اینجا انجام می شود
//        Button buttonShare = (Button) dlg.findViewById(R.id.shareViaText);
//
//        rippleText.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
//            @Override
//            public void onComplete(RippleView rippleView) {
//
//                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//                sharingIntent.setType("text/plain");
//                final Intent intent = sharingIntent.putExtra(Intent.EXTRA_TEXT, "Quarter Tone Tuner by SoutAzin. Persian Version. Download from : " + "https://play.google.com/store/apps/details?id=" + getPackageName());
//                startActivity(Intent.createChooser(sharingIntent, "Share Rohab"));
//            }
//        });
//// اینجا انتخاب می شود که از علامت های فارسی استفاده شود یا خیر
////        chkPersian = (CheckBox) dlg.findViewById(R.id.chk_persian_signs);
////        chkPersian.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
////            @Override
////            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
////                 در دستگاه ذخیره کن
////                PrefrencesHelper.getInstance().setPersian(b);
////            }
////        });
//
////        chkPersian.setChecked(PrefrencesHelper.getInstance().isPersian());
//
//
//        dlg.show();
//
//
//    }

    public void initFloats() {

        baseFreqChanged = false;

        // از فایلی که روی دستگاه هست فرکانسها را بگیر و در آرایه بریز
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("user.data")));

            String mLine = reader.readLine();
            int c = 0;
            while (mLine != null) {
                String[] tArr = mLine.split("\t");
                for (int j = 0; j < tArr.length; j++) {
                    if (j == (int) PrefrencesHelper.getInstance().getBaseFrequency() - 414) {
                        /*allFrequencies*/
                        freqInThisRefrence[c] = Float.parseFloat(tArr[j]);
                    }
                }
                mLine = reader.readLine();
                c++;
            }


        } catch (IOException e) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }

    }

    private void startDecision(final float freqHertz, final int index, final double cents) {
        float xscale = width / 11;

        float dge2 = ((float) cents * (xscale / gaugeSpinInteger)/*, -height / 64*/);
        // انیشمیشن مربوط به چرخش اندیکاتر
        RotateAnimation animation = new RotateAnimation(0, dge2, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(1200);
        animation.setFillAfter(true);
        animation.setFillEnabled(true);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(1);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                lock = true;
                // تصمیم گرفته شده را نمایش بده به محض شروع چرخش اندیگاتور
                viewDecision(freqHertz, index, cents);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // نمایش تصمیم را کنسل کن
                viewDecision(0, 0, cents);
                lock = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imgGauge.startAnimation(animation);
    }

    private void viewDecision(float freqHertz, int index, double cents) {
        // نمایش تصمیم
        String note = "", sharp = "", octave = "";
        int additive = PrefrencesHelper.getInstance().getBaseBemol().length() * 2;
        Log.d("SATUNER", "viewing decision---> index : " + index + " , bemols factor   :" + additive + " , octave :" + OCTAVE);
        if (PrefrencesHelper.getInstance().getBaseNote().startsWith("C")) {
            note = freqHertz <= 0.0f ? "" : notes_C[/*audio.*/(index + additive) % OCTAVE];
            sharp = freqHertz <= 0.0f ? "" : sharps_C[/*audio.*/(index + additive) % OCTAVE];
        } else if (PrefrencesHelper.getInstance().getBaseNote().startsWith("E")) {
            note = freqHertz <= 0.0f ? "" : notes_Eb[/*audio.*/(index + additive) % OCTAVE];
            sharp = freqHertz <= 0.0f ? "" : sharps_Eb[/*audio.*/(index + additive) % OCTAVE];
        } else if (PrefrencesHelper.getInstance().getBaseNote().startsWith("F")) {
            note = freqHertz <= 0.0f ? "" : notes_F[/*audio.*/(index + additive) % OCTAVE];
            sharp = freqHertz <= 0.0f ? "" : sharps_F[/*audio.*/(index + additive) % OCTAVE];
        } else if (PrefrencesHelper.getInstance().getBaseNote().startsWith("B")) {
            note = freqHertz <= 0.0f ? "" : notes_Bb[/*audio.*/(index + additive) % OCTAVE];
            sharp = freqHertz <= 0.0f ? "" : sharps_Bb[/*audio.*/(index + additive) % OCTAVE];
        }
        if (freqHertz != 0f) {
            txtFreqViewer.setText(String.format("%.2f", freqHertz) + "\nHz");
        }
        else
        {
            txtFreqViewer.setText("");
        }

// فیلتر مربوط به کوک بودن یا نبودن نت
        int correctnessResource = R.drawable.wrong;

        if (cents <= 5.0f && cents >= -5.0f) {
            correctnessResource = R.drawable.correct;
        }
        int sharpResourceId = 0;
        if (sharp.equals("♯³")) {
            if (BuildConfig.TYPE.startsWith("PERSIAN") /*PrefrencesHelper.getInstance().isPersian()*/) {
                sharpResourceId = R.drawable.sori;
            } else {
                sharpResourceId = R.drawable.diese3;
            }
        } else if (sharp.equals("♯")) {
            sharpResourceId = R.drawable.diese;
        } else if (sharp.equals("♭²")) {
            if (BuildConfig.TYPE.startsWith("PERSIAN")) {
                sharpResourceId = R.drawable.koron;
            } else {
                sharpResourceId = R.drawable.bemol2;
            }
        } else if (sharp.equals("♭")) {
            sharpResourceId = R.drawable.bemol1;
        }

        int octaveFound = /*audio.*/(index + additive) / OCTAVE - 1;
        octave = /*audio.frequency*/freqHertz == 0.0f ? "" : String.valueOf(octaveFound);

        if (freqHertz <= 0.0f || octaveFound < 0) {
            txtNoteViewer.setText("");
            txtCentsViewer.setText("");
            txtOctaveViewer.setText("");
            imgSignViewer.setVisibility(View.INVISIBLE);
            layCentsViewer.setVisibility(View.INVISIBLE);
            imgcorrectnessView.setVisibility(View.INVISIBLE);
        } else {
            // در نسخه دمو در اینجا بررسی می شود که اگر از نت های دو یا ر یا می  یا فا هست نشان بده و در غیر این صورت - را نمایش بده
            if (BuildConfig.TYPE.endsWith("DEMO")) {
                if (note.equals("C") || note.equals("D") || note.equals("E") || note.equals("F")) {

                    imgSignViewer.setImageResource(sharpResourceId);
                    txtOctaveViewer.setText(octave);
                    txtNoteViewer.setText(note);
                    imgSignViewer.setVisibility(View.VISIBLE);
                    imgSignViewer.setImageResource(sharpResourceId);
                    layCentsViewer.setVisibility(View.VISIBLE);
                    String s = "";
                    if (cents > 0) {
                        s = "+";
                    } else {
                        s = "-";
                    }
                    String centstring = String.format("%.0f", (Math.abs(cents)));
                    if (!centstring.equals("0")) {
                        s += centstring;
                    } else {
                        s = centstring;
                    }
                    txtCentsViewer.setText(cents == 0 ? "" : s);
                    imgcorrectnessView.setVisibility(View.VISIBLE);
                    imgcorrectnessView.setImageResource(correctnessResource);
                } else {
                    imgGauge.clearAnimation();
                    if (demoDialog == null || !demoDialog.isShowing()) {
                        demoDialog = createAndShowDemoDialog();
                        demoDialog.show();
                    }
                }
            }
            else
            {
                imgSignViewer.setImageResource(sharpResourceId);
                txtOctaveViewer.setText(octave);
                txtNoteViewer.setText(note);
                imgSignViewer.setVisibility(View.VISIBLE);
                imgSignViewer.setImageResource(sharpResourceId);
                layCentsViewer.setVisibility(View.VISIBLE);
                String s = "";
                if (cents > 0) {
                    s = "+";
                } else {
                    s = "-";
                }
                String centstring = String.format("%.0f", (Math.abs(cents)));
                if (!centstring.equals("0")) {
                    s += centstring;
                } else {
                    s = centstring;
                }
                txtCentsViewer.setText(cents == 0 ? "" : s);
                imgcorrectnessView.setVisibility(View.VISIBLE);
                imgcorrectnessView.setImageResource(correctnessResource);
            }
        }

    }
}
