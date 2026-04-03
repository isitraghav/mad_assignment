package com.raghav.sensorapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor light;
    private Sensor proximity;

    private TextView Accelerometertext;
    private TextView Lighttext;
    private TextView Proximitytext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Accelerometertext = findViewById(R.id.tvAccelerometer);
        Lighttext = findViewById(R.id.tvLight);
        Proximitytext = findViewById(R.id.tvProximity);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (accelerometer == null) Accelerometertext.setText("no support");
        if (light == null) Lighttext.setText("no suppport");
        if (proximity == null) Proximitytext.setText("no support");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        if (light != null) sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_UI);
        if (proximity != null) sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                Accelerometertext.setText(String.format("X: %.2f m/s²\nY: %.2f m/s²\nZ: %.2f m/s²",
                        event.values[0], event.values[1], event.values[2]));
                break;
            case Sensor.TYPE_LIGHT:
                Lighttext.setText(String.format("%.1f lux", event.values[0]));
                break;
            case Sensor.TYPE_PROXIMITY:
                Proximitytext.setText(String.format("%.1f cm", event.values[0]));
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
