package com.chaoyang805.cloudnote.model;

/**
 * Created by chaoyang805 on 2015/10/25.
 */
public class Attachment {

    private int mId;

    private long mNoteId;

    private int mPostAuthor;

    public boolean isSynced() {
        return mIsSynced;
    }

    public void setIsSynced(boolean isSynced) {
        mIsSynced = isSynced;
    }

    private boolean mIsSynced = false;

    private String mPath;
    /**
     * 附件的类型，1表示图片，0表示视频
     */
    private int mType;


    public Attachment(){

    }

    public Attachment(int id, int authorId, int noteId, boolean isSynced,int type,String path) {
        mId = id;
        mType = type;
        mPostAuthor = authorId;
        mIsSynced = isSynced;
        mNoteId = noteId;
        mPath = path;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public long getNoteId() {
        return mNoteId;
    }

    public void setNoteId(long noteId) {
        mNoteId = noteId;
    }

    public int getPostAuthor() {
        return mPostAuthor;
    }

    public void setPostAuthor(int postAuthor) {
        mPostAuthor = postAuthor;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }
}
