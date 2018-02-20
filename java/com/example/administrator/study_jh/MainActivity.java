package com.example.administrator.study_jh;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

public class MainActivity extends AppCompatActivity {

    private long pressedTime = 0;
    public ListView tabMenuListView;
    private ArrayList<TabItem> tabMenu = new ArrayList<>();
    public TabItemAdapter tabAdapter;
    public View drawerView;
    public DrawerLayout drawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.nav_drawer);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerView = findViewById(R.id.tab_drawer);

        tabAdapter = new TabItemAdapter(this, R.layout.tab_listitem, tabMenu);
        tabMenuListView = (ListView)findViewById(R.id.tab_listview);
        tabMenu.add(new TabItem(new FileListHome(), "FileListHome"));
        tabMenu.add(new TabItem(new FileList(), "Main Storage"));

        tabMenuListView.setAdapter(tabAdapter);

        tabMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Fragment fName = tabMenu.get(position).getFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                if (fName != null) {
                    ft.replace(R.id.content_fragment_layout, fName);
                    ft.addToBackStack(null);
                    ft.commit();
                }


                drawer.closeDrawer(drawerView);
        }});

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tabMenu.add(new TabItem(new FileList(), "Main Storage"));
                tabAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void onBackPressed() {

        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if(drawer.isDrawerOpen(drawerView)) {
            drawer.closeDrawer(drawerView);
        }

        if ( pressedTime == 0 ) {
            Toast.makeText(MainActivity.this, " 한 번 더 누르면 종료됩니다." , Toast.LENGTH_SHORT).show();
            pressedTime = System.currentTimeMillis();
        }
        else {
            int seconds = (int) (System.currentTimeMillis() - pressedTime);

            if ( seconds > 2000 ) {
                Toast.makeText(MainActivity.this, " 한 번 더 누르면 종료됩니다." , Toast.LENGTH_SHORT).show();
                pressedTime = 0 ;
            }
            else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return true;
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
}
