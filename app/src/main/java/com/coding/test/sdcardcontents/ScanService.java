package com.coding.test.sdcardcontents;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//This service provide specific method for getting data from external storage
public class ScanService extends Service implements Runnable {

    private static final File EXTERNAL   = Environment.getExternalStorageDirectory().getAbsoluteFile();
    private static final int    NOT_ID = 99;

    private ScannerNotification mScannerNotification;

    private Thread scanThread;

    private BroadcastReceiver mReceiver;

    private Map<String,Integer> extentionsMap;
    private Set<Map.Entry<String,Integer>> entrySet;
    private List<Map.Entry<String,Integer>> sorted;


    private volatile boolean isScanning = false;
    private volatile boolean isDone     = false;
    private volatile boolean isPaused   = false;

    private volatile int  scannedFiles    = 0;
    private volatile long scannedBytesSoFar    = 0;


    private int scanFrequency = 24;
    private volatile int freqCheck   = 0;

    private Rescan currentStatus = new Rescan();


    public ScanService() {}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mScannerNotification = mScannerNotification.getInstance();

        //create a map to store extensions name and time
        extentionsMap = new HashMap<>();

        //receive the command from MainActivity for operation
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){
                    case Constant.TAG_MAIN+".START":

                        startScan();
                        break;
                    case Constant.TAG_MAIN+".PAUSE":

                        pauseScan();
                        break;
                    case Constant.TAG_MAIN+".UPDATE":

                        sendUpdate();
                        break;
                    case Constant.TAG_MAIN+".STOP":

                        stopScan();
                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.TAG_MAIN+".START");
        intentFilter.addAction(Constant.TAG_MAIN+".PAUSE");
        intentFilter.addAction(Constant.TAG_MAIN+".UPDATE");
        intentFilter.addAction(Constant.TAG_MAIN+".STOP");
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,intentFilter);
        return Service.START_NOT_STICKY;
    }



    /**
     * Excute command including start, pause, etc.
     */
    private void startScan(){
        isPaused = false;
        if((!isScanning && !isDone) || (!isScanning && isDone)){
            mScannerNotification.initialize(NOT_ID,"Scanned: "+currentStatus.totalFileSizeInMB + "MBs",this);
            reStart();
        }

    }

    private void pauseScan(){
        isPaused = true;
    }

    private void stopScan(){
        isScanning = false;
        isDone     = true;
        mScannerNotification.updateMessage(NOT_ID, "Scanning Stopped!", this);
    }

    private void sendUpdate(){

        Intent intent = new Intent(Constant.TAG_SCAN + ".UPDATE");
        intent.putExtra(Constant.TAG_UPDATE, currentStatus);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        mScannerNotification.updateMessage(NOT_ID,"Scanned: "+currentStatus.totalFileSizeInMB + "MBs",this);

    }

    private void reStart(){
        isScanning = true;
        isDone = false;
        currentStatus = new Rescan();
        scannedBytesSoFar = 0;
        scannedFiles = 0;
        extentionsMap = new HashMap<>();
        scanThread = new Thread(this);
        scanThread.start();
    }


    @Override
    public void run() {
        scan(EXTERNAL);


        isDone = true;
        isScanning = false;
        currentStatus.isDone = 1;
        sendUpdate();
        mScannerNotification.updateMessage(NOT_ID, "Scanning Done!", this);
    }

    /**
     * read each files from external storage
     */
    private void scan(File directory){
        if(isScanning){
            File[] listFile = directory.listFiles();

            if (listFile != null) {
                for (int i = 0; i < listFile.length; i++) {

                    if (listFile[i].isDirectory()) {
                        scan(listFile[i]);
                    } else {
                        while (isPaused){
                            try {
                                Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}
                        }
                        freqCheck++;
                        scannedFiles++;
                        if(freqCheck==scanFrequency){
                            sendUpdate();
                            freqCheck=0;
                        }
                        process(listFile[i].getName(), getFileExtension(listFile[i].getName()), listFile[i].length());

                    }
                }
            }
        }
    }


    /**
     * Process of get files, calculate the total size and average size
     */
    private void process(String name, String ext, long size){

        filterFileData(name,ext,size);
        filterExtData(name,ext,size);


        double avgD = ((double)scannedBytesSoFar / (double)scannedFiles)/1000000;

        currentStatus.aveFileSize = avgD;

        currentStatus.totalFileSizeInMB = (int)(((double)scannedBytesSoFar/(double)1000000));



        scannedBytesSoFar+=size;

    }

    /**
     * Obtain the biggest 10 files
     */
    private void filterFileData(String name, String ext, long size){
        int sizeIndex = fitLong(currentStatus.tenBiggestFileSize, size);
        retract(currentStatus.tenBiggestFile,name,sizeIndex);
    }

    /**
     * Obtain the 5 most frequent extensions
     */
    private void filterExtData(String name, String ext, long size){
        if(!extentionsMap.containsKey(ext)){
            extentionsMap.put(ext, 1);
        }
        else{
            extentionsMap.put(ext, extentionsMap.get(ext) + 1);
        }

        entrySet = extentionsMap.entrySet();
        sorted = new ArrayList<>(entrySet);

        //get each extension's name and compare their frequency
        Collections.sort(sorted, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> a,
                               Map.Entry<String, Integer> b) {
                return b.getValue() - a.getValue();
            }
        });

        int len = sorted.size()>5 ? 5:sorted.size();
        for(int i = 0; i <len;i++){
            currentStatus.mostFrequentFiveExtensions[i]=sorted.get(i).getKey();
        }
    }

    /**
     * Compare each file size and sort in array
     */
    private int fitLong(long[] ary,long val){
        boolean isBigger = false;
        int retract = ary.length-1;
        if(ary[retract]<val){
            for(int i = ary.length-1;i>=0;i--){
                if(ary[i]<val){
                    retract=i;
                    isBigger = true;
                }
            }
        }
        if(isBigger) {
            long next = ary[retract];
            ary[retract] = val;
            for (int i = retract + 1; i < ary.length; i++) {
                ary[i] = next;
                if (i + 1 < ary.length) {
                    next = ary[i + 1];
                }

            }
        }

        return isBigger ? retract : -1;
    }

    private void retract(String[] strings, String string, int index){
        if(index == -1){return;}
        String next = strings[index];
        strings[index] = string;
        for(int i = index+1;i<strings.length;i++){
            strings[i] = next;
            if(i+1<strings.length){next=strings[i+1];}

        }
    }

    /**
     * Obtain each file's extension name
     */
    private String getFileExtension(String file){
        int dot = 0;
        for(int i = 0;i<file.length()-1;i++){
            if(file.charAt(file.length()-(1+i)) == '.'){
                dot = i;
                break;
            }
        }
        return file.substring(file.length()-(dot+1),file.length());
    }


    @Override
    public void onDestroy() {
        isPaused   = false;
        isScanning = false;
        super.onDestroy();
    }
}
