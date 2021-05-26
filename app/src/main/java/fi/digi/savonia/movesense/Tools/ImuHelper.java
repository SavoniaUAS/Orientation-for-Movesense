package fi.digi.savonia.movesense.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import fi.digi.savonia.movesense.Models.Movesense.Data.Imu9Data;
import fi.digi.savonia.movesense.Models.Movesense.Float3DVector;
import fi.digi.savonia.movesense.Tools.Listeners.ImuActionListener;

/**
 * Työkalu Asennon laskemiseen Movesense-sensorin datan perusteella
 */
public class ImuHelper {


    // Muuttujat parametrien kalibrointiin

    /**
     * Kulmanmuutos X-akselin kalibrointi
     */
    private float gyroX_Offset=0;
    /**
     * Kulmanmuutos Y-akselin kalibrointi
     */
    private float gyroY_Offset=0;
    /**
     * Kulmanmuutos Z-akselin kalibrointi
     */
    private float gyroZ_Offset=0;
    /**
     * Magnetometrin X-akselin kalibrointi
     */
    private float magnX_Offset=-77.35114f;
    /**
     * Magnetometrin Y-akselin kalibrointi
     */
    private float magnY_Offset=-34.174217f;
    /**
     * Magnetometrin Z-akselin kalibrointi
     */
    private float magnZ_Offset=-185.7783f;
    /**
     * Kuuntelija Tapahtumille
     */
    private ImuActionListener mImuActionListener;
    /**
     * Värähtelyn intesiteetin lasketaan lista. Listaan Kiihtyvyys-parametrin arvoja.
     */
    List<Float> vibrationBuffer;
    /**
     * Magnetometrin kalibrointiin tarvittava lista. Listaan Magnetometri-parametrin arvoja.
     */
    List<Float3DVector> magnetometerCalibrationBuffer;
    /**
     * Data lista Magnetometrin kalibroinnin ja värähtelyn laskennan käyttöön
     */
    List<Imu9Data> dataBuffer;
    /**
     * Liukuvankeskiarvon laskenta
     */
    SlidingAverage slidingAverage;
    /**
     * Movesense-sensorin asennon sensorifuusioon perustuva arvionti
     */
    CustomOrientationAlgorithm filter;
    /**
     * Kiihtyys-parametrin arvojen suodatin
     */
    LowPassFilterCustom accelerationLowPass;
    /**
     * Kulmanmuutos-parametrin arvojen suodatin
     */
    LowPassFilterCustom gyroscopeLowPass;
    /**
     * Ajastin analyysin suorittamiseen
     */
    Timer timer;
    /**
     * Kulman korjauksen määrä X-, Y- ja Z-akselille
     */
    Float3DVector angle_correction;
    /**
     * Laskettu Movesense-sensorin asento
     */
    Float3DVector output;
    /**
     * Kulmakorjatun tuloksen kulmaero painovoima vektoriin
     */
    float angle=0;
    /**
     * Värähtelyn datan listan pituus
     */
    int VibrationBufferLength;
    /**
     * Näytteiden välinen aika
     */
    float deltaT;
    /**
     * Laskurimuuttuja Magnetometrin kalibrointiin. Kalibrointi vaatii kehitystystä ja testaamista.
     */
    int calibrateMagnetometerCount=0;
    /**
     * Tila muuttuja liukuvakeskiarvo
     */
    boolean useSlidingAverage = false;
    /**
     * Tilamuuttuja kalibroi kulmakorjaus.
     */
    boolean calibrateAngles = false;

    /**
     * Ajastimen tehtävä analyysiin
     */
    private class AnalyseTask extends TimerTask
    {
        @Override
        public void run() {
            OnUpdate();
        }
    }

    /**
     * Määrittää Luokantoiminnan lukuintervallin perusteella
     * @param sampleRate Lukuintervalli (Näytettä sekunnissa)
     */
    public ImuHelper(float sampleRate)
    {
        filter = new CustomOrientationAlgorithm();
        accelerationLowPass = new LowPassFilterCustom((int) sampleRate,3);
        gyroscopeLowPass = new LowPassFilterCustom((int) sampleRate,3);
        deltaT = (1/sampleRate);
        VibrationBufferLength = (int) (sampleRate);
        slidingAverage = new SlidingAverage((int) (sampleRate/5));
        vibrationBuffer = new ArrayList<>();
        magnetometerCalibrationBuffer = new ArrayList<>();
        dataBuffer = new ArrayList<>();
        timer = new Timer();
        angle_correction = new Float3DVector();
    }

    /**
     * Magnetometrin kalibrointi. Kalibrointi vaatisi lisätyötä.
     */
    public synchronized void MagnetometerCalibration()
    {
        float total_x=0,total_y=0,total_z=0;
        for (Float3DVector magnetometerResult:magnetometerCalibrationBuffer) {
            total_x+=magnetometerResult.x;
            total_y+=magnetometerResult.y;
            total_z+=magnetometerResult.z;
        }

        magnX_Offset = total_x/magnetometerCalibrationBuffer.size();
        magnY_Offset = total_y/magnetometerCalibrationBuffer.size();
        magnZ_Offset = total_z/magnetometerCalibrationBuffer.size();
        magnetometerCalibrationBuffer.clear();

    }

    /**
     * Uuden mittaustuloksen syöttäminen analyysiin
     * @param data
     */
    public synchronized void Update(Imu9Data data)
    {
        ConcurrencyHelper.GetInstance().Run( ()-> {
            Float3DVector gyroscope,accelerometer, magnetometer;
            AddToBuffer(data);


            // Mittaus sisältää useita mittauksia yhdessä päivityksessä
            for(int i =0;i<data.ArrayAcc.length;i++)
            {
                gyroscope = data.ArrayGyro[i];
                accelerometer = data.ArrayAcc[i];
                magnetometer = data .ArrayMagn[i];

                accelerometer.ApplyLowPassFilter(accelerationLowPass);
                gyroscope.ApplyLowPassFilter(gyroscopeLowPass);

                //Halutessa voi gyro driftiä voi vähentään asettamalla minimi arvo. Arvon alittaessa asetun rajan arvon on 0
                //gyroscope.MininumAccetableValue(3);

                output = filter.calculateOrientation(deltaT,new float[] {gyroscope.x-gyroX_Offset,gyroscope.y-gyroY_Offset,gyroscope.z-gyroZ_Offset},new float[] {accelerometer.x,accelerometer.y, accelerometer.z},new float[] {magnetometer.x-magnX_Offset,magnetometer.y-magnY_Offset,magnetometer.z-magnZ_Offset});
                //output.ApplyLowPassFilter(accelerationLowPass);
                if(calibrateAngles)
                {
                    calibrateAngles=false;
                    angle_correction.x = output.x;
                    angle_correction.y = output.y;
                    angle_correction.z = output.z;
                }
                else
                {
                    output.CorrectAngles(angle_correction);
                }
                angle = GetOrientation();
                slidingAverage.Add(angle);


            }

        }, ConcurrencyHelper.ThreadType.Imu);
    }

    /**
     * Lisätään data magnetometrin kalibrointiin.
     * @param data
     */
    private synchronized void AddToBuffer(Imu9Data data) {

        magnetometerCalibrationBuffer.add(data.ArrayMagn[0]);
        if(magnetometerCalibrationBuffer.size()>=1000)
        {
            if(calibrateMagnetometerCount>0)
            {
                calibrateMagnetometerCount--;
                MagnetometerCalibration();
            }
        }




        dataBuffer.add(data);
        int overLimitBuffer = dataBuffer.size() - 5;
        while(overLimitBuffer>0)
        {
            dataBuffer.remove(0);
            overLimitBuffer--;
        }

        for(int i= 0;i<data.ArrayAcc.length;i++)
        {
            vibrationBuffer.add(data.ArrayAcc[i].x);
        }
        int overLimitCount = vibrationBuffer.size()-VibrationBufferLength;

        while(overLimitCount>0)
        {
            vibrationBuffer.remove(0);
            overLimitCount--;
        }
    }

    /**
     * Asennon analyysin aktivointi
     * @param updateRate Analyysin intervalli (Analyysia sekunnissa) Enintään 13!
     */
    public void StartUpdates(int updateRate)
    {
        timer = new Timer();
        int delay = (int) ((1/(float)updateRate)*1000.0);
        timer.schedule(new AnalyseTask(),delay,delay);
    }

    /**
     * Asennon analyysin deaktivointi
     */
    public void StopUpdates()
    {
        timer.cancel();
    }

    /**
     * Sisäinen metodi Analyysin tuloksen välittämiiseen eteenpäin
     */
    private void OnUpdate()
    {
        if(useSlidingAverage)
        {
            mImuActionListener.OnUpdate(slidingAverage.GetAverage(),GetVibration());
        }
        else
        {
            mImuActionListener.OnUpdate(angle,GetVibration());
        }
    }

    /**
     * Aktivoi/Deaktivoi liukuva keskiarvo
     * @param use
     */
    public void UseSlidingAverage(boolean use)
    {
        this.useSlidingAverage = use;
    }

    /**
     * Kalibroi kulmanmuutos sensorin. Movesense-sensorin tulisi olla paikallaan tällöin.
     */
    public void Calibrate()
    {
        ConcurrencyHelper.GetInstance().Run(() -> {

            float gyroX_drift=0,gyroY_drift=0, gyroZ_drift=0;
            int measurements = 0;

            synchronized (this)
            {
                for (Imu9Data data:dataBuffer) {
                    for(int i = 0;i<data.ArrayGyro.length;i++)
                    {
                        gyroX_drift+=data.ArrayGyro[i].x;
                        gyroY_drift+=data.ArrayGyro[i].y;
                        gyroZ_drift+=data.ArrayGyro[i].z;
                        measurements++;
                    }
                }
                gyroX_Offset = gyroX_drift/measurements;
                gyroY_Offset = gyroY_drift/measurements;
                gyroZ_Offset = gyroZ_drift/measurements;

                calibrateAngles = true;

            }

            calibrateMagnetometerCount=1;
            magnetometerCalibrationBuffer.clear();

            mImuActionListener.CalibrationReady();

        }, ConcurrencyHelper.ThreadType.Imu);
    }

    /**
     * Asettaa kuuntelijan luokkaan.
     * @param imuActionListener
     */
    public void SetImuActionListener(ImuActionListener imuActionListener)
    {
        mImuActionListener = imuActionListener;
    }

    /**
     * Analysoi sensorin asennon
     * @return Vektori kulman ero painovoima vektoriin
     */
    public float GetOrientation()
    {

        output.x= Math.abs(output.x);
        output.y= Math.abs(output.y);
        output.z= Math.abs(output.z);

        Float3DVector orientationVector = RotationUtil.Rotate(output);

        //Sensorin orientaatio vektorina
        Vector3 orientation = Vector3.New(orientationVector.x,orientationVector.y,orientationVector.z);
        //Painovoiman vektori
        Vector3 reference = Vector3.New(0,0,1);

        //Lasketaan painovoimavektorin ja sensorin asennon vektorin kulman ero toisiinsa
        float dot_product = orientation.dot(reference);
        float magnitude_a = Math.abs(orientation.norm());
        float magnitude_b = Math.abs(reference.norm());
        float angle = (float) Math.toDegrees(Math.acos((dot_product)/(magnitude_a*magnitude_b)));

        return angle;
    }

    /**
     * Laskee tärinän intensiteetin historian perusteella.
     * @return
     */
    public synchronized float GetVibration()
    {
        float min=0;
        float max=0;
        if(vibrationBuffer.size()>0)
        {
            min=max=vibrationBuffer.get(0);
            for(int i=0;i<vibrationBuffer.size();i++)
            {
                if(vibrationBuffer.get(i)<min)
                {
                    min = vibrationBuffer.get(i);
                }
                if(vibrationBuffer.get(i)>max)
                {
                    max = vibrationBuffer.get(i);
                }
            }
            return max-min;
        }
        return 0;

    }

}
