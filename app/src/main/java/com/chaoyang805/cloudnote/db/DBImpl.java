package com.chaoyang805.cloudnote.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.chaoyang805.cloudnote.model.Attachment;
import com.chaoyang805.cloudnote.model.Note;
import com.chaoyang805.cloudnote.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaoyang805 on 2015/10/19.
 */
public class DBImpl implements DbDAO {

    private NoteDBHelper mHelper;
    private SQLiteDatabase mDatabase;

    public DBImpl(Context context) {
        mHelper = new NoteDBHelper(context);
    }

    @Override
    public long insertUser(String name, String email, String passwordMd5) {
        mDatabase = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(NoteDBHelper.COLUMN_NAME_NAME, name);
        cv.put(NoteDBHelper.COLUMN_NAME_EMAIL, email);
        cv.put(NoteDBHelper.COLUMN_NAME_PASSWORD, passwordMd5);
        long id = mDatabase.insert(NoteDBHelper.TABLE_NAME_USERS, null, cv);
        mDatabase.close();
        return id;
    }

    @Override
    public int deleteUser(String email) {
        mDatabase = getWritableDatabase();
        int count = mDatabase.delete(NoteDBHelper.TABLE_NAME_USERS, NoteDBHelper.COLUMN_NAME_EMAIL + " = ?", new String[]{email});
        mDatabase.close();
        return count;
    }

    @Override
    public User queryUser(String eamil) {
        mDatabase = getReadableDatabase();
        Cursor c = mDatabase.query(NoteDBHelper.TABLE_NAME_USERS, null,
                NoteDBHelper.COLUMN_NAME_EMAIL + " = ?", new String[]{eamil}, null, null, null);
        if (c.moveToFirst()) {
            String name = c.getString(c.getColumnIndex(NoteDBHelper.COLUMN_NAME_NAME));
            String passwordMd5 = c.getString(c.getColumnIndex(NoteDBHelper.COLUMN_NAME_PASSWORD));
            String email = c.getString(c.getColumnIndex(NoteDBHelper.COLUMN_NAME_EMAIL));
            int id = c.getInt(c.getColumnIndex(NoteDBHelper.COLUMN_NAME_ID));
            User user = new User(id, name, passwordMd5, email);
            return user;
        }
        mDatabase.close();
        return null;
    }

    @Override
    public long insertNote(Note note) {
        //插入的时候先查看数据库中是否有这条记录，如果有的话，再比较modify的日期，决定是否更新。
        mDatabase = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(NoteDBHelper.COLUMN_NAME_NOTE_ID, note.getNoteId());
        cv.put(NoteDBHelper.COLUMN_NAME_POST_AUTHOR, note.getPostAuthor());
        cv.put(NoteDBHelper.COLUMN_NAME_POST_TITLE, note.getTitle());
        cv.put(NoteDBHelper.COLUMN_NAME_IS_POST_SYNCED, note.isSynced());
        cv.put(NoteDBHelper.COLUMN_NAME_POST_CONTENT, note.getContent());
        cv.put(NoteDBHelper.COLUMN_NAME_PLAIN_CONTENT, note.getPlainContent());
        cv.put(NoteDBHelper.COLUMN_NAME_POST_DATE, note.getPostDate());
        cv.put(NoteDBHelper.COLUMN_NAME_MODIFY_DATE,note.getModifyDate());
        long id = mDatabase.insert(NoteDBHelper.TABLE_NAME_POSTS, null, cv);
        mDatabase.close();
        return id;
    }

    public void deleteNotes(List<Integer> ids) {
        mDatabase = getWritableDatabase();
        for (Integer id : ids) {
            mDatabase.delete(NoteDBHelper.TABLE_NAME_POSTS, NoteDBHelper.COLUMN_NAME_ID + "=?", new String[]{id + ""});
        }
        mDatabase.close();
    }

    public int deleteNote(long id) {
        mDatabase = getWritableDatabase();
        int count = mDatabase.delete(NoteDBHelper.TABLE_NAME_POSTS, NoteDBHelper.COLUMN_NAME_ID + "=?", new String[]{id + ""});
        mDatabase.close();
        return count;
    }

    @Override
    public int deleteNote(Note note) {
        mDatabase = getWritableDatabase();
        int count = mDatabase.delete(NoteDBHelper.TABLE_NAME_POSTS, NoteDBHelper.COLUMN_NAME_ID + "=?", new String[]{note.getId() + ""});
        mDatabase.close();
        return count;
    }

    /**
     * 笔记同步到网络后将网络数据库中的id更新到本地数据库中
     *
     * @param _id    本地数据库中的id
     * @param noteId 网络数据库中的id
     * @return 影响到的行数
     */
    public int updateNoteId(int _id, int noteId) {
        mDatabase = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(NoteDBHelper.COLUMN_NAME_NOTE_ID, noteId);
        cv.put(NoteDBHelper.COLUMN_NAME_IS_POST_SYNCED, true);
        int count = mDatabase.update(NoteDBHelper.TABLE_NAME_POSTS, cv, NoteDBHelper.COLUMN_NAME_ID + "=?", new String[]{_id + ""});
        mDatabase.close();
        return count;
    }

    public void updateNote(Note note) {
        mDatabase = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(NoteDBHelper.COLUMN_NAME_POST_TITLE, note.getTitle());
        cv.put(NoteDBHelper.COLUMN_NAME_IS_POST_SYNCED, note.isSynced());
        cv.put(NoteDBHelper.COLUMN_NAME_POST_CONTENT, note.getContent());
        cv.put(NoteDBHelper.COLUMN_NAME_PLAIN_CONTENT, note.getPlainContent());
        cv.put(NoteDBHelper.COLUMN_NAME_POST_DATE, note.getPostDate());
        cv.put(NoteDBHelper.COLUMN_NAME_MODIFY_DATE,note.getModifyDate());
        mDatabase.update(NoteDBHelper.TABLE_NAME_POSTS, cv, NoteDBHelper.COLUMN_NAME_ID + "=?",
                new String[]{note.getId() + ""});
        mDatabase.close();
    }

    @Override
    public Note queryNote(long id) {
        mDatabase = getReadableDatabase();
        Cursor cursor = mDatabase.query(NoteDBHelper.TABLE_NAME_POSTS, null, NoteDBHelper.COLUMN_NAME_ID + "=?", new String[]{id + ""}, null, null, null);
        if (cursor.moveToFirst()) {
            Note note = new Note();
            int _id = cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_ID));
            int noteId = cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_NOTE_ID));
            int authorId = cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_POST_AUTHOR));
            int isSynced = cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_IS_POST_SYNCED));
            String title = cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_POST_TITLE));
            String content = cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_POST_CONTENT));
            String plainContent = cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_PLAIN_CONTENT));
            String postDate = cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_POST_DATE));
            String modifyDate = cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_MODIFY_DATE));

            note.setId(_id);
            note.setNoteId(noteId);
            note.setPostAuthor(authorId);
            note.setTitle(title);
            note.setContent(content);
            note.setPlainContent(plainContent);
            note.setPostDate(postDate);
            note.setModifyDate(modifyDate);
            note.setAttachments(queryAttachmentsByNote(id));
            note.setIsSynced(isSynced == 1);

            return note;
        }
        mDatabase.close();
        return null;
    }

    public Note queryNoteByNoteId(int noteId) {
        mDatabase = getReadableDatabase();
        Cursor cursor = mDatabase.query(NoteDBHelper.TABLE_NAME_POSTS, null, NoteDBHelper.COLUMN_NAME_NOTE_ID + "=?", new String[]{noteId + ""}, null, null, null);
        if (cursor.moveToFirst()) {
            Note note = new Note();
            int _id = cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_ID));
            int authorId = cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_POST_AUTHOR));
            int isSynced = cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_IS_POST_SYNCED));
            String title = cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_POST_TITLE));
            String content = cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_POST_CONTENT));
            String plainContent = cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_PLAIN_CONTENT));
            String postDate = cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_POST_DATE));
            String modifyDate = cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_MODIFY_DATE));

            note.setId(_id);
            note.setNoteId(noteId);
            note.setPostAuthor(authorId);
            note.setTitle(title);
            note.setContent(content);
            note.setPlainContent(plainContent);
            note.setPostDate(postDate);
            note.setModifyDate(modifyDate);
            note.setAttachments(queryAttachmentsByNote(_id));
            note.setIsSynced(isSynced == 1);
            return note;
        }
        mDatabase.close();
        return null;
    }


    @Override
    public List<Note> queryNotesByUserId(int userId) {
        mDatabase = getReadableDatabase();
        Cursor cursor = mDatabase.query(NoteDBHelper.TABLE_NAME_POSTS, null, NoteDBHelper.COLUMN_NAME_POST_AUTHOR + "=?", new String[]{userId + ""}, null, null, NoteDBHelper.COLUMN_NAME_ID + " DESC");
        List<Note> notes = new ArrayList<>();
        Note note;
        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_ID));
            int authorId = cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_POST_AUTHOR));
            int noteId = cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_NOTE_ID));
            int isSynced = cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_IS_POST_SYNCED));
            String title = cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_POST_TITLE));
            String content = cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_POST_CONTENT));
            String plainContent = cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_PLAIN_CONTENT));
            String postDate = cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_POST_DATE));
            String modifyDate = cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_MODIFY_DATE));

            note = new Note();
            note.setId(_id);
            note.setPostAuthor(authorId);
            note.setNoteId(noteId);
            note.setTitle(title);
            note.setContent(content);
            note.setPlainContent(plainContent);
            note.setPostDate(postDate);
            note.setModifyDate(modifyDate);
            note.setAttachments(queryAttachmentsByNote(_id));
            note.setIsSynced(isSynced == 1);
            notes.add(note);
        }
        mDatabase.close();
        return notes;
    }

    @Override
    public long insertAttachment(Attachment attachment) {
        mDatabase = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(NoteDBHelper.COLUMN_NAME_NOTE_ID, attachment.getNoteId());
        cv.put(NoteDBHelper.COLUMN_NAME_POST_AUTHOR, attachment.getPostAuthor());
        cv.put(NoteDBHelper.COLUMN_NAME_TYPE, attachment.getType());
        cv.put(NoteDBHelper.COLUMN_NAME_ATTACHEMENT_PATH, attachment.getPath());
        cv.put(NoteDBHelper.COLUMN_NAME_IS_POST_SYNCED, attachment.isSynced());
        long id = mDatabase.insert(NoteDBHelper.TABLE_NAME_ATTACHMENT, null, cv);
        mDatabase.close();
        return id;
    }

    public void insertAllAttachments(List<Attachment> attachments) {
        for (Attachment attachment : attachments) {
            insertAttachment(attachment);
        }
    }

    @Override
    public int deleteAttachment(Attachment attachment) {
        mDatabase = getWritableDatabase();
        int count = mDatabase.delete(NoteDBHelper.TABLE_NAME_ATTACHMENT, NoteDBHelper.COLUMN_NAME_ID + "=?", new String[]{attachment.getId() + ""});
        mDatabase.close();
        return count;
    }

    @Override
    public int deleteAttachmentByNoteId(long noteId) {
        mDatabase = getWritableDatabase();
        int count = mDatabase.delete(NoteDBHelper.TABLE_NAME_ATTACHMENT, NoteDBHelper.COLUMN_NAME_NOTE_ID + "=?", new String[]{noteId + ""});
        mDatabase.close();
        return count;
    }

    public List<Attachment> queryVideoAttachsByNoteId(long noteId){
        mDatabase = getReadableDatabase();
        Cursor cursor = mDatabase.query(NoteDBHelper.TABLE_NAME_ATTACHMENT, null, NoteDBHelper.COLUMN_NAME_NOTE_ID + "=? AND " +
                NoteDBHelper.COLUMN_NAME_TYPE + "=?", new String[]{noteId + "", 0 + ""}, null, null, null);
        List<Attachment> attachments = new ArrayList<>();
        Attachment attachment;
        while (cursor.moveToNext()) {
            attachment = new Attachment(
                    cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_ID)),
                    cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_POST_AUTHOR)),
                    cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_NOTE_ID)),
                    cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_IS_POST_SYNCED)) == 1,
                    cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_TYPE)),
                    cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_ATTACHEMENT_PATH)));
            attachments.add(attachment);
        }
        mDatabase.close();
        return attachments;
    }

    public List<Attachment> queryImageAttachsByNoteId(long noteId){
        mDatabase = getReadableDatabase();
        Cursor cursor = mDatabase.query(NoteDBHelper.TABLE_NAME_ATTACHMENT, null, NoteDBHelper.COLUMN_NAME_NOTE_ID + "=? AND " +
                NoteDBHelper.COLUMN_NAME_TYPE + "=?", new String[]{noteId + "", 1 + ""}, null, null, null);
        List<Attachment> attachments = new ArrayList<>();
        Attachment attachment;
        while (cursor.moveToNext()) {
            attachment = new Attachment(
                    cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_ID)),
                    cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_POST_AUTHOR)),
                    cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_NOTE_ID)),
                    cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_IS_POST_SYNCED)) == 1,
                    cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_TYPE)),
                    cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_ATTACHEMENT_PATH)));
            attachments.add(attachment);
        }
        mDatabase.close();
        return attachments;
    }

    @Override
    public List<Attachment> queryAttachmentsByNote(long noteId) {
        mDatabase = getReadableDatabase();
        Cursor cursor = mDatabase.query(NoteDBHelper.TABLE_NAME_ATTACHMENT, null, NoteDBHelper.COLUMN_NAME_NOTE_ID + "=?",
                new String[]{noteId + ""}, null, null, null);
        List<Attachment> attachments = new ArrayList<>();
        Attachment attachment;
        while (cursor.moveToNext()) {
            attachment = new Attachment(
                    cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_ID)),
                    cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_POST_AUTHOR)),
                    cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_NOTE_ID)),
                    cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_IS_POST_SYNCED)) == 1,
                    cursor.getInt(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_TYPE)),
                    cursor.getString(cursor.getColumnIndex(NoteDBHelper.COLUMN_NAME_ATTACHEMENT_PATH)));
            attachments.add(attachment);
        }
        mDatabase.close();
        return attachments;
    }

    private SQLiteDatabase getWritableDatabase() {
        if (mDatabase == null) {
            return mHelper.getWritableDatabase();
        }
        if (mDatabase.isOpen()) {
            mDatabase.close();
        }
        return mHelper.getWritableDatabase();
    }

    private SQLiteDatabase getReadableDatabase() {
        if (mDatabase == null) {
            return mHelper.getReadableDatabase();
        }
        if (mDatabase.isOpen()) {
            mDatabase.close();
        }
        return mHelper.getReadableDatabase();
    }
}
