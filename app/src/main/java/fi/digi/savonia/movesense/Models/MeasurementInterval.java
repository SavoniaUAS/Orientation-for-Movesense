package fi.digi.savonia.movesense.Models;

import fi.digi.savonia.movesense.Tools.MovesenseHelper;

/**
 *
 */
public class MeasurementInterval {

    public MeasurementInterval(MovesenseHelper.Sensor sensor, int[] array)
    {
        DataRateArray = array;
        this.sensor = sensor;
    }

    public MeasurementInterval(MovesenseHelper.Sensor sensor, int[] array, int limit)
    {
        DataRateArray = array;
        Limit = limit;
        this.sensor = sensor;
    }

    public MeasurementInterval(MovesenseHelper.Sensor sensor, int[] array, int limit, String infoString)
    {
        DataRateArray = array;
        Limit = limit;
        this.infoString = infoString;
        this.sensor = sensor;
    }

    public MeasurementInterval(MovesenseHelper.Sensor sensor, int[] array, int limit, String infoString,int indexNextInfo, String infoString2)
    {
        DataRateArray = array;
        Limit = limit;
        this.infoString = infoString;
        this.sensor = sensor;
        this.infoString2 = infoString2;
        nextInfoIndex = indexNextInfo;
        dualInfo = true;
    }

    public MeasurementInterval(MovesenseHelper.Sensor sensor, int[] array, int limit, String infoString,int indexNextInfo, String infoString2,int indexNextInfo2, String infoString3)
    {
        DataRateArray = array;
        Limit = limit;
        this.infoString = infoString;
        this.sensor = sensor;
        this.infoString2 = infoString2;
        nextInfoIndex = indexNextInfo;
        nextInfoIndex2 = indexNextInfo2;
        this.infoString3 = infoString3;
        tripleInfo = true;
    }

    public MovesenseHelper.Sensor sensor;

    private final int NO_VALUE = -1;
    private int[] DataRateArray;
    private int Limit = NO_VALUE;
    private int limitIndex = NO_VALUE;

    private String infoString= "Empty";
    private String infoString2 = "Empty";
    private String infoString3 = "Empty";
    public boolean Enabled = false;
    private int selectedIndex = 0;
    private int nextInfoIndex = NO_VALUE;
    private int nextInfoIndex2 = NO_VALUE;
    private boolean dualInfo = false;
    private boolean tripleInfo = false;

    private final String EmptyString = "Empty";

    public void Select(int index)
    {
        selectedIndex = index;
    }

    public int GetSelectedIndex()
    {
        return selectedIndex;
    }

    public String GetSelectedValueString(boolean ShowInfo)
    {
        if(infoString.equals(EmptyString) && ShowInfo)
        {
            return infoString.replace("{value}", String.valueOf(DataRateArray[selectedIndex]));
        }
        else
        {
            return String.valueOf(DataRateArray[selectedIndex]);
        }
    }

    public int GetSelectedValue()
    {
        if(dualInfo)
        {
            if(selectedIndex<nextInfoIndex)
            {
                return DataRateArray[selectedIndex];
            }
            else
            {
                return DataRateArray[selectedIndex]*60;
            }
        }
        else if(tripleInfo)
        {
            if(selectedIndex<nextInfoIndex)
            {
                return DataRateArray[selectedIndex];
            }
            else if(selectedIndex<nextInfoIndex2)
            {
                return DataRateArray[selectedIndex]*60;
            }
            else
            {
                return DataRateArray[selectedIndex]*360;
            }
        }
        else
        {
            return DataRateArray[selectedIndex];
        }


    }

    public String[] getDataRateArrayString()
    {
        final int arrayLength = getDataRateArray().length;
        String[] temp = new String[arrayLength];
        if(infoString.equals(EmptyString))
        {
            for(int i=0;i<arrayLength;i++)
            {
                temp[i] = String.valueOf(DataRateArray[i]);
            }
            return temp;
        }
        else
        {
            for(int i=0;i<getDataRateArray().length;i++)
            {
                if(dualInfo)
                {
                    if(i < nextInfoIndex)
                    {
                        temp[i] = infoString.replace("{value}", String.valueOf(DataRateArray[i]));
                    }
                    else
                    {
                        temp[i] = infoString2.replace("{value}",String.valueOf(DataRateArray[i]));
                    }
                }
                else if(tripleInfo)
                {
                    if(i < nextInfoIndex)
                    {
                        temp[i] = infoString.replace("{value}", String.valueOf(DataRateArray[i]));
                    }
                    else if(i<nextInfoIndex2)
                    {
                        temp[i] = infoString2.replace("{value}",String.valueOf(DataRateArray[i]));
                    }
                    else
                    {
                        temp[i] = infoString3.replace("{value}",String.valueOf(DataRateArray[i]));
                    }
                }
                else
                {
                    temp[i] = infoString.replace("{value}", String.valueOf(DataRateArray[i]));
                }

            }
            return temp;
        }
    }

    public int[] getDataRateArray() {
        if(Limit == NO_VALUE)
            return DataRateArray;
        else
        {
            for(int i = 0; i<DataRateArray.length;i++)
            {
                if(DataRateArray[i]>Limit)
                {
                    limitIndex = i;
                    break;
                }
            }
            if(limitIndex!=NO_VALUE)
            {
                return getSliceOfArray(DataRateArray,0,limitIndex);
            }
            else
                return DataRateArray;
        }
    }

    private int[] getSliceOfArray(int[] arr,
                                        int start, int end)
    {

        // Get the slice of the Array
        int[] slice = new int[end - start];

        // Copy elements of arr to slice
        for (int i = 0; i < slice.length; i++) {
            slice[i] = arr[start + i];
        }

        // return the slice
        return slice;
    }

    public MeasurementInterval Copy()
    {
        MeasurementInterval temp;

        if(dualInfo == false && tripleInfo == false)
        {
            temp = new MeasurementInterval(this.sensor,DataRateArray,Limit,infoString);
        }
        else if(dualInfo)
        {
            temp = new MeasurementInterval(this.sensor,DataRateArray,Limit,infoString,nextInfoIndex,infoString2);
        }
        else
        {
            temp = new MeasurementInterval(this.sensor,DataRateArray,Limit,infoString,nextInfoIndex,infoString2,nextInfoIndex2,infoString3);
        }

        temp.Select(this.selectedIndex);
        temp.Enabled = this.Enabled;
        return temp;
    }
}
