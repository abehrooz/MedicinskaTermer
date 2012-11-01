package com.example.android.searchabledict;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MyAlphabetizedAdapter extends SimpleCursorAdapter implements SectionIndexer {

    private static final int TYPE_HEADER = 1;
    private static final int TYPE_NORMAL = 0;

    private static final int TYPE_COUNT = 2;

    private AlphabetIndexer indexer;

    private int[] usedSectionNumbers;

    private Map<Integer, Integer> sectionToOffset;
    private Map<Integer, Integer> sectionToPosition;

    public MyAlphabetizedAdapter(Context context, int layout, Cursor c,
                                 String[] from, int[] to) {
        super(context, layout, c, from, to);

        indexer = new AlphabetIndexer(c, c.getColumnIndexOrThrow(DictionaryDatabase.KEY_WORD), "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        sectionToPosition = new TreeMap<Integer, Integer>();
        sectionToOffset = new HashMap<Integer, Integer>();

        final int count = super.getCount();

        int i;
        for (i = count - 1 ; i >= 0; i--){
            sectionToPosition.put(indexer.getSectionForPosition(i), i);
        }

        i = 0;
        usedSectionNumbers = new int[sectionToPosition.keySet().size()];

        for (Integer section : sectionToPosition.keySet()){
            sectionToOffset.put(section, i);
            usedSectionNumbers[i] = section;
            i++;
        }

        for(Integer section: sectionToPosition.keySet()){
            sectionToPosition.put(section, sectionToPosition.get(section) + sectionToOffset.get(section));
        }
    }

    @Override
    public int getCount() {
        if (super.getCount() != 0){
            return super.getCount() + usedSectionNumbers.length;
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (getItemViewType(position) == TYPE_NORMAL){//we define this function later
            return super.getItem(position - sectionToOffset.get(getSectionForPosition(position)) - 1);
        }

        return null;
    }

    @Override
    public int getPositionForSection(int section) {
        if (! sectionToOffset.containsKey(section)){
            int i = 0;
            int maxLength = usedSectionNumbers.length;

            while (i < maxLength && section > usedSectionNumbers[i]){
                i++;
            }
            if (i == maxLength) return getCount();

            return indexer.getPositionForSection(usedSectionNumbers[i]) + sectionToOffset.get(usedSectionNumbers[i]);
        }

        return indexer.getPositionForSection(section) + sectionToOffset.get(section);
    }

    @Override
    public int getSectionForPosition(int position) {
        int i = 0;
        int maxLength = usedSectionNumbers.length;

        while (i < maxLength && position >= sectionToPosition.get(usedSectionNumbers[i])){
            i++;
        }
        return usedSectionNumbers[i-1];
    }

    @Override
    public Object[] getSections() {
        return indexer.getSections();
    }

    //nothing much to this: headers have positions that the sectionIndexer manages.
    @Override
    public int getItemViewType(int position) {
        if (position == getPositionForSection(getSectionForPosition(position))){
            return TYPE_HEADER;
        } return TYPE_NORMAL;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    //return the header view, if it's in a section header position
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int type = getItemViewType(position);
        if (type == TYPE_HEADER){
            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) MyApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView = inflater.inflate(R.layout.header, parent, false);
            }
            ((TextView)convertView.findViewById(R.id.header)).setText((String)getSections()[getSectionForPosition(position)]);
            return convertView;
        }
        return super.getView(position - sectionToOffset.get(getSectionForPosition(position)) - 1, convertView, parent);
    }


    //these two methods just disable the headers
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        if (getItemViewType(position) == TYPE_HEADER){
            return false;
        }
        return true;
    }
}
