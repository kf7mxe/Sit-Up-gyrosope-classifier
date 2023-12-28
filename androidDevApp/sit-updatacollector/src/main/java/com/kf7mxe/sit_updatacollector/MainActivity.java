package com.kf7mxe.sit_updatacollector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public SensorManager manager;
    private Button didSitUp;
    private TextView testTextView;
    private Sensor mGyroscope;
    private String test;
    private String axisXString;
    private String axisYString;
    private String axisZString;

    private Switch positiveNegativeSwitch;

    private boolean record;
    private boolean dataUpdated;
    private boolean savePositive;


    private Vibrator vibrator;


    private Long lastTimeStamp;
    private Instant instant;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 1; // 1 miliseconds

    private ArrayList<String> singleSitUpData;
    private ArrayList<ArrayList> allSitUpData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        vibrator = (Vibrator) getSystemService(this.VIBRATOR_SERVICE ) ;

        positiveNegativeSwitch = findViewById(R.id.positiveNegativeSwitch);

        if(positiveNegativeSwitch.isChecked()){
            savePositive = true;
        }else{
            savePositive = false;
        }

        positiveNegativeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(positiveNegativeSwitch.isChecked()){
                    savePositive = true;
                }else{
                    savePositive = false;
                }
            }
        });

        manager = (SensorManager)getSystemService(SENSOR_SERVICE);
        instant = Instant.now(); // Current moment in UTC.

        record = false;
        singleSitUpData = new ArrayList<>();
        allSitUpData = new ArrayList<>();
        didSitUp = (Button) findViewById(R.id.didSitUp);
        didSitUp.setText("Start");
        testTextView = (TextView) findViewById(R.id.testDataAccess);
        mGyroscope = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        //loadFile();

        manager.registerListener(gyroscopeListener,mGyroscope,SensorManager.SENSOR_DELAY_FASTEST);

//        manager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);

        didSitUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(record==false) {
                    record = true;
                    didSitUp.setText("Stop");
                    vibrate();
                } else {
                    record = false;
                    didSitUp.setText("Start");
                    vibrate();
                }
            }
        });
        // Enables Always-on
        //setAmbientEnabled();
    }

    protected void onResume(){
        super.onResume();
        manager.registerListener(gyroscopeListener, mGyroscope, SensorManager.SENSOR_DELAY_FASTEST );

    }

    public void vibrate(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES. O ) {
            vibrator.vibrate(VibrationEffect. createOneShot ( 50 ,
                    VibrationEffect. DEFAULT_AMPLITUDE )) ;
        } else {
            //deprecated in API 26
            vibrator.vibrate( 500 ) ;
        }
    }

    public void saveJsonVector(){
        String allSitUpDataString = allSitUpData.toString();
        String dir = "";
        if (savePositive){
            dir = "positive";
        } else {
            dir = "negative";
        }

        // create a folder with name positive

        File folder = new File(getFilesDir(), dir);
        if(!folder.exists()){
            folder.mkdirs();
        }
            File file = new File(folder,"sitUpData"+instant.toString()+".json");
        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(allSitUpDataString);
            bufferedWriter.close();
        } catch (IOException e){
            Log.println(Log.ERROR,"Error",e.toString());
            Toast.makeText(MainActivity.this,"error Saving file",Toast.LENGTH_SHORT);
        }
    }



    public void loadFile(){
        try {
            File file = new File(this.getFilesDir(), "sitUpData.json");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            String responce = stringBuilder.toString();
            JSONObject jsonObject  = new JSONObject(responce);
            int pause = 0;


        }
        catch (FileNotFoundException e){
            Toast.makeText(this,e.getLocalizedMessage(),Toast.LENGTH_LONG);
        }
        catch(IOException e){
            Toast.makeText(this,e.getLocalizedMessage(),Toast.LENGTH_LONG);
        }
        catch (JSONException e){
            Toast.makeText(this,e.getLocalizedMessage(),Toast.LENGTH_LONG);
        }
    }

    protected void onPause() {
        super.onPause();
        manager.unregisterListener(gyroscopeListener);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    public SensorEventListener gyroscopeListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // This time step's delta rotation to be multiplied by the current rotation
            // after computing it from the gyro sample data.
            //final float dT = (event.timestamp - timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            if(lastTimeStamp ==null){
                lastTimeStamp = event.timestamp;
            }

            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];
            axisXString = Float.toString(axisX);
            axisYString = Float.toString(axisY);
            axisZString = Float.toString(axisZ);
            Log.e("test",Boolean.toString(record));


            testTextView.setText(Integer.toString(allSitUpData.size()));

            if(record){
                JSONObject temp = new JSONObject();
                try {
                    temp.put("x",axisXString);
                    temp.put("y",axisYString);
                    temp.put("z",axisYString);
                } catch (Exception e){
                    Log.e("debug",e.toString());
                    Toast.makeText(MainActivity.this,"error with creating json object",Toast.LENGTH_SHORT);
                }
                singleSitUpData.add(temp.toString());
                dataUpdated = true;
            } else if(dataUpdated == true){
                //save
                ArrayList<String> copyOfSingleSitUpData = (ArrayList<String>) singleSitUpData.clone();
                allSitUpData.add(copyOfSingleSitUpData);
                singleSitUpData.clear();
                dataUpdated = false;
                saveJsonVector();
            } else{

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    if(record==false) {
                        record = true;
                        didSitUp.setText("Stop");
                        vibrate();
                    } else {
                        record = false;
                        didSitUp.setText("Start");
                        vibrate();
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }


}