package com.chaoyang805.cloudnote.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.chaoyang805.cloudnote.R;
import com.chaoyang805.cloudnote.UrlImageGetter;
import com.chaoyang805.cloudnote.app.App;
import com.chaoyang805.cloudnote.db.DBImpl;
import com.chaoyang805.cloudnote.model.Attachment;
import com.chaoyang805.cloudnote.model.Note;
import com.chaoyang805.cloudnote.utils.Constant;
import com.chaoyang805.cloudnote.utils.FileTool;
import com.chaoyang805.cloudnote.utils.LogHelper;
import com.chaoyang805.cloudnote.utils.ToastUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by chaoyang805 on 2015/10/30.
 */
public class NoteCreateActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = LogHelper.makeLogTag(NoteCreateActivity.class);

    public static final String EXTRA_REQUEST_TYPE = "request_type";

    public static final int REQUEST_TYPE_CREATE = 0x2003;

    public static final int REQUEST_TYPE_EDIT = 0x2004;

    private ImageButton mNewPhoto;

    private ImageButton mNewCamera;

    private ImageButton mNewVideo;

    private EditText mEtContent;

    private EditText mEtTitle;

    private String mCurrentMediaPath;

    private ArrayList<String> mAttachmentsPath;

    private DBImpl mDatabase;

    private boolean mIsEditMode = false;

    private String mOriginTitle;

    private Editable mOriginContent;

    private int mUserId;

    private Note mEditNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_create);
        initToolbar();
        int requestType = getIntent().getIntExtra(EXTRA_REQUEST_TYPE, -1);
        switch (requestType) {
            case REQUEST_TYPE_CREATE:
                getSupportActionBar().setTitle("新建笔记");
                break;
            case REQUEST_TYPE_EDIT:
                mIsEditMode = true;
                getSupportActionBar().setTitle("编辑笔记");
                break;
            default:
                break;
        }
        initDatas();
        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private AlertDialog mSaveDialog = null;

    @Override
    public void onBackPressed() {
        if (TextUtils.isEmpty(mEtTitle.getText()) && TextUtils.isEmpty(mEtContent.getText())) {
            finish();
            return;
        }
        if (mSaveDialog != null && mSaveDialog.isShowing()) {
            mSaveDialog.dismiss();
            return;
        }
        if (shouldSave()) {
            mSaveDialog = new AlertDialog.Builder(this).setTitle("是否保存？").setMessage("是否要保存当前的修改？").setPositiveButton("保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveAttachment(saveNote());
                    ToastUtils.showToast(NoteCreateActivity.this, "保存成功");
                    NoteCreateActivity.super.onBackPressed();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
        } else {
            finish();
        }
    }

    private void initDatas() {
        mAttachmentsPath = new ArrayList<>();
        mUserId = getSharedPreferences(Constant.SP_NAME, MODE_PRIVATE).getInt(Constant.SP_KEY_CUR_USER_ID, -1);
        mDatabase = ((App) getApplication()).getDB();
    }

    private void initViews() {
        mNewCamera = (ImageButton) findViewById(R.id.ib_new_camera);
        mNewPhoto = (ImageButton) findViewById(R.id.ib_new_photo);
        mNewVideo = (ImageButton) findViewById(R.id.ib_new_video);
        mEtTitle = (EditText) findViewById(R.id.et_title);
        mEtContent = (EditText) findViewById(R.id.et_content);
        mEtContent.setMovementMethod(LinkMovementMethod.getInstance());

        mOriginTitle = mEtTitle.getText().toString();
        mOriginContent = mEtContent.getText();

        mNewCamera.setOnClickListener(this);
        mNewPhoto.setOnClickListener(this);
        mNewVideo.setOnClickListener(this);
        findViewById(R.id.ib_done).setOnClickListener(this);

        if (mIsEditMode) {
            long noteId = getIntent().getLongExtra(NoteDetailActivity.EXTRA_NOTE_ID, -1);
            if (noteId >= 0) {
                mEditNote = mDatabase.queryNote(noteId);
                mEtTitle.setText(mEditNote.getTitle());
                mOriginTitle = mEditNote.getTitle();
                mEtContent.setMovementMethod(new LinkMovementMethod());

                mOriginContent = (Editable) Html.fromHtml(mEditNote.getContent());

                mEtContent.setText(Html.fromHtml(mEditNote.getContent(), new UrlImageGetter(this, mEtContent), null));

                CharSequence charSequence = Html.fromHtml(mEditNote.getContent(), new UrlImageGetter(this, mEtContent), null);
                SpannableString ss = new SpannableString(charSequence);
                URLSpan[] urlSpans = ss.getSpans(0, ss.length(), URLSpan.class);
                for (URLSpan urlSpan : urlSpans) {
                    int start = ss.getSpanStart(urlSpan);
                    final int end = ss.getSpanEnd(urlSpan);
                    ss.removeSpan(urlSpan);
                    ss.setSpan(new URLSpan(urlSpan.getURL()) {
                        @Override
                        public void onClick(View widget) {
                            mEtContent.setSelection(end);
                        }
                    }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                mEtContent.setText(ss);
            }
        }
    }

    private void initToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.ib_new_camera:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File dir = getMediaDir();
                File imageFile = new File(dir, "JPG_" + System.currentTimeMillis() + ".jpg");
                if (!imageFile.exists()) {
                    try {
                        imageFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mCurrentMediaPath = imageFile.getAbsolutePath();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                startActivityForResult(intent, Constant.REQUEST_CODE_GET_IMAGE_FROM_CAMERA);
                break;
            case R.id.ib_new_photo:
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, Constant.REQUEST_CODE_GET_IMAGE_FROM_GALLERY);
                break;
            case R.id.ib_new_video:
                intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                File videoFile = new File(getMediaDir(), "VID_" + System.currentTimeMillis() + ".mp4");
                if (!videoFile.exists()) {
                    try {
                        videoFile.createNewFile();
                        mCurrentMediaPath = videoFile.getAbsolutePath();
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile));
                        startActivityForResult(intent, Constant.REQUEST_CODE_GET_VIDEO_FROM_CAMERA);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.ib_done:
                if (TextUtils.isEmpty(mEtTitle.getText()) || TextUtils.isEmpty(mEtContent.getText())) {
                    ToastUtils.showToast(NoteCreateActivity.this, "标题和内容不能为空");
                    return;
                }
                saveAttachment(saveNote());
                ToastUtils.showToast(this, "保存成功");
                finish();
                break;
            default:
                break;
        }
    }

    private boolean shouldSave() {
        return !(mEtContent.getText().equals(mOriginContent) && mEtTitle.getText().toString().equals(mOriginTitle));
    }

    /**
     * 将笔记先保存到本地数据库
     */
    private long saveNote() {
        //1.保存edittext中的文本内容
        String title = mEtTitle.getText().toString();
        Note note = new Note();
        Editable editable = mEtContent.getText();
        ImageSpan[] imageSpans = editable.getSpans(0, editable.length(), ImageSpan.class);
        URLSpan[] urlSpans = editable.getSpans(0, editable.length(), URLSpan.class);
        int start;
        int end;
        mAttachmentsPath.clear();
        String htmlContent = Html.toHtml(mEtContent.getText());

        sortSpans(editable, imageSpans);

        for (int i = 0;i<imageSpans.length;i++) {
            start = editable.getSpanStart(imageSpans[i]);
            end = editable.getSpanEnd(imageSpans[i]);
            String imagePath = editable.subSequence(start,end).toString();
            mAttachmentsPath.add(editable.subSequence(start,end).toString());
            htmlContent = htmlContent.replaceFirst("null",imagePath);
        }

        sortSpans(editable,urlSpans);

        for (int i = 0;i < urlSpans.length;i++) {
            start = editable.getSpanStart(urlSpans[i]);
            end = editable.getSpanEnd(urlSpans[i]);
            mAttachmentsPath.add(urlSpans[i].getURL());
        }
        note.setContent(htmlContent);
        String plainContent = getPlainContent();
        note.setTitle(title);
        note.setPlainContent(plainContent);
        note.setPostAuthor(mUserId);
        note.setIsSynced(false);
        note.setPostDate(FileTool.getPostdateString());
        note.setModifyDate(FileTool.getPostdateString());
        long _id = -1;
        if (mIsEditMode) {
            note.setId(mEditNote.getId());
            mDatabase.updateNote(note);
            _id = mEditNote.getId();
        } else {
            _id = mDatabase.insertNote(note);
        }
        return _id;
    }

    private void sortSpans(Editable editable, CharacterStyle[] spans) {
        CharacterStyle tmp;

        for (int i = 0; i < spans.length - 1; i++) {
            for (int j = 1; j < spans.length; j++) {
                int startI = editable.getSpanStart(spans[i]);
                int startJ = editable.getSpanStart(spans[j]);
                if (startI > startJ) {
                    tmp = spans[i];
                    spans[i] = spans[j];
                    spans[j] = tmp;
                }
            }

        }
    }

    private void saveAttachment(long _id) {
        //保存笔记的附件
        Attachment attachment;
        for (String attachmentPath : mAttachmentsPath) {
            attachment = new Attachment();
            attachment.setPostAuthor(mUserId);
            attachment.setType(attachmentPath.endsWith("mp4") ? 0 : 1);
            attachment.setPath(attachmentPath);
            attachment.setNoteId(_id);
            attachment.setIsSynced(false);
            mDatabase.insertAttachment(attachment);
        }
    }

    private String getPlainContent() {
        Editable editableContent = mEtContent.getText();
        String plainContent = editableContent.toString();
        if (mIsEditMode) {
            plainContent = plainContent.replaceAll("￼", "");
        }
        for (String path : mAttachmentsPath) {
            if (plainContent.contains(path)) {
                plainContent = plainContent.replace(path, "");
            }
        }
        return plainContent;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case Constant.REQUEST_CODE_GET_IMAGE_FROM_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    if (isFileTooBig(new File(mCurrentMediaPath).length())) {
                        ToastUtils.showToast(this, "上传的文件不能超过64MB,请重新选择");
                        return;
                    }
                    //mAttachmentsPath.add(mCurrentMediaPath);
                    showImageOnEditText(mCurrentMediaPath);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    File f = new File(mCurrentMediaPath);
                    if (f.exists()) {
                        f.delete();
                    }
                }
                break;
            case Constant.REQUEST_CODE_GET_IMAGE_FROM_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = intent.getData();
                    mCurrentMediaPath = FileTool.getPath(this, uri);
                    if (isFileTooBig(new File(mCurrentMediaPath).length())) {
                        ToastUtils.showToast(this, "上传的文件不能超过64MB,请重新选择");
                        return;
                    }
                    //mAttachmentsPath.add(mCurrentMediaPath);
                    showImageOnEditText(mCurrentMediaPath);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    ToastUtils.showToast(this, "未选择内容");
                }
                break;
            case Constant.REQUEST_CODE_GET_VIDEO_FROM_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    if (isFileTooBig(new File(mCurrentMediaPath).length())) {
                        ToastUtils.showToast(this, "上传的文件不能超过64MB,请重新选择");
                        return;
                    }
                    mAttachmentsPath.add(mCurrentMediaPath);
                    showVideoLink(mCurrentMediaPath);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    File f = new File(mCurrentMediaPath);
                    if (f.exists()) {
                        f.delete();
                    }
                }
                break;
            default:
                break;
        }

    }

    /**
     * 上传的文件大小不能超过64m
     *
     * @param length 文件的长度，以字节表示
     */
    private boolean isFileTooBig(long length) {
        return length > 64 * 1024 * 1024;
    }

    /**
     * 创建的连接并显示在edittext中，点击时跳转到播放器进行播放
     *
     * @param path
     * @return
     */
    private void showVideoLink(String path) {
        //Bitmap bitmap = FileTool.getVideoThumbnail(this, path, MediaStore.Images.Thumbnails.MINI_KIND);
        SpannableString ss = new SpannableString(path.substring(path.lastIndexOf('/') + 1));
        URLSpan urlSpan = new URLSpan(path) {
            @Override
            public void onClick(View widget) {
                String url = getURL();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(url)), "video/mp4");
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(0xfff90202);
            }
        };
        ss.setSpan(urlSpan, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        insertIntoEditText(ss);
    }

    /**
     * 将选择的图片显示在EditText中
     *
     * @param path
     */
    private void showImageOnEditText(String path) {
        Bitmap bitmap = FileTool.getImageThumbnail(path);
        if (bitmap != null) {
            SpannableString ss = getBitmapMime(bitmap, path);
            insertIntoEditText(ss);
        }
    }

    private SpannableString getBitmapMime(Bitmap bitmap, String path) {
        SpannableString ss = new SpannableString(path);
        android.text.style.ImageSpan imageSpan = new android.text.style.ImageSpan(this, bitmap);
        ss.setSpan(imageSpan, 0, path.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    private void insertIntoEditText(SpannableString ss) {
        Editable editable = mEtContent.getText();
        int start = mEtContent.getSelectionStart();
        if (start < 0) {
            mEtContent.setSelection(0);
            start = 0;
        }
        editable.insert(start, ss);
        mEtContent.setText(editable);
        mEtContent.setSelection(start + ss.length());
        mEtContent.setFocusable(true);
        mEtContent.setFocusableInTouchMode(true);
    }

    @NonNull
    private File getMediaDir() {
        File dir = new File(Environment.getExternalStorageDirectory(), "cloudnote");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
