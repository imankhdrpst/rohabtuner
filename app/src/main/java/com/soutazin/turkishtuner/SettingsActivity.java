package com.soutazin.turkishtuner;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.andexert.library.RippleView;


public class SettingsActivity extends Activity {

    private TextView txtBaseNote;
    private NumberPicker refrencePicker;
    private ImageView imgBemol1;
    private ImageView imgBemol2;
    private ImageView imgBemol3;
    private ImageView imgBemol4;
    private LinearLayout laySelectBemol;
    private LinearLayout laySelectBaseNote;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        getActionBar().hide();
        RippleView rippleFB = (RippleView) findViewById(R.id.fb_ripple);
        RippleView rippleText = (RippleView) findViewById(R.id.text_ripple);


        refrencePicker = (NumberPicker) findViewById(R.id.numRefrenceFrequency);
        refrencePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        refrencePicker.setMinValue(414);
        refrencePicker.setMaxValue(454);
        refrencePicker.setValue((int) PrefrencesHelper.getInstance().getBaseFrequency());
        refrencePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // فرکانس انتخابی را در دستگاه ذخیره کن
                PrefrencesHelper.getInstance().setBaseFrequency(newVal);
                MyActivity.baseFreqChanged = true;
//                initCurrentBaseFrequesncyArray();

            }
        });

        txtBaseNote = (TextView) findViewById(R.id.txt_base_not_view);
        imgBemol1 = (ImageView) findViewById(R.id.bemol1);
        imgBemol2 = (ImageView) findViewById(R.id.bemol2);
        imgBemol3 = (ImageView) findViewById(R.id.bemol3);
        imgBemol4 = (ImageView) findViewById(R.id.bemol4);
//        // تنظیم نیم گرده ها
        if (PrefrencesHelper.getInstance().getBaseBemol().length() > 4) {
            PrefrencesHelper.getInstance().setBaseBemol("");

        }
        if (PrefrencesHelper.getInstance().getBaseBemol().length() == 0) {
            imgBemol1.setVisibility(View.INVISIBLE);
            imgBemol2.setVisibility(View.INVISIBLE);
            imgBemol3.setVisibility(View.INVISIBLE);
            imgBemol4.setVisibility(View.INVISIBLE);
        } else if (PrefrencesHelper.getInstance().getBaseBemol().length() == 1) {
            imgBemol1.setVisibility(View.VISIBLE);
            imgBemol2.setVisibility(View.INVISIBLE);
            imgBemol3.setVisibility(View.INVISIBLE);
            imgBemol4.setVisibility(View.INVISIBLE);
        } else if (PrefrencesHelper.getInstance().getBaseBemol().length() == 2) {
            imgBemol1.setVisibility(View.VISIBLE);
            imgBemol2.setVisibility(View.VISIBLE);
            imgBemol3.setVisibility(View.INVISIBLE);
            imgBemol4.setVisibility(View.INVISIBLE);
        } else if (PrefrencesHelper.getInstance().getBaseBemol().length() == 3) {
            imgBemol1.setVisibility(View.VISIBLE);
            imgBemol2.setVisibility(View.VISIBLE);
            imgBemol3.setVisibility(View.VISIBLE);
            imgBemol4.setVisibility(View.INVISIBLE);
        } else if (PrefrencesHelper.getInstance().getBaseBemol().length() == 4) {
            imgBemol1.setVisibility(View.VISIBLE);
            imgBemol2.setVisibility(View.VISIBLE);
            imgBemol3.setVisibility(View.VISIBLE);
            imgBemol4.setVisibility(View.VISIBLE);
        }


        laySelectBemol = (LinearLayout) findViewById(R.id.layBemolSelection);
        laySelectBemol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // بصورت چرخشی ربع پرده را تغییر بده
                PrefrencesHelper.getInstance().setBaseBemol("b" + PrefrencesHelper.getInstance().getBaseBemol());
                if (PrefrencesHelper.getInstance().getBaseBemol().length() > 4) {
                    PrefrencesHelper.getInstance().setBaseBemol("");

                }
                if (PrefrencesHelper.getInstance().getBaseBemol().length() == 0) {
                    imgBemol1.setVisibility(View.INVISIBLE);
                    imgBemol2.setVisibility(View.INVISIBLE);
                    imgBemol3.setVisibility(View.INVISIBLE);
                    imgBemol4.setVisibility(View.INVISIBLE);
                } else if (PrefrencesHelper.getInstance().getBaseBemol().length() == 1) {
                    imgBemol1.setVisibility(View.VISIBLE);
                    imgBemol2.setVisibility(View.INVISIBLE);
                    imgBemol3.setVisibility(View.INVISIBLE);
                    imgBemol4.setVisibility(View.INVISIBLE);
                } else if (PrefrencesHelper.getInstance().getBaseBemol().length() == 2) {
                    imgBemol1.setVisibility(View.VISIBLE);
                    imgBemol2.setVisibility(View.VISIBLE);
                    imgBemol3.setVisibility(View.INVISIBLE);
                    imgBemol4.setVisibility(View.INVISIBLE);
                } else if (PrefrencesHelper.getInstance().getBaseBemol().length() == 3) {
                    imgBemol1.setVisibility(View.VISIBLE);
                    imgBemol2.setVisibility(View.VISIBLE);
                    imgBemol3.setVisibility(View.VISIBLE);
                    imgBemol4.setVisibility(View.INVISIBLE);
                } else if (PrefrencesHelper.getInstance().getBaseBemol().length() == 4) {
                    imgBemol1.setVisibility(View.VISIBLE);
                    imgBemol2.setVisibility(View.VISIBLE);
                    imgBemol3.setVisibility(View.VISIBLE);
                    imgBemol4.setVisibility(View.VISIBLE);
                }
            }
        });
        txtBaseNote.setText(PrefrencesHelper.getBaseNote());

        laySelectBaseNote = (LinearLayout) findViewById(R.id.lay_base_not);
        laySelectBaseNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // نت پایه را تغییر بده و در دستگاه ذخیره کن
                String bN = PrefrencesHelper.getInstance().getBaseNote();
                String nextBaseNote = "";
                if (bN.equals("C")) {
                    nextBaseNote = "B♭";
                } else if (bN.endsWith("B♭")) {
                    nextBaseNote = "F";
                } else if (bN.equals("F")) {
                    nextBaseNote = "E♭";
                } else if (bN.equals("E♭")) {
                    nextBaseNote = "C";
                }
                PrefrencesHelper.getInstance().setBaseNote(nextBaseNote);
                txtBaseNote.setText(nextBaseNote);
            }
        });


// تنظیمات به اشتراک گذاری متنی در اینجا انجام می شود
        Button buttonShare = (Button) findViewById(R.id.shareViaText);

        rippleText.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                final Intent intent = sharingIntent.putExtra(Intent.EXTRA_TEXT, "Quarter Tone Tuner by SoutAzin. Persian Version. Download from Cafe Bazaar or Google Play");
                startActivity(Intent.createChooser(intent, "Share Rohab"));
            }
        });



    }
}
