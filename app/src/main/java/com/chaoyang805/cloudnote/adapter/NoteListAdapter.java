package com.chaoyang805.cloudnote.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chaoyang805.cloudnote.R;
import com.chaoyang805.cloudnote.model.Note;
import com.chaoyang805.cloudnote.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaoyang805 on 2015/10/18.
 */
public class NoteListAdapter extends BaseAdapter {

    private static final String TAG = LogHelper.makeLogTag(NoteListAdapter.class);

    private List<Note> mNotes;
    private Context mContext;
    private boolean mActionModeStarted;
    private int[] mItemState;

    public NoteListAdapter(Context context, List<Note> notes) {
        mContext = context;
        mNotes = notes;
        mItemState = new int[notes.size()];
        for (int i = 0; i < mItemState.length; i++) {
            mItemState[i] = 0;
        }
    }

    public List<Integer> getSelectedItemIds() {

        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < mItemState.length; i++) {
            if (mItemState[i] == 1) {
                ids.add(mNotes.get(i).getId());
            }
        }
        return ids;
    }

    public void deleteSelectedItems() {
        for (int i = mItemState.length - 1; i >= 0; i--) {
            if (mItemState[i] == 1) {
                mNotes.remove(i);
            }
        }
        notifyDataSetChanged();
        mItemState = new int[mNotes.size()];
        for (int i = 0; i < mItemState.length; i++) {
            mItemState[i] = 0;
        }
    }

    public int[] getItemState() {
        return mItemState;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public int getCount() {
        return mNotes.size();
    }

    @Override
    public Note getItem(int position) {
        return mNotes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    private Drawable mBackground = null;
    private boolean isCacheBackground = false;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Note note = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_note_list, parent, false);
            holder = new ViewHolder();
            holder.mTitle = (TextView) convertView.findViewById(R.id.note_title);
//            holder.mContent = (TextView) convertView.findViewById(R.id.note_content);
            holder.mDate = (TextView) convertView.findViewById(R.id.note_date);
            holder.mIcon = (ImageView) convertView.findViewById(R.id.note_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (!isCacheBackground) {
            isCacheBackground = true;
            mBackground = convertView.getBackground();
        }
        updateBackground(position, convertView);
        holder.mTitle.setText(note.getTitle());
//        holder.mContent.setText(note.getPlainContent());
        holder.mDate.setText(note.getPostDate());
        holder.mIcon.setVisibility(((note.getAttachments() == null) || (note.getAttachments().size() == 0)) ? View.INVISIBLE : View.VISIBLE);
        return convertView;
    }

    public void updateBackground(int position, View view) {
        if (mItemState[position] == 0) {
            view.setBackgroundDrawable(mBackground);
        }else if (mItemState[position] == 1) {
            view.setBackgroundColor(0xFFDFDFDF);
        }
    }

    public void uncheckAll() {
        for (int i = 0; i < mItemState.length; i++) {
            mItemState[i] = 0;
        }
    }

    public boolean isAllChecked() {
        for (int i : mItemState) {
            if (i == 0) return false;
        }
        return true;
    }

    public void checkAll() {
        for (int i = 0; i < mItemState.length; i++) {
            mItemState[i] = 1;
        }
    }

    public int getCheckedItemCount() {
        int count = 0;
        for (int i : mItemState) {
            if (i == 1) count++;
        }
        return count;
    }

    public void setActionModeState(boolean flag) {
        mActionModeStarted = flag;
    }

    public boolean isActionModeStart() {
        return mActionModeStarted;
    }

    public class ViewHolder {

        TextView mTitle;
//        TextView mContent;
        TextView mDate;
        ImageView mIcon;

    }
}
