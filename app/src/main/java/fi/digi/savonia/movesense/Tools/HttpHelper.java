package fi.digi.savonia.movesense.Tools;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import fi.digi.savonia.movesense.Tools.Listeners.HttpActionListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Aputyökalu Http-pyynnön lähettämiseen
 */
public class HttpHelper {

    private OkHttpClient client;

    private HttpActionListener mHttpActionListener;

    private static volatile HttpHelper instance;

    public static HttpHelper GetInstance()
    {
        if(instance == null)
        {
            synchronized (HttpHelper.class){
                if(instance==null){
                    instance = new HttpHelper();
                }
            }
        }
        return instance;
    }

    private HttpHelper()
    {
         client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
    }

    public void SetListener(HttpActionListener httpActionListener)
    {
        mHttpActionListener = httpActionListener;
    }

    public void EnqueuePostRequest(String url, RequestBody body, int id)
    {
        Request request = new Request.Builder().url(url).post(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                mHttpActionListener.onError(e,id);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                mHttpActionListener.onResult(response.body().string(),id);
            }
        });
    }

}
