package com.example.tp_2_m3_2c_2024;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private MqttHandler mqttHandler;

    private TextView txtJson;
    private TextView txtModo;
    private Button cmdModo;

    public IntentFilter filterReceive;
    public IntentFilter filterConncetionLost;
    private ReceptorOperacion receiver = new ReceptorOperacion();
    private ConnectionLost connectionLost = new ConnectionLost();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.mytoolbar);
        Switch switchModo = findViewById(R.id.swt_modo);

        mqttHandler = new MqttHandler(getApplicationContext());

        connect();

        configurarBroadcastReciever();

        switchModo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // on below line we are checking
                // if switch is checked or not.
                if (isChecked) {
                    publishMessage(MqttHandler.TOPIC_MOVIMIENTO, "Escuchando full");
                } else {
                    publishMessage(MqttHandler.TOPIC_MOVIMIENTO, "Sonido desactivado");
                }
            }
        });

        setSupportActionBar(toolbar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity1:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.activity2:
                startActivity(new Intent(this, FuncActivity.class));
                break;
            default:

        }

        return super.onOptionsItemSelected(item);
    }


    private void connect() {
        mqttHandler.connect(mqttHandler.BROKER_URL, mqttHandler.CLIENT_ID, mqttHandler.USER, mqttHandler.PASS);


        try {

            Thread.sleep(1000);
            subscribeToTopic(MqttHandler.TOPIC_MOVIMIENTO);
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
    protected void onDestroy() {
        mqttHandler.disconnect();
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void publishMessage(String topic, String message) {
        Toast.makeText(this, "Publishing message: " + message, Toast.LENGTH_SHORT).show();
        mqttHandler.publish(topic, message);
    }

    private void subscribeToTopic(String topic) {
        Toast.makeText(this, "Subscribing to topic " + topic, Toast.LENGTH_SHORT).show();
        mqttHandler.subscribe(topic);
    }


    public class ConnectionLost extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {

            Toast.makeText(getApplicationContext(), "Conexion Perdida", Toast.LENGTH_SHORT).show();

            connect();

        }

    }

}


public class ReceptorOperacion extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        String msgJson = intent.getStringExtra("msgJson");
        txtJson.setText(msgJson);

        try {
            JSONObject jsonObject = new JSONObject(msgJson);
            String value = jsonObject.getString("value");
            txtTemp.setText(value+"°");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


}
