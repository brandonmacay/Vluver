package com.vluver.beta;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.facebook.login.LoginManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.vluver.beta.activities.PostActivity;
import com.vluver.beta.activities.searchinvluver.SearchInVluver;
import com.vluver.beta.adapter.BottomMenuItemAdapter;
import com.vluver.beta.adapter.BottomNavigationViewPager;
import com.vluver.beta.utils.FragmentHistory;

import java.io.File;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MaterialSearchBar.OnSearchActionListener {
    Fragment frag;
    public static BottomNavigationViewPager mBottomNavigationViewPager;
    public static BottomMenuItemAdapter mBottomMenuItemAdapter;
    private FragmentHistory fragmentHistory;
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigation;
    FloatingActionButton floatingActionButton;
    private TextView names,email;
    private ImageView avatar;
    MaterialSearchBar searchBar;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab_post);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PostActivity.class));
            }
        });

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        names = (TextView) headerView.findViewById(R.id.name_user);
        email = (TextView) headerView.findViewById(R.id.email_user);
        avatar = (ImageView) headerView.findViewById(R.id.avatar_user);
        searchBar = findViewById(R.id.searchBar);

        navigationView.setNavigationItemSelectedListener(this);
        searchBar.setOnSearchActionListener(this);
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchInVluver.class);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });
        searchBar.inflateMenu(R.menu.main);
        fragmentHistory = new FragmentHistory();
        mBottomNavigationViewPager = (BottomNavigationViewPager) findViewById(R.id.contentContainer);
        mBottomMenuItemAdapter = new BottomMenuItemAdapter(getSupportFragmentManager());
        mBottomNavigationViewPager.setAdapter(mBottomMenuItemAdapter);
        mBottomNavigationViewPager.setPagingEnable(true);
        frag = mBottomMenuItemAdapter.getCurrentFragment();
        bottomNavigation = (BottomNavigationView)findViewById(R.id.bottom_navigation);
        fragmentHistory.push(0);
        mBottomNavigationViewPager.setOffscreenPageLimit(4);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int numero = 0;
                switch (menuItem.getItemId()) {
                    case R.id.feed:
                        floatingActionButton.show();
                        mBottomNavigationViewPager.setCurrentItem(0, true);
                        frag = mBottomMenuItemAdapter.getCurrentFragment();
                        break;
                    case R.id.ads:
                        numero = 1;
                        floatingActionButton.hide();
                        mBottomNavigationViewPager.setCurrentItem(1, true);
                        frag = mBottomMenuItemAdapter.getCurrentFragment();
                        break;
                    case R.id.map:
                        numero = 2;
                        floatingActionButton.hide();
                        mBottomNavigationViewPager.setCurrentItem(2, true);
                        frag = mBottomMenuItemAdapter.getCurrentFragment();
                        break;
                    case R.id.notifications:
                        numero = 3;
                        floatingActionButton.hide();
                        mBottomNavigationViewPager.setCurrentItem(3, true);
                        frag = mBottomMenuItemAdapter.getCurrentFragment();
                        break;
                    default:
                        numero = 0;
                        floatingActionButton.show();
                        mBottomNavigationViewPager.setCurrentItem(0, true);
                        frag = mBottomMenuItemAdapter.getCurrentFragment();
                        break;
                }
                fragmentHistory.push(numero);
                return true;

            }
        });
        mBottomNavigationViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                switch (i){
                    case 0:
                        bottomNavigation.getMenu().findItem(R.id.feed).setChecked(true);
                        floatingActionButton.show();
                        break;
                    case 1:
                        bottomNavigation.getMenu().findItem(R.id.ads).setChecked(true);
                        floatingActionButton.hide();
                        break;
                    case 2:
                        bottomNavigation.getMenu().findItem(R.id.map).setChecked(true);
                        floatingActionButton.hide();
                        break;
                    case 3:
                        bottomNavigation.getMenu().findItem(R.id.notifications).setChecked(true);
                        floatingActionButton.hide();
                        break;
                }
                fragmentHistory.push(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        //bottomNavigation.setSelectedItemId(R.id.feed);
        //setCurrentItem(0);
        getdataUser();
    }

    private void getdataUser(){
        /*names.setText(Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName());
        email.setText(mAuth.getCurrentUser().getEmail());
        GlideLoadImages::(MainActivity.this, String.valueOf(mAuth.getCurrentUser().getPhotoUrl()),avatar);*/
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (fragmentHistory.getStackSize() > 1){
                int position = fragmentHistory.popPrevious();
                fragmentHistory.pop();
                switch (position) {
                    case 0:
                        bottomNavigation.setSelectedItemId(R.id.feed);
                        break;
                    case 1:
                        bottomNavigation.setSelectedItemId(R.id.ads);

                        break;
                    case 2:
                        bottomNavigation.setSelectedItemId(R.id.map);

                        break;
                    case 3:
                        bottomNavigation.setSelectedItemId(R.id.notifications);
                        break;
                    default:
                        bottomNavigation.setSelectedItemId(R.id.feed);
                        break;
                }
            }else{
                super.onBackPressed();

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            logout();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            updateUI();

        }
    }
    private void updateUI(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);

    }
    @Override
    protected void onResume() {
        int state = mBottomNavigationViewPager.getCurrentItem();
        //Toast.makeText(this, ""+state, Toast.LENGTH_SHORT).show();
        super.onResume();
    }

    public void logout(){
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        deleteCache(this);
        salir();
        // Intent get = getIntent();
        //String typelogin = get.getStringExtra("login_type");
    }
    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public void salir(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {

    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode) {
            case MaterialSearchBar.BUTTON_NAVIGATION:
                drawer.openDrawer(Gravity.LEFT);
                break;
            case MaterialSearchBar.BUTTON_SPEECH:
                break;
            case MaterialSearchBar.BUTTON_BACK:
                searchBar.disableSearch();
                break;
        }
    }

}