package com.devdroid.languagetranslator;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Spinner toSpinner, fromSpinner;
    private TextInputEditText edtSource;
    private ImageView imgMic;
    private MaterialButton btnTranslate;
    private TextView txtTranslateText;

    String[] fromLanguage = {"From", "English", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech",
            "Welsh", "Hindi", "Urdu"};

    String[] toLanguage = {"To", "English", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech",
            "Welsh", "Hindi", "Urdu"};

    private static final int REQUEST_PERMISSION_CODE = 1; // audio permission
    int languageCode, fromLanguageCode, toLanguageCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize variable and connect to xml
        init();

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fromLanguageCode = getLanguageCode(fromLanguage[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, fromLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);


        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                toLanguageCode = getLanguageCode(toLanguage[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, toLanguage);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        btnTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtTranslateText.setText("");
                // condition check
                if (Objects.requireNonNull(edtSource.getText()).toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please input your text to translate", Toast.LENGTH_SHORT).show();
                } else if (fromLanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "Please select source language", Toast.LENGTH_SHORT).show();
                } else if (toLanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "Please select to language", Toast.LENGTH_SHORT).show();
                } else {
                    translateText(fromLanguageCode, toLanguageCode, edtSource.getText().toString());
                }

            }
        });

        imgMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to convert into text");

                try {
                    startActivityForResult(intent, REQUEST_PERMISSION_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                edtSource.setText(result.toString());
            }
        }

    }

    private void translateText(int fromCode, int toCode, String source) {
        txtTranslateText.setText("Downloading Model...");

        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                txtTranslateText.setText("Translating...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        txtTranslateText.setText(s);
                        edtSource.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to translate!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        txtTranslateText.setText("Translated Text");
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to downloading language model!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                txtTranslateText.setText("Translated Text");
            }
        });
    }

    private int getLanguageCode(String s) {
        languageCode = 0;

        switch (s) {
            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                break;

            case "Afrikaans":
                languageCode = FirebaseTranslateLanguage.AF;
                break;

            case "Arabic":
                languageCode = FirebaseTranslateLanguage.AR;
                break;

            case "Belarusian":
                languageCode = FirebaseTranslateLanguage.BE;
                break;

            case "Bulgarian":
                languageCode = FirebaseTranslateLanguage.BG;
                break;

            case "Bengali":
                languageCode = FirebaseTranslateLanguage.BN;
                break;

            case "Catalan":
                languageCode = FirebaseTranslateLanguage.CA;
                break;

            case "Czech":
                languageCode = FirebaseTranslateLanguage.CS;
                break;

            case "Welsh":
                languageCode = FirebaseTranslateLanguage.CY;
                break;

            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                break;

            case "Urdu":
                languageCode = FirebaseTranslateLanguage.UR;
                break;

            default:
                languageCode = 0; // english
                break;
        }

        return languageCode;
    }

    private void init() {
        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        edtSource = findViewById(R.id.edtSource);
        imgMic = findViewById(R.id.imgMic);
        btnTranslate = findViewById(R.id.btnTranslate);
        txtTranslateText = findViewById(R.id.txtTranslatedText);
    }
}