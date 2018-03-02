package com.example.pattakak.aunjaiai;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;


import com.example.pattakak.aunjaiai.RSSPackage.RssConverterFactory;
import com.example.pattakak.aunjaiai.RSSPackage.RssFeed;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by pattakak on 26/02/2561.
 */

public class AIAunJai {

    Context context;
    LayoutInflater inflater;
    TextSpeechAPI textSpeech;

    private String state = "standby";

    public AIAunJai(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        textSpeech = TextSpeechAPI.getInstance(context);
    }

    public Boolean checkAunjai(String text) {
        Boolean check = false;
        if (text.matches("(.*)อุ่นใจ(.*)")) {
            check = true;
            checkLight(text);
        }
        return check;
    }

    public void stopTTS(){
        textSpeech.clear();
    }

    public Boolean checkLight(String text) {
        if (text.matches("(.*)เปิดไฟ(.*)")) {
            if (text.matches("(.*)ห้องนอน(.*)")) {
                state = "command-open-bedroom";
            } else if (text.matches("(.*)ห้องนั่งเล่น(.*)")) {
                state = "command-open-livingroom";
            } else if (text.matches("(.*)หน้าบ้าน(.*)")) {
                state = "command-open-fronthome";
            } else {
                state = "command-open";
            }
        } else if (text.matches("(.*)ปิดไฟ(.*)")) {
            if (text.matches("(.*)ห้องนอน(.*)")) {
                state = "command-close-bedroom";
            } else if (text.matches("(.*)ห้องนั่งเล่น(.*)")) {
                state = "command-close-livingroom";
            } else if (text.matches("(.*)หน้าบ้าน(.*)")) {
                state = "command-close-fronthome";
            } else {
                state = "command-close";
            }
        } else if ((text.matches("(.*)อัพเดทข่าว(.*)") && text.matches("(.*)ให้หน่อย(.*)"))
                || text.matches("(.*)อ่านข่าว(.*)")) {
            if (text.matches("(.*)การเมือง(.*)")) {
                state = "command-news-politics";
            } else if (text.matches("(.*)บันเทิง(.*)")) {
                state = "command-news-entertainment";
            } else if (text.matches("(.*)รอบโลก(.*)")) {
                state = "command-news-world";
            } else if (text.matches("(.*)ไลฟ์สไตล์(.*)")) {
                state = "command-news-lifestyle";
            } else if (text.matches("(.*)เศรษฐกิจ(.*)")) {
                state = "command-news-economy";
            } else if (text.matches("(.*)การเงิน(.*)")) {
                state = "command-news-money";
            } else if (text.matches("(.*)การตลาด(.*)")) {
                state = "command-news-market";
            } else if (text.matches("(.*)ไอที(.*)")) {
                state = "command-news-it";
            } else {
                state = "command-news";
            }
        } else if (text.matches("(.*)เช็คไฟ(.*)") || text.matches("(.*)ดูไฟ(.*)")) {
            if (text.matches("(.*)ห้องนอน(.*)")) {
                state = "command-status-bedroom";
            } else if (text.matches("(.*)ห้องนั่งเล่น(.*)")) {
                state = "command-status-livingroom";
            } else if (text.matches("(.*)หน้าบ้าน(.*)")) {
                state = "command-status-fronthome";
            } else {
                state = "command-status";
            }
        }
        return true;
    }

    public void setLight(boolean active) {
        String url = "https://blynk-cloud.com/580a5279aaa14eb3af3d1acaf2d55851/update/";
        OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        APIService service = retrofit.create(APIService.class);
        Call<ResponseBody> call = null;
        if (active) {
            if (state.matches("(.*)livingroom(.*)")) {
                call = service.openLightD4();
            } else {
                call = service.openLight();
            }
        } else {
            if (state.matches("(.*)livingroom(.*)")) {
                call = service.closeLightD4();
            } else {
                call = service.closeLight();
            }
        }

        Log.d("setLight", "++++++++ " + call.request().toString());
        if (call != null) {
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d("Response", "==============");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d("failure", "============== " + t.getMessage());
                }
            });
        }
    }

    public void StatusLight() {
        String url = "https://blynk-cloud.com/580a5279aaa14eb3af3d1acaf2d55851/get/";
        OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        APIService service = retrofit.create(APIService.class);
        Call<String[]> call = null;
        if (state.matches("(.*)livingroom(.*)")) {
            call = service.statusLightD4();
        } else {
            call = service.statusLight();
        }
        Log.d("statusLight", "++++++++ " + call.request().toString());
        if (call != null) {
            call.enqueue(new Callback<String[]>() {
                @Override
                public void onResponse(Call<String[]> call, Response<String[]> response) {
                    String[] result = response.body();
                    Log.d("Response statusLight", "============== " + result[0]);
                    String text = "";
                    if (result[0].equals("1")) {
                        if (state.matches("(.*)bedroom(.*)")) {
                            text = "ตอนนี้ไฟห้องนอนเปิดอยู่คะ";
                        } else if (state.matches("(.*)livingroom(.*)")) {
                            text = "ตอนนี้ไฟห้องนั่งเล่นเปิดอยู่คะ";
                        } else if (state.matches("(.*)fronthome(.*)")) {
                            text = "ตอนนี้ไฟหน้าบ้านเปิดอยู่คะ";
                        }
                    } else {
                        if (state.matches("(.*)bedroom(.*)")) {
                            text = "ตอนนี้ไฟห้องนอนปิดอยู่คะ";
                        } else if (state.matches("(.*)livingroom(.*)")) {
                            text = "ตอนนี้ไฟห้องนั่งเล่นปิดอยู่คะ";
                        } else if (state.matches("(.*)fronthome(.*)")) {
                            text = "ตอนนี้ไฟหน้าบ้านปิดอยู่คะ";
                        }
                    }
                    textSpeech.speak(text);
                }

                @Override
                public void onFailure(Call<String[]> call, Throwable t) {
                    Log.d("failure statusLight", "============== " + t.getMessage());
                }
            });
        }
    }

    public String getUrlNews() {
        String url = "";
        if (state.equals("command-news")) {
            url = "rss/src/breakingnews.xml";
        } else if (state.equals("command-news-politics")) {
            url = "rss/src/politics.xml";
        } else if (state.equals("command-news-entertainment")) {
            url = "rss/src/entertainment.xml";
        } else if (state.equals("command-news-world")) {
            url = "rss/src/world.xml";
        } else if (state.equals("command-news-lifestyle")) {
            url = "rss/src/lifestyle.xml";
        } else if (state.equals("command-news-economy")) {
            url = "rss/src/economy.xml";
        } else if (state.equals("command-news-money")) {
            url = "rss/src/money.xml";
        } else if (state.equals("command-news-market")) {
            url = "rss/src/market.xml";
        } else if (state.equals("command-news-it")) {
            url = "rss/src/it.xml";
        }
        return url;
    }

    public void checkNEWS(String url) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.posttoday.com/")
                .addConverterFactory(RssConverterFactory.create())
                .build();


        APIService service = retrofit.create(APIService.class);
        Log.d("NEWS RSS", "+++ " + state);
        Call<RssFeed> call = service.getRss(url);
        call.enqueue(new Callback<RssFeed>() {
            @Override
            public void onResponse(Call<RssFeed> call, Response<RssFeed> response) {
                // Populate list with response.body().getItems()
                RssFeed result = response.body();
//                Log.d("Response RSS", "+++ " + result.getItems().toString());
                String text = "";
                for (int i = 0; i < 3; i++) {
                    text += "ข่าวที่ " + (i + 1) + ". " + result.getItems().get(i).getTitle() + "\n";
                    text += response.body().getItems().get(i).getDescription() + "\n\n";
                }
//                returnedText.setText(text);
                textSpeech.speak(text);

            }

            @Override
            public void onFailure(Call<RssFeed> call, Throwable t) {
                // Show failure message
                Log.d("Failure RSS", "+++ " + t.getMessage());
//                returnedText.setText(t.getMessage());

            }
        });

    }

    public void checkShow() {
        if (state.equals("standby")) {
            textSpeech.speak("มีอะไรให้อุ่นใจช่วยคะ");
            state = "command";
        } else if (state.equals("command-open")) {
            setLight(true);
            textSpeech.speak("เปิดไฟเรียบร้อยคะ");
            state = "standby";
        } else if (state.equals("command-close")) {
            setLight(false);
            textSpeech.speak("ปิดไฟเรียบร้อยคะ");
            state = "standby";
        } else if (state.equals("command-open-bedroom")) {
            setLight(true);
            textSpeech.speak("เปิดไฟห้องนอนเรียบร้อยคะ");
            state = "standby";
        } else if (state.equals("command-close-bedroom")) {
            setLight(false);
            textSpeech.speak("ปิดไฟห้องนอนเรียบร้อยคะ");
            state = "standby";
        } else if (state.equals("command-open-livingroom")) {
            setLight(true);
            textSpeech.speak("เปิดไฟห้องนั่งเล่นเรียบร้อยคะ");
            state = "standby";
        } else if (state.equals("command-close-livingroom")) {
            setLight(false);
            textSpeech.speak("ปิดไฟห้องนั่งเล่นเรียบร้อยคะ");
            state = "standby";
        } else if (state.equals("command-status-fronthome")) {
            setLight(true);
            textSpeech.speak("ไฟหน้าบ้านเปิดอยู่คะ");
            state = "standby";
        } else if (state.equals("command-status-fronthome")) {
            setLight(false);
            textSpeech.speak("ไฟหน้าบ้านปิดอยู่คะ");
            state = "standby";
        } else if (state.matches("(.*)command-status(.*)")) {
            StatusLight();
            state = "standby";
        } else if (state.matches("(.*)command-news(.*)")) {
//            checkNEWS();
            state = "standby";
//            timer = 60000;
        } else if (state.equals("command")) {
            textSpeech.speak("แล้วเจอกันใหม่นะคะ");
            state = "standby";
        }
    }

    public String AICheckLight(String intent, String speech) {
        if (intent.equals("turn_on.light")) {
            AISetLight(true, "frontside");
        } else if (intent.equals("turn_off.light")) {
            AISetLight(false, "frontside");
        } else if (intent.equals("turn_on.light.frontside")) {
            AISetLight(true, "frontside");
        } else if (intent.equals("turn_off.light.frontside")) {
            AISetLight(false, "frontside");
        } else if (intent.equals("turn_on.light.bedroom")) {
            AISetLight(true, "bedroom");
        } else if (intent.equals("turn_off.light.bedroom")) {
            AISetLight(false, "bedroom");
        } else if (intent.equals("turn_on.light.livingroom")) {
            AISetLight(true, "livingroom");
        } else if (intent.equals("turn_off.light.livingroom")) {
            AISetLight(false, "livingroom");
        } else if (intent.equals("check_status.light.frontside")) {
            AIStatusLight("frontside", speech);
            return "0";
        } else if (intent.equals("check_status.light.bedroom")) {
            AIStatusLight("bedroom", speech);
            return "0";
        } else if (intent.equals("check_status.light.livingroom")) {
            AIStatusLight("bedroom", speech);
            return "0";
        } else if (intent.equals("update_content.newsfeed.business")) {
            checkNEWS("rss/src/market.xml");
            return "60000";
        } else if (intent.equals("update_content.newsfeed.econimic")) {
            checkNEWS("rss/src/economy.xml");
            return "60000";
        } else if (intent.equals("update_content.newsfeed.entertainment")) {
            checkNEWS("rss/src/entertainment.xml");
            return "60000";
        } else if (intent.equals("update_content.newsfeed.finance")) {
            checkNEWS("rss/src/money.xml");
            return "60000";
        } else if (intent.equals("update_content.newsfeed.it")) {
            checkNEWS("rss/src/it.xml");
            return "60000";
        } else if (intent.equals("update_content.newsfeed.lifestyle")) {
            checkNEWS("rss/src/lifestyle.xml");
            return "60000";
        } else if (intent.equals("update_content.newsfeed.politics")) {
            checkNEWS("rss/src/politics.xml");
            return "60000";
        } else if (intent.equals("update_content.newsfeed.sport")) {
            checkNEWS("rss/src/sport.xml");
            return "60000";
        } else if (intent.equals("update_content.newsfeed.worldwide")) {
            checkNEWS("rss/src/world.xml");
            return "60000";
        }
        return "1";
    }

    public void AISetLight(boolean active, String room) {
        String url = "https://blynk-cloud.com/580a5279aaa14eb3af3d1acaf2d55851/update/";
        OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        APIService service = retrofit.create(APIService.class);
        Call<ResponseBody> call = null;
        if (active) {
            if (room.equals("bedroom")) {
                call = service.openLight();
            } else if (room.equals("frontside")) {
                call = service.openLight();
            } else if (room.equals("livingroom")) {
                call = service.openLightD4();
            } else {
                call = service.openLight();
            }
        } else {
            if (room.equals("bedroom")) {
                call = service.closeLight();
            } else if (room.equals("frontside")) {
                call = service.closeLight();
            } else if (room.equals("livingroom")) {
                call = service.closeLightD4();
            } else {
                call = service.openLight();
            }
        }

        Log.d("setLight", "++++++++ " + call.request().toString());
        if (call != null) {
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d("Response", "==============");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d("failure", "============== " + t.getMessage());
                }
            });
        }
    }

    public void AIStatusLight(String room, final String speech) {
        String url = "https://blynk-cloud.com/580a5279aaa14eb3af3d1acaf2d55851/get/";
        OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        APIService service = retrofit.create(APIService.class);
        Call<String[]> call = null;

        if (room.equals("bedroom")) {
            call = service.statusLight();
        } else if (room.equals("frontside")) {
            call = service.statusLight();
        } else if (room.equals("livingroom")) {
            call = service.statusLightD4();
        } else {
            call = service.statusLight();
        }

        Log.d("statusLight", "++++++++ " + room + " " + speech);
        Log.d("statusLight", "++++++++ " + call.request().toString());

        if (call != null) {
            call.enqueue(new Callback<String[]>() {
                @Override
                public void onResponse(Call<String[]> call, Response<String[]> response) {
                    String[] result = response.body();
                    Log.d("Response statusLight", "============== " + result[0]);
                    String text = "";
                    if (speech.matches("(.*)\\{(.*)\\}(.*)")) {
                        final Pattern pattern = Pattern.compile("\\{(.*)\\}");
                        final Matcher matcher = pattern.matcher(speech);
                        String word = "";
                        while (matcher.find()) {
                            Log.i("Full match: ", "=============================== " + matcher.group(0));
                            for (int i = 1; i <= matcher.groupCount(); i++) {
                                Log.i("Group ", "============================" + i + ": " + matcher.group(i));
                                word = matcher.group(0);
                            }
                        }

                        String newWord = "";
                        if (result[0].equals("1")) {
                            newWord = speech.replace(word, "เปิด");
                        } else {
                            newWord = speech.replace(word, "ปิด");
                        }
                        Log.i("newWord", "+++++++++++++++++++++++++++ " + newWord);
                        textSpeech.speak(newWord);
                    }
                }

                @Override
                public void onFailure(Call<String[]> call, Throwable t) {
                    Log.d("failure statusLight", "============== " + t.getMessage());
                }
            });
        }
    }




    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
