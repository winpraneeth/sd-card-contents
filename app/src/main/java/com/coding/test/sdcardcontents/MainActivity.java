package com.coding.test.sdcardcontents;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Rescan mostRecent;

    private Button shareBtn;
    private TextView percView;
    private TextView avgView;

    private FileFragment fileFragment;
    private ExtentionFragment extentionFragment;
    private static final int MY_EXTERNAL_STORAGE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!isScannerActive()){startService(new Intent(this,ScanService.class));}

        Button startBtn = findViewById(R.id.button_start);
        startBtn.setOnClickListener(this);

        Button pauseButton = findViewById(R.id.button_pause);
        pauseButton.setOnClickListener(this);

        shareBtn = findViewById(R.id.button_share);
        shareBtn.setOnClickListener(this);
        shareBtn.setVisibility(View.INVISIBLE);

        percView = findViewById(R.id.perc_view);
        avgView  = findViewById(R.id.avg_view);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.TAG_SCAN+".UPDATE");

        BroadcastReceiver scanBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mostRecent = intent.getParcelableExtra(Constant.TAG_UPDATE);
                refreshUI();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(scanBroadcastReceiver,intentFilter);

        fileFragment = (FileFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_file_data);
        extentionFragment  = (ExtentionFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_ext_data);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(Constant.TAG_UPDATE, mostRecent);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mostRecent = savedInstanceState.getParcelable(Constant.TAG_UPDATE);
        if(mostRecent!=null){refreshUI();}
    }

    /**
     * Send local broadcast to service for start scanning files from SD card
     */
    public void startScan(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_EXTERNAL_STORAGE);

        } else {
            Intent intent = new Intent(Constant.TAG_MAIN+".START");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

    }

    /**
     * Send local broadcast to service to pause scanning files from SD card
     */
    public void pauseScan(){
        Intent intent = new Intent(Constant.TAG_MAIN+".PAUSE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Send local broadcast to service for start for rescanning files from SD card
     */
    public void requestUpdate(){
        Intent intent = new Intent(Constant.TAG_MAIN+".UPDATE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Send local broadcast to service for stop scanning
     */
    public void stopScan(){
        Intent intent = new Intent(Constant.TAG_MAIN+".STOP");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Share scanning details via email using intent
     */
    public void shareResults(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_SUBJECT, "External Storage results");
        i.putExtra(Intent.EXTRA_TEXT   , mostRecent.totalFileSizeInMB + "MBs of data has been scanned.");

        startActivity(Intent.createChooser(i, "Send mail..."));

    }

    /**
     * Check scanning service is running or not
     */
    private boolean isScannerActive(){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (ScanService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * After rescan refresh the UI with new updated data
     */
    private void refreshUI() {

        fileFragment.updateData(mostRecent);
        extentionFragment.updateData(mostRecent);

        if (mostRecent.isDone == 1) {shareBtn.setVisibility(View.VISIBLE);
        }else{shareBtn.setVisibility(View.INVISIBLE);}

        percView.setText("Total:" + mostRecent.totalFileSizeInMB + "MBs");
        avgView.setText("Avg:" + (float) mostRecent.aveFileSize + "MBs");

    }

    /**
     * Click events for buttons
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_start:
                startScan();
                break;
            case R.id.button_pause:
                pauseScan();
                break;
            case R.id.button_share:
                shareResults();
                break;
        }
    }

    /**
     * When user press back button then service stop for scanning and also kill activity
     */
    @Override
    public void onBackPressed() {
        stopScan();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_EXTERNAL_STORAGE: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(Constant.TAG_MAIN+".START");
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {

                    }
                }
            }
            break;
        }
    }

}


