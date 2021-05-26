package fi.digi.savonia.movesense.Models.Movesense.Config;

import com.google.gson.annotations.SerializedName;

public class MagnetometerConfig {

    @SerializedName("config")
    public final MagnetometerSensorConfig config;

    public MagnetometerConfig(MagnetometerSensorConfig config) {
        this.config = config;
    }

    public static class MagnetometerSensorConfig
    {
        @SerializedName("GRange")
        public int GRange;

        public MagnetometerSensorConfig(int GRange)
        {
            this.GRange = GRange;
        }
    }
}
