package fi.digi.savonia.movesense.Models.Movesense.Config;

import com.google.gson.annotations.SerializedName;

public class LinearAccelerationConfig {
    @SerializedName("config")
    public final AccelerationSensorConfig config;

    public LinearAccelerationConfig(AccelerationSensorConfig config) {
        this.config = config;
    }

    public static class AccelerationSensorConfig
    {
        @SerializedName("GRange")
        public int GRange;

        public AccelerationSensorConfig(int GRange)
        {
            this.GRange = GRange;
        }
    }

}
