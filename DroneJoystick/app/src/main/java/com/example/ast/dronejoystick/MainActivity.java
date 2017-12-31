package com.example.ast.dronejoystick;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.example.virtualjoystick.JoystickView;


public class MainActivity extends AppCompatActivity {

    // For logger
    private final String TAG = "MainActivity";

    // UI controls
    private TextView mTextViewAngleLeft;
    private TextView mTextViewStrengthLeft;

    private TextView mTextViewAngleRight;
    private TextView mTextViewStrengthRight;

    private TextView throttleText;
    private TextView pitchText;
    private TextView rollText;

    private Switch mArm;
    private Switch mAux;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        throttleText = findViewById(R.id.throttleText);
        pitchText = findViewById(R.id.pitchText);
        rollText = findViewById(R.id.rollText);

        final ControlProcessor controlProcessor = new ControlProcessor(getApplicationContext(), throttleText, pitchText, rollText);

        // -----------------------------------------------------------------------------------------
        // START - Throttle controls
        mTextViewAngleLeft = (TextView) findViewById(R.id.textView_angle_left);
        mTextViewStrengthLeft = (TextView) findViewById(R.id.textView_strength_left);

        final JoystickView joystickLeft = (JoystickView) findViewById(R.id.joystickView_left);
        joystickLeft.setEnabled(false);
        joystickLeft.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                mTextViewAngleLeft.setText(angle + "°");
                mTextViewStrengthLeft.setText(strength + "%");
                controlProcessor.processThrottle(strength, angle);
            }
        },30);
        // END - Throttle controls

        // -----------------------------------------------------------------------------------------

        // START - Pitch & Roll controls
        mTextViewAngleRight = (TextView) findViewById(R.id.textView_angle_right);
        mTextViewStrengthRight = (TextView) findViewById(R.id.textView_strength_right);

        final JoystickView joystickRight = (JoystickView) findViewById(R.id.joystickView_right);
        joystickRight.setEnabled(false);
        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                mTextViewAngleRight.setText(angle + "°");
                mTextViewStrengthRight.setText(strength + "%");

                // In future might change this to separate things
                controlProcessor.processPitchAndRoll(strength, Math.toRadians(angle));
            }
        },30);

        joystickRight.setOnTouchReleasedListener(new JoystickView.OnTouchReleasedListener() {
            @Override
            public void onTouchRelease() {
                controlProcessor.processPitchAndRoll(0, 0);
            }
        });

        // END - Pitch & Roll controls

        // -----------------------------------------------------------------------------------------

        // START - Arm control
        mArm = (Switch) findViewById(R.id.armSwitch);
        mArm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                controlProcessor.processArm(b);

                // TODO :: Only if arm succeeds then joystick should be enabled
                if (b){
                    joystickLeft.setEnabled(true);
                    joystickRight.setEnabled(true);
                } else {
                    joystickLeft.setEnabled(false);
                    joystickRight.setEnabled(false);
                }
            }
        });
        // END - Arm control

        // -----------------------------------------------------------------------------------------

        // START - Aux control
        mAux = (Switch) findViewById(R.id.auxSwitch);
        mAux.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                controlProcessor.processAux(b);
            }
        });
        // END - Aux control
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final ControlProcessor controlProcessor = new ControlProcessor(getApplicationContext(), throttleText, pitchText, rollText);

        switch (item.getItemId()){
            case R.id.forceStop:
                controlProcessor.ultraForceStop();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
