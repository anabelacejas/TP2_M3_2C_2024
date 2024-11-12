package com.example.tp_2_m3_2c_2024;

import static com.example.tp_2_m3_2c_2024.MqttHandler.BROKER_URL;
import static com.example.tp_2_m3_2c_2024.MqttHandler.CLIENT_ID;
import static com.example.tp_2_m3_2c_2024.MqttHandler.PASS;
import static com.example.tp_2_m3_2c_2024.MqttHandler.TOPIC_MODO;
import static com.example.tp_2_m3_2c_2024.MqttHandler.TOPIC_MOVIMIENTO;
import static com.example.tp_2_m3_2c_2024.MqttHandler.USER;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;

import android.view.View;

import android.widget.Button;

import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {

    private MqttHandler mqttHandler;
    public IntentFilter filterReceive;
    public IntentFilter filterConncetionLost;
    private final ReceptorOperacion receiver = new ReceptorOperacion();
    private final ConnectionLost connectionLost = new ConnectionLost();
    public static final String CAMBIO_MODO = "CAMBIO_MODO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_func);
        Toolbar toolbar = findViewById(R.id.mytoolbar);
        ImageButton btnSetting = findViewById(R.id.btnSetting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mqttHandler.disconnect();
                startActivity(new Intent(MainActivity.this, FuncActivity.class));
            }
        });

        Button btnMode = findViewById(R.id.btnModo);
        btnMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishMessage();
            }
        });

        mqttHandler = new MqttHandler(getApplicationContext());

        connect();

        configurarBroadcastReciever();

        setSupportActionBar(toolbar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.func), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void connect() {
        mqttHandler.connect(BROKER_URL, CLIENT_ID, USER, PASS);
        try {
            Thread.sleep(1000);
            subscribeToTopic();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void configurarBroadcastReciever() {
        filterReceive = new IntentFilter(MqttHandler.ACTION_DATA_RECEIVE);
        filterConncetionLost = new IntentFilter(MqttHandler.ACTION_CONNECTION_LOST);

        filterReceive.addCategory(Intent.CATEGORY_DEFAULT);
        filterConncetionLost.addCategory(Intent.CATEGORY_DEFAULT);

        registerReceiver(receiver, filterReceive);
        registerReceiver(connectionLost, filterConncetionLost);

    }

    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter) {
        if (Build.VERSION.SDK_INT >= 34 && getApplicationInfo().targetSdkVersion >= 34) {
            return super.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            return super.registerReceiver(receiver, filter);
        }
    }

    @Override
    protected void onDestroy() {
        mqttHandler.disconnect();
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void publishMessage() {
        Toast.makeText(this, "Publishing message: " + CAMBIO_MODO, Toast.LENGTH_SHORT).show();
        mqttHandler.publish(TOPIC_MODO, CAMBIO_MODO);
    }

    private void subscribeToTopic() {
        Toast.makeText(this, "Subscribing to topic " + TOPIC_MOVIMIENTO, Toast.LENGTH_SHORT).show();
        mqttHandler.subscribe(TOPIC_MOVIMIENTO);
    }


    public class ConnectionLost extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), "Conexion Perdida", Toast.LENGTH_SHORT).show();
            connect();
        }
    }

    public class ReceptorOperacion extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            TextView txtLcd = findViewById(R.id.txtDisplay);
            String msgMov = intent.getStringExtra("msgMov");
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            txtLcd.setText(msgMov);
        }
    }
}



