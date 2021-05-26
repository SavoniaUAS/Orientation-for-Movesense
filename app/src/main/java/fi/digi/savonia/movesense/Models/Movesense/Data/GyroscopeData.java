package fi.digi.savonia.movesense.Models.Movesense.Data;

import java.util.Date;

import fi.digi.savonia.movesense.Models.Movesense.Float3DVector;

public class GyroscopeData {
    public long Timestamp;
    public Float3DVector[] ArrayGyro;

    public Date GetDateTime()
    {
        return new Date(Timestamp);
    }
}
