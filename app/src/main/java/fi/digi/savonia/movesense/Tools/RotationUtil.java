package fi.digi.savonia.movesense.Tools;

import android.opengl.Matrix;

import fi.digi.savonia.movesense.Models.Movesense.Float3DVector;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * Helppokäyttöinen wrapper rotaatiomatriisi laskentaan
 */
public class RotationUtil {

    /**
     * X-akselin rotaatiomatriisi listan muodossa
     */
    private static final float[] rX = new float[16];
    /**
     * Y-akselin rotaatiomatriisi listan muodossa
     */
    private static final float[] rY = new float[16];
    /**
     * Z-akselin rotaatiomatriisi listan muodossa
     */
    private static final float[] rZ = new float[16];
    /**
     * X- ja Y-akseleiden välinen rotaatiomatrisiin tulos
     */
    private static float[] resultXY;
    /**
     * X-, Y- ja Z-akseleiden välinen rotaatiomatriisin tulos
     */
    private static float[] result;
    /**
     * Kääntävä vektori
     */
    private static final float[] inVec = new float[4];
    /**
     * Käännetty vektori
     */
    private static final float[] outVec = new float[4];


    /**
     * Vektoria Pitch, Roll ja Yaw akselin kääntö
     * @param in Pitch, roll ja Yaw muuttujassa
     * @return Käännetty vektori
     */
    public static Float3DVector Rotate(Float3DVector in)
    {
        CreateRotationMatrix(in.x,in.y,in.z);

        inVec[0] = 0;
        inVec[1] = 0;
        inVec[2] = 1;
        inVec[3] = 1;

        Matrix.multiplyMM(resultXY,0,rX,0,rY,0);
        Matrix.multiplyMM(result,0,resultXY,0,rZ,0);
        Matrix.multiplyMV(outVec,0,result,0,inVec,0);

        Float3DVector ret = new Float3DVector();
        ret.x=outVec[0];
        ret.y=outVec[1];
        ret.z=outVec[2];
        return ret;
    }

    /**
     * Rotaatiomatriisin luonti X-, Y- ja Z-akselin arvojen perusteella. (Arvot Radiaaneja)
     * @param x X-akseli
     * @param y Y-akseli
     * @param z Z-akseli
     */
    private static void CreateRotationMatrix(float x, float y, float z)
    {
        result = new float[16];
        resultXY = new float[16];

        rX[0] = 1;
        rX[1] = 0;
        rX[2] = 0;
        rX[3] = 0;

        rX[4] = 0;
        rX[5] = (float) cos(x);
        rX[6] = (float) sin(x);
        rX[7] = 0;

        rX[8] = 0;
        rX[9] = (float) -sin(x);
        rX[10] = (float) cos(x);
        rX[11] = 0;

        rX[12] = 0;
        rX[13] = 0;
        rX[14] = 0;
        rX[15] = 1;

        rY[0] = (float) cos(y);
        rY[1] = 0;
        rY[2] = (float) -sin(y);
        rY[3] = 0;

        rY[4] = 0;
        rY[5] = 1;
        rY[6] = 0;
        rY[7] = 0;

        rY[8] = (float) sin(y);
        rY[9] = 0;
        rY[10] = (float) cos(y);
        rY[11] = 0;

        rY[12] = 0;
        rY[13] = 0;
        rY[14] = 0;
        rY[15] = 1;

        rZ[0] = (float) cos(z);
        rZ[1] = (float) sin(z);
        rZ[2] = 0;
        rZ[3] = 0;

        rZ[4] = (float) -sin(z);
        rZ[5] = (float) cos(z);
        rZ[6] = 0;
        rZ[7] = 0;

        rZ[8] = 0;
        rZ[9] = 0;
        rZ[10] = 1;
        rZ[11] = 0;

        rZ[12] = 0;
        rZ[13] = 0;
        rZ[14] = 0;
        rZ[15] = 1;


    }

}
