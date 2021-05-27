package fi.digi.savonia.movesense.Tools;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsConnectionListener;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsHeader;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.MdsSubscription;

import java.util.Timer;
import java.util.TimerTask;

import fi.digi.savonia.movesense.Models.MeasurementInterval;
import fi.digi.savonia.movesense.Models.Movesense.Config.GyroscopeConfig;
import fi.digi.savonia.movesense.Models.Movesense.Config.LinearAccelerationConfig;
import fi.digi.savonia.movesense.Models.Movesense.Config.MagnetometerConfig;
import fi.digi.savonia.movesense.Models.Movesense.Data.BatteryVoltageData;
import fi.digi.savonia.movesense.Models.Movesense.Data.ECGData;
import fi.digi.savonia.movesense.Models.Movesense.Data.GyroscopeData;
import fi.digi.savonia.movesense.Models.Movesense.Data.HeartrateData;
import fi.digi.savonia.movesense.Models.Movesense.Data.Imu6Data;
import fi.digi.savonia.movesense.Models.Movesense.Data.Imu6mData;
import fi.digi.savonia.movesense.Models.Movesense.Data.Imu9Data;
import fi.digi.savonia.movesense.Models.Movesense.Data.LinearAccelerationData;
import fi.digi.savonia.movesense.Models.Movesense.Data.MagnetometerData;
import fi.digi.savonia.movesense.Models.Movesense.Data.TemperatureData;
import fi.digi.savonia.movesense.Models.Movesense.Info.ECGInfo;
import fi.digi.savonia.movesense.Models.Movesense.Info.GyroscopeInfo;
import fi.digi.savonia.movesense.Models.Movesense.Info.HeartrateInfo;
import fi.digi.savonia.movesense.Models.Movesense.Info.ImuInfo;
import fi.digi.savonia.movesense.Models.Movesense.Info.LinearAccelerationInfo;
import fi.digi.savonia.movesense.Models.Movesense.Info.MagnetometerInfo;
import fi.digi.savonia.movesense.Models.Movesense.Info.TemperatureInfo;
import fi.digi.savonia.movesense.Tools.Listeners.MovesenseActionListener;

/**
 * Helppokäyttöinen wrapper Movesense-sensorin kommunikaatioon
 */
public class MovesenseHelper {

    /**
     * Movesense-sensorin kirjaston olio
     */
    private Mds mMovesense;
    /**
     * Movesense-sensori apukirjaston ilmoitusten kuuntelija
     */
    private MovesenseActionListener movesenseActionListener;
    /**
     * Google Gson Tekstin serialisointiin ja objektien deserialisointiin.
     */
    private Gson gson = new GsonBuilder().create();
    /**
     * Resurssi URI: Yhteyden muodostaneet laitteet
     */
    private final String URI_CONNECTEDDEVICES = "suunto://MDS/ConnectedDevices";
    /**
     * Resurssi URI: Tapahtuma kuultelija
     */
    private final String URI_EVENTLISTENER = "suunto://MDS/EventListener";
    /**
     * Resurssi URI: Resusrssi uri alkaa aina tällä
     */
    private final String SCHEME_PREFIX = "suunto://";
    /**
     * Info tyyppisen tiedon loppuosa
     */
    private final String INFO_ENDFIX = "/Info";
    /**
     * Konfiguraatio tyyppisen tiedon loppuosa
     */
    private final String CONFIG_ENDFIX = "/Config";
    /**
     * Resurssi URI: EKG
     */
    private final String URI_ECG = "/Meas/ECG";
    /**
     * Resurssi URI: Syke
     */
    private final String URI_HEARTRATE = "/Meas/Hr";
    /**
     * Resurssi URI: Lämpötila
     */
    private final String URI_TEMPERATURE = "/Meas/Temp";
    /**
     * Resurssi URI: Kiihtyvyys
     */
    private final String URI_LINEAR_ACCELERATION = "/Meas/Acc";
    /**
     * Resurssi URI: Kulmanmuutos
     */
    private final String URI_GYROSCOPE = "/Meas/Gyro";
    /**
     * Resurssi URI: Magnetometri
     */
    private final String URI_MAGNETOMETER = "/Meas/Magn";
    /**
     * Resurssi URI: Pariston varaus
     */
    private final String URI_BATTERY = "/System/Energy/Level";
    /**
     * Resurssi URI: Kiihtyvyys + Kulmanmuutos
     */
    private final String URI_IMU6 = "/Meas/IMU6";
    /**
     * Resurssi URI: Kiihtyvyys + Magnetometri
     */
    private final String URI_IMU6m = "/Meas/IMU6m";
    /**
     * Resurssi URI: Kiihtyvyys + kulmanmuutos + Magnetometri
     */
    private final String URI_IMU9 = "/Meas/IMU9";
    /**
     * Resurssi URI: Ineartial Measurement Unit Info
     */
    private final String URI_IMU_INFO = "/Meas/IMU";

    /**
     * Movesense-sensorin sarjanumero
     */
    private String deviceAddress;
    /**
     * Tila muuttuja: yhteys muodostettu Movesense-sensoriin
     */
    private boolean isConnected = false;
    /**
     * Lämpötila mittausten ajastin
     */
    private Timer temperatureMeasurementTimer;
    /**
     * Pariston varauksen lukemisen ajastin
     */
    private Timer batteryVoltageMeasurementTimer;
    /**
     * EKG-parametrin tilaus
     */
    private MdsSubscription ecgSubscription;
    /**
     * Syke-parametrin tilaus
     */
    private MdsSubscription hrSubscription;
    /**
     * Kiihtyvyys-parametrin tilaus
     */
    private MdsSubscription accSubscription;
    /**
     * kulmanmuutos-parametrin tilaus
     */
    private MdsSubscription gyroSubscription;
    /**
     * Kiihtyvyyden ja kulmanmuutoksen parametrien tilaus
     */
    private MdsSubscription imu6Subscription;
    /**
     * Kiihtyvyyden ja Magnetometrin parametrin tilaus
     */
    private MdsSubscription imu6mSubscription;
    /**
     * Kiihtyvyyden, Kulmanmuutoksen ja Magnetometrin parametrien tilaus.
     */
    private MdsSubscription imu9Subscription;
    /**
     * Magnetometrin parametrin tilaus
     */
    private MdsSubscription magnSubscription;

    /**
     * Lämpötilan oletus lukuintervalli (millisekuntia)
     */
    private long temperatureMeasurementInterval= 5000;
    /**
     * Paristonvarauksen oletus lukuintervalli (millisekuntia)
     */
    private long batteryVoltageMeasurementInterval = 5000;
    /**
     * Kiihtyvyyden lukuintervalli (näytettä sekunissa)
     */
    private String linearAccelerationDataRate;
    /**
     * Kulmamuutoksen lukuintervalli (näytettä sekunissa)
     */
    private String gyroscopeDataRate;
    /**
     * Magnetometrin lukuintervalli (näytettä sekunissa)
     */
    private String magnetometerDataRate;
    /**
     * EKG lukuintervalli (näytettä sekunissa)
     */
    private String ecgDataRate;
    /**
     * Inertial Measurement Unit lukuintervalli (näytettä sekunissa)
     */
    private String imuDataRate;
    /**
     * Sykkeen lukuintervalli (näytettä sekunissa)
     */
    private int HRDataRate;
    /**
     * Laskuri muuttuja Sykkeen lukemiseen
     */
    private int HRCounter = 0;
    /**
     * Laskuri muuttuja Inertial Measurement Unit konfiguraation aputoimintoon
     */
    private int counterImuConfigSuccess=0;

    /**
     * Movesense-sensorin parametrit
     */
    public enum Sensor
    {
        /**
         * Lämpötila
         */
        Temperature,
        /**
         * Pariston varaus
         */
        BatteryVoltage,
        /**
         * Kiihtyvyys
         */
        LinearAcceleration,
        /**
         * Kulmanmuutos
         */
        Gyroscope,
        /**
         * Magnetometri
         */
        Magnetometer,
        /**
         * EKG
         */
        ECG,
        /**
         * Kiihtyvyys + Kulmanmuutos
         */
        IMU6,
        /**
         * Kiihtyvyys + Magnetometri
         */
        IMU6m,
        /**
         * Kiihtyvyys + Kulmanmuutos + Magnetometri
         */
        IMU9,
        /**
         * Syke
         */
        HeartRate
    }

    /**
     * Movesense-sensorin sarjanumero
     */
    private String serial;

    /**
     * Ajastettu tehtävä lämpötilan mittaamiseen
     */
    private class TemperatureTask extends TimerTask
    {

        @Override
        public void run() {
            InternalGet(Sensor.Temperature);
        }
    }

    /**
     * Ajastettu tehtävä pariston varauksen lukemiseen
     */
    private class BatteryVoltageTask extends TimerTask
    {

        @Override
        public void run() {
            InternalGet(Sensor.BatteryVoltage);
        }
    }

    /**
     * Sisäinen metodi Ajastimien aktivointiin
     * @param sensor
     */
    private void InitTimer(Sensor sensor)
    {

        switch (sensor)
        {
            case Temperature:
                temperatureMeasurementTimer = new Timer();
                temperatureMeasurementTimer.schedule(new TemperatureTask(),temperatureMeasurementInterval,temperatureMeasurementInterval);
                break;
            case BatteryVoltage:
                batteryVoltageMeasurementTimer = new Timer();
                batteryVoltageMeasurementTimer.schedule(new BatteryVoltageTask(),batteryVoltageMeasurementInterval,batteryVoltageMeasurementInterval);
                break;
        }

    }

    /**
     * Kuuntelija yhteyden muodostuksen tapahtumien seurantaan.
     */
    private MdsConnectionListener mdsConnectionListener = new MdsConnectionListener() {
        @Override
        public void onConnect(String s) {

        }

        @Override
        public void onConnectionComplete(String s, String s1) {
            serial = s1;
            movesenseActionListener.ConnectionResult(true);
            isConnected=true;
        }

        @Override
        public void onError(MdsException e) {
            movesenseActionListener.OnError(e);
        }

        @Override
        public void onDisconnect(String s) {
            movesenseActionListener.OnDisconnect(s);
            isConnected=false;
        }
    };

    /**
     * lukuintervallin asetus parametrille
     * @param measurementInterval Lukuintervalli Luokkamuodossa.
     */
    public void SetDataRate(MeasurementInterval measurementInterval)
    {
        switch (measurementInterval.sensor)
        {
            case Temperature:
                temperatureMeasurementInterval = measurementInterval.GetSelectedValue() * 1000;
                break;
            case BatteryVoltage:
                batteryVoltageMeasurementInterval = measurementInterval.GetSelectedValue() * 1000 * 60;
                break;
            case LinearAcceleration:
                linearAccelerationDataRate = measurementInterval.GetSelectedValueString(false);
                break;
            case Gyroscope:
                gyroscopeDataRate = measurementInterval.GetSelectedValueString(false);
                break;
            case Magnetometer:
                magnetometerDataRate = measurementInterval.GetSelectedValueString(false);
                break;
            case ECG:
                ecgDataRate = measurementInterval.GetSelectedValueString(false);
                break;
            case IMU6:
            case IMU6m:
            case IMU9:
                imuDataRate = measurementInterval.GetSelectedValueString(false);
                break;
            case HeartRate:
                HRDataRate = measurementInterval.GetSelectedValue();
                break;
        }
    }

    /**
     * lukuintervallin asetus parametrille. Lukuintervallin täytyy olla tuettu. Tuetut lukuintervallin Parametrin Info-tiedolla.
     * @param sensor valittu parametri
     * @param rate lukuintervalli numerona
     */
    public void SetDataRate(Sensor sensor, int rate)
    {
        switch (sensor)
        {
            case Temperature:
                temperatureMeasurementInterval = rate * 1000;
                break;
            case BatteryVoltage:
                batteryVoltageMeasurementInterval = rate * 1000;
                break;
            case LinearAcceleration:
                linearAccelerationDataRate = String.valueOf(rate);
                break;
            case Gyroscope:
                gyroscopeDataRate = String.valueOf(rate);
                break;
            case Magnetometer:
                magnetometerDataRate = String.valueOf(rate);
                break;
            case ECG:
                ecgDataRate = String.valueOf(rate);
                break;
            case IMU6:
            case IMU6m:
            case IMU9:
                imuDataRate = String.valueOf(rate);
                break;
            case HeartRate:
                HRDataRate = rate;
                break;
        }
    }

    /**
     * Kuuntelijan asetus
     * @param movesenseActionListener Tapahtumien kuuntelija.
     */
    public void SetMovesenseActionListener(MovesenseActionListener movesenseActionListener)
    {
        this.movesenseActionListener = movesenseActionListener;
    }

    /**
     * Luokan käyttö vaatii sovelluksen kontekstia. Konteksti on Movesense-kirjastolle.
     * @param context Sovelluksen konteksti
     */
    public MovesenseHelper(Context context)
    {
        mMovesense = Mds.builder().build(context);
    }

    /**
     * Yhteyden muodostus Movesense-sensoriin.
     * @param deviceAddress Bluetooth MAC-osoite
     */
    public void Connect(String deviceAddress)
    {
        mMovesense.connect(deviceAddress, mdsConnectionListener);
        this.deviceAddress = deviceAddress;

    }

    /**
     * Yhteyden katkaisu Movesense-sensoriin
     */
    public void Disconnect()
    {
        if(isConnected)
        {
            UnsubscribeAll();
            mMovesense.disconnect(deviceAddress);
        }
    }

    /**
     * IMU9 parametrin konfiguraation asetus ja sen aktivointi.
     * @param info Movesense-sensorin rapotoima IMU INFO
     * @param SampleRateIndex Lukuintervallin valinta indeksinä. Indeksi Info parametrin lukuintervalleista.
     * @param AccIndex Kiihtyvyys anturin skaalan valinta indeksinä
     * @param GyroIndex Kulmanmuutos anturin skaalan valinta indeksinä
     * @param MagnIndex Magnetometri anturin skaalan valinta indeksinä
     */
    public void SetConfigAndSubscribe(ImuInfo info, int SampleRateIndex,int AccIndex,int GyroIndex,int MagnIndex)
    {
        AccIndex = info.AccRanges[AccIndex];
        GyroIndex = info.GyroRanges[GyroIndex];
        MagnIndex = info.MagnRanges[MagnIndex];

        SetDataRate(Sensor.IMU9,info.SampleRates[SampleRateIndex]);

        String[] putPaths = new String[3];
        String[] putContent = new String[3];

        putPaths[0] = SCHEME_PREFIX + serial + URI_LINEAR_ACCELERATION + CONFIG_ENDFIX;
        putPaths[1] = SCHEME_PREFIX + serial + URI_GYROSCOPE + CONFIG_ENDFIX;
        putPaths[2] = SCHEME_PREFIX + serial + URI_MAGNETOMETER + CONFIG_ENDFIX;

        LinearAccelerationConfig AccConfig = new LinearAccelerationConfig(new LinearAccelerationConfig.AccelerationSensorConfig(AccIndex));
        GyroscopeConfig GyroConfig = new GyroscopeConfig(new GyroscopeConfig.GyroscopeSensorConfig(GyroIndex));
        MagnetometerConfig MagnConfig = new MagnetometerConfig(new MagnetometerConfig.MagnetometerSensorConfig(MagnIndex));

        putContent[0] = gson.toJson(AccConfig);
        putContent[1] = gson.toJson(GyroConfig);
        putContent[2] = gson.toJson(MagnConfig);

        MdsResponseListener listener = new MdsResponseListener() {
            @Override
            public void onSuccess(String data, MdsHeader header) {
                OnSuccess();
            }

            @Override
            public void onError(MdsException e) {
                movesenseActionListener.OnError(e);
                OnFailure();
            }
        };

        mMovesense.put(putPaths[0],putContent[0],listener);
        // TODO: Gyroskoopin konfiguraatio ei toimi jostain syystä.
        //mMovesense.put(putPaths[1],putContent[1],listener);
        mMovesense.put(putPaths[0],putContent[0],listener);
        // TODO: Magnetomerin konfiguraatio ei toimi jostain syystä.
        //mMovesense.put(putPaths[2],putContent[2],listener);
        mMovesense.put(putPaths[0],putContent[0],listener);
    }

    /**
     * 3 onnistunutta konfiguraatiota käynnistää mittaus datan lukemisen.
     */
    private void OnSuccess()
    {
        counterImuConfigSuccess++;
        if(counterImuConfigSuccess ==3)
        {
            counterImuConfigSuccess =0;
            Subscribe(Sensor.IMU9);
        }
    }

    /**
     * Sisäinen metodi epäonnistumisen sattuessa.
     */
    private void OnFailure()
    {
        // TODO: TEE jotain
    }

    /**
     * lämpötilan tai pariston varauksen lukeminen luokan sisäisesti.
     * @param sensor
     */
    private void InternalGet(Sensor sensor)
    {
        String getPath = "";
        switch (sensor)
        {
            case Temperature:
                getPath = URI_TEMPERATURE;
                break;
            case BatteryVoltage:
                getPath = URI_BATTERY;
                break;
        }

        String uriBase = SCHEME_PREFIX + serial + getPath;

        mMovesense.get(uriBase, null, new MdsResponseListener() {
            @Override
            public void onSuccess(String data, MdsHeader header) {

                ConcurrencyHelper.GetInstance().Run(() -> {
                    Object oData = ConvertStringToObject(data,sensor,false);

                    movesenseActionListener.OnDataReceived(oData,sensor);
                }, ConcurrencyHelper.ThreadType.Data);
            }

            @Override
            public void onError(MdsException e) {
                movesenseActionListener.OnError(e);
            }
        });
    }

    /**
     * Sykkeen luku Movesense-sensorilta sisäisesti joka N näyte. 1/N
     * @param serial Movesense-sensorin sarjanumero
     * @param uri Tilausosoite
     * @param sensor Parametrin tyyppi
     * @return Palauttaa tilauksen hallinta objektin
     */
    private MdsSubscription InternalSubscribe(String serial, String uri, Sensor sensor)
    {
        return mMovesense.subscribe(URI_EVENTLISTENER, formatContractToJson(serial, uri), new MdsNotificationListener() {
            @Override
            public void onNotification(String s) {
                boolean convert = true;
                if(sensor == Sensor.HeartRate)
                {
                    if(HRDataRate != 1)
                    {
                        HRCounter+=1;
                        if(HRCounter == HRDataRate)
                        {
                            HRCounter = 0;

                        }
                        else
                        {
                            convert = false;
                        }
                    }
                }

                if(convert)
                {
                    ConcurrencyHelper.GetInstance().Run(() -> {
                        Object oData = ConvertStringToObject(s,sensor,false);

                        movesenseActionListener.OnDataReceived(oData,sensor);
                    }, ConcurrencyHelper.ThreadType.Data);
                }


            }

            @Override
            public void onError(MdsException e) {
                movesenseActionListener.OnError(e);
            }
        });
    }

    /**
     * Parametrien luku Movesense-sensorilta sisäisesti
     * @param serial Movesense-sensorin sarjanumero
     * @param uri Tilausosoite
     * @param sensor Parametrin tyyppi
     * @return Palauttaa tilauksen hallinta objektin
     */
    private MdsSubscription InternalSubscribe(String serial, String uri, String rate, Sensor sensor)
    {
        return mMovesense.subscribe(URI_EVENTLISTENER, formatContractToJson(serial, uri+"/"+rate), new MdsNotificationListener() {
            @Override
            public void onNotification(String s) {
                ConcurrencyHelper.GetInstance().Run(() -> {
                    Object oData = ConvertStringToObject(s,sensor,false);
                    movesenseActionListener.OnDataReceived(oData,sensor);
                }, ConcurrencyHelper.ThreadType.Data);

            }

            @Override
            public void onError(MdsException e) {
                movesenseActionListener.OnError(e);
            }
        });
    }

    /**
     * MeasurementIntervals listan parametrit tilaus Movesense-sensorilta
     * @param measurementIntervals Parametrien tiedot
     */
    public void SubscribeAll(MeasurementInterval[] measurementIntervals)
    {
        for(int i = 0; i<measurementIntervals.length;i++)
        {
            if(measurementIntervals[i].Enabled)
            {
                SetDataRate(measurementIntervals[i]);
                Subscribe(measurementIntervals[i].sensor);
            }
        }

    }

    /**
     * Parametrin tilaus Movesense-sensorilta. Lukuintervallin on asetettu aiemmin.
     * @param sensor
     * @return Tilauksen onnistuminen
     */
    public boolean Subscribe(Sensor sensor)
    {
        switch (sensor)
        {
            case Temperature:
                InitTimer(sensor);
                break;
            case BatteryVoltage:
                InitTimer(sensor);
                break;
            case LinearAcceleration:
                accSubscription = InternalSubscribe(serial,URI_LINEAR_ACCELERATION,linearAccelerationDataRate, sensor);
                break;
            case Gyroscope:
                gyroSubscription = InternalSubscribe(serial,URI_GYROSCOPE,gyroscopeDataRate, sensor);
                break;
            case Magnetometer:
                magnSubscription = InternalSubscribe(serial,URI_MAGNETOMETER,magnetometerDataRate, sensor);
                break;
            case ECG:
                ecgSubscription = InternalSubscribe(serial,URI_ECG,ecgDataRate, sensor);
                break;
            case IMU6:
                imu6Subscription = InternalSubscribe(serial,URI_IMU6,imuDataRate, sensor);
                break;
            case IMU6m:
                imu6mSubscription = InternalSubscribe(serial,URI_IMU6m,imuDataRate, sensor);
                break;
            case IMU9:
                imu9Subscription = InternalSubscribe(serial,URI_IMU9,imuDataRate, sensor);
                break;
            case HeartRate:
                hrSubscription = InternalSubscribe(serial,URI_HEARTRATE, sensor);
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Tilauksen keskeyttäminen Movesense-sensorilta.
     * @param sensor Parametrin valinta
     * @return Tilauksen keskeytyksen onnistuminen
     */
    public boolean Unsubscribe(Sensor sensor)
    {
        switch (sensor)
        {
            case Temperature:
                temperatureMeasurementTimer.cancel();
                temperatureMeasurementTimer = null;
                break;
            case BatteryVoltage:
                batteryVoltageMeasurementTimer.cancel();
                temperatureMeasurementTimer = null;
                break;
            case LinearAcceleration:
                accSubscription.unsubscribe();
                accSubscription = null;
                break;
            case Gyroscope:
                gyroSubscription.unsubscribe();
                gyroSubscription = null;
                break;
            case Magnetometer:
                magnSubscription.unsubscribe();
                magnSubscription = null;
                break;
            case ECG:
                ecgSubscription.unsubscribe();
                ecgSubscription = null;
                break;
            case IMU6:
                imu6Subscription.unsubscribe();
                imu6Subscription = null;
                break;
            case IMU6m:
                imu6mSubscription.unsubscribe();
                imu6mSubscription = null;
            case IMU9:
                imu9Subscription.unsubscribe();
                imu9Subscription = null;
                break;
            case HeartRate:
                hrSubscription.unsubscribe();
                hrSubscription = null;
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Kaikkien tilausten keskeytys Movesense-sensorilta
     */
    public void UnsubscribeAll()
    {
        if(temperatureMeasurementTimer!=null)
        {
            temperatureMeasurementTimer.cancel();
            temperatureMeasurementTimer = null;
        }

        if(batteryVoltageMeasurementTimer!=null)
        {
            batteryVoltageMeasurementTimer.cancel();
            batteryVoltageMeasurementTimer = null;
        }

        if(accSubscription!=null)
        {
            accSubscription.unsubscribe();
            accSubscription = null;
        }

        if(gyroSubscription!=null)
        {
            gyroSubscription.unsubscribe();
            gyroSubscription = null;
        }

        if(magnSubscription!=null)
        {
            magnSubscription.unsubscribe();
            magnSubscription = null;
        }

        if(ecgSubscription!=null)
        {
            ecgSubscription.unsubscribe();
            ecgSubscription = null;
        }

        if(imu6Subscription!=null)
        {
            imu6Subscription.unsubscribe();
            imu6Subscription = null;
        }

        if(imu6mSubscription!=null)
        {
            imu6mSubscription.unsubscribe();
            imu6mSubscription = null;
        }

        if(imu9Subscription!=null)
        {
            imu9Subscription.unsubscribe();
            imu9Subscription = null;
        }

        if(hrSubscription!=null)
        {
            hrSubscription.unsubscribe();
            hrSubscription = null;
        }
    }

    /**
     * Kokonaisen URI-resurssin muodostus
     * @param serial Movesense-sensorin sarjanumero
     * @param uri Uri
     * @return Muodostettu resurssi URI
     */
    private String formatContractToJson(String serial, String uri) {
        StringBuilder sb = new StringBuilder();
        return sb.append("{\"Uri\": \"").append(serial).append(uri).append("\"}").toString();
    }

    /**
     * Kokonaisen URI-resurssin muodostus
     * @param serial Movesense-sensorin sarjanumero
     * @param path polku resurssiin
     * @return Muodostettu polku resurssiin
     */
    private String pathFormatHelper(String serial, String path) {
        final StringBuilder sb = new StringBuilder();
        return sb.append(SCHEME_PREFIX).append(serial).append("/").append(path).toString();
    }

    /**
     * Parametrin info-tiedon pyyntö Movesense-sensorilta. Info sisältää skaalat, lukuintervallit, ym.
     * @param sensor Valittu parametri
     */
    public void GetInfo(Sensor sensor)
    {
        String getPath = "";
        switch (sensor)
        {
            case Temperature:
                getPath = URI_TEMPERATURE;
                break;
            case BatteryVoltage:
                getPath = URI_BATTERY;
                break;
            case LinearAcceleration:
                getPath = URI_LINEAR_ACCELERATION;
                break;
            case Gyroscope:
                getPath = URI_GYROSCOPE;
                break;
            case Magnetometer:
                getPath = URI_MAGNETOMETER;
                break;
            case ECG:
                getPath = URI_ECG;
                break;
            case HeartRate:
                getPath = URI_HEARTRATE;
                break;
            case IMU6:
                getPath = URI_IMU_INFO;
                break;
            case IMU9:
                getPath = URI_IMU_INFO;
                break;
        }

        String uriBase = SCHEME_PREFIX + serial + getPath + INFO_ENDFIX;

        mMovesense.get(uriBase, null, new MdsResponseListener() {
            @Override
            public void onSuccess(String data, MdsHeader header) {

                ConcurrencyHelper.GetInstance().Run(() -> {
                    Object oData = ConvertStringToObject(data,sensor,true);

                    movesenseActionListener.OnInfoReceived(oData,sensor);
                }, ConcurrencyHelper.ThreadType.Data);


            }

            @Override
            public void onError(MdsException e) {
                movesenseActionListener.OnError(e);
            }
        });
    }

    /**
     * Movesense-sensorin vastauksen deserialisointi
     * @param sData Movesense-sensorin vastaus
     * @param sensor Valittu sensori
     * @param info Onko Info-tyyppi
     * @return Vastaus tyypiteltynä objektina
     */
    private Object ConvertStringToObject(String sData, Sensor sensor, Boolean info)
    {
        JsonElement content;
        String sContent;
        if(info || sensor == Sensor.Temperature || sensor == Sensor.BatteryVoltage)
        {

            content = JsonParser.parseString(sData).getAsJsonObject().get("Content");
        }
        else
        {
            content = JsonParser.parseString(sData).getAsJsonObject().get("Body");
        }

        sContent = content.toString();

        if(info)
        {
            switch (sensor)
            {

                case Temperature:
                    return gson.fromJson(sContent, TemperatureInfo.class);
                case LinearAcceleration:
                    return gson.fromJson(sContent, LinearAccelerationInfo.class);
                case Gyroscope:
                    return gson.fromJson(sContent, GyroscopeInfo.class);
                case Magnetometer:
                    return gson.fromJson(sContent, MagnetometerInfo.class);
                case ECG:
                    return gson.fromJson(sContent, ECGInfo.class);
                case IMU6:
                    return gson.fromJson(sContent, ImuInfo.class);
                case IMU6m:
                    return gson.fromJson(sContent, ImuInfo.class);
                case IMU9:
                    return gson.fromJson(sContent, ImuInfo.class);
                case HeartRate:
                    return gson.fromJson(sContent, HeartrateInfo.class);
            }
        }
        else
        {
            switch (sensor)
            {

                case Temperature:
                    return gson.fromJson(sContent, TemperatureData.class);
                case BatteryVoltage:
                    BatteryVoltageData batteryVoltageData = new BatteryVoltageData();
                    batteryVoltageData.Percent = content.getAsInt();
                    return batteryVoltageData;
                    //return gson.fromJson(sContent, BatteryVoltageData.class);
                case LinearAcceleration:
                    return gson.fromJson(sContent, LinearAccelerationData.class);
                case Gyroscope:
                    return gson.fromJson(sContent, GyroscopeData.class);
                case Magnetometer:
                    return gson.fromJson(sContent, MagnetometerData.class);
                case ECG:
                    return gson.fromJson(sContent, ECGData.class);
                case IMU6:
                    return gson.fromJson(sContent, Imu6Data.class);
                case IMU6m:
                    return gson.fromJson(sContent, Imu6mData.class);
                case IMU9:
                    return gson.fromJson(sContent, Imu9Data.class);
                case HeartRate:
                    return gson.fromJson(sContent, HeartrateData.class);
            }
        }

        return null;
    }


}
