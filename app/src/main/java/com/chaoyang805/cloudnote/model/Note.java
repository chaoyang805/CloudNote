package com.chaoyang805.cloudnote.model;

import java.util.List;

/**
 * Created by chaoyang805 on 2015/10/18.
 * 笔记的实体类
 */
public class Note {
    /**
     * 笔记在数据库中的id
     */
    private int mId;
    /**
     * 这条笔记在网络数据库中的id，未同步时为-1
     */
    private int mNoteId = -1;

    private int mPostAuthor;

    public boolean isSynced() {
        return mIsSynced;
    }

    public void setIsSynced(boolean isSynced) {
        mIsSynced = isSynced;
    }

    private boolean mIsSynced = false;

    /**
     * 笔记的标题
     */
    private String mTitle;
    /**
     * 笔记的具体内容
     */
    private String mContent;

    private String mPlainContent;
    /**
     * 笔记发布的时间
     */
    private String mPostDate = "1970-01-01 00:00:00";

    public String getModifyDate() {
        return mModifyDate;
    }

    public void setModifyDate(String modifyDate) {
        mModifyDate = modifyDate;
    }

    private String mModifyDate = "1970-01-01 00:00:00";

    /**
     * 笔记中插入的图片地址的集合
     */
    private List<Attachment> mAttachments;

    public Note() {
//        this("null","null");
    }

//    public Note(String title,String content){
//        mTitle = title;
//        mContent = content;
//    }


    public String getPlainContent() {
        return mPlainContent;
    }

    public void setPlainContent(String plainContent) {
        mPlainContent = plainContent;
    }

    public int getPostAuthor() {
        return mPostAuthor;
    }

    public void setPostAuthor(int postAuthor) {
        mPostAuthor = postAuthor;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getPostDate() {
        return mPostDate;
    }

    public void setPostDate(String postDate) {
        mPostDate = postDate;
    }

    public int getNoteId() {
        return mNoteId;
    }

    public void setNoteId(int noteId) {
        mNoteId = noteId;
    }

    public List<Attachment> getAttachments() {
        return mAttachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        mAttachments = attachments;
    }
}
