package com.example.accelerometerapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;


public class DataBase extends AppCompatActivity  {

    private DatabaseHelper myDB;
    private Button btnview_data;
    private Button back;
    private BarChart barChart;
    private SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_base);
        myDB = new DatabaseHelper(this);

        back= (Button) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_new_activity();
            }
        });
        btnview_data = (Button) findViewById(R.id.view_data);
        view_data_DB();

        int nr_walking = nr_aparitii("walking");
        int nr_jogging = nr_aparitii("jogging");
        int nr_sitting = nr_aparitii("sitting");
        int nr_standing = nr_aparitii("standing");

        BarChart barChart = (BarChart)findViewById(R.id.graph2);

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(nr_sitting,0));
        entries.add(new BarEntry(nr_standing,1));
        entries.add(new BarEntry(nr_walking,2));
        entries.add(new BarEntry(nr_jogging,3));

        BarDataSet barDataSet = new BarDataSet(entries,"Activities");

        ArrayList<String> labels = new ArrayList<>();
        labels.add("sitting");
        labels.add("standing");
        labels.add("walking");
        labels.add("jogging");

        BarData theData = new BarData(labels,barDataSet);

        barChart.setData(theData);
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);

    }

    public void view_data_DB(){
        btnview_data.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor res = myDB.getAllData();
                        if (res.getCount() == 0){
                            //show message
                            showMessage("Error", "No activity");
                            return;
                        }
                        StringBuffer buffer = new StringBuffer();
                        while (res.moveToNext()){
                            buffer.append("Id: "+res.getString(0)+"\n");
                            buffer.append("Activity: "+res.getString(1)+"\n");
                            buffer.append("Date and hour: "+res.getString(2)+"\n");
                            buffer.append("X: "+res.getString(3)+"\n");
                            buffer.append("Y: "+res.getString(4)+"\n");
                            buffer.append("Z: "+res.getString(5)+"\n");
                        }

                        showMessage("Database",buffer.toString()); //show al data
                    }
                }
        );
    }

    public  void showMessage(String title,String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }
    public void open_new_activity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public int nr_aparitii(String act){
        int nr = 0;
        Cursor res = myDB.getAllData();
        while(res.moveToNext()){
            if (res.getString(1).equals(act) )
                nr++;
        }
        return nr;
    }
}
