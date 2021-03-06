package fi.digi.savonia.movesense;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import fi.digi.savonia.movesense.Fragments.ProtoFragment;
import fi.digi.savonia.movesense.Fragments.ScanFragment;
import fi.digi.savonia.movesense.Models.Movesense.Data.Imu9Data;
import fi.digi.savonia.movesense.Models.Movesense.Info.ImuInfo;
import fi.digi.savonia.movesense.Tools.BluetoothHelper;
import fi.digi.savonia.movesense.Tools.ImuHelper;
import fi.digi.savonia.movesense.Tools.Listeners.BluetoothActionListener;
import fi.digi.savonia.movesense.Tools.Listeners.ImuActionListener;
import fi.digi.savonia.movesense.Tools.Listeners.MovesenseActionListener;
import fi.digi.savonia.movesense.Tools.MovesenseHelper;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.movesense.mds.MdsException;
import com.polidea.rxandroidble2.RxBleDevice;

public class MainActivity extends AppCompatActivity implements BluetoothActionListener, MovesenseActionListener, ScanFragment.OnFragmentInteractionListener, ProtoFragment.OnFragmentInteractionListener, ImuActionListener {

    /**
     * ID-tunniste tarkalle paikannukselle.
     */
    final int LOCATION_FINE_PERMISSIONS_REQUEST = 5;
    /**
     * Bluetooth LE-haku
     */
    BluetoothHelper bluetoothHelper;
    /**
     * Movesense-sensorin yhteyden hallinta
     */
    MovesenseHelper movesenseHelper;
    /**
     * Kiihtyvyys ja kulmamuutos parametrien fuusio asennoksi, analysointi ja vertailu
     */
    ImuHelper imuHelper;
    /**
     * Yhteyden muodostus info UI elementti
     */
    ProgressDialog progressDialog;
    /**
     * ID-tunniste Bluetooth-yhteyden aktivoinille
     */
    final int REQUEST_ENABLE_BT = 876;
    /**
     * Tilamuuttuja: Bluetooth LE-skannaus mahdollinen
     */
    boolean BleScanEnabled = false;
    /**
     * Tilamuuttuja: K??ytt??j??lle n??ytett??v?? Fraagment-n??kym??
     */
    Page currentPage = Page.scan;
    /**
     * Tuettuja arvoja on 13,26,52,104.
     * <P></P>
     * SampleRateIndex     0  1  2   3
     */
    int dataRate = 52;


    /**
     * Sivun tunniste
     */
    public enum Page
    {
        /**
         * Etsii Movesense-sensorin Bluetooth LE-yhteydell??.
         */
        scan,
        /**
         * N??ytt???? t??rin??n ja kulmaeron k??ytt??j??lle.
         */
        proto
    }

    /**
     * Luo n??kym??n ja pakotettaa sovelluksen toimimaan pystytilassa.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        CheckPermissions();

    }

    /**
     * K??ynnist???? mittaus datan keruun Movesense-sensorilta.
     */
    private void StartSensorReading()
    {
        movesenseHelper.GetInfo(MovesenseHelper.Sensor.IMU9);
    }

    /**
     * Asettaa Fragmentin k??ytt??j??n n??kyville.
     * @param fragment Fragmentin instanssi
     * @param page Sivun tunniste
     */
    protected void setFragment(Fragment fragment, Page page) {
        currentPage = page;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment,page.name());
        fragmentTransaction.commit();
    }

    /**
     * Muodostaa yhteyden Movesense-sensoriin ja keskeytt???? Bluetooth LE - haun.
     * @param device
     */
    private void ConnectToMovesense(RxBleDevice device)
    {
        bluetoothHelper.StopScan();
        movesenseHelper.Connect(device.getMacAddress());
        CreateLoadingDialog(getString(R.string.loading_title),getString(R.string.connecting_message));
    }

    /**
     * Asettaa otsikon ja viestin Progress Dialog elementtiin. Lopuksi Progress Dialog on n??kyvill??.
     * @param title Otsikko
     * @param message Viesti
     */
    private void CreateLoadingDialog(String title, String message)
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    /**
     * Luvan kysyminen k??ytt??j??lt?? tarkkaan paikannukseen. ACCESS_FINE_LOCATION
     */
    private void CheckPermissions()
    {
        // Here, thisActivity is the current activity
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_FINE_PERMISSIONS_REQUEST);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Program();


        }
    }

    /**
     * Paluu n??pp??int?? on painettu
     */
    @Override
    public void onBackPressed() {

        if(GetFragment(Page.scan)!=null)
        {
            //setFragment(new ConfigurationFragment(),Page.configuration);
            finishAndRemoveTask();
        }
        else if(GetFragment(Page.proto)!=null)
        {
            movesenseHelper.Disconnect();
            setFragment(new ScanFragment(),Page.scan);
        }

    }

    /**
     *Paikannus oikeuden kyselyn tulos k??ytt??j??lt??
     * @param requestCode Pyynn??n tunniste
     * @param permissions Pyydetty lupa
     * @param grantResults Pyynn??n vastaus
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_FINE_PERMISSIONS_REQUEST)
        {
            if(grantResults[0] ==  PackageManager.PERMISSION_GRANTED)
            {
                Program();
            }
            else
            {
                Toast.makeText(this,getString(R.string.notification_location_is_required),Toast.LENGTH_LONG);
                //TODO something user interaction
            }
        }
    }

    /**
     * K??ytt??j??lt?? on pyydetty Bluetooth-yhteyden aktivointi.
     * @param requestCode Pyynn??n tunniste
     * @param resultCode Pyynn??n vastaus
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT)
        {
            if(resultCode == RESULT_OK)
            {
                bluetoothHelper.CheckRequirements();
            }
            else
            {
                Toast.makeText(this,getString(R.string.notification_bluetooth_must_be_on),Toast.LENGTH_LONG);
                //TODO something user interaction
            }
        }
    }

    /**
     * M????ritt???? aputy??kalut, asettaa haku-fragmentin n??kyville ja tarkistaa Bluetooth-yhteyden vaatimusten t??yttymisen.
     */
    private void Program()
    {
        bluetoothHelper = new BluetoothHelper(this);
        bluetoothHelper.SetBluetoothActionListener(this);

        movesenseHelper = new MovesenseHelper(this);
        movesenseHelper.SetMovesenseActionListener(this);

        imuHelper = new ImuHelper((float) dataRate);
        imuHelper.SetImuActionListener(this);

        setFragment(new ScanFragment(),Page.scan);

        bluetoothHelper.CheckRequirements();
    }

    /**
     * Etsii n??ytett??v??n fragment-n??kym??n instanssin.
     * @param page Sivun tunniste
     * @return N??ytett??v?? fragmentti. Null jos ei @page tyyppist?? sivua ei ole auki.
     */
    private Fragment GetFragment(Page page)
    {
        return getSupportFragmentManager().findFragmentByTag(page.name());
    }

    // Bluetooth Action Listener

    /**
     * Bluetooth LE-haku on l??yt??nyt Movesense BLE-laitteen.
     * @param bleDevice Movesense-sensorin BLE-objekti
     */
    @Override
    public void BleDeviceFound(RxBleDevice bleDevice) {
        ScanFragment fragment = (ScanFragment) GetFragment(Page.scan);
        fragment.AddNewBleDevice(bleDevice);
    }

    /**
     * Valmis Bluetooth LE-hakuun
     */
    @Override
    public void ReadyToScan() {
        bluetoothHelper.Scan(30000);
        BleScanEnabled = true;
    }

    /**
     * Bluetooth-yhteys ei ole aktivoitu. k??ytt??j???? on pyydetty aktivoimaan yhteyden.
     */
    @Override
    public void BluetoothNotEnabled() {

        Intent intentEnableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intentEnableBluetooth,REQUEST_ENABLE_BT);
    }

    /**
     * Paikannus oikeutta ei ole my??nnetty! Bluetooth haku ei toimi.
     */
    @Override
    public void LocationPermissionNotGranted() {
        Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
    }

    /**
     * Paikannus-toiminto ei ole aktivoitu.
     */
    @Override
    public void LocationNotEnabled() {
        Toast.makeText(this, "Location not enabled", Toast.LENGTH_SHORT).show();
    }

    /**
     * Bluetooth-yhteys ei ole saatavilla laitteessa.
     */
    @Override
    public void BluetoothNotAvailable() {
        Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_SHORT).show();
    }

    /**
     * Virhe on tapahtunut!
     * @param explanation Virheen instanssi
     */
    @Override
    public void Error(String explanation) {
        Toast.makeText(this, explanation, Toast.LENGTH_SHORT).show();
    }

    //ScanFragment Interaction Listener

    /**
     * Bluetooth LE- haku on aktivoitu
     */
    @Override
    public void onScanButtonPressed() {
        if(BleScanEnabled)
        {
            bluetoothHelper.StopScan();
            ScanFragment scanFragment = (ScanFragment) GetFragment(Page.scan);
            scanFragment.ClearList();
            bluetoothHelper.Scan(30000);
        }
    }

    /**
     * Movesense-sensori on valittu. Valittuun laitteeseen muodostetaan yhdistet????n.
     * @param bleDevice Valitun Movesense-sensorin BLE-objekti
     */
    @Override
    public void onDeviceSelected(RxBleDevice bleDevice) {

        ConnectToMovesense(bleDevice);

    }

    //Movesense Action Listener

    /**
     * Yhteyden muodostus Movesense-sensoriin on palauttanut vastauksen.
     * @param success Yhteys muodostettu True/False
     */
    @Override
    public void ConnectionResult(boolean success) {
        String sConnectResult;
        progressDialog.dismiss();

        if(success)
        {
            sConnectResult = "Connected to the device successfully!";
        }
        else
        {
            sConnectResult = "Failed connecting to the device!";
        }

        Toast.makeText(MainActivity.this, sConnectResult, Toast.LENGTH_SHORT).show();
        setFragment(new ProtoFragment(),Page.proto);

    }

    /**
     * Yhteys Movesense-sensoriin on katkaistu.
     * @param reason Syy tekstin??
     */
    @Override
    public void OnDisconnect(String reason) {
        Toast.makeText(this,R.string.notification_movesense_disconnect, Toast.LENGTH_SHORT).show();

    }

    /**
     * Virhe yhteydess?? Movesense-sensoriin.
     * @param mdsException Syy
     */
    @Override
    public void OnError(MdsException mdsException) {

        Toast.makeText(this, R.string.notification_movesense_error, Toast.LENGTH_SHORT).show();

    }

    /**
     * Virhe yhteydess?? Movesense-sensoriin.
     * @param reason Syy
     */
    @Override
    public void OnError(String reason) {
        Toast.makeText(this, R.string.notification_movesense_error, Toast.LENGTH_SHORT).show();

    }

    /**
     * Movesense-sensori on l??hett??nyt mittaus dataa.
     * @param Data Data
     * @param sensor Parametri
     */
    @Override
    public void OnDataReceived(Object Data, MovesenseHelper.Sensor sensor) {

        switch (sensor)
        {
            case Temperature:
                break;
            case BatteryVoltage:
                break;
            case LinearAcceleration:
                break;
            case Gyroscope:
                break;
            case Magnetometer:
                break;
            case ECG:
                break;
            case IMU6:
                break;
            case IMU6m:
                break;
            case IMU9:
                imuHelper.Update((Imu9Data) Data);
                break;
            case HeartRate:
                break;
        }

        //measurementHelper.AddMeasurement(Data,sensor);
    }

    /**
     * Movesense-sensorin parametri info on vastaanotettu.
     * @param Data Infon data
     * @param sensor Sensori
     */
    @Override
    public void OnInfoReceived(Object Data, MovesenseHelper.Sensor sensor) {

        if(sensor == MovesenseHelper.Sensor.IMU9)
        {
            movesenseHelper.SetConfigAndSubscribe((ImuInfo) Data,2,1,0,1);
        }

        Log.i("Info_debug received", sensor.name());
    }

    // Proto Fragment

    /**
     * Proto fragment-n??kym?? on valmis k??ytt????n. K??ynnist???? mittausdatan Movesense-sensorilta.
     */
    @Override
    public void onReadyProto() {
        StartSensorReading();
    }

    /**
     * Proto fragment-n??kym??ss?? on painettu Aloitettu nappia
     */
    @Override
    public void onStartProto() {
        imuHelper.StartUpdates(10);
    }

    /**
     * Proto fragment-n??kym??ss?? on painettu lopeta nappia
     */
    @Override
    public void onStopProto() {
        imuHelper.StopUpdates();
    }

    /**
     * Liukuvakeskiarvo on aktivoitu tai deaktivoitu.
     * @param enabled
     */
    @Override
    public void onSlidingAverage(boolean enabled) {
        imuHelper.UseSlidingAverage(enabled);
    }

    /**
     * Proto fragment-n??kym??ss?? on painettu Kalibroi nappia
     */
    @Override
    public void onCalibrationProto() {
        imuHelper.Calibrate();
    }

    /**
     * Proto fragment-n??kym??ss?? on painettu kalibroi nappia
     */
    @Override
    public void onResetProto() {

    }

    // Imu Action Listener

    /**
     * Kalibrointi on valmis!
     */
    @Override
    public void CalibrationReady() {

    }

    /**
     * Kulmaero ja t??rin?? on analysoitu.
     * @param Angle Kulmaero
     * @param vibration T??rin??
     */
    @Override
    public void OnUpdate(float Angle, float vibration) {
        runOnUiThread(() -> {

            ProtoFragment protoFragment = (ProtoFragment) GetFragment(Page.proto);
            protoFragment.SetPenAngle(Angle);
            protoFragment.SetVibrationIntensity(vibration);
        });
    }

}
