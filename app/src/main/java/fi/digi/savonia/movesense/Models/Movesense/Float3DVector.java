package fi.digi.savonia.movesense.Models.Movesense;


import fi.digi.savonia.movesense.Tools.LowPassFilterCustom;

/**
 * Float3DVector muuttuja! + aputoimintoja
 */
public class Float3DVector {
    public float x=0;
    public float y=0;
    public float z=0;


    @Override
    public String toString()
    {
        String ToStringTemplate = "X: '%1s' Y: '%2s' Z: '%3s'";
        return String.format(ToStringTemplate,x,y,z);
    }

    public void ApplyLowPassFilter(LowPassFilterCustom filter)
    {
        float[] filteredValues = filter.filter(new float[]{x,y,z});
        x=filteredValues[0];
        y=filteredValues[1];
        z=filteredValues[2];
    }

    public float[] toArray()
    {
        return new float[] {x,y,z};
    }

    public void MininumAccetableValue(float value)
    {
        if(x<value)
        {
            x=0;
        }
        if(y<=value)
        {
            y=0;
        }
        if(z<=value)
        {
            z=0;
        }
    }

    public void CorrectAngles(Float3DVector correction)
    {
        this.x -= correction.x;
        this.y -= correction.y;
        this.z -= correction.z;
    }
}
