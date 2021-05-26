package fi.digi.savonia.movesense.Models.Movesense.Data;

public class TemperatureData {

    public long Timestamp;
    public float Measurement;

    public float ConvertToCelcius() {
        return (float) (Measurement - 273.15);
    }
}
