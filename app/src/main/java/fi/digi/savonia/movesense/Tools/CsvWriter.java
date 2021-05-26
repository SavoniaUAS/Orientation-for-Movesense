package fi.digi.savonia.movesense.Tools;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import fi.digi.savonia.movesense.Models.CsvDataModel;

/**
 * Aputyökalu Csv-tiedoston kirjoittamiseen.
 */
public class CsvWriter {

    private String filePath;
    private List<CsvDataModel> buffer = new ArrayList<>();
    Context context;

    public CsvWriter(Context context, String path)
    {
        this.filePath = path;
    }

    public boolean Save()
    {
        File file;
        if(isExternalStorageWritable())
        {
            try {
                String fileName = filePath+System.currentTimeMillis()+".csv";
                file = new File(context.getExternalFilesDir("DataLogs"),fileName);

                FileOutputStream fOut = new FileOutputStream(file);

                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fOut));
                bufferedWriter.write(CsvDataModel.GetHeaders());
                bufferedWriter.newLine();

                for(int i=0;i<buffer.size();i++)
                {
                    CsvDataModel dataPoint = buffer.get(i);
                    float penAngle = dataPoint.PenAngle;
                    float vibration = dataPoint.Vibration;
                    bufferedWriter.write(penAngle+";"+vibration);
                    bufferedWriter.newLine();
                }
                bufferedWriter.close();
                fOut.close();

                return true;

            }
            catch (Exception e)
            {
                Log.d("CsvWriter",e.getLocalizedMessage());
            }
        }
        return false;
    }

    public void Add(CsvDataModel csvData)
    {
        buffer.add(csvData);
    }

    public void Clear()
    {
        buffer.clear();
    }

    private boolean isExternalStorageWritable() {

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
