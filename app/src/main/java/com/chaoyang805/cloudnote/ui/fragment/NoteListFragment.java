package com.chaoyang805.cloudnote.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.chaoyang805.cloudnote.R;
import com.chaoyang805.cloudnote.SimpleOnNetFinishedCallback;
import com.chaoyang805.cloudnote.adapter.NoteListAdapter;
import com.chaoyang805.cloudnote.db.DBImpl;
import com.chaoyang805.cloudnote.model.Attachment;
import com.chaoyang805.cloudnote.model.Note;
import com.chaoyang805.cloudnote.net.NetConnection;
import com.chaoyang805.cloudnote.ui.activity.NoteActivity;
import com.chaoyang805.cloudnote.utils.Constant;
import com.chaoyang805.cloudnote.utils.FileTool;
import com.chaoyang805.cloudnote.utils.LogHelper;
import com.chaoyang805.cloudnote.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaoyang805 on 2015/10/18.
 */
public class NoteListFragment extends Fragment {


    private static final String TAG = LogHelper.makeLogTag(NoteListFragment.class);
    private ListView mNoteList;

    private List<Note> mNotes;

    private NoteListAdapter mAdapter;

    private MenuCallback mCallback = new MenuCallback();

    private int mUserId;

    private DBImpl mDatabase;

    private List<Integer> mSelectedItems = new ArrayList<>();
    private AdapterView.OnItemClickListener mItemClickListener;
    private boolean mIsSyncFinished = false;
    private Animation mAnimation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_notelist, container, false);

        mUserId = ((NoteActivity) getActivity()).getCurrentUseId();
        mDatabase = ((NoteActivity) getActivity()).getDatabase();
        mNotes = ((NoteActivity) getActivity()).getDatabase().queryNotesByUserId(mUserId);
        mAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_anim);

        getNotes();

        mAdapter = new NoteListAdapter(getActivity(), mNotes);
        mNoteList = (ListView) rootView.findViewById(R.id.note_list);
        mNoteList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mNoteList.setMultiChoiceModeListener(mCallback);
        mNoteList.setAdapter(mAdapter);

        mItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAdapter.isActionModeStart()) {
                    int value = mAdapter.getItemState()[position] == 1 ? 0 : 1;
                    mAdapter.getItemState()[position] = value;
                    mCallback.setSelectedCountShow();
                    mAdapter.notifyDataSetInvalidated();
                } else {
                    if (mListener != null) {
                        mListener.onItemClick(parent, view, position, mAdapter.getItem(position).getId());
                    }
                }
            }
        };
        mNoteList.setOnItemClickListener(mItemClickListener);
        return rootView;
    }

    private void getNotes() {
        new NetConnection(getActivity()).getNotes(mUserId, mNetCallback);
        ToastUtils.showToast(getActivity(), "正在同步笔记");
    }

    public interface OnItemClickListener {
        void onItemClick(AdapterView<?> parent, View view, int position, long _id);
    }

    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void refreshList() {
        mNotes = mDatabase.queryNotesByUserId(mUserId);
        mAdapter = new NoteListAdapter(getActivity(), mNotes);
        mNoteList.setAdapter(mAdapter);
    }

    private SimpleOnNetFinishedCallback mNetCallback = new SimpleOnNetFinishedCallback() {

        @Override
        public void onRequestFinished(String result) {
            super.onRequestFinished(result);
            stopAnimation();
            if (result != null) {
                try {
                    JSONObject resultJson = new JSONObject(result);
                    int resultCode = resultJson.getInt(Constant.JSON_KEY_RESULT);

                    switch (resultCode) {
                        case 100:
                            //出现异常
                            ToastUtils.showToast(getActivity(), "请求失败");
                            break;
                        case 101:
                            //发布成功
                            handleSuccess(resultJson);
                            break;
                        case 102:
                            //更新成功
                            ToastUtils.showToast(getActivity(), "更新成功");
                            break;
                        case 103:
                            //删除成功
                            ToastUtils.showToast(getActivity(), "删除成功");
                            mDatabase.deleteNotes(mSelectedItems);
                            refreshList();
                            break;
                        case 104:
                            //从服务器拉取数据成功
                            if (prepareDatas(resultJson.getJSONArray("posts"))) {
                                ToastUtils.showToast(getActivity(), "同步成功");
                                refreshList();
                            }
                            break;
                        default:
                            ToastUtils.showToast(getActivity(), "未知错误" + result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ToastUtils.showToast(getActivity(), "出现错误");
                }
            }
        }
    };

    private void stopAnimation() {
        if (mRefreshItem != null) {
            View actionView = mRefreshItem.getActionView();
            if (actionView != null) {
                actionView.clearAnimation();
                mRefreshItem.setActionView(null);
            }
        }
    }

    private boolean prepareDatas(JSONArray posts) {
        if (posts.length() <= 0) {
            ToastUtils.showToast(getActivity(), "没有发布过笔记");
            return false;
        }
        try {
            JSONObject noteJson;
            for (int i = 0; i < posts.length(); i++) {
                noteJson = posts.getJSONObject(i);
                parseJsonNoteAndSave(noteJson);
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void parseJsonNoteAndSave(JSONObject noteJson) {
        Note note = new Note();
        try {
            note.setNoteId(noteJson.getInt("ID"));
            note.setPostAuthor(noteJson.getInt("post_author"));
            note.setPostDate(noteJson.getString("post_date"));
            note.setModifyDate(noteJson.getString("post_modified"));
            note.setTitle(noteJson.getString("post_title"));
            note.setIsSynced(true);
            String postContent = noteJson.getString("post_content");
            note.setContent(postContent);
            note.setPlainContent(FileTool.getPlainContent(postContent));
            long id = insertOrUpdate(note);
            List<String> imageUrls = FileTool.getImageSrc(postContent);
            List<Attachment> attachments = new ArrayList<>();
            for (String imageUrl : imageUrls) {
                Attachment attachment = new Attachment();
                attachment.setNoteId(id);
                attachment.setIsSynced(true);
                attachment.setType(Constant.TYPE_PHOTO);
                attachment.setPostAuthor(noteJson.getInt("post_author"));
                attachment.setPath(imageUrl);
                attachments.add(attachment);
            }
            mDatabase.insertAllAttachments(attachments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private long insertOrUpdate(Note note) {
        long id;
        String modifyDate = note.getModifyDate();
        Note localNote = mDatabase.queryNoteByNoteId(note.getNoteId());
        if (localNote == null) {
            LogHelper.d(TAG, "insert note");
            id = mDatabase.insertNote(note);
        } else {
            String localDate = localNote.getModifyDate();
            if (FileTool.isNewDate(modifyDate, localDate)) {
                LogHelper.d(TAG, "update note" + note.getNoteId());
                mDatabase.updateNote(note);
            }
            id = note.getId();
        }
        return id;
    }

    private void handleSuccess(JSONObject resultJson) throws JSONException {
        if (resultJson.has(Constant.JSON_KEY_NEW_NOTE)) {
            JSONObject newNote = resultJson.getJSONObject(Constant.JSON_KEY_NEW_NOTE);
            int _id = resultJson.getInt("post_local_id");
            mDatabase.updateNoteId(_id, newNote.getInt("ID"));
        }
        if (mIsSyncFinished) {
            mIsSyncFinished = false;
            ToastUtils.showToast(getActivity(), "同步完成");
        }
    }

    private MenuItem mRefreshItem = null;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_choice) {
            mNoteList.setChoiceMode(ListView.CHOICE_MODE_NONE);
            mNoteList.setMultiChoiceModeListener(null);
            getActivity().startActionMode(mCallback);
            mAdapter.setActionModeState(true);
        } else if (itemId == R.id.action_sync) {
            mRefreshItem = item;
            syncNoteList();
        }
        return super.onOptionsItemSelected(item);
    }

    private void startAnimation() {
        ImageView refreshView = (ImageView) LayoutInflater.from(getActivity()).inflate(R.layout.action_view, null);
        refreshView.setImageResource(R.mipmap.ic_menu_sync_36dp);
        mRefreshItem.setActionView(refreshView);

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_anim);
        refreshView.startAnimation(animation);
    }

    /**
     * 同步本地的内容到服务器
     */
    private void syncNoteList() {

        for (int i = 0; i < mNotes.size(); i++) {
            final Note note = mNotes.get(i);
            if (!note.isSynced()) {
                startAnimation();
                new NetConnection(getActivity()).uploadFile(note.getAttachments(), new SimpleOnNetFinishedCallback() {
                    String htmlStr = note.getContent();

                    @Override
                    public void onUploadFinished(ArrayList<String> results) {
                        if (results != null) {
                            List<Attachment> videos = mDatabase.queryVideoAttachsByNoteId(note.getId());
                            List<Attachment> images = mDatabase.queryImageAttachsByNoteId(note.getId());
                            for (String result : results) {
                                try {
                                    JSONObject jsonResult = new JSONObject(result);
                                    String path = jsonResult.getString("file");

                                    if (path.endsWith("mp4")) {
                                        String videoPath;
                                        String videoName;
                                        for (Attachment video : videos) {
                                            videoPath = video.getPath();
                                            videoName = videoPath.substring(videoPath.lastIndexOf("/") + 1);
                                            LogHelper.d(TAG, "videoName " + videoName);
                                            if (path.contains(videoName)) {
                                                LogHelper.d(TAG, "contains" + videoName + " replace!");
                                                htmlStr = htmlStr.replace(videoPath, path);
                                            }
                                        }
                                    } else {
                                        String imagePath;
                                        for (Attachment image : images) {
                                            imagePath = image.getPath();
                                            String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
                                            LogHelper.d(TAG, "imageName " + imageName);
                                            if (path.contains(imageName)) {

                                                LogHelper.d(TAG, path + "contains " + imageName + "replace!");
                                                htmlStr = htmlStr.replace(imagePath, path);
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
//                            note.setContent(htmlStr);
                            note.setIsSynced(true);
                            mDatabase.updateNote(note);
                            note.setContent(htmlStr);
                            if (note.getNoteId() >= 0) {
                                new NetConnection(getActivity()).updateNote(note, mNetCallback);
                            } else {
                                new NetConnection(getActivity()).publishNote(mUserId, note, mNetCallback);
                            }
                        } else {
                            stopAnimation();
                            ToastUtils.showToast(getActivity(), "上传失败");
                        }
                    }
                });
            }
            if (i == mNotes.size() - 1) {
                mIsSyncFinished = true;
            }
        }
    }

    private void deleteNotes() {
        List<Integer> ids = mAdapter.getSelectedItemIds();
        JSONArray jsonArr = new JSONArray();
        try {
            for (Integer id : ids) {
                Note note = mDatabase.queryNote(id);
                if (note.isSynced()) {
                    JSONObject json = new JSONObject();
                    json.put("note_id", note.getNoteId());
                    jsonArr.put(json);
                }
            }
            new NetConnection(getActivity()).deleteNote(jsonArr.toString(), mNetCallback);
            mSelectedItems = mAdapter.getSelectedItemIds();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    public void onActionModeFinished(ActionMode mode) {
        mAdapter.setActionModeState(false);
        mNoteList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mNoteList.setMultiChoiceModeListener(mCallback);
    }

    public class MenuCallback implements ListView.MultiChoiceModeListener {

        private View mMultiSelectActionbarView;
        private TextView mSelectedConvCount;
        private boolean allCheckMode;

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            int checkedCount = 0;
            if (allCheckMode) {
                if (checked) {
                    mAdapter.getItemState()[position] = 0;
                } else {
                    mAdapter.getItemState()[position] = 1;
                }
                checkedCount = mAdapter.getCheckedItemCount();
            } else {
                if (checked) {
                    mAdapter.getItemState()[position] = 1;
                } else {
                    mAdapter.getItemState()[position] = 0;
                }
                checkedCount = mAdapter.getCheckedItemCount();
            }
            mSelectedConvCount.setText(checkedCount + "");
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            allCheckMode = false;
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.multi_select_menu, menu);

            if (mMultiSelectActionbarView == null) {
                mMultiSelectActionbarView = LayoutInflater.from(getActivity()).inflate(R.layout.multi_select_title, null);
                mSelectedConvCount = (TextView) mMultiSelectActionbarView.findViewById(R.id.selected_conv_count);
            }
            mode.setCustomView(mMultiSelectActionbarView);
            ((TextView) mMultiSelectActionbarView.findViewById(R.id.title)).setText(R.string.select_item);
            mSelectedConvCount.setText(mAdapter.getCheckedItemCount() + "");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (mMultiSelectActionbarView == null) {
                ViewGroup v = (ViewGroup) LayoutInflater
                        .from(getActivity()).inflate(R.layout.multi_select_title, null);
                mode.setCustomView(v);
                mSelectedConvCount = (TextView) v.findViewById(R.id.selected_conv_count);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_select:
                    if (mAdapter.isAllChecked()) {
                        item.setTitle("全选");
                        mAdapter.uncheckAll();
                        mNoteList.clearChoices();
                        mode.finish();
                    } else {
                        mAdapter.checkAll();
                        item.setTitle("取消");
                        for (int i = 0; i < mAdapter.getCount(); i++) {
                            mNoteList.setSelection(i);
                        }
                        allCheckMode = true;
                    }
                    mAdapter.notifyDataSetChanged();
                    mSelectedConvCount.setText(mAdapter.getCheckedItemCount() + "");
                    break;
                case R.id.action_delete:
                    deleteNotes();
                    mode.finish();
                default:
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.uncheckAll();
            allCheckMode = false;
        }

        public void setSelectedCountShow() {
            mSelectedConvCount.setText(mAdapter.getCheckedItemCount() + "");
        }
    }


}
