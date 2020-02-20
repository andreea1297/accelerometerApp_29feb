package com.example.accelerometerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private static List<Float> x ;
    private static List<Float> y;
    private static List<Float> z;
    private Button deschide_pagina;
    private int nr = 10;
    private SensorManager SM;
    private Sensor mySensor;

    private DatabaseHelper myDB;
    private Date timp;
    private String timp_str;

    private TextView textView_rezultat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDB = new DatabaseHelper(this);

        timp = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        timp_str = dateFormat.format(timp);

        x = new ArrayList<Float>();
        y = new ArrayList<Float>();
        z = new ArrayList<Float>();

        //citesc datele de la accelerometru

        SM = (SensorManager) getSystemService(SENSOR_SERVICE);
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SM.registerListener(this,mySensor,SensorManager.SENSOR_DELAY_NORMAL); //3 secunde

        setContentView(R.layout.activity_main);

        deschide_pagina = (Button) findViewById(R.id.statistici);
        deschide_pagina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_new_activity();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //adaug datele accelerometrului intr-o lista

        TextView xText = (TextView) findViewById(R.id.xText);
        TextView yText = (TextView) findViewById(R.id.yText);
        TextView zText = (TextView) findViewById(R.id.zText);

        // afisez nr cu 2 zecimale
        DecimalFormat df = new DecimalFormat("0.00");

        xText.setText("X: "+df.format(event.values[0]) );
        yText.setText("Y: "+df.format(event.values[1]));
        zText.setText("Z: "+df.format(event.values[2]));


        float _x = event.values[0];
        float _y = event.values[1];
        float _z = event.values[2];

        if(x.size() == 0 && y.size() == 0 && z.size() == 0) {
            x.add(_x);
            y.add(_y);
            z.add(_z);

       // Log.e( "Z get.size()",String.valueOf(x.size()));
        }
        else {
            int index_x = x.size()-1;
            int index_y = y.size()-1;
            int index_z = z.size()-1;

            if (Math.abs(x.get(index_x)-_x) >= 0.001 && Math.abs(y.get(index_y)-_y) >= 0.001 && Math.abs(z.get(index_z)-_y) >= 0.001){
                x.add(_x);
                y.add(_y);
                z.add(_z);}

           // Log.e( "X ",String.valueOf(Math.abs(x.get(index_x)-_x)));
         //   Log.e( "Y ",String.valueOf(Math.abs(y.get(index_y)-_y)));
          //      Log.e( "Z ",String.valueOf(Math.abs(z.get(index_z)-_z)));
       }

      //  x.add(_x);
      //  y.add(_y);
      //  z.add(_z);

        prediction();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    protected void onPause(){
        super.onPause();
        SM.unregisterListener(this);
    }

    protected void onResume(){
        super.onResume();
        SM.registerListener(this,mySensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    //fac predictiile
    private void prediction() {


        ArrayList<Float> data_all = new ArrayList<>();

        if (x.size() == nr && y.size() == nr && z.size() == nr) {

            float maxX = max_function(x);
            float maxY = max_function(y);
            float maxZ = max_function(z);

            //creez o lista cu 3 elemente
            data_all.add(max_function(x));
            data_all.add(max_function(y));
            data_all.add(max_function(z));

            x.clear();
            y.clear();
            z.clear();

            Module network = null;

            try {
                //loading the module
                network = Module.load(assetFilePath(
                        this,
                        "model_v7_2.pt"));


            } catch (IOException e) {
                Log.e("PytorchError", "Error reading assets", e);
                finish();
            }

            Tensor input = Tensor.fromBlob(new float[]{maxX,maxY,maxZ},new long[]{1,3});
            IValue output = network.forward(IValue.from(input));
            float[] scores = output.toTensor().getDataAsFloatArray();

            //calling the forward method of the model to run our input
            //network.forward(IValue.listFrom(mediaX,mediaY,mediaZ));
//            final float[] rezultat = output.getDataAsFloatArray();

            //indexul cu valoarea cea mai mare
            float max_score = -Float.MAX_VALUE;
            int ms_ix = -1;
            for (int i = 0; i < scores.length; i++) {

                if (scores[i] > max_score) {

                    max_score = scores[i];
                    ms_ix = i;
                }
            }

            String clasa_detectata = ModelClasses.MODEL_CLASSES[ms_ix];

            //afisare  clasa detectata-> predictie
            textView_rezultat = (TextView) findViewById(R.id.textView_rezultat);
            textView_rezultat.setText(clasa_detectata);

            boolean isInserted =  myDB.insertData(clasa_detectata,timp_str);

            if (isInserted == true)
                Toast.makeText(MainActivity.this,"Data inserted in DB!",Toast.LENGTH_LONG).show();
            else
                Toast.makeText(MainActivity.this,"Data NOT inserted in DB!",Toast.LENGTH_LONG).show();

        }
        }

    //determina val maxima de pe fiecare linie
    public float max_function (List < Float > list) {
        float max = -10000;
        for (int i = 0; i < list.size(); i++) {
          if (list.get(i) > max){
              max = list.get(i);
          }
        }

        if(list.size() == 0){max =0; return max;}
        else
        {
            return max;
        }
    }

    //calea fisierului .pt
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    public void open_new_activity(){
        Intent intent = new Intent(this, DataBase.class);
        startActivity(intent);

    }
}
