package com.creativeapps.schoolbusdriver.ui.activity.main.settings;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.fragment.app.Fragment;

import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.data.Util;
import com.creativeapps.schoolbusdriver.data.network.models.Driver;
import com.creativeapps.schoolbusdriver.data.network.models.Parent;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivity;
import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivityModel;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class SettingsFragment extends Fragment implements
        View.OnClickListener, AdapterView.OnItemSelectedListener {
    private MainActivityModel mViewModel;
    private Driver mDriver;
    Button btnTimePicker;
    TextView txtTime;
    private ArrayList<String>  items;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ((MainActivity) getActivity()).createViewModel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mDriver = Util.getSavedObjectFromPreference(getContext(),
                "mPreference", "Driver", com.creativeapps.schoolbusdriver.data.network.models.Driver.class);

        if(mDriver!=null) {
            mViewModel.getDriverServer(mDriver.getCountry_code(), mDriver.getTel_number(), mDriver.getSecretKey());
        }
        else
        {
            ((MainActivity) getActivity()).logout();
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        btnTimePicker = (Button) view.findViewById(R.id.btn_time);
        txtTime = (TextView) view.findViewById(R.id.in_time);
        btnTimePicker.setOnClickListener(this);

        String afternoon_time = Util.getSavedObjectFromPreference(getContext(),
                "mPreference", "afternoon_time", String.class);
        if (afternoon_time != null) {
            txtTime.setText(afternoon_time);
        }

        //get the spinner from the xml.
        Spinner dropdown = view.findViewById(R.id.spinner_last_afternoon);
        //create a list of items for the spinner.
        items = new ArrayList();
        items.add("School");
        for (Parent parent : mDriver.getParents()) {
            if(parent.getAddress_latitude() != null && parent.getAddress_longitude() != null) {
                items.add(parent.getName() + ":" + parent.getTel_number());
            }
        }
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(),
                android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);

        String last_afternoon_parent = Util.getSavedObjectFromPreference(getActivity().getApplicationContext(),
                "mPreference", "last_afternoon_parent", String.class);
        if(last_afternoon_parent != null)
        {
            int spinnerPosition = adapter.getPosition(last_afternoon_parent);
            dropdown.setSelection(spinnerPosition);
        }
    }
    void displayPicker() {
        //display the time picker
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this.getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay,
                                          int minute) {
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minute);
                        c.set(Calendar.SECOND, 0);
                        c.set(Calendar.MILLISECOND, 0);
                        String afternoon_time = getTime(c.getTime());
                        txtTime.setText(afternoon_time);
                        //save the afternoon time
                        Util.saveObjectToSharedPreference(getContext(), "mPreference",
                                "afternoon_time", afternoon_time);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    private String getTime(Date time) {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        return format.format(time);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_time) {
            displayPicker();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        String last_afternoon_parent = (String) adapterView.getItemAtPosition(position);
        if(position != 0)
        {
            Util.saveObjectToSharedPreference(getContext(), "mPreference",
                    "last_afternoon_parent", last_afternoon_parent);
        }
        else
        {
            Util.saveObjectToSharedPreference(getContext(), "mPreference",
                    "last_afternoon_parent", "School");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
