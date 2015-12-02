package com.chaoyang805.cloudnote.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.chaoyang805.cloudnote.R;
import com.chaoyang805.cloudnote.SimpleOnNetFinishedCallback;
import com.chaoyang805.cloudnote.UrlImageGetter;
import com.chaoyang805.cloudnote.app.App;
import com.chaoyang805.cloudnote.db.DBImpl;
import com.chaoyang805.cloudnote.model.Note;
import com.chaoyang805.cloudnote.net.NetConnection;
import com.chaoyang805.cloudnote.utils.Constant;
import com.chaoyang805.cloudnote.utils.LogHelper;
import com.chaoyang805.cloudnote.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by chaoyang805 on 2015/10/30.
 */
public class NoteDetailActivity extends AppCompatActivity {

    private static final String TAG = LogHelper.makeLogTag(NoteDetailActivity.class);

    private Toolbar mToolbar;

    private TextView mTvContent;

    private TextView mTvTitle;

    private Html.ImageGetter mImageGetter;

    private long mNoteId = -1;

    public static final String EXTRA_NOTE_ID = "note_id";

    private DBImpl mDatabase;
    private SimpleOnNetFinishedCallback mNetCallback = new SimpleOnNetFinishedCallback() {

        @Override
        public void onRequestFinished(String result) {
            try {
                JSONObject resultJson = new JSONObject(result);
                int resultCode = resultJson.getInt(Constant.JSON_KEY_RESULT);
                if (resultCode == 103) {
                    ToastUtils.showToast(NoteDetailActivity.this, "删除成功");
                    mDatabase.deleteNote(mNoteId);
                    mDatabase.deleteAttachmentByNoteId(mNoteId);
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);
        initToolbar();
        mNoteId = getIntent().getLongExtra(EXTRA_NOTE_ID, 0);
        mDatabase = ((App) getApplication()).getDB();
        Note note = mDatabase.queryNote(mNoteId);

        String title = note.getTitle();
        getSupportActionBar().setTitle(title);
        String htmlContent = note.getContent();
        mTvContent = (TextView) findViewById(R.id.tv_note_content);
        mTvTitle = (TextView) findViewById(R.id.tv_note_title);

        mImageGetter = new UrlImageGetter(this, mTvContent);
        mTvTitle.setText(title);

        mTvContent.setMovementMethod(new LinkMovementMethod());

        CharSequence charSequence = Html.fromHtml(htmlContent, mImageGetter, null);
        SpannableString ss = new SpannableString(charSequence);
        URLSpan[] urlSpans = ss.getSpans(0, ss.length(), URLSpan.class);
        for (URLSpan urlSpan : urlSpans) {
            int start = ss.getSpanStart(urlSpan);
            int end = ss.getSpanEnd(urlSpan);
            ss.removeSpan(urlSpan);
            ss.setSpan(new URLSpan(urlSpan.getURL()) {
                @Override
                public void onClick(View widget) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(getURL())), "video/mp4");
                    startActivity(intent);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mTvContent.setText(ss);
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            deleteNote();
            return true;
        } else if (id == R.id.action_edit) {
            editNote();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 编辑当前的笔记
     */
    private void editNote() {
        Intent intent = new Intent(this, NoteCreateActivity.class);
        intent.putExtra(NoteCreateActivity.EXTRA_REQUEST_TYPE, NoteCreateActivity.REQUEST_TYPE_EDIT);
        intent.putExtra(EXTRA_NOTE_ID, mNoteId);
        startActivity(intent);
        finish();
    }

    /**
     * 删除当前对应的条目
     */
    private void deleteNote() {
        try {
            Note note = mDatabase.queryNote(mNoteId);
            if (note.isSynced()) {
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("note_id", mDatabase.queryNote(mNoteId).getNoteId());
                jsonArray.put(jsonObject);
                new NetConnection(this).deleteNote(jsonArray.toString(), mNetCallback);
            } else {
                mDatabase.deleteNote(mNoteId);
                mDatabase.deleteAttachmentByNoteId(mNoteId);
                onBackPressed();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

