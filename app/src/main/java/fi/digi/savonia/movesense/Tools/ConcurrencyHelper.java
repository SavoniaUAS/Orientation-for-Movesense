package fi.digi.savonia.movesense.Tools;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Aputyökalu rinnakkaissuorittamiseen Android-laitteilla
 */
public class ConcurrencyHelper {

    private HandlerThread networkThread = new HandlerThread("networkThread");
    private HandlerThread dataThread = new HandlerThread("dataThread");
    private HandlerThread imuThread = new HandlerThread("imuThread");
    private HandlerThread kalmanThread = new HandlerThread("kalmanThread");
    private Handler networkHandler;
    private Handler dataHandler;
    private Handler imuHandler;
    private Handler kalmanHandler;

    public enum ThreadType
    {
        Network,
        Data,
        Imu,
        Kalman
    }

    private static volatile ConcurrencyHelper instance;

    /**
     * Hae olemassa oleva instassi tai luo sellainen ensimmäisellä kerralla
     * @return Rinnakkaussuorittamisen luokan
     */
    public static ConcurrencyHelper GetInstance()
    {
        if(instance == null)
        {
            synchronized (ConcurrencyHelper.class){
                if(instance==null){
                    instance = new ConcurrencyHelper();
                }
            }
        }
        return instance;
    }

    /**
     * Alustaa luokan valmiiksi käyttöä varten
     */
    private ConcurrencyHelper()
    {
        networkThread.start();
        networkHandler = new Handler(networkThread.getLooper());
        dataThread.start();
        dataHandler = new Handler(dataThread.getLooper());
        imuThread.start();
        imuHandler = new Handler(imuThread.getLooper());
        kalmanThread.start();
        kalmanHandler = new Handler(kalmanThread.getLooper());

    }

    /**
     * Suorittaa Runnable toiminnon säie tyypissä.
     * @param runnable Suoritettava toiminto
     * @param threadType Toiminnnon tyyppi
     */
    public void Run(Runnable runnable, ThreadType threadType)
    {
        switch (threadType)
        {
            case Network:
                networkHandler.post(runnable);
                break;
            case Data:
                dataHandler.post(runnable);
                break;
            case Imu:
                imuHandler.post(runnable);
                break;
            case Kalman:
                kalmanHandler.post(runnable);
                break;
        }
    }

}
