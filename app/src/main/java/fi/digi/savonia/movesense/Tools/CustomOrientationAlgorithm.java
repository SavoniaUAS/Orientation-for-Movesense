package fi.digi.savonia.movesense.Tools;

import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import fi.digi.savonia.movesense.Models.Movesense.Float3DVector;
import fi.digi.savonia.movesense.Tools.ConcurrencyHelper;


/**
 * Helppokäyttöinen wrapper asennon arviointiin sensorifuusio-algoritmillä.
 */
public class CustomOrientationAlgorithm {

    /**
     * Pitch - akseli. Keulan kulma
     */
    private float pitch=0;
    /**
     * Roll - akseli. rungon rotaatio
     */
    private float roll=0;
    /**
     * Madgwick-algoritmin asennon tunnistamiseen. Ei käytössä tällä hetkellä.
     */
    private MadgwickAHRS filter = new MadgwickAHRS((float) (1.0/52.0),1f);;
    /**
     * Mahony-algoritmi asennon tunnistamiseen. Ei käytössä tällä hetkellä.
     */
    private MahonyAHRS filter_mahony = new MahonyAHRS();

    /**
     * Asennon arviointi sensorifuusio-algoritmilla
     * @param deltaT Ajanjakso näytteiden välillä
     * @param gyroscope Kulmanmuutos parametri Movesense-sensorilta
     * @param acceleration Kiihtyvyys parametri Movesense-sensorilta
     * @param magnetic Magnetometri parametri Movesense-sensorilta
     * @return X-, Y- , Z-akselin kulma verrattuna nollapisteeseen. (Pitch, Roll ja Yaw). Yaw on lukittu 0 asteeseen.
     */
    public Float3DVector calculateOrientation(float deltaT, float[] gyroscope, float[] acceleration, float[] magnetic) {

        //https://sites.google.com/site/myimuestimationexperience/filters/complementary-filter
        //Vaihtoehtoja sensorifuusion algoritmille on: MahonyAHRS tai MadgwickAHRS.

        Float alpha = 0.04f;

        Float3DVector ret = new Float3DVector();

        float pitchAcc, rollAcc;

        float pow_x = acceleration[0]*acceleration[0];
        float pow_y = acceleration[1]*acceleration[1];
        float pow_z = acceleration[2]*acceleration[2];

        pitch+=gyroscope[2]*deltaT;
        roll+=gyroscope[1]*deltaT;




        pitchAcc = (float) Math.toDegrees(Math.atan((acceleration[2]/Math.sqrt(pow_y + pow_x))));
        pitch = (float) ((pitch *(1-alpha))+(pitchAcc * alpha));

        rollAcc = (float) Math.toDegrees(Math.atan((acceleration[1]/Math.sqrt(pow_x + pow_z))));
        roll = (roll*(1-alpha))+(rollAcc * alpha);


        ret.x = (float) Math.toRadians(pitch);
        ret.y = (float) Math.toRadians(roll);
        ret.z = 0;

        //Kommentoitu vaihtoehto orientaatio Movesense-sensori vaakasuorassa

/*
        pitch+=gyroscope[0]*deltaT;
        roll+=gyroscope[1]*deltaT;

*/

/*
        pitchAcc = (float) Math.toDegrees(Math.atan((acceleration[0]/Math.sqrt(pow_y + pow_z))));
        pitch = (float) ((pitch *(1-alpha))+(pitchAcc * alpha));

        rollAcc = (float) Math.toDegrees(Math.atan((acceleration[1]/Math.sqrt(pow_x + pow_z))));
        roll = (roll*(1-alpha))+(rollAcc * alpha);

         */

/*
        ret.x = (float) Math.toRadians(eulerAngles[0]);
        ret.y = (float) Math.toRadians(eulerAngles[1]);
        ret.z = (float) Math.toRadians(eulerAngles[2]);



 */

        return  ret;
    }
}