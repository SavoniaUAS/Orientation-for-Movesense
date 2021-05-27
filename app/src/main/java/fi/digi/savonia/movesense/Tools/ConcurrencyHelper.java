package fi.digi.savonia.movesense.Tools;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Android-laitteille helppokäyttöinen wrapper samanaikaisien tehtävien suorittamiseen.
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
     * Luokan instassin palautus tai sen luonti ensimmäisellä kerralla
     * @return luokan staattinen instanssi
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
     * Rinnakkaissuorittamisen säikeiden valmistelu käyttövalmiiksi.
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
     * Suorittaa Runnable tehtävän valitussa säie tyypissä.
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
