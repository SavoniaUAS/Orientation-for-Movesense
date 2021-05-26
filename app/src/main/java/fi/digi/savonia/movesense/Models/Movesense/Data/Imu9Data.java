package fi.digi.savonia.movesense.Models.Movesense.Data;

import fi.digi.savonia.movesense.Models.Movesense.Float3DVector;

public class Imu9Data {
    public long Timestamp;
    public Float3DVector[] ArrayAcc;
    public Float3DVector[] ArrayGyro;
    public Float3DVector[] ArrayMagn;
}
