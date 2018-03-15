package com.example.administrator.study_jh;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.administrator.study_jh.listview.TabItem;
import com.example.administrator.study_jh.listview.TabItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private long pressedTime = 0;
    public ListView tabMenuListView;
    private ArrayList<TabItem> tabMenu = new ArrayList<>();
    public TabItemAdapter tabAdapter;
    public View drawerView;
    public DrawerLayout drawer;
    int fragmentStack = 0;
    FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.nav_drawer);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerView = findViewById(R.id.tab_drawer);

        ActionBarDrawerToggle mDrawerToggle;

        mDrawerToggle = new ActionBarDrawerToggle(this, drawer,
                null, 0, 0) {

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //Toast.makeText(MainActivity.this, " drawer close", Toast.LENGTH_SHORT).show();

                tabAdapter.notifyDataSetChanged();
                invalidateOptionsMenu();
            }
        };

        drawer.setDrawerListener(mDrawerToggle);

        tabAdapter = new TabItemAdapter(this, R.layout.tab_listitem, tabMenu);
        tabMenuListView = (ListView)findViewById(R.id.tab_listview);
        tabMenu.add(new TabItem(getResources().getDrawable(R.drawable.home), new FileListHome(), "HOME"));

        tabMenuListView.setAdapter(tabAdapter);

        tabMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Fragment fName = tabMenu.get(position).getFragment();
                FragmentTransaction ft = fragmentManager.beginTransaction();

                if (fName != null) {
                    ft.replace(R.id.content_fragment_layout, fName);
                    ft.addToBackStack(String.valueOf(fragmentStack));
                    ft.commit();
                }

                drawer.closeDrawer(drawerView);
        }});

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fragmentStack = tabMenuListView.getCount();

                if(fragmentStack < 5) {
                    fragmentStack++;
                    tabMenu.add(new TabItem(getResources().getDrawable(R.drawable.folder), new FileList(), "Main Storage"));
                    tabAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(drawerView)) {
            drawer.closeDrawer(drawerView);
        }

        if (pressedTime == 0) {
            Toast.makeText(MainActivity.this, " 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
            pressedTime = System.currentTimeMillis();
        } else {
            int seconds = (int) (System.currentTimeMillis() - pressedTime);

            if (seconds > 2000) {
                Toast.makeText(MainActivity.this, " 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                pressedTime = 0;
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                    drawer.openDrawer(drawerView);
                break;

            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void checkPermission() {

        String[] permission = new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        ArrayList<String> notGranntedPermission = new ArrayList<>();
        final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

            }
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        PERMISSIONS_REQUEST_READ_CONTACTS);


        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! do the
                    // calendar task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
        }
    }

}
