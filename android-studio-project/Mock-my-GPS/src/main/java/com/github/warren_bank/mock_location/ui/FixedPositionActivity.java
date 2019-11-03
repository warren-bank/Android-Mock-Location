package com.github.warren_bank.mock_location.ui;

import com.github.warren_bank.mock_location.R;
import com.github.warren_bank.mock_location.data_model.LocPoint;
import com.github.warren_bank.mock_location.data_model.SharedPrefs;
import com.github.warren_bank.mock_location.service.LocationService;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FixedPositionActivity extends Activity {
    private LocPoint originalLoc;

    private TextView input_fixed_position;
    private Button   button_toggle_state;
    private Button   button_update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixed_position);

        originalLoc = SharedPrefs.getTripOrigin(FixedPositionActivity.this);

        input_fixed_position = (TextView) findViewById(R.id.input_fixed_position);
        button_toggle_state  = (Button)   findViewById(R.id.button_toggle_state);
        button_update        = (Button)   findViewById(R.id.button_update);

        reset();

        input_fixed_position.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (!LocationService.isStarted()) return;

                try {
                    String fixed_position = s.toString();
                    LocPoint modifiedLoc  = new LocPoint(fixed_position);

                    if (originalLoc.equals(modifiedLoc)) {
                        button_update.setVisibility(View.GONE);
                    }
                    else {
                        button_update.setVisibility(View.VISIBLE);
                    }
                }
                catch(Exception e) {}
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        button_toggle_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (LocationService.isStarted()) {
                        LocationService.doStop(FixedPositionActivity.this, true);
                        button_toggle_state.setText(R.string.label_button_start);
                    }
                    else {
                        doStart();
                        button_toggle_state.setText(R.string.label_button_stop);
                    }
                    button_update.setVisibility(View.GONE);
                }
                catch(Exception e) {}
            }
        });

        button_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (LocationService.isStarted()) {
                        doStart();
                    }
                    button_update.setVisibility(View.GONE);
                }
                catch(Exception e) {}
            }
        });
    }

    private void reset() {
        input_fixed_position.setText(originalLoc.toString());

        if (LocationService.isStarted())
            button_toggle_state.setText(R.string.label_button_stop);

        button_update.setVisibility(View.GONE);
    }

    private void doStart() {
        String fixed_position = input_fixed_position.getText().toString();
        LocPoint modifiedLoc  = new LocPoint(fixed_position);

        LocationService.doStart(FixedPositionActivity.this, true, modifiedLoc, null, 0);

        SharedPrefs.putTripOrigin(FixedPositionActivity.this, modifiedLoc);
        originalLoc = modifiedLoc;
    }
}
