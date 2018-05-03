package com.coding.test.sdcardcontents;

import android.os.Parcel;
import android.os.Parcelable;


//Create object to obtain the details like biggest 10 files, total size, etc.
public class Rescan implements Parcelable {

    int totalFileSizeInMB = 0;
    double aveFileSize  = 0;

    String[] tenBiggestFile = new String[10];
    public long[]   tenBiggestFileSize = new long[10];

    public String[] mostFrequentFiveExtensions = new String[5];

    public byte isDone = 0;

    public Rescan(){}

    /**
     * Get file details from the parcelable
     */
    protected Rescan(Parcel in) {
        totalFileSizeInMB = in.readInt();
        aveFileSize = in.readDouble();
        in.readStringArray(tenBiggestFile);
        in.readLongArray(tenBiggestFileSize);
        in.readStringArray(mostFrequentFiveExtensions);
        isDone = in.readByte();
    }
    /**
     * The system asked to create a Creator in the parcelable for Rescan
     */
    public static final Creator<Rescan> CREATOR = new Creator<Rescan>() {
        @Override
        public Rescan createFromParcel(Parcel in) {
            return new Rescan(in);
        }

        @Override
        public Rescan[] newArray(int size) {
            return new Rescan[size];
        }
    };

    /**
     * Description content list
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Adding file information in the parcelable
     */
    @Override
    public void writeToParcel(Parcel mparce, int flags) {
        mparce.writeInt(totalFileSizeInMB);
        mparce.writeDouble(aveFileSize);

        mparce.writeStringArray(tenBiggestFile);
        mparce.writeLongArray(tenBiggestFileSize);

        mparce.writeStringArray(mostFrequentFiveExtensions);

        mparce.writeByte(isDone);
    }
}
