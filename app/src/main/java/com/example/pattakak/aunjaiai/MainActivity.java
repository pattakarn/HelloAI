package com.example.pattakak.aunjaiai;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RecognitionListener {

    private static final String TAG = "aiDataService";
    private static final int REQUEST_RECORD_PERMISSION = 100;
    private TextView returnedText;
    private FloatingActionButton btnFab;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    private String state = "standby";
    private Boolean statusSpeech = false;
    private Boolean speak = false;
    private int timer = 2000;
    private Button btnNext;
    Context context;

    private AIAunJai aunJai;
    AIDataService aiDataService;
    TextSpeechAPI textSpeech;
    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        returnedText = (TextView) findViewById(R.id.textView1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        btnFab = (FloatingActionButton) findViewById(R.id.fab);
        btnNext = (Button) findViewById(R.id.btn_next);
        aunJai = new AIAunJai(MainActivity.this);
        context = MainActivity.this;

        progressBar.setVisibility(View.INVISIBLE);
        textSpeech = TextSpeechAPI.getInstance(context);

        createSpeech();

        final AIConfiguration config = new AIConfiguration("decb05094e7f4dbf9be0b79ccf23cb68",
                AIConfiguration.SupportedLanguages.fromLanguageTag("TH"),
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(config);

        btnFab.setOnClickListener(this);
        btnNext.setOnClickListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    speech.startListening(recognizerIntent);
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast
                            .LENGTH_SHORT).show();
//                    speech.startListening(recognizerIntent);
                }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }
    }


    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        setChange(false);
//        speech.startListening(recognizerIntent);

    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage + " - " + errorCode);
        returnedText.setText(errorMessage);
        setChange(false);
        speech.destroy();
//        createSpeech();
//        setChange(true);

        new CountDownTimer(1000, 1000) {

            public void onTick(long millisUntilFinished) {
//                mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
//                mTextField.setText("done!");
                createSpeech();
                setChange(true);
            }
        }.start();

    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        boolean check = false;
        for (String result : matches) {
            text += result + "\n";
            if (state.equals("standby")) {
                check = aunJai.checkAunjai(result);
                if (check)
                    break;
            } else if (state.equals("command")) {
                check = aunJai.checkLight(result);
                state = aunJai.getState();
            }

        }
//
//        if (check) {
//            timer = 2000;
//            aunJai.setState(state);
//            aunJai.checkShow();
//            state = aunJai.getState();
//        } else {
//            timer = 2000;
//            state = "standby";
//        }

//        returnedText.setText(text);
////        setChange(true);
//        new CountDownTimer(timer, 1000) {
//
//            public void onTick(long millisUntilFinished) {
////                mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
//            }
//
//            public void onFinish() {
////                mTextField.setText("done!");
////                createSpeech();
//                setChange(true);
//            }
//        }.start();
        String query = "";
        for (String result : matches) {
            query = result;
            break;
        }


        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery(query);

        new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                final AIRequest request = requests[0];
                try {
                    Log.d("aiDataService", "++++++++++++++++++++++++++++++++ test");
                    final AIResponse response = aiDataService.request(aiRequest);

                    ai.api.model.Status status = response.getStatus();
                    Log.i(TAG, "Status code: " + status.getCode());
                    Log.i(TAG, "Status type: " + status.getErrorType());

                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                if (aiResponse != null) {
                    Unmute();
                    // process aiResponse here
                    Log.i(TAG, "onPostExecute: ================================");
                    Result result = aiResponse.getResult();

                    Log.i(TAG, "onPostExecute Resolved query: " + result.getResolvedQuery());
                    Log.i(TAG, "onPostExecute Action: " + result.getAction());

                    final String speech = result.getFulfillment().getSpeech();
                    Log.i(TAG, "onPostExecute: " + speech);

                    Metadata metadata = result.getMetadata();
                    if (metadata != null) {
                        Log.i(TAG, "Intent id: " + metadata.getIntentId());
                        Log.i(TAG, "Intent name: " + metadata.getIntentName());
                    }

                    HashMap<String, JsonElement> params = result.getParameters();
                    if (params != null && !params.isEmpty()) {
                        Log.i(TAG, "Parameters: ");
                        for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                            Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                        }
                    }


                    String checkStatus = aunJai.AICheckLight(metadata.getIntentName(), speech);

                    returnedText.setText(speech);
                    if (checkStatus.equals("1")) {
                        Log.i(TAG, "Speak: ================================ Speak");
                        TextSpeechAPI.getInstance(context).speak(speech);
                        timer = 2000;
                    }
                    if (checkStatus.equals("0")) {
                        timer = 2000;
                    } else if (checkStatus.equals("60000")) {
                        timer = 60000;
                        speak = true;
                    } else {
                        TextSpeechAPI.getInstance(context).speak(speech);
                        timer = 2000;
                    }

                    countDownTimer = new CountDownTimer(timer, 1000) {
                        public void onTick(long millisUntilFinished) {
//                mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);

                        }

                        public void onFinish() {
//                mTextField.setText("done!");
//                createSpeech();
                            setChange(true);
                        }
                    }.start();


                }
            }
        }.execute(aiRequest);
    }

//    public Boolean checkAunjai(String text){
//        Boolean check =false;
//        if (text.matches("(.*)อุ่นใจ(.*)")) {
//            check = true;
//            aunJai.checkLight(text);
//            state = aunJai.getState();
//        }
//        return check;
//    }

//    public Boolean checkLight(String text){
//        if (text.matches("(.*)เปิดไฟ(.*)")) {
//            if (text.matches("(.*)ห้องนอน(.*)")) {
//                state = "command-open-bedroom";
//            } else if (text.matches("(.*)ห้องนั่งเล่น(.*)")) {
//                state = "command-open-livingroom";
//            } else if (text.matches("(.*)หน้าบ้าน(.*)")) {
//                state = "command-open-fronthome";
//            } else {
//                state = "command-open";
//            }
//        } else if (text.matches("(.*)ปิดไฟ(.*)")) {
//            if (text.matches("(.*)ห้องนอน(.*)")) {
//                state = "command-close-bedroom";
//            } else if (text.matches("(.*)ห้องนั่งเล่น(.*)")) {
//                state = "command-close-livingroom";
//            }  else if (text.matches("(.*)หน้าบ้าน(.*)")) {
//                state = "command-close-fronthome";
//            } else {
//                state = "command-close";
//            }
//        } else if ((text.matches("(.*)อัพเดทข่าว(.*)") && text.matches("(.*)ให้หน่อย(.*)"))
//                || text.matches("(.*)อ่านข่าว(.*)")) {
//            if (text.matches("(.*)การเมือง(.*)")) {
//                state = "command-news-politics";
//            } else if (text.matches("(.*)บันเทิง(.*)")) {
//                state = "command-news-entertainment";
//            } else if (text.matches("(.*)รอบโลก(.*)")) {
//                state = "command-news-world";
//            } else if (text.matches("(.*)ไลฟ์สไตล์(.*)")) {
//                state = "command-news-lifestyle";
//            } else if (text.matches("(.*)เศรษฐกิจ(.*)")) {
//                state = "command-news-economy";
//            } else if (text.matches("(.*)การเงิน(.*)")) {
//                state = "command-news-money";
//            } else if (text.matches("(.*)การตลาด(.*)")) {
//                state = "command-news-market";
//            } else if (text.matches("(.*)ไอที(.*)")) {
//                state = "command-news-it";
//            } else {
//                state = "command-news";
//            }
//        } else if (text.matches("(.*)เช็คไฟ(.*)") || text.matches("(.*)ดูไฟ(.*)")) {
//            if (text.matches("(.*)ห้องนอน(.*)")) {
//                state = "command-status-bedroom";
//            } else if (text.matches("(.*)ห้องนั่งเล่น(.*)")) {
//                state = "command-status-livingroom";
//            }  else if (text.matches("(.*)หน้าบ้าน(.*)")) {
//                state = "command-status-fronthome";
//            } else {
//                state = "command-status";
//            }
//        }
//        return true;
//    }

//    public void checkShow(){
//        if (state.equals("standby")) {
//            TextSpeechAPI.getInstance(getApplication())
//                    .speak("มีอะไรให้อุ่นใจช่วยคะ");
//            state = "command";
//        } else if (state.equals("command-open")) {
//            setLight(true);
//            TextSpeechAPI.getInstance(getApplication())
//                    .speak("เปิดไฟเรียบร้อยคะ");
//            state = "standby";
//        } else if (state.equals("command-close")) {
//            setLight(false);
//            TextSpeechAPI.getInstance(getApplication())
//                    .speak("ปิดไฟเรียบร้อยคะ");
//            state = "standby";
//        } else if (state.equals("command-open-bedroom")) {
//            setLight(true);
//            TextSpeechAPI.getInstance(getApplication())
//                    .speak("เปิดไฟห้องนอนเรียบร้อยคะ");
//            state = "standby";
//        } else if (state.equals("command-close-bedroom")) {
//            setLight(false);
//            TextSpeechAPI.getInstance(getApplication())
//                    .speak("ปิดไฟห้องนอนเรียบร้อยคะ");
//            state = "standby";
//        } else if (state.equals("command-open-livingroom")) {
//            setLight(true);
//            TextSpeechAPI.getInstance(getApplication())
//                    .speak("เปิดไฟห้องนั่งเล่นเรียบร้อยคะ");
//            state = "standby";
//        } else if (state.equals("command-close-livingroom")) {
//            setLight(false);
//            TextSpeechAPI.getInstance(getApplication())
//                    .speak("ปิดไฟห้องนั่งเล่นเรียบร้อยคะ");
//            state = "standby";
//        } else if (state.equals("command-open-fronthome")) {
//            setLight(true);
//            TextSpeechAPI.getInstance(getApplication())
//                    .speak("เปิดไฟหน้าบ้านเรียบร้อยคะ");
//            state = "standby";
//        } else if (state.equals("command-close-fronthome")) {
//            setLight(false);
//            TextSpeechAPI.getInstance(getApplication())
//                    .speak("ปิดไฟหน้าบ้านเรียบร้อยคะ");
//            state = "standby";
//        } else if (state.matches("(.*)command-status(.*)")) {
//            StatusLight();
//            state = "standby";
//        } else if (state.matches("(.*)command-news(.*)")) {
//            checkNEWS();
//            state = "standby";
//            timer = 60000;
//        } else if (state.equals("command")) {
//            TextSpeechAPI.getInstance(getApplication())
//                    .speak("แล้วเจอกันใหม่นะคะ");
//            state = "standby";
//
//        }
//    }

    public void Mute() {
        Log.d("Mute", "++++++++++++++++++++++++++++++++++++++++++++++++");
        AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
        amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        amanager.setStreamMute(AudioManager.STREAM_RING, true);
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
    }

    public void Unmute() {
        Log.d("Unmute", "++++++++++++++++++++++++++++++++++++++++++++++++");
        AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
        amanager.setStreamMute(AudioManager.STREAM_ALARM, false);
        amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        amanager.setStreamMute(AudioManager.STREAM_RING, false);
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

//    public void StatusLight() {
//        String url = "https://blynk-cloud.com/580a5279aaa14eb3af3d1acaf2d55851/get/";
//        OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(url)
//                .addConverterFactory(GsonConverterFactory.create())
//                .client(okHttpClient)
//                .build();
//
//        APIService service = retrofit.create(APIService.class);
//        Call<String[]> call = null;
//        if (state.matches("(.*)livingroom(.*)")){
//            call = service.statusLightD4();
//        } else {
//            call = service.statusLight();
//        }
//        Log.d("statusLight", "++++++++ " + call.request().toString());
//        if (call != null) {
//            call.enqueue(new Callback<String[]>() {
//                @Override
//                public void onResponse(Call<String[]> call, Response<String[]> response) {
//                    String[] result = response.body();
//                    Log.d("Response statusLight", "============== " + result[0]);
//                    String text = "";
//                    if (result[0].equals("1")) {
//                        if (state.matches("(.*)bedroom(.*)")){
//                            text = "ตอนนี้ไฟห้องนอนเปิดอยู่คะ";
//                        } else if (state.matches("(.*)livingroom(.*)")) {
//                            text = "ตอนนี้ไฟห้องนั่งเล่นเปิดอยู่คะ";
//                        } else if (state.matches("(.*)fronthome(.*)")) {
//                            text = "ตอนนี้ไฟหน้าบ้านเปิดอยู่คะ";
//                        }
//                    } else {
//                        if (state.matches("(.*)bedroom(.*)")){
//                            text = "ตอนนี้ไฟห้องนอนปิดอยู่คะ";
//                        } else if (state.matches("(.*)livingroom(.*)")) {
//                            text = "ตอนนี้ไฟห้องนั่งเล่นปิดอยู่คะ";
//                        } else if (state.matches("(.*)fronthome(.*)")) {
//                            text = "ตอนนี้ไฟหน้าบ้านปิดอยู่คะ";
//                        }
//                    }
//                    TextSpeechAPI.getInstance(getApplication())
//                            .speak(text);
//                }
//
//                @Override
//                public void onFailure(Call<String[]> call, Throwable t) {
//                    Log.d("failure statusLight", "============== " + t.getMessage());
//                }
//            });
//        }
//    }

//    public void setLight(boolean active) {
//        String url = "https://blynk-cloud.com/580a5279aaa14eb3af3d1acaf2d55851/update/";
//        OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(url)
//                .addConverterFactory(GsonConverterFactory.create())
//                .client(okHttpClient)
//                .build();
//
//        APIService service = retrofit.create(APIService.class);
//        Call<ResponseBody> call = null;
//        if (active) {
//            if (state.matches("(.*)livingroom(.*)")) {
//                call = service.openLightD4();
//            } else {
//                call = service.openLight();
//            }
//        } else {
//            if (state.matches("(.*)livingroom(.*)")) {
//                call = service.closeLightD4();
//            } else {
//                call = service.closeLight();
//            }
//        }
//
//        Log.d("setLight", "++++++++ " + call.request().toString());
//        if (call != null) {
//            call.enqueue(new Callback<ResponseBody>() {
//                @Override
//                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                    Log.d("Response", "==============");
//                }
//
//                @Override
//                public void onFailure(Call<ResponseBody> call, Throwable t) {
//                    Log.d("failure", "============== " + t.getMessage());
//                }
//            });
//        }
//    }

    public void setChange(Boolean isChecked) {
        Log.d("CheckChange", "+++++++++++++" + isChecked);
        if (isChecked) {
            Mute();
            statusSpeech = true;
            createSpeech();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
            ActivityCompat.requestPermissions
                    (MainActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            REQUEST_RECORD_PERMISSION);
//                    speech.startListening(recognizerIntent);
        } else {
            statusSpeech = false;
            progressBar.setIndeterminate(false);
            progressBar.setVisibility(View.INVISIBLE);
            speech.stopListening();

        }
    }

    public void createSpeech() {
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "th-TH");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (speak) {
                    setChange(false);
                    speak = false;
                    speech.destroy();
                    btnFab.setImageResource(R.drawable.ic_mic_white);
                    btnFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    Log.i(LOG_TAG, "Click Speak: ===============================================");
                    countDownTimer.cancel();
                    aunJai.stopTTS();
//                    aunJai.stopTTS();
                } else if (statusSpeech) {
                    aunJai.stopTTS();
                    speech.destroy();
                    setChange(false);
                    btnFab.setImageResource(R.drawable.ic_mic_white);
                    btnFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    Log.i(LOG_TAG, "Click status 1: ==========================================");

                } else {
                    setChange(true);
                    btnFab.setImageResource(R.drawable.ic_stop_white);
                    btnFab.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
//                    aunJai.stopTTS();
                    Log.i(LOG_TAG, "onBufferReceived: ========================================");


                }
//                checkNEWS();
                break;
            case R.id.btn_next:
                startActivity(new Intent(MainActivity.this, DialogFlowActivity.class));
//                getDialog();
                break;
        }

    }

    public void getDialog() {


        String url = "https://dialogflow.googleapis.com/v2beta1/";
        OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        APIService service = retrofit.create(APIService.class);
        Call<String> call = null;
        call = service.getDialog2();
        Log.d("statusLight", "++++++++ " + call.request().toString());
        if (call != null) {
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    String result = response.body();
//                    Log.d("Response getDialog", "============== " + result.toString());
                    Log.d("Response getDialog", "============== " + response.toString());
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.d("failure statusLight", "============== " + t.getMessage());
                }
            });
        }
    }

//    public static void detectIntentTexts(String projectId, List<String> texts, String sessionId,
//                                         String languageCode) throws Exception {
//        // Instantiates a client
//        try (SessionsClient sessionsClient = SessionsClient.create()) {
//            // Set the session name using the sessionId (UUID) and projectID (my-project-id)
//            SessionName session = SessionName.of(projectId, sessionId);
//            System.out.println("Session Path: " + session.toString());
//
//            // Detect intents for each text input
//            for (String text : texts) {
//                // Set the text (hello) and language code (en-US) for the query
//                Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode);
//
//                // Build the query with the TextInput
//                QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();
//
//                // Performs the detect intent request
//                DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);
//
//                // Display the query result
//                QueryResult queryResult = response.getQueryResult();
//
//                System.out.println("====================");
//                System.out.format("Query Text: '%s'\n", queryResult.getQueryText());
//                System.out.format("Detected Intent: %s (confidence: %f)\n",
//                        queryResult.getIntent().getDisplayName(), queryResult.getIntentDetectionConfidence());
//                System.out.format("Fulfillment Text: '%s'\n", queryResult.getFulfillmentText());
//            }
//        }
//    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

//    public String getUrlNews(){
//        String url = "";
//        if (state.equals("command-news")) {
//            url = "rss/src/breakingnews.xml";
//        } else if (state.equals("command-news-politics")) {
//            url = "rss/src/politics.xml";
//        } else if (state.equals("command-news-entertainment")) {
//            url = "rss/src/entertainment.xml";
//        } else if (state.equals("command-news-world")) {
//            url = "rss/src/world.xml";
//        } else if (state.equals("command-news-lifestyle")) {
//            url = "rss/src/lifestyle.xml";
//        } else if (state.equals("command-news-economy")) {
//            url = "rss/src/economy.xml";
//        } else if (state.equals("command-news-money")) {
//            url = "rss/src/money.xml";
//        } else if (state.equals("command-news-market")) {
//            url = "rss/src/market.xml";
//        } else if (state.equals("command-news-it")) {
//            url = "rss/src/it.xml";
//        }
//        return url;
//    }

//    public void checkNEWS(){
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://www.posttoday.com/")
//                .addConverterFactory(RssConverterFactory.create())
//                .build();
//
//
//        APIService service = retrofit.create(APIService.class);
//        Log.d("NEWS RSS", "+++ " + state);
//        Call<RssFeed> call = service.getRss(getUrlNews());
//        call.enqueue(new Callback<RssFeed>() {
//                    @Override
//                    public void onResponse(Call<RssFeed> call, Response<RssFeed> response) {
//                        // Populate list with response.body().getItems()
//                        RssFeed result = response.body();
//                        Log.d("Response RSS", "+++ " + result.getItems().toString());
//                        String text = "";
//                        for (int i = 0; i< 3; i++) {
//                            text += "ข่าวที่ " + (i+1) + ". " + result.getItems().get(i).getTitle() + "\n";
//                            text += response.body().getItems().get(i).getDescription() + "\n\n";
//                        }
//                        returnedText.setText(text);
//                        TextSpeechAPI.getInstance(getApplication())
//                                .speak(text);
//
//                    }
//
//                    @Override
//                    public void onFailure(Call<RssFeed> call, Throwable t) {
//                        // Show failure message
//                        Log.d("Failure RSS", "+++ " + t.getMessage());
//                        returnedText.setText(t.getMessage());
//                    }
//                });
//
//    }
}
