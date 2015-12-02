package com.chaoyang805.cloudnote.db;

import com.chaoyang805.cloudnote.model.Attachment;
import com.chaoyang805.cloudnote.model.Note;
import com.chaoyang805.cloudnote.model.User;

import java.util.List;

/**
 * Created by chaoyang805 on 2015/10/19.
 */
public interface DbDAO {

    public long insertUser(String name ,String email,String passwordMd5);

    public int deleteUser(String email);

    public User queryUser(String eamil);

    public long insertNote(Note note);

    public int deleteNote(Note note);

    public Note queryNote(long id);

    public List<Note> queryNotesByUserId(int userId);

    public long insertAttachment(Attachment attachment);

    public int deleteAttachment(Attachment attachment);

    public int deleteAttachmentByNoteId(long noteId);

    public List<Attachment> queryAttachmentsByNote(long noteId);


}
