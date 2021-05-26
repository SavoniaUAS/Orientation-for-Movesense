package fi.digi.savonia.movesense.Models.Movesense.Config;

import com.google.gson.annotations.SerializedName;

public class GyroscopeConfig {

    @SerializedName("config")
    public final GyroscopeSensorConfig config;

    public GyroscopeConfig(GyroscopeSensorConfig config) {
        this.config = config;
    }

    public static class GyroscopeSensorConfig
    {
        @SerializedName("DSPRange")
        public int DPSRange;

        public GyroscopeSensorConfig(int DPSRange)
        {
            this.DPSRange = DPSRange;
        }
    }


}
