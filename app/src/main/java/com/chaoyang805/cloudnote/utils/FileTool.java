package com.chaoyang805.cloudnote.utils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import com.chaoyang805.cloudnote.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chaoyang805 on 2015/10/25.
 */
public class FileTool {

    private static final String TAG = LogHelper.makeLogTag(FileTool.class);

    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

//    public static Bitmap getImageThumbnail(String path) {
//        String imagePath = getPath(context, uri);
//        return getImageThumbnail(imagePath);
//    }

    public static boolean isNewDate(String newDate,String oldDate) {
        return parseDateString(newDate).after(parseDateString(oldDate));
    }

    private static Date parseDateString(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        try {
            Date date = format.parse(dateStr);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getImageThumbnail(String imagePath) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        bitmap = BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false;
        int h = options.outHeight;
        int w = options.outWidth;
        int heightFactor = h / 1000;
        int widthFactor = w / 600;
        int factor = Math.max(heightFactor, widthFactor);
        if (factor < 0) {
            factor = 1;
        }
        options.inSampleSize = factor;
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        return bitmap;
    }

    public static Bitmap getVideoThumbnail(Context context, String videoPath, int kind) {
        Bitmap bitmap = null;
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        Canvas canvas = new Canvas(bitmap);
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.mipmap.attachment_video);
        int left = bitmap.getWidth() / 2 - logo.getWidth() / 2;
        int top = bitmap.getHeight() / 2 - logo.getHeight() / 2;
        canvas.drawBitmap(logo, left, top, null);
        return bitmap;
    }

    public static String getPostdateString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date(System.currentTimeMillis()));
    }

    private static final String IMG_REG = "<img.*src=(.*?)[^>]*?>";

    private static final String URL_REG = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
    private static final String pattern = "href=\"([^\"]*)\"";
    private static final String CONTENT_REG = "<[a-zA-Z]+.*?>([\\s\\S]*?)</[a-zA-Z]*>";

    public static String getPlainContent(String htmlStr) {
        Pattern p = Pattern.compile(CONTENT_REG, Pattern.MULTILINE);
        htmlStr = htmlStr.replace("&nbsp;", "");
        StringBuilder sb = new StringBuilder();
        Matcher matcher = p.matcher(htmlStr);
        while (matcher.find()) {
            String data = matcher.group(1).trim();
            if (!"".equals(data)) {
                sb.append(data);
            }
        }
        return sb.toString();
    }

    public static List<String> getHrefUrl(String htmlStr) {
        List<String> urls = new ArrayList<>();
        Matcher matcher = Pattern.compile(URL_REG).matcher(htmlStr);
        while (matcher.find()) {
            urls.add(matcher.group().substring(0, matcher.group().length() - 1));
        }
        return urls;
    }

    /***
     * 获取ImageSrc地址
     *
     * @param htmlStr
     * @return
     */
    public static List<String> getImageSrc(String htmlStr) {
        List<String> imageUrls = new ArrayList<>();
        Matcher matcher = Pattern.compile(IMG_REG, Pattern.MULTILINE).matcher(htmlStr);
        while (matcher.find()) {
            imageUrls.add(matcher.group().substring(0, matcher.group().length() - 1));
        }
        return imageUrls;
    }

}
