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
     * Tarkan paikannuksen luvan kyselyn ID
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
     * Sensorifuusio analysointi ja vertailu
     */
    ImuHelper imuHelper;
    /**
     * Yhteyden muodostamisen status käyttöliittymä objekti
     */
    ProgressDialog progressDialog;
    /**
     * Bluetooth-toiminnon aktivointi kyselyn ID
     */
    final int REQUEST_ENABLE_BT = 876;
    /**
     * Tilamuuttuja: Onko skannaus mahdollinen
     */
    boolean canScan = false;
    /**
     * Tilamuuttuja Nykyinen näytettävä Fragment-sivu
     */
    Page currentPage = Page.scan;
    /**
     * Tuettuja arvoja on 13,26,52,104.
     * SampleRateIndex     0  1  2   3
     */
    int dataRate = 52;


    /**
     * Sivun tunniste
     */
    public enum Page
    {
        /**
         * Movesense-sensori haku
         */
        scan,
        /**
         * Tärinän ja Kulmaeron näyttäminen. Sovelluksen pääsivu
         */
        proto
    }

    /**
     * Luodaan näkymä ja pakotetaan sovellus toimimaan pystytilassa.
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
     * Käynnistää Proton datan keruun. Pyytää sensorilta IMU9 parametrin infon.
     */
    private void StartSensorReading()
    {
        movesenseHelper.GetInfo(MovesenseHelper.Sensor.IMU9);
    }

    /**
     * Asettaa Fragmentin näkyville
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
     * Yhdistää Movesense-sensoriin ja lopettaa Bluetooth LE - haun
     * @param device
     */
    private void ConnectToMovesense(RxBleDevice device)
    {
        bluetoothHelper.StopScan();
        movesenseHelper.Connect(device.getMacAddress());
        CreateLoadingDialog(getString(R.string.loading_title),getString(R.string.connecting_message));
    }

    /**
     * Avaa odotattamis ilmoituksen ruudulle
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
     * Kysyy käyttäjältä oikeutta käyttää Tarkkaa paikannusta. GPS-yhteyttä ei käytetä. Bluetooth LE - haku vaatii tätä.
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
     * Paluu näppäintä on painettu
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
     *Paikannus oikeuden kyselyn tulos käyttäjältä
     * @param requestCode Pyynnön tunniste
     * @param permissions Pyydetty oikeus
     * @param grantResults Pyynnön tulos
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
     * Pyytää käyttäjää hyväksymään Bluetooth-yhteyden käyttöönoton
     * @param requestCode Pyynnön tunniste
     * @param resultCode Pyynnön vastauksen koodi
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
     * Alustaa tarvittavat työkalut ja aloittaa Bluetooth haun.
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
     * Hakee Näytettävän fragmentin instanssin
     * @param page Sivun tunniste
     * @return Näytettävä fragmentti
     */
    private Fragment GetFragment(Page page)
    {
        return getSupportFragmentManager().findFragmentByTag(page.name());
    }

    // Bluetooth Action Listener

    /**
     * Movesense BLE-laite on löytynyt!
     * @param bleDevice Movesense-sensorin BLE-objekti
     */
    @Override
    public void BleDeviceFound(RxBleDevice bleDevice) {
        ScanFragment fragment = (ScanFragment) GetFragment(Page.scan);
        fragment.AddNewBleDevice(bleDevice);
    }

    /**
     * Valmiina Bluetooth LE-hakuun
     */
    @Override
    public void ReadyToScan() {
        bluetoothHelper.Scan(30000);
        canScan = true;
    }

    /**
     * Bluetooth-yhteys ei ole aktivoitu. Pyytää käyttäjää aktivoimaan yhteyden.
     */
    @Override
    public void BluetoothNotEnabled() {

        Intent intentEnableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intentEnableBluetooth,REQUEST_ENABLE_BT);
    }

    /**
     * Paikannus oikeutta ei ole annettu! Bluetooth haku ei toimi!!!
     */
    @Override
    public void LocationPermissionNotGranted() {
        Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
    }

    /**
     * Paikannus ei ole aktivoitu!
     */
    @Override
    public void LocationNotEnabled() {
        Toast.makeText(this, "Location not enabled", Toast.LENGTH_SHORT).show();
    }

    /**
     * Bluetooth-yhteyttä ei ole saatavilla laitteessa.
     */
    @Override
    public void BluetoothNotAvailable() {
        Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_SHORT).show();
    }

    /**
     * Jokin virhe on tapahtunut!
     * @param explanation Virheen instanssi
     */
    @Override
    public void Error(String explanation) {
        Toast.makeText(this, explanation, Toast.LENGTH_SHORT).show();
    }

    //ScanFragment Interaction Listener

    /**
     * Skannaus aktivoitu
     */
    @Override
    public void onScanButtonPressed() {
        if(canScan)
        {
            bluetoothHelper.StopScan();
            ScanFragment scanFragment = (ScanFragment) GetFragment(Page.scan);
            scanFragment.ClearList();
            bluetoothHelper.Scan(30000);
        }
    }

    /**
     * Movesense-sensori on valittu. Valittuun laitteeseen yhdistetään.
     * @param bleDevice Valitun Movesense-sensorin BLE-objekti
     */
    @Override
    public void onDeviceSelected(RxBleDevice bleDevice) {

        ConnectToMovesense(bleDevice);

    }

    //Movesense Action Listener

    /**
     * Movesense-sensori yhdistämisen tulos.
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
     * Yhteys Movesense-sensoriin on katkaistu
     * @param reason Syy tekstinä
     */
    @Override
    public void OnDisconnect(String reason) {
        Toast.makeText(this,R.string.notification_movesense_disconnect, Toast.LENGTH_SHORT).show();

    }

    /**
     * Virhe yhteydessä Movesense-sensoriin.
     * @param mdsException Syy
     */
    @Override
    public void OnError(MdsException mdsException) {

        Toast.makeText(this, R.string.notification_movesense_error, Toast.LENGTH_SHORT).show();

    }

    /**
     * Virhe yhteydessä Movesense-sensoriin.
     * @param reason Syy
     */
    @Override
    public void OnError(String reason) {
        Toast.makeText(this, R.string.notification_movesense_error, Toast.LENGTH_SHORT).show();

    }

    /**
     * Movesense-sensorilta on vastaanotettu dataa!
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
     * Parametrin info on vastaanotettu.
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
     * Proton toiminto päälle!
     */
    @Override
    public void onReadyProto() {
        StartSensorReading();
    }

    /**
     * Proton näkymän päivitys aktiiviseksi
     */
    @Override
    public void onStartProto() {
        imuHelper.StartUpdates(10);
    }

    /**
     * Proton näkymän päivitysten deaktivointi
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
     * Kalibroidaan kulmanmuutos ja asetetaan kulman vertailupiste.
     */
    @Override
    public void onCalibrationProto() {
        imuHelper.Calibrate();
    }

    /**
     * Reset toiminto aktivoitu Proto näkymästä
     */
    @Override
    public void onResetProto() {

    }

    // Imu Action Listener

    /**
     * Kalibrointi valmis!
     */
    @Override
    public void CalibrationReady() {

    }

    /**
     * Päivittää muutoksen käyttöliittymään
     * @param Angle Kulmaero
     * @param vibration Tärinä
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
