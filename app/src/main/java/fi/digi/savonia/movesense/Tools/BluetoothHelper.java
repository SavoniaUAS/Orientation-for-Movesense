package fi.digi.savonia.movesense.Tools;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.ArrayList;
import java.util.List;

import fi.digi.savonia.movesense.Tools.Listeners.BluetoothActionListener;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Helppokäyttöinen wrapper Bluetooth LE - hakuun.
 */
public class BluetoothHelper {

    /**
     * Bluetooth LE-client
     */
    RxBleClient rxBleClient;
    /**
     * Bluetooth LE-haun instanssi
     */
    Disposable scanSubscription;
    /**
     * Sovelluksen konteksti
     */
    Context _context;
    /**
     * Tapahtumien kuuntelija
     */
    BluetoothActionListener _bluetoothActionListener;

    List<String> FoundDevices = new ArrayList<String>();

    /**
     * Bluetooth LE-clientin luominen ja määritys
     * @param context Sovelluksen konteksti
     */
    public BluetoothHelper(Context context)
    {
        _context = context;
        rxBleClient = RxBleClient.create(context);
    }

    /**
     * Kuuntelijan asetus luokan tapahtumille.
     * @param bluetoothActionListener
     */
    public void SetBluetoothActionListener(BluetoothActionListener bluetoothActionListener)
    {
        _bluetoothActionListener = bluetoothActionListener;
    }

    /**
     * Bluetooth LE-yhteyden vaatimusten tarkistus
     */
    public void CheckRequirements()
    {
        switch (rxBleClient.getState()) {

            case READY:
                _bluetoothActionListener.ReadyToScan();
                break;
            case BLUETOOTH_NOT_AVAILABLE:
                _bluetoothActionListener.BluetoothNotAvailable();
                break;
                // basically no functionality will work here
            case LOCATION_PERMISSION_NOT_GRANTED:
                _bluetoothActionListener.LocationPermissionNotGranted();
                break;
                // scanning and connecting will not work
            case BLUETOOTH_NOT_ENABLED:
                _bluetoothActionListener.BluetoothNotEnabled();
                break;
                // scanning and connecting will not work
            case LOCATION_SERVICES_NOT_ENABLED:
                _bluetoothActionListener.LocationNotEnabled();
                break;
                // scanning will not work
            default:
                _bluetoothActionListener.Error("Unknown error");
                break;
        }
    }

    /**
     * Tarkistaa Bluetooth LE-haun aktiivisuuden
     * @return Onko Skannaus käynnissä?
     */
    public boolean IsScanning()
    {
        if(scanSubscription != null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Bluetooth LE-haun keskeytys.
     */
    private void dispose() {
        scanSubscription.dispose();
        Log.i("Scan","Scan stop");
    }


    /**
     * Bluetooth LE-haussa on löytynyt laite. Tarkastaa onko laite Movesense-sensori.
     * @param scanResult
     */
    private void onScanResult(ScanResult scanResult)
    {
        String name = scanResult.getBleDevice().getName();
        if(name!=null && name.startsWith("Movesense"))
        {
            boolean found = false;
            if(FoundDevices.size()!=0)
            {
                if(!FoundDevices.contains(name))
                {
                    FoundDevices.add(name);
                    found = true;
                }
            }
            else
            {
                FoundDevices.add(name);
                found=true;
            }

            if(found)
            {
                Log.i("Movesense found",name);
                _bluetoothActionListener.BleDeviceFound(scanResult.getBleDevice());
            }

        }
    }


    /**
     * Bluetooth LE-haun aikana on tapahtunut virhe!
     * @param throwable Virhe
     */
    private void onScanFailure(Throwable throwable) {
        Log.i("Scan","Scan failure : " + throwable.getLocalizedMessage());
        _bluetoothActionListener.Error(throwable.getMessage());
    }

    /**
     * Bluetooth LE-haun käynnistys.
     * @param timeout Skannauksen kesto sekuntia
     */
    public void Scan(long timeout)
    {
        FoundDevices.clear();
        ScanSettings scanSettings = new ScanSettings.Builder().build();
        ScanFilter scanFilter = new ScanFilter.Builder().build();


        scanSubscription = rxBleClient.scanBleDevices(scanSettings,scanFilter)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::dispose)
                .subscribe(this::onScanResult,this::onScanFailure);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                scanSubscription.dispose();
            }
        },timeout);
    }

    /**
     * Bluetooth LE - haun pysäyttäminen. Tarkastaa onko haku käynnissä.
     */
    public void StopScan()
    {
        if(IsScanning())
        {
            dispose();
        }
    }


}
