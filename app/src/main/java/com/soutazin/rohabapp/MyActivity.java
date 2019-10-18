package com.soutazin.rohabapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.soutazin.rohabapp.models.NoteModel;
import com.soutazin.rohabapp.util.PrefrencesHelper;
import com.soutazin.rohabapp.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import ru.katso.livebutton.LiveButton;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MyActivity extends AppCompatActivity {


    private static final String TAG = "TUNER";
    private static final float CENTS_FACTOR = -1200.0f;// ثابت ضریب محاسبه سنت
    private static final int PERMISSION_RECORD_AUDIO_REQUEST_CODE = 1001;
    private static final int MAX_NUMBER_OF_OCC_TO_VIEW = 5;// تعیین کننده این است که چند بار ظاهر شدن یک نت باعث نمایش ان در صفحه نمایش میشود؟
    private static final float PITCHED_THRESHOLD = 5.0f; // تعیین کننده بازه ای است که اگر سنت در آن بازه باشد یعنی چراغ سبز شود
    private static final long GAUGE_ANIMATION_DURATION = 1000;
    private static int TG_OCTAVE = 0;  // نگه دارنده اکتاو در دیاپازون

    public static boolean baseFreqChanged = true;  // این پرچم نمایش میدهد که آیا فرکانس پایه تغییر کرده یا خیر

    int currentIndexOfThisOctave = 0; // نشان می دهد که اندیس اکتاو جاری برای محاسبات چیست

    private final int duration = 3; // زمان به صدا درآمدن دیاپازون

    private final int sampleRate = 8000; // ویژه دیاپازون

    private final int numSamples = duration * sampleRate; // ویژه دیاپازون

    private final double sample[] = new double[numSamples]; // ویژه دیاپازون

    private final byte generatedSnd[] = new byte[2 * numSamples]; // یک آرایه برای انجام محاسبات ریاضی برای تولید صدا

    private static final Handler handler = new Handler(); // هندلر اصلی برنامه

    private static Thread mainThread = null; // نخ اصلی پروسه صدا

    private static AudioDispatcher dispatcher = null; // کلاس بررسی وانتقال صدا به فرکانس

    private static PitchDetectionHandler pdh = null; // هندلر اصلی کتابخانه TarsosDSP

    private static final List<Integer> indicesList = new ArrayList<>(); // لیست اندیس صدا های کشف شده در آخرین صدایی که تشخیص داده شده است

    private static final List<Integer> centsList = new ArrayList<>(); // لیست سنت های کشف شده برای آخرین صدای کشف شده

    // کلاس نگهدارنده Object های disposable مربطو به ساختار RxJava
    // ساختار RxJava برای این به پروژه افزوده شده است
    // که تضمینی بر عملیات Multi-Threading برای فرآیدن های تشخیص نت باشد
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    static float[] freqInThisReference = new float[289]; // تمام فرکانس های تعریف شده در مرجع جاری (مثلا 450) در این آرایه ریخته خواهد شد تا در محاسبات شرکت کند

    private ImageView imgGauge; // این View همان نمایشگر درجه دار وسط صفحه است

    private AtomicBoolean lock = new AtomicBoolean(false); // یک پرچم همگام سازی شده برای اینکه انجام برخی محاسبات را متوقف سازد


    private static final int OCTAVE = 24; // نمایشگر اکتاو که بصورت پیش فرض روی 24 قرار داده شده

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
    private float tg_frequency = 0.0f; // فرکانس مربوط به ToneGenerator
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
    private RotateAnimation gaugeAnimation;

    @Override
    public void onResume() {
        super.onResume();
        if (baseFreqChanged) { // اگر از صفحه setting برگردیم و تغییراتی داشته باشیم برای اعمال آن ها لازم است از ابتدا محاسبات انجام شود
            initFloats();
        }
        startTuner();
    }


    @Override
    protected void onPause() {
        super.onPause();
        try {
            dispatcher.stop(); // لازم است هنگام متوقف شدن برنامه dispatcher از کار بیفتد
        } catch (Exception e) {

        }
    }

    private void startTuner() {

        // این تیونر به این شکل کار میکند که از کتابخانه Tarsos برای تشخیص صدا از میکروفون دستگاه استفاده می کند
        // این کتابخانه یک Handler دارد که صدا را به صورت یک Object جاوائی برمیگرداند.
        // برای تشخیص صدا از میکروفون و ارسال آن به این Handler از یک Dispatcher استفاده می کند.
        // در پایین این توضیحات نحوه راه اندازی این Dispatcher مشخص است.
        // برای تنظیم SampleRate بطور پیش فرض 44100 در نظر گرفته شده است.

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED) {
            // راه اندازی اولیه نخ بند مربوط به تیونر
            dispatcher = AudioDispatcherFactory
                    .fromDefaultMicrophone((int) PrefrencesHelper.getInstance().getSampleRate(), 2048, 0);

// این هندلر برای دریافت فرکانس ورودی راه اندازی می شود
            pdh = new PitchDetectionHandler() {
                @Override
                public void handlePitch(final PitchDetectionResult result, AudioEvent e) {
                    try {

                        final float _pitchInHertz = result.getPitch();

                        compositeDisposable.add(
                                getObservable(_pitchInHertz)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<NoteModel>() {
                                            @Override
                                            public void accept(NoteModel noteModel) throws Exception {
                                                // نت محاسبه شده و لازم است تا نمایش داده شود
                                                startDecision(noteModel);
                                            }
                                        }, new Consumer<Throwable>() {
                                            @Override
                                            public void accept(Throwable throwable) throws Exception {

                                            }
                                        }));


                    } catch (Exception ex) {
                    }

                }
            };
            AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                    PrefrencesHelper.getInstance().getSampleRate(), 2048, pdh);
            dispatcher.addAudioProcessor(p);

            mainThread = new Thread(dispatcher, "audioRecorder");
//         به محض اتمام راه اندازی های اولیه ، ماشین تصمیم گیری را شروع کن
            mainThread.start();
        } else // permission must be granted
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_RECORD_AUDIO_REQUEST_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_RECORD_AUDIO_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PERMISSION_GRANTED) {
                    startTuner();
                } else {
//                    new AlertDialog.Builder(this)
//                            .setTitle("Permission Required")
//                            .setMessage("ROHAB needs to be granted to use your audio recorder")
//                            .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    startTuner();
//                                }
//                            })
//                            .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    finish();
//                                }
//                            })
//                            .show();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    private Observable<NoteModel> getObservable(final float _pitchInHertz) {
        return Observable.fromCallable(new Callable<NoteModel>() {
            @Override
            public NoteModel call() throws Exception {
                if (_pitchInHertz == -1.0f) {
                    // یعنی هیچ فرکانسی را دریافت نکرده است
                    return null;
                } else {
                    // به کمک کلاس های مربطوه به جتجوی فرکانس در میان نت های رهاب بپرداز

                    int _indexOfNearest = Util.nearInclusive(freqInThisReference, /*audio.frequency*/ _pitchInHertz);

                    indicesList.add(_indexOfNearest);
//                    _indexOfNearest = Collections.max(indicesList);
                    if (Collections.frequency(indicesList, _indexOfNearest) > MAX_NUMBER_OF_OCC_TO_VIEW) {

                        // سنت را محاسبه کن
                        long foundCents = Math.round(CENTS_FACTOR * (Math.log10(freqInThisReference[_indexOfNearest] / _pitchInHertz) / Math.log10(2.0)));
                        centsList.add((int) foundCents);

                        long _cents = Collections.max(centsList);

                        // ابتدا باید آ رایه های شامل نت ها و سنت های پیدا شده پاک شوند تا در محاسبات بعدی شرکت نکنند
                        indicesList.clear();
                        centsList.clear();

                        return new NoteModel(freqInThisReference[_indexOfNearest], _indexOfNearest, _cents, _cents != 0 && _cents <= PITCHED_THRESHOLD && _cents >= -PITCHED_THRESHOLD);

                    }
                    return null;
                }

            }
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // تنظیمات اولیه تیونر
//        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        PrefrencesHelper.getInstance().init(getApplicationContext());

        // دسترسی به ابعاد گوشی برای محاسبات چرخش درجه ها
        Display display = getWindowManager().getDefaultDisplay();
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

                // با کلیک روی دکمه ToneGenerator  صفحه مربوطه باز می شود
                createToneGeneratorDialog();

            }
        });

        btnSetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                // با کلیک روی دکمه Setting صفحه مربوطه باز می شود
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

    @Override
    public void onDestroy() { // اگر از برنامه خارج شد
        super.onDestroy();
        try {
            dispatcher.stop();
            compositeDisposable.dispose();
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

        // ظاهر دیاپازون از این لایوت خوانده می شود
        Dialog dlg = new Dialog(MyActivity.this, R.style.TransparentProgressDialog);
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


// نمایش حالت انتخاب شده برای تولید صدا در دیاپازون
        tgTxtNoteToneViewer.setText(notes_C[currentIndexOfThisOctave]);
        tgTxtOctaveViewer.setText(String.valueOf(TG_OCTAVE));
        tgImgSignViewer.setImageResource(getResourceBySharpSign(sharps_C[currentIndexOfThisOctave]));


        btnPlaySound.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                    tg_frequency = freqInThisReference[TG_OCTAVE * 24 + currentIndexOfThisOctave];
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

                tg_frequency = freqInThisReference[TG_OCTAVE * 24 + currentIndexOfThisOctave];

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
                tg_frequency = freqInThisReference[TG_OCTAVE * 24 + currentIndexOfThisOctave];

            }
        });

        tgTxtNoteToneViewer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // اگر روی نت تپ شد ، صدا را تولید کن
                tgTxtNoteToneViewer.setText(notes_C[currentIndexOfThisOctave]);
                tgTxtOctaveViewer.setText(String.valueOf(TG_OCTAVE));
                tgImgSignViewer.setImageResource(getResourceBySharpSign(sharps_C[currentIndexOfThisOctave]));
                tg_frequency = freqInThisReference[TG_OCTAVE * 24 + currentIndexOfThisOctave];

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
                tg_frequency = freqInThisReference[TG_OCTAVE * 24 + currentIndexOfThisOctave];

            }
        });

        dlg.show();
    }


    void initToneGenerator(final double frqOfTone) {
        // نخ بند مربوط به دیاپازون تعریف می شود
        Thread thread = new Thread(new Runnable() {
            public void run() {
                // در حین اجرا صدا مربوطه توسط متود زیر تولید می شود
                handler.post(new Runnable() {

                    public void run() {
                        try {
                            // صدای تولید شده در مزحله قبلی در اینجا پخش می شود
                            genTone(frqOfTone);
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

        txtHelp.setText("This is a demonstration of " + BuildConfig.NATIONALITY + " Rohab , this version is limited to detect these notes : C, D, E, F  \nHow to use Tuner:\nPlace your device in front of the sound source, play your intended note gently and wait for the tuner to detect the note.\nSetting:\nTo set up the tuner, Tap on the indicator once. You can set up the tuner here; tap on the first option to adjust the frequency range of standard “A” tone from 410 Hz. to 450 Hz. The second option is used to enable flat modes: b, bb, bbb, bbbb. Tap on the third option to select tone mode. Select C, Bb, F, Eb for woodwind musical instruments and select “C” for other musical instruments. Switch between Persian quarter-tone signs or Turkish sings to use them.\n\n\nTone generator:\nFirst tap and then hold on the indicator to use tone generator. You can choose your intended octave here. Tap on “a quarter tone higher” and “a quarter tone lower” key to make it continuous and circular tone ranging between 2c-8c.\n");

        btnHelpSkip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                final String appPackageName = BuildConfig.PAID_VERSION; // getPackageName() from Context or Activity object

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
//                if (BuildConfig.PLATFORM.equals("play")) {
//                } else {
//                    Intent intent = new Intent(Intent.ACTION_EDIT);
//                    intent.setData(Uri.parse("bazaar://details?id=" + appPackageName));
//                    intent.setPackage("com.farsitel.bazaar");
//                    startActivity(intent);
//                }
//                Intent intent = new Intent(Intent.ACTION_EDIT);
//                intent.setData(Uri.parse("bazaar://details?id=" + appPackageName));
//                intent.setPackage("com.farsitel.bazaar");
//                startActivity(intent);

                dlg.dismiss();
            }
        });


        return dlg;
    }


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
                        freqInThisReference[c] = Float.parseFloat(tArr[j]);
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

    private void startDecision(final NoteModel noteModel) {

        // انیشمیشن مربوط به چرخش اندیکاتر
        float dge2 = (float) Math.toDegrees(Math.tan(noteModel.getCents() / 30) * 1.44);

        if (lock.compareAndSet(true, true)) {
            try {
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        gaugeAnimation = new RotateAnimation(0, dge2, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        gaugeAnimation.setDuration(GAUGE_ANIMATION_DURATION);
        gaugeAnimation.setFillAfter(true);
        gaugeAnimation.setFillEnabled(true);
        gaugeAnimation.setRepeatMode(Animation.REVERSE);
        gaugeAnimation.setRepeatCount(1);
        gaugeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                lock.set(true);
                // تصمیم گرفته شده را نمایش بده به محض شروع چرخش اندیگاتور
                viewDecision(noteModel, false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // نمایش تصمیم را کنسل کن
                viewDecision(new NoteModel(0, 0, noteModel.getCents(), false), true);
                lock.set(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imgGauge.startAnimation(gaugeAnimation);
    }

    private void viewDecision(NoteModel noteModel, boolean clear) {
        // نمایش تصمیم
        String note = "", sharp = "", octave = "";

        int additive = PrefrencesHelper.getInstance().getBaseBemol().length() * 2;

        if (PrefrencesHelper.getInstance().getBaseNote().startsWith("C")) {
            note = noteModel.getFrequency() <= 0.0f ? "" : notes_C[/*audio.*/(noteModel.getIndex() + additive) % OCTAVE];
            sharp = noteModel.getFrequency() <= 0.0f ? "" : sharps_C[/*audio.*/(noteModel.getIndex() + additive) % OCTAVE];
        } else if (PrefrencesHelper.getInstance().getBaseNote().startsWith("E")) {
            note = noteModel.getFrequency() <= 0.0f ? "" : notes_Eb[/*audio.*/(noteModel.getIndex() + additive) % OCTAVE];
            sharp = noteModel.getFrequency() <= 0.0f ? "" : sharps_Eb[/*audio.*/(noteModel.getIndex() + additive) % OCTAVE];
        } else if (PrefrencesHelper.getInstance().getBaseNote().startsWith("F")) {
            note = noteModel.getFrequency() <= 0.0f ? "" : notes_F[/*audio.*/(noteModel.getIndex() + additive) % OCTAVE];
            sharp = noteModel.getFrequency() <= 0.0f ? "" : sharps_F[/*audio.*/(noteModel.getIndex() + additive) % OCTAVE];
        } else if (PrefrencesHelper.getInstance().getBaseNote().startsWith("B")) {
            note = noteModel.getFrequency() <= 0.0f ? "" : notes_Bb[/*audio.*/(noteModel.getIndex() + additive) % OCTAVE];
            sharp = noteModel.getFrequency() <= 0.0f ? "" : sharps_Bb[/*audio.*/(noteModel.getIndex() + additive) % OCTAVE];
        }
        if (noteModel.getFrequency() != 0f) {
            txtFreqViewer.setText(String.format("%.2f", noteModel.getFrequency()) + "\nHz");
        } else {
            txtFreqViewer.setText("");
        }

// فیلتر مربوط به کوک بودن یا نبودن نت
        int correctnessResource = R.drawable.wrong;

        if (noteModel.getCents() <= 5.0f && noteModel.getCents() >= -5.0f) {
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

        int octaveFound = /*audio.*/(noteModel.getIndex() + additive) / OCTAVE - 1;
        octave = /*audio.frequency*/noteModel.getFrequency() == 0.0f ? "" : String.valueOf(octaveFound);

        if (clear && noteModel.getFrequency() <= 0.0f || octaveFound < 0) {
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
                    if (noteModel.getCents() > 0) {
                        s = "+";
                    } else {
                        s = "-";
                    }
                    String centstring = String.format("%.0f", (Math.abs(noteModel.getCents())));
                    if (!centstring.equals("0")) {
                        s += centstring;
                    } else {
                        s = centstring;
                    }
                    txtCentsViewer.setText(noteModel.getCents() == 0 ? "" : s);
                    imgcorrectnessView.setVisibility(View.VISIBLE);
                    imgcorrectnessView.setImageResource(correctnessResource);
                } else {
                    imgGauge.clearAnimation();
                    if (demoDialog == null || !demoDialog.isShowing()) {
                        demoDialog = createAndShowDemoDialog();
                        demoDialog.show();
                    }
                }
            } else {
                imgSignViewer.setImageResource(sharpResourceId);
                txtOctaveViewer.setText(octave);
                txtNoteViewer.setText(note);
                imgSignViewer.setVisibility(View.VISIBLE);
                imgSignViewer.setImageResource(sharpResourceId);
                layCentsViewer.setVisibility(View.VISIBLE);
                String s = "";
                if (noteModel.getCents() > 0) {
                    s = "+";
                } else {
                    s = "-";
                }
                String centstring = String.format("%.0f", (Math.abs(noteModel.getCents())));
                if (!centstring.equals("0")) {
                    s += centstring;
                } else {
                    s = centstring;
                }
                txtCentsViewer.setText(noteModel.getCents() == 0 ? "" : s);
                imgcorrectnessView.setVisibility(View.VISIBLE);
                imgcorrectnessView.setImageResource(correctnessResource);
            }
        }

    }
}
