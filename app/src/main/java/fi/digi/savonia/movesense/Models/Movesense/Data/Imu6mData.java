package fi.digi.savonia.movesense.Models.Movesense.Data;

import fi.digi.savonia.movesense.Models.Movesense.Float3DVector;

public class Imu6mData {
    public long Timestamp;
    public Float3DVector[] ArrayAcc;
    public Float3DVector[] ArrayMagn;
}
