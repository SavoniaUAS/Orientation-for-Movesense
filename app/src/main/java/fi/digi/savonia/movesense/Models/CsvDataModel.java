package fi.digi.savonia.movesense.Models;

/**
 * Dataluokka Csv-tiedoston kirjoittamiseen
 */
public class CsvDataModel {
    public float Vibration;
    public float PenAngle;

    public static String GetHeaders()
    {
        return "Vibration;PenAngle";
    }
}
