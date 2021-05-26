package fi.digi.savonia.movesense.Tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polidea.rxandroidble2.RxBleDevice;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import fi.digi.savonia.movesense.R;

/**
 * Räätälöity lista-adapteri Bluetooth LE hakuun
 */
public class CustomDeviceListAdapter extends ArrayAdapter<RxBleDevice> {

    /**
     * Listan data listassa
     */
    private ArrayList<RxBleDevice> dataSet;
    /**
     * Sovelluksen konteksti
     */
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView Id;
        TextView mac;
    }

    public CustomDeviceListAdapter(@NonNull Context context, ArrayList<RxBleDevice> data) {
        super(context, R.layout.custom_list_view, data);
        this.dataSet = data;
        this.mContext = context;
    }

    private int lastPosition = -1;

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        RxBleDevice bleDevice = getItem(position);
        ViewHolder viewHolder;
        final View result;

        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.custom_list_view,parent,false);
            viewHolder.Id = convertView.findViewById(R.id.custom_list_view_id);
            viewHolder.mac = convertView.findViewById(R.id.custom_list_view_mac);

            result = convertView;

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        String id = bleDevice.getName().split(" ")[1];

        viewHolder.Id.setText(id);
        viewHolder.mac.setText(bleDevice.getMacAddress());

        return convertView;
    }
}
