package com.coding.test.sdcardcontents;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


//This class is provide adapter to show the name and size in list
public class FileAdapter extends ArrayAdapter<FileFragment.FileItems> {

    private Context mContext;
    private int resourceID;
    private List<FileFragment.FileItems> fileList;

    public FileAdapter(Context context, int resource, List<FileFragment.FileItems> fileList) {
        super(context, resource);
        this.mContext = context;
        this.resourceID = resource;
        this.fileList = fileList;
    }

    /**
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder viewHolder = null;

        if(row == null)
        {
            // This block exists to inflate the settings list item conditionally based on
            // whether, we want to support a list view.
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(resourceID, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.name = (TextView)row.findViewById(R.id.file_name_view);
            viewHolder.size = (TextView)row.findViewById(R.id.file_size_view);

            row.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder)row.getTag();
        }

        FileFragment.FileItems dataHeld = fileList.get(position);

        if (dataHeld.name.length() > 15) {
            viewHolder.name.setText(dataHeld.name.substring(0, 15));
        } else {
            viewHolder.name.setText(dataHeld.name);
        }


        viewHolder.size.setText(dataHeld.size);

        return row;
    }

    /**
     * Clear all data from list
     */
    @Override
    public void clear() {
        super.clear();
        fileList.clear();
        notifyDataSetChanged();
    }

    /**
     * Add item objects in the list
     */
    @Override
    public void add(FileFragment.FileItems object) {
        super.add(object);
        fileList.add(object);
        notifyDataSetChanged();
    }

    /**
     * Holder for the list items.
     */
    private static class ViewHolder{
        TextView name;
        TextView size;
    }
}
