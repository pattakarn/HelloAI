package com.example.pattakak.aunjaiai;

import com.example.pattakak.aunjaiai.RSSPackage.RssFeed;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by pattakak on 20/02/2561.
 */

public interface APIService {

    //    @POST("/key/{key}/one/{one}")
//    Call<TestJSON> loadRepo(@Query("key") String key, @Query("one") String one);
//    @POST("key/{key}/one/{one}")
//    Call<TestJSON> loadRepo(@Path("key") String key, @Path("one") String one);

    @GET("D2?value=1")
    Call<ResponseBody> openLight();

    @GET("D2?value=0")
    Call<ResponseBody> closeLight();

    @GET("D2")
    Call<String[]> statusLight();

    @GET("D4?value=1")
    Call<ResponseBody> openLightD4();

    @GET("D4?value=0")
    Call<ResponseBody> closeLightD4();

    @GET("D4")
    Call<String[]> statusLightD4();

    @GET
    Call<RssFeed> getRss(@Url String url);

    @GET("projects/aunjai-89814/agent")
    Call<String> getDialog();

    @GET("projects/mr-mit-12fe7/agent/sessions/8cf38648-005c-4cfb-bb59-8d15e4fdf30d:detectIntent")
    Call<String> getDialog2();

}
