package com.coding.test.sdcardcontents;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;


//This class is using for show the 5 most frequent extesions in fragment
public class ExtentionFragment extends Fragment {
    private ArrayAdapter<String> mArrayAdapter;
    private ListView mListView;


    public ExtentionFragment() {}


    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View mView = inflater.inflate(R.layout.fragment_extention, container, false);
        mListView = (ListView)mView.findViewById(R.id.extentionList);
        mArrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1);
        mListView.setAdapter(mArrayAdapter);

        return mView;
    }

    /**
     * This method provide 5 most frequent extensions files
     */
    public void updateData(Rescan mRescan){
        mArrayAdapter.clear();
        for(int i = 0;i < 5 ; i++){
            mArrayAdapter.add(mRescan.mostFrequentFiveExtensions[i]);
        }
        mArrayAdapter.notifyDataSetChanged();
    }


}
