package com.chaoyang805.cloudnote.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.chaoyang805.cloudnote.model.Attachment;
import com.chaoyang805.cloudnote.model.Note;
import com.chaoyang805.cloudnote.utils.Constant;
import com.chaoyang805.cloudnote.utils.LogHelper;
import com.chaoyang805.cloudnote.utils.ToastUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaoyang805 on 2015/10/20.
 */
public class NetConnection {
    private static final String TAG = LogHelper.makeLogTag(NetConnection.class);

    private Context mContext;

    public NetConnection(Context context) {
        mContext = context;
        // System.setProperty("http.keepAlive", "false");
    }

    public boolean isNetWorkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isAvailable();
        }
        return false;
    }

    public interface OnNetFinishedCallback {
        void onUploadFinished(ArrayList<String> results);

        void onRequestFinished(String result);

    }

    public void uploadFile(final List<Attachment> attachments, @NonNull final OnNetFinishedCallback callback) {
        ArrayList<String> paths = new ArrayList<>();
        for (Attachment attachment : attachments) {
            paths.add(attachment.getPath());
        }
        uploadFile(paths, callback);
    }

    public void uploadFile(final ArrayList<String> filePaths, @NonNull final OnNetFinishedCallback callback) {

        final String end = "\r\n";
        final String twoHyphens = "--";
        final String boundary = "******";

        if (!isNetWorkAvailable()) {
            ToastUtils.showToast(mContext, "网络不可用");
            return;
        }

        new AsyncTask<Void, Void, ArrayList<String>>() {

            @Override
            protected ArrayList<String> doInBackground(Void... params) {

                ArrayList<String> results = new ArrayList<>();
                for (String filePath : filePaths) {
                    try {

                        URL url = new URL(Constant.UPLOAD_URL);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setChunkedStreamingMode(128 * 1024);

                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        connection.setUseCaches(true);
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Connection", "Keep-Alive");
                        connection.setRequestProperty("Chaset", "UTF-8");
                        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                        DataOutputStream dos = new DataOutputStream(connection.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + end);
                        dos.writeBytes("Content-Disposition: form-data;name=\"uploadedfile\"; filename=\""
                                + filePath.substring(filePath.lastIndexOf("/") + 1)
                                + "\""
                                + end);
                        dos.writeBytes(end);

                        FileInputStream fis = new FileInputStream(filePath);
                        byte[] buffer = new byte[8192];
                        int count = 0;
                        while ((count = fis.read(buffer)) != -1) {
                            dos.write(buffer, 0, count);
                        }
                        fis.close();
                        dos.writeBytes(end);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
                        dos.flush();
                        InputStreamReader isr = new InputStreamReader(connection.getInputStream(), "UTF-8");
                        BufferedReader bfr = new BufferedReader(isr);
                        String result = bfr.readLine();
                        dos.close();
                        isr.close();
                        results.add(result);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        continue;
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
                return results;
            }

            @Override
            protected void onPostExecute(ArrayList<String> results) {
                callback.onUploadFinished(results);
            }
        }.execute();

    }

    public void getNotes(int userId, OnNetFinishedCallback callback) {
        String[] keys = new String[]{"action", "user_id"};
        String[] values = new String[]{"get_posts", userId + ""};
        operatePost(Constant.OPERATE_POST_URL, Constant.METHOD_POST, keys, values, callback);
    }

    public void deleteNote(String jsonArr, final OnNetFinishedCallback callback) {
        String[] keys = new String[]{"action", "post_ids"};
        String[] values = new String[]{"delete", jsonArr};
        operatePost(Constant.OPERATE_POST_URL, Constant.METHOD_POST, keys, values, callback);
    }

    public void updateNote(Note note, final OnNetFinishedCallback callback) {
        String[] keys = new String[]{"action", "post_id", "post_title", "post_content", "post_local_id"};
        String[] values = new String[]{"update", note.getNoteId() + "", note.getTitle(), note.getContent(), note.getId() + ""};
        operatePost(Constant.OPERATE_POST_URL, Constant.METHOD_POST, keys, values, callback);
    }

    public void publishNote(int userId, Note note, final OnNetFinishedCallback callback) {
        String[] keys = new String[]{"action", "post_author", "post_title", "post_content", "post_local_id"};
        String[] values = new String[]{"new", userId + "", note.getTitle(), note.getContent(), note.getId() + ""};
        operatePost(Constant.OPERATE_POST_URL, Constant.METHOD_POST, keys, values, callback);
    }

    public void operatePost(final String requestUrl, final String method, String[] keys, String[] values, final OnNetFinishedCallback callback) {

        final StringBuilder urlParams = new StringBuilder();

        if (!isNetWorkAvailable()) {
            ToastUtils.showToast(mContext, "网络不可用");
            return;
        }

        for (int i = 0; i < keys.length; i++) {
            urlParams.append(keys[i]);
            urlParams.append("=");
            try {
                urlParams.append(URLEncoder.encode(values[i], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (i != keys.length - 1) {
                urlParams.append("&");
            }
        }

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                URL url = null;
                InputStream in = null;
                InputStreamReader isr = null;
                BufferedReader bfr = null;
                HttpURLConnection connection = null;
                BufferedWriter bfw = null;
                try {
                    switch (method) {
                        case Constant.METHOD_POST:
                            url = new URL(requestUrl);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setDoOutput(true);
                            bfw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), Constant.CHAR_SET));
                            bfw.write(urlParams.toString());
                            bfw.flush();
                            bfw.close();
                            break;
                        case Constant.METHOD_GET:
                            url = new URL(requestUrl + "?" + urlParams.toString());
                            connection = (HttpURLConnection) url.openConnection();
                            break;
                    }
                    in = connection.getInputStream();
                    isr = new InputStreamReader(in);
                    bfr = new BufferedReader(isr);
                    String line = null;
                    StringBuffer resultBuffer = new StringBuffer();
                    while ((line = bfr.readLine()) != null) {
                        resultBuffer.append(line);
                    }
                    bfr.close();
                    isr.close();
                    in.close();
                    return resultBuffer.toString();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "{\"result\":100}";
            }

            @Override
            protected void onPostExecute(final String result) {
                if (callback != null) {
                    callback.onRequestFinished(result);
                }
            }

        }.execute();
    }

}
