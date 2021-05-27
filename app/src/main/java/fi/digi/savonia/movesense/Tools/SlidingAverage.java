package fi.digi.savonia.movesense.Tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Helppokäyttöinen wrapper liukuvan keskiarvon laskemiseen
 */
public class SlidingAverage {

    /**
     * Näytteet ovat listassa
     */
    List<Float> buffer = new ArrayList<>();
    /**
     * Liukuvan keskiarvon laskennassa huomioon otettava datapisteiden määrä
     */
    int bufferSize = 50;


    /**
     * Liukuvan keskiarvon asetusten määrittely
     * @param bufferSize Liukuvan keskiarvon pituus
     */
    public SlidingAverage(int bufferSize)
    {
        this.bufferSize = bufferSize;
    }

    /**
     * Näytteen lisäys listaan.
     * @param value Listaan lisättävä arvo
     */
    public synchronized void Add(float value)
    {
        if(!Float.isNaN(value))
        {
            buffer.add(value);
            int over = buffer.size()-bufferSize;
            while(over>0)
            {
                buffer.remove(0);
                over--;
            }
        }
    }

    /**
     * Liukuvan keskiarvon laskenta
     * @return Laskettu liukuva keskiarvo
     */
    public synchronized float GetAverage()
    {
        float total = 0;
        for(int i=0;i<buffer.size();i++)
        {
            total+=buffer.get(i);
        }

        return total/(float)buffer.size();
    }
}
