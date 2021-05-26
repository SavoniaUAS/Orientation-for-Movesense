package fi.digi.savonia.movesense.Tools.Listeners;

import fi.digi.savonia.movesense.Models.Movesense.Float3DVector;

/**
 * Kuuntelijan metodin IMU-sensoridatan analysointiin ImuHelper-luokalla
 */
public interface ImuActionListener {
    void CalibrationReady();
    void OnUpdate(float Angle,float vibration);

}
