package fi.digi.savonia.movesense.Tools;

import uk.me.berndporr.iirj.Butterworth;

/**
 * Alipäästösuodatin mittausdatan suodattamiseen
 */
public class LowPassFilterCustom
{

    // Gravity and linear accelerations components for the
    // Wikipedia low-pass fusedOrientation
    /**
     * X-, Y- ja Z-akselin laskennan tulos
     */
    private float[] output = new float[] { 0, 0, 0 };

    /**
     * Suodatin X-akselille
     */
    private Butterworth x_filter = new Butterworth();
    /**
     * Suodatin Y-akselille
     */
    private Butterworth y_filter = new Butterworth();
    /**
     * Suodatin Z-akselille
     */
    private Butterworth z_filter = new Butterworth();


    /**
     *
     * @param sampleRate Lukuintervalli (näytettä sekunnissa )
     * @param cutOffFrequency Suodattimen alipäästön yläraja (Hz)
     */
    public LowPassFilterCustom(int sampleRate, int cutOffFrequency) {
        x_filter.lowPass(1,sampleRate,cutOffFrequency);
        y_filter.lowPass(1,sampleRate,cutOffFrequency);
        z_filter.lowPass(1,sampleRate,cutOffFrequency);
    }

    /**
     * Lisää näytteen X-, Y- ja Z-akselilta suodattimeen
     * @param values X-, Y- ja Z-akselin näytteet.
     * @return Palauttaa suodatettut arvot X-, Y- ja Z-askelilta
     */
    public float[] filter(float[] values)
    {

        output[0] = (float) x_filter.filter(values[0]);
        output[1] = (float) y_filter.filter(values[1]);
        output[2] = (float) z_filter.filter(values[2]);

        return output;
    }

}
