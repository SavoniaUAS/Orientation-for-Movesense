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
     * Näytteen X-, Y- ja Z-akselilta käsittely suodattimella
     * @param values X-, Y- ja Z-akselin näyte.
     * @return Palauttaa suodatetun arvon X-, Y- ja Z-askelilla
     */
    public float[] filter(float[] values)
    {

        output[0] = (float) x_filter.filter(values[0]);
        output[1] = (float) y_filter.filter(values[1]);
        output[2] = (float) z_filter.filter(values[2]);

        return output;
    }

}
