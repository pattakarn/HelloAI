package com.example.pattakak.aunjaiai;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;

public class DialogFlowActivity extends AppCompatActivity implements AIListener {

    private static final String TAG = "aiDataService";
    private Button aiButton;
    private FloatingActionButton fab;
    private TextView tv;
    private AIService aiService;

    private Boolean control = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_flow);

        //        aiButton = (Button) findViewById(R.id.btn);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        tv = (TextView) findViewById(R.id.tv);

        final AIConfiguration config = new AIConfiguration("decb05094e7f4dbf9be0b79ccf23cb68",
                AIConfiguration.SupportedLanguages.fromLanguageTag("TH"),
                AIConfiguration.RecognitionEngine.System);
        final LayoutInflater inflater =(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        final AIDataService aiDataService = new AIDataService(config);

        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery("เปิดไฟห้องนอน");

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

                    Result result = response.getResult();
                    Log.i(TAG, "Resolved query: " + result.getResolvedQuery());
                    Log.i(TAG, "Action: " + result.getAction());

                    String speech = result.getFulfillment().getSpeech();
                    Log.i(TAG, "Speech: " + speech);

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

                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }
            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                if (aiResponse != null) {
                    // process aiResponse here



                }

            }
        }.execute(aiRequest);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (control) {
                    listenButtonOnClick(null);
                    control = !control;
                    fab.setImageResource(R.drawable.ic_stop_white);
                    fab.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                } else {
                    aiService.stopListening();
                    control = !control;
                    fab.setImageResource(R.drawable.ic_mic_white);
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                }
            }
        });
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                listenButtonOnClick(v);
//                AIDialog aiDialog = new AIDialog(inflater.getContext(), config);
//                aiDialog.setResultsListener(new AIDialog.AIDialogListener() {
//                    @Override
//                    public void onResult(AIResponse result) {
//                        Toast.makeText(inflater.getContext(), result.getResult().toString(), Toast.LENGTH_LONG).show();
//                        Log.d("result", result.getResult().getAction());
//                        Log.d("result", result.getResult().getResolvedQuery());
//                        tv.setText("Query:" + result.getResult().getResolvedQuery() +
//                                "\nAction: " + result.getResult().getAction());
//                    }
//
//                    @Override
//                    public void onError(AIError error) {
//                        Toast.makeText(inflater.getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
//                    }
//
//                    @Override
//                    public void onCancelled() {
//
//                    }
//                });
//                aiDialog.showAndListen();
//            }
//        });
    }

    @Override
    public void onError(AIError error) {
        Log.d("DialogFlow", "onError");
        tv.setText(error.toString());

        if (!control) {
            new CountDownTimer(1000, 1000) {

                public void onTick(long millisUntilFinished) {
//                mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                }

                public void onFinish() {
//                mTextField.setText("done!");
                    listenButtonOnClick(null);
                }
            }.start();
        }

    }

    @Override
    public void onAudioLevel(float level) {
        Log.d("DialogFlow", "onAudioLevel");
    }

    @Override
    public void onListeningStarted() {
        Log.d("DialogFlow", "onListeningStarted");
    }

    @Override
    public void onListeningCanceled() {
        Log.d("DialogFlow", "onListeningCanceled");
    }

    @Override
    public void onListeningFinished() {
        Log.d("DialogFlow", "onListeningFinished");
        listenButtonOnClick(null);
    }

    public void listenButtonOnClick(final View view) {
        aiService.startListening();
    }

    @Override
    public void onResult(final AIResponse response) {
        Log.d("DialogFlow", "onResult");
        Result result = response.getResult();

        // Get parameters
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }

        tv.setText("Query:" + result.getResolvedQuery() +
                "\nAction: " + result.getAction() +
                "\nParameters: " + parameterString);
        listenButtonOnClick(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onStop", "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy", "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

}
