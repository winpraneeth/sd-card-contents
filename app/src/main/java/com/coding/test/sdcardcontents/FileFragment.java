package com.coding.test.sdcardcontents;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


//This class using for show the 10 biggest files in fragment
public class FileFragment extends Fragment {


    private List<FileItems> listFileDetail;
    private FileAdapter mFileAdapter;
    private ListView mListView;


    public FileFragment() {}


    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_file, container, false);

        listFileDetail = new ArrayList<>();
        mListView = (ListView)mView.findViewById(R.id.fileDataList);
        mFileAdapter = new FileAdapter(getContext(),R.layout.file_item, listFileDetail);
        mListView.setAdapter(mFileAdapter);

        return mView;
    }

    /**
     * This method scan biggest files from SD card
     */
    public void updateData(Rescan mRescanned){
        mFileAdapter.clear();
        for(int i = 0; i<mRescanned.tenBiggestFile.length; i++){
            FileItems fileDataItem = new FileItems();
            fileDataItem.name = mRescanned.tenBiggestFile[i];
            fileDataItem.size = Long.toString(mRescanned.tenBiggestFileSize[i]);
            mFileAdapter.add(fileDataItem);
        }
        mFileAdapter.notifyDataSetChanged();
    }

    /**
     * This class is used for file items
     */

    public static class FileItems {
        public String name ;
        public String size ;
    }

}
