package com.chaoyang805.cloudnote.utils;

/**
 * Created by chaoyang805 on 2015/10/20.
 */
public class Constant {

    public static final int TYPE_PHOTO = 1;

    public static final int TYPE_VIDEO = 0;

    public static final int IDENTI_LOGOUT = 0x1010;

    public static final int IDENTI_MYNOTES = 0x1011;

    public static final int IDENTI_ABOUT = 0x1012;

    public static final int IDENTI_SETTING = 0x1013;

    public static final String METHOD_GET = "GET";

    public static final String METHOD_POST = "POST";

    public static final int REQUEST_CODE_GET_IMAGE_FROM_GALLERY = 0x2000;

    public static final int REQUEST_CODE_GET_IMAGE_FROM_CAMERA = 0x2001;

    public static final int REQUEST_CODE_GET_VIDEO_FROM_CAMERA = 0x2002;

    public static final int REQUEST_CODE_CREATE_NEW_NOTE = 0x2003;

//http://localhost/wordpress/wp-client/wp-user-login.php?action=log_in&user_name=testUser&password=$P$BI/1zouYN8vD.PMfh5q6LyKmCv64/J1

//    public static final String LOGIN_URL = "http://192.168.0.107/wordpress/wp-client/wp-user-login.php";
    public static final String LOGIN_URL = "http://1.jokesdemo.sinaapp.com/wp-client/wp-user-login.php";

//        public static final String OPERATE_POST_URL = "http://192.168.0.107/wordpress/wp-client/wp-post-new.php";
    public static final String OPERATE_POST_URL = "http://1.jokesdemo.sinaapp.com/wp-client/wp-modify-post.php";

    public static final String UPLOAD_URL = "http://1.jokesdemo.sinaapp.com/wp-client/wp-receive-file.php";
//    public static final String UPLOAD_URL = "http://192.168.0.107/wordpress/wp-client/wp-receive-file.php";

    public static final String CHAR_SET = "UTF-8";

    public static final String SP_NAME = "cloudnote";

    public static final String SP_KEY_CUR_USER_EMAIL = "current_user_email";

    public static final String SP_KEY_CUR_USER_NAME = "current_user_name";

    public static final String SP_KEY_CUR_USER_ID = "current_user_id";

    public static final String JSON_KEY_USER_ID = "user_id";

    public static final String JSON_KEY_USER_NAME = "user_name";

    public static final String JSON_KEY_EMAIL = "email";

    public static final String JSON_KEY_RESULT = "result";

    public static final String JSON_KEY_NEW_NOTE = "new_note";

    public static final String ACTION_LOGIN = "log_in";

    public static final String ACTION_REGISTER = "register";

}
