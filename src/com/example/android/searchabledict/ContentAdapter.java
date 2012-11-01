package com.example.android.searchabledict;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ContentAdapter extends ResourceCursorAdapter implements SectionIndexer {

    private AlphabetIndexer indexer;

    private int[] usedSectionNumbers;

    private Map<Integer, Integer> sectionToOffset;
    private Map<Integer, Integer> sectionToPosition;
    public ContentAdapter(Context context, int layout, Cursor c) {
        super(context, layout, c);
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

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }
        viewHolder.bind(cursor, context);
    }

    private static class ViewHolder {
        private TextView wordTextView;

        private TextView definitionTextView;

        public ViewHolder(View view) {
            wordTextView = (TextView) view.findViewById(R.id.word);
            definitionTextView = (TextView) view.findViewById(R.id.definition);
        }

        /**
         * Bind the data from the cursor to the proper views that are hold in
         * this holder
         * @param cursor
         */
        public void bind(Cursor cursor, Context context) {
            definitionTextView.setText(cursor.getString(cursor.getColumnIndex(DictionaryDatabase.KEY_DEFINITION)));
            wordTextView.setText(cursor.getString(cursor.getColumnIndex(DictionaryDatabase.KEY_WORD)));
        }
    }
}
