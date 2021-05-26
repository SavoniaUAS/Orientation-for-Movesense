package fi.digi.savonia.movesense.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import fi.digi.savonia.movesense.R;

/**
 * Fragmentti näyttää käyttäjälle värinän, Movesense-sensorin asennon kulmaeron painovoima vektoriin
 */
public class ProtoFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    /**
     * Käyttöliittymän tekstinäkymät
     */
    private TextView current_vibration,current_angle,min_vibration,min_angle,max_vibration,max_angle;
    /**
     * Käyttöliittymän näppäimet
     */
    private Button reset,calibrate,start_stop;
    /**
     * Käyttöliittymän muokattavat tekstikentät
     */
    private EditText edit_vibration_min,edit_angle_min,edit_vibration_max,edit_angle_max;
    /**
     * Käyttöliittymän kytkin komponentti
     */
    private Switch sliding_average;
    /**
     * Apumuuttuja Analyysin tilaan (Käynnistetty/Pysäytetty)
     */
    private boolean isEnabled = false;
    /**
     * Minimi ja maksimi arvojen taltiointi
     */
    float min_angle_value,max_angle_value,min_vibration_value,max_vibration_value;
    /**
     * Viimeisin kulmaeron arvo
     */
    float last_degree_value =0;

    /**
     * Indikaattori värin tila muuttuja kulmaerolle.
     */
    IndicatorStatus indicatorStatus_angle;
    /**
     * Indikaattori värin tila muuttuja minimi kulmaerolle.
     */
    IndicatorStatus indicatorStatus_angle_min;
    /**
     * Indikaattori värin tila muuttuja maksimi kulmaerolle.
     */
    IndicatorStatus indicatorStatus_angle_max;
    /**
     * Indikaattori värin tila muuttuja värinälle.
     */
    IndicatorStatus indicatorStatus_vibration;
    /**
     * Indikaattori värin tila muuttuja minimi värinälle.
     */
    IndicatorStatus indicatorStatus_vibration_min;
    /**
     * Indikaattori värin tila muuttuja maksimi värinälle.
     */
    IndicatorStatus indicatorStatus_vibration_max;


    /**
     * Huomiovärin valinta. Väritön, Vihreä, keltainen ja punainen ovat vaihtoehtoja.
     */
    public enum IndicatorStatus {
        Green,
        Yellow,
        Red,
        None
    }

    /**
     * Luokan ilmoitusten kuuntelija
     */
    private OnFragmentInteractionListener mListener;

    /**
     * Asettaa muuttuville alkutilan
     */
    public ProtoFragment() {
        // Required empty public constructor
        min_vibration_value = min_angle_value = max_angle_value = max_vibration_value = Float.MAX_VALUE;
        indicatorStatus_angle_min = indicatorStatus_angle_max = indicatorStatus_angle = indicatorStatus_vibration_min = indicatorStatus_vibration_max = indicatorStatus_vibration = IndicatorStatus.None;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_proto, container, false);

        InitViews(view);
        mListener.onReadyProto();
        return view;
    }

    /**
     * Hakee UI:n komponentit muuttujiksi näkymästä
     * @param view Näkymä
     */
    private void InitViews(View view) {
        current_vibration = view.findViewById(R.id.proto_vibration_value);
        current_angle = view.findViewById(R.id.proto_angle_value);
        min_vibration = view.findViewById(R.id.proto_min_vibration_value);
        min_angle = view.findViewById(R.id.proto_min_angle_value);
        max_vibration = view.findViewById(R.id.proto_max_vibration_value);
        max_angle = view.findViewById(R.id.proto_max_angle_value);

        edit_vibration_min = view.findViewById(R.id.proto_min_edit_vibration);
        edit_vibration_max = view.findViewById(R.id.proto_max_edit_vibration);
        edit_angle_min = view.findViewById(R.id.proto_min_edit_angle);
        edit_angle_max = view.findViewById(R.id.proto_max_edit_angle);

        reset = view.findViewById(R.id.proto_button_reset);
        calibrate = view.findViewById(R.id.proto_button_calibration);
        start_stop = view.findViewById(R.id.proto_button_star_stop);
        sliding_average = view.findViewById(R.id.proto_switch_average);
        sliding_average.setOnCheckedChangeListener(this);

        reset.setOnClickListener(this);
        calibrate.setOnClickListener(this);
        start_stop.setOnClickListener(this);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ProtoFragment.OnFragmentInteractionListener) {
            mListener = (ProtoFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Asettaa tärähtelyn arvon käyttöliittymään
     * @param vibration Tärähtenyt arvo
     */
    public void SetVibrationIntensity(float vibration)
    {
        if(vibration<min_vibration_value || min_vibration_value == Float.MAX_VALUE)
        {
            min_vibration_value = vibration;
            min_vibration.setText(String.format("%.1f",vibration));
        }
        if(vibration>max_vibration_value || max_vibration_value == Float.MAX_VALUE)
        {
            max_vibration_value = vibration;
            max_vibration.setText(String.format("%.1f",vibration));
        }

        current_vibration.setText(String.format("%.1f",vibration));

        String text_vibration_min = edit_vibration_min.getText().toString();
        String text_vibration_max = edit_vibration_max.getText().toString();

        if(!text_vibration_max.isEmpty() || !text_vibration_min.isEmpty())
        {
            Float VibrationYellowWarningFloat = Float.valueOf(text_vibration_min);
            Float VibrationRedWarningFloat = Float.valueOf(text_vibration_max);

            if(VibrationRedWarningFloat<vibration)
            {
                if(indicatorStatus_vibration != IndicatorStatus.Red)
                {
                    current_vibration.setBackgroundResource(R.drawable.indicator_red);
                    indicatorStatus_vibration = IndicatorStatus.Red;
                }
            }
            else if(VibrationYellowWarningFloat<vibration)
            {
                if(indicatorStatus_vibration != IndicatorStatus.Yellow)
                {
                    current_vibration.setBackgroundResource(R.drawable.indicator_yellow);
                    indicatorStatus_vibration = IndicatorStatus.Yellow;
                }
            }
            else
            {
                if(indicatorStatus_vibration != IndicatorStatus.Green)
                {
                    current_vibration.setBackgroundResource(R.drawable.indicator_green);
                    indicatorStatus_vibration = IndicatorStatus.Green;
                }
            }

            if(VibrationRedWarningFloat<min_vibration_value)
            {
                if(indicatorStatus_vibration_min != IndicatorStatus.Red)
                {
                    min_vibration.setBackgroundResource(R.drawable.indicator_red);
                    indicatorStatus_vibration_min = IndicatorStatus.Red;
                }
            }
            else if(VibrationYellowWarningFloat<min_vibration_value)
            {
                if(indicatorStatus_vibration_min != IndicatorStatus.Yellow)
                {
                    min_vibration.setBackgroundResource(R.drawable.indicator_yellow);
                    indicatorStatus_vibration_min = IndicatorStatus.Yellow;
                }
            }
            else
            {
                if(indicatorStatus_vibration_min != IndicatorStatus.Green)
                {
                    min_vibration.setBackgroundResource(R.drawable.indicator_green);
                    indicatorStatus_vibration_min = IndicatorStatus.Green;
                }
            }

            if(VibrationRedWarningFloat<max_vibration_value)
            {
                if(indicatorStatus_vibration_max != IndicatorStatus.Red)
                {
                    max_vibration.setBackgroundResource(R.drawable.indicator_red);
                    indicatorStatus_vibration_max = IndicatorStatus.Red;
                }
            }
            else if(VibrationYellowWarningFloat<max_vibration_value)
            {
                if(indicatorStatus_vibration_max != IndicatorStatus.Yellow)
                {
                    max_vibration.setBackgroundResource(R.drawable.indicator_yellow);
                    indicatorStatus_vibration_max = IndicatorStatus.Yellow;
                }
            }
            else
            {
                if(indicatorStatus_vibration_max != IndicatorStatus.Green)
                {
                    max_vibration.setBackgroundResource(R.drawable.indicator_green);
                    indicatorStatus_vibration_max = IndicatorStatus.Green;
                }
            }
        }


    }

    /**
     * Asettaa mitatun kulmaeron käyttöliittymään.
     * @param degrees Mitattu kulmaero verrattuna vertailu vektoriin
     */
    public void SetPenAngle(float degrees)
    {
        last_degree_value = degrees;
        degrees = Math.abs(degrees);
        if(degrees<min_angle_value || min_angle_value == Float.MAX_VALUE)
        {
            min_angle_value = degrees;
            min_angle.setText(String.format("%.1f",degrees));
        }
        if(degrees>max_angle_value || max_angle_value == Float.MAX_VALUE)
        {
            max_angle_value = degrees;
            max_angle.setText(String.format("%.1f",degrees));
        }

        current_angle.setText(String.format("%.1f",degrees));

        String text_angle_min = edit_angle_min.getText().toString();
        String text_angle_max = edit_angle_max.getText().toString();

        if(!text_angle_max.isEmpty() || !text_angle_min.isEmpty())
        {
            Float AngleYellowWarningFloat = Float.valueOf(text_angle_min);
            Float AngleRedWarningFloat = Float.valueOf(text_angle_max);

            if(AngleRedWarningFloat<degrees)
            {
                if(indicatorStatus_angle != IndicatorStatus.Red)
                {
                    current_angle.setBackgroundResource(R.drawable.indicator_red);
                    indicatorStatus_angle = IndicatorStatus.Red;
                }
            }
            else if(AngleYellowWarningFloat<degrees)
            {
                if(indicatorStatus_angle != IndicatorStatus.Yellow)
                {
                    current_angle.setBackgroundResource(R.drawable.indicator_yellow);
                    indicatorStatus_angle = IndicatorStatus.Yellow;
                }
            }
            else
            {
                if(indicatorStatus_angle != IndicatorStatus.Green)
                {
                    current_angle.setBackgroundResource(R.drawable.indicator_green);
                    indicatorStatus_angle = IndicatorStatus.Green;
                }
            }



            if(AngleRedWarningFloat<max_angle_value)
            {
                if(indicatorStatus_angle_max != IndicatorStatus.Red)
                {
                    max_angle.setBackgroundResource(R.drawable.indicator_red);
                    indicatorStatus_angle_max = IndicatorStatus.Red;
                }
            }
            else if(AngleYellowWarningFloat<max_angle_value)
            {
                if(indicatorStatus_angle_max != IndicatorStatus.Yellow)
                {
                    max_angle.setBackgroundResource(R.drawable.indicator_yellow);
                    indicatorStatus_angle_max = IndicatorStatus.Yellow;
                }
            }
            else
            {
                if(indicatorStatus_angle_max != IndicatorStatus.Green)
                {
                    max_angle.setBackgroundResource(R.drawable.indicator_green);
                    indicatorStatus_angle_max = IndicatorStatus.Green;
                }
            }
        }


    }

    /**
     * Resetoi käyttöliittymän kulmaeron ja tärinän arvot
     */
    private void ResetMinMaxValues(){
        min_vibration_value = min_angle_value = max_angle_value = max_vibration_value = Float.MAX_VALUE;
        min_angle.setText("");
        min_vibration.setText("");
        max_angle.setText("");
        max_vibration.setText("");
    }

    /**
     * Liukuvan keskiarvon aktivointi/deaktivointi
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mListener.onSlidingAverage(isChecked);
    }

    /**
     * Näppäintä painettu! Tarkistaa mikä näppäin
     * @param v
     */
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.proto_button_star_stop)
        {
            isEnabled = !isEnabled;
            if(isEnabled)
            {
                start_stop.setText(R.string.proto_stop_promp);
                edit_angle_min.setEnabled(false);
                edit_angle_max.setEnabled(false);
                edit_vibration_min.setEnabled(false);
                edit_vibration_max.setEnabled(false);
                mListener.onStartProto();
            }
            else
            {
                start_stop.setText(R.string.proto_start_promp);
                edit_angle_min.setEnabled(true);
                edit_angle_max.setEnabled(true);
                edit_vibration_min.setEnabled(true);
                edit_vibration_max.setEnabled(true);
                mListener.onStopProto();
            }
        }
        else if(v.getId()==R.id.proto_button_calibration)
        {
            //angle_calibration_correction = last_raw_degree_value;
            mListener.onCalibrationProto();
        }
        else if(v.getId()==R.id.proto_button_reset)
        {
            ResetMinMaxValues();
            mListener.onResetProto();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void onReadyProto();
        void onStartProto();
        void onStopProto();
        void onSlidingAverage(boolean enabled);
        void onCalibrationProto();
        void onResetProto();
    }
}