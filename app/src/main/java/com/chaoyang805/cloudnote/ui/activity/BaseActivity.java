package com.chaoyang805.cloudnote.ui.activity;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.chaoyang805.cloudnote.R;
import com.chaoyang805.cloudnote.app.App;
import com.chaoyang805.cloudnote.db.DBImpl;
import com.chaoyang805.cloudnote.utils.Constant;
import com.chaoyang805.cloudnote.utils.ToastUtils;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

/**
 * Created by chaoyang805 on 2015/10/18.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    protected Toolbar mToolbar;

    protected FloatingActionButton mFab;

    protected FragmentManager mFragmentManager;
    /**
     * fragment容器的id
     */
    protected int mContainerId;

    protected AccountHeader mAccountHeader;

    protected Drawer mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mContainerId = R.id.container;
        setSupportActionBar(mToolbar);
        mFragmentManager = getFragmentManager();
        //initDrawer
        initDrawer();
    }

    /**
     * 退出登录时的操作
     */
    protected abstract void onLogout();

    protected abstract boolean onDrawerItemSelected(int identifier);

    protected abstract IProfile onCreateProfile();


    @Override
    public void onBackPressed() {
        if (mResult.isDrawerOpen()) {
            mResult.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 初始化drawer菜单
     */
    protected void initDrawer() {
        final IProfile profile = onCreateProfile();
        if (profile == null) {
            return;
        }
//        final IProfile profile = new ProfileDrawerItem()
//                .withName(mPassword).withEmail(mEmail)
//                .withIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.profile2));
        mAccountHeader = new AccountHeaderBuilder().withActivity(this)
                .addProfiles(profile,
                        new ProfileSettingDrawerItem().withName(getString(R.string.logout)).withIdentifier(Constant.IDENTI_LOGOUT))
                .withHeaderBackground(R.mipmap.header)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        int identifier = profile.getIdentifier();
                        switch (identifier) {
                            case Constant.IDENTI_LOGOUT:
                                onLogout();
                                break;
                            default:
                                return false;
                        }
                        return true;
                    }
                })
                .build();
        mResult = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withAccountHeader(mAccountHeader)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.my_notes).withIdentifier(Constant.IDENTI_MYNOTES),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(R.string.setting).withIdentifier(Constant.IDENTI_SETTING),
                        new PrimaryDrawerItem().withName(R.string.about).withIdentifier(Constant.IDENTI_ABOUT)
                )
                .withActionBarDrawerToggleAnimated(true)
                .withOnDrawerNavigationListener(new Drawer.OnDrawerNavigationListener() {
                    @Override
                    public boolean onNavigationClickListener(View clickedView) {
                        ToastUtils.showToast(BaseActivity.this, "navigation clicked");
                        return true;
                    }
                })
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        return onDrawerItemSelected(drawerItem.getIdentifier());
                    }
                })
                .build();
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public DBImpl getDatabase() {
        return ((App) getApplication()).getDB();
    }
}
