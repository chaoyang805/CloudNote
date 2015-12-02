package com.chaoyang805.cloudnote.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.chaoyang805.cloudnote.R;
import com.chaoyang805.cloudnote.ui.fragment.NoteListFragment;
import com.chaoyang805.cloudnote.utils.Constant;
import com.chaoyang805.cloudnote.utils.LogHelper;
import com.chaoyang805.cloudnote.utils.ToastUtils;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

public class NoteActivity extends BaseActivity implements View.OnClickListener, NoteListFragment.OnItemClickListener {

    private static final String TAG = LogHelper.makeLogTag(NoteActivity.class);
    private static final int CURRENT_LIST_FRAGMENT = 0;
    private static final int REQUEST_CODE_NOTE_DETAIL = 0x1234;

    private String mUserName;
    private String mEmail;
    private NoteListFragment mNoteListFragment;
    private int mCurrentFragment = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mUserName = getSharedPreferences(Constant.SP_NAME, MODE_PRIVATE).getString(Constant.SP_KEY_CUR_USER_NAME, "");
        mEmail = getSharedPreferences(Constant.SP_NAME, MODE_PRIVATE).getString(Constant.SP_KEY_CUR_USER_EMAIL, "");
        super.onCreate(savedInstanceState);
        mFab.setOnClickListener(this);
        mNoteListFragment = new NoteListFragment();
        mNoteListFragment.setOnItemClickListener(this);
        mFragmentManager.beginTransaction().replace(mContainerId, mNoteListFragment).commit();

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, NoteCreateActivity.class);
        intent.putExtra(NoteCreateActivity.EXTRA_REQUEST_TYPE, NoteCreateActivity.REQUEST_TYPE_CREATE);
        startActivity(intent);
    }

    @Override
    protected void onLogout() {
        ToastUtils.showToast(this, R.string.logout);
        //删除保存在本地的登录信息
        SharedPreferences.Editor e = getSharedPreferences(Constant.SP_NAME, MODE_PRIVATE).edit();
        e.remove(Constant.SP_KEY_CUR_USER_NAME);
        e.remove(Constant.SP_KEY_CUR_USER_EMAIL);
        e.commit();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected boolean onDrawerItemSelected(int identifier) {
        switch (identifier) {
            case Constant.IDENTI_MYNOTES:
                break;
            case Constant.IDENTI_SETTING:
                ToastUtils.showToast(this, "setting");
                break;
            case Constant.IDENTI_ABOUT:
                ToastUtils.showToast(this, "about");
                break;
            default:
                return false;
        }
        mResult.closeDrawer();
        return true;
    }

    @Override
    protected IProfile onCreateProfile() {
        IProfile profile = new ProfileDrawerItem()
                .withName(mUserName).withEmail(mEmail)
                .withIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.profile2));
        return profile;
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        mNoteListFragment.onActionModeFinished(mode);
        super.onActionModeFinished(mode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (mCurrentFragment) {
            case CURRENT_LIST_FRAGMENT:
                mNoteListFragment.onOptionsItemSelected(item);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * @param parent
     * @param view
     * @param position
     * @param _id      笔记在数据库中的id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long _id) {
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, _id);
        startActivityForResult(intent, REQUEST_CODE_NOTE_DETAIL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            mNoteListFragment.refreshList();
        }
    }

    public int getCurrentUseId() {
        return getSharedPreferences(Constant.SP_NAME, MODE_PRIVATE).getInt(Constant.SP_KEY_CUR_USER_ID, -1);
    }
}
