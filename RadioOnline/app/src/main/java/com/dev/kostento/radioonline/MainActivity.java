package com.dev.kostento.radioonline;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, CoverLoader.CoverLoaderCallBack,RecyclerViewAdapter.SelectStationListener {


    private Intent playIntent;
    private TextView txt_title, txt_subtitle;
    private TextView small_txt_title, small_txt_subtitle;
    private ImageView imageView, imageView_small;
    private MusicService musicSrv;
    private View player_slide, backgroundGradient;
    private View main_container_player;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private ProgressBar progressBar;
    private View btn_play;
    private View btn_stop;
    private RecyclerViewAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.loadLibrary("bass");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        RecyclerView mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter=new RecyclerViewAdapter(loadStations(), this);
        mRecyclerView.setAdapter(mAdapter);


        fab =   findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addNewStation();

            }
        });

        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView =  findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        txt_title = findViewById(R.id.status1);
        txt_subtitle = findViewById(R.id.status2);
        small_txt_title = findViewById(R.id.small_status1);
        small_txt_subtitle = findViewById(R.id.small_status2);
        imageView = findViewById(R.id.imageView_fg);
        imageView_small = findViewById(R.id.imageView_small);
        main_container_player = findViewById(R.id.main_container_player);
        backgroundGradient = findViewById(R.id.backgroundGradient);
        progressBar = findViewById(R.id.progress_bar_audio);
        btn_play = findViewById(R.id.play);
        btn_stop = findViewById(R.id.stop);

        player_slide = findViewById(R.id.slide_player);
        SlidingUpPanelLayout slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                player_slide.setAlpha(1.0f - slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });

        SeekBar volumeSeekbar = findViewById(R.id.seekBar);
        volumeSeekbar.setMax(100);
        volumeSeekbar.setProgress((int) MusicService.volume);
        volumeSeekbar.setProgress((int) (MusicService.volume));
        volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                musicSrv.setVolume(progress);

            }
        });

        playIntent = new Intent(this, MusicService.class);
        startService(playIntent);
    }

    private void addNewStation() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Додати нову станцію");

        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText editTextName = new EditText(MainActivity.this);
        editTextName.setHint("Введіть назву станції");
        layout.addView(editTextName);

        final EditText editTextUrl = new EditText(MainActivity.this);
        editTextUrl.setHint("Введіть посилання");
        layout.addView(editTextUrl);
        editTextUrl.setText("http://s12.myradiostream.com:15208/listen.mp3/;stream/1");


        builder.setView(layout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              String  m_TextName = editTextName.getText().toString();
              String  m_TextuUrl = editTextUrl.getText().toString();
                 mAdapter.addItem(m_TextName,m_TextuUrl);
                 saveStations(mAdapter.getData());
                Snackbar.make(findViewById(R.id.coordinatorLayout), "Додано нову станцію", Snackbar.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Скасувати", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

        loadStations();
    }

    private ArrayList<RadioStation> loadStations() {

        try {

            FileInputStream fis =  openFileInput("user_station.json");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            String json = sb.toString();
            Gson gson = new Gson();
            ArrayList<RadioStation> arrayList = gson.fromJson(json, new TypeToken<ArrayList<RadioStation>>(){}.getType());

            return arrayList;
        } catch (Exception e) {
            e.printStackTrace();
            return new  ArrayList<>();
        }
    }

    private void saveStations(ArrayList<RadioStation> arrayList){


        FileOutputStream outputStream;

        try {
            outputStream =  openFileOutput("user_station.json", Context.MODE_PRIVATE);
            outputStream.write(new Gson().toJson(arrayList).getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onResume() {
        bindService(playIntent, mConnection, BIND_AUTO_CREATE);
        super.onResume();

    }

    @Override
    public void onPause() {
        unbindService(mConnection);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMusicReceiver);

    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {


        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //   Toast.makeText(getApplicationContext(), "Service connected", Toast.LENGTH_SHORT).show();
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicSrv = binder.getService();
            if (musicSrv.isPlaying()) {
                new CoverLoader(MainActivity.this, musicSrv.getCurrent(), MainActivity.this, new Handler()).start();
            }

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {

            showAbout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private  void showAbout(){

        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.this);

        builder.setMessage("Онлайн радіо\n© Костенко Олександр 2018\nГрупа Кніт-21");

        builder.setCancelable(false);

        builder.setTitle("Про програму");

        builder.setPositiveButton("Закрити", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();


    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

          if (id == R.id.nav_share) {

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, musicSrv.getCurrent().getSongname()+" - "+musicSrv.getCurrent().getSongwriter());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);

        } else if (id == R.id.nav_about) {
            showAbout();
        }

        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void playButtonClick(View view) {

        if (musicSrv.isPlaying()) {
            musicSrv.stopPlayer();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            musicSrv.playPlayer();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMusicReceiver, new IntentFilter(MusicService.BROADCAST_ACTION));

    }


    private BroadcastReceiver mMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(((TrackInfo) intent.getSerializableExtra("trackInfo")));

        }
    };


    private void updateUI(TrackInfo trackInfo) {
        if (trackInfo != null) {
            txt_title.setText(trackInfo.getSongname());
            txt_subtitle.setText(trackInfo.getSongwriter());
            small_txt_title.setText(trackInfo.getSongname());
            small_txt_subtitle.setText(trackInfo.getSongwriter());
            new CoverLoader(this, trackInfo, this, new Handler()).start();
        }

        if (MusicService.getStatus() == MusicService.MUSIC_BUFFERING) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }

        if (MusicService.isPlaying()) {
            btn_play.setVisibility(View.INVISIBLE);
            btn_stop.setVisibility(View.VISIBLE);
        } else {
            btn_play.setVisibility(View.VISIBLE);
            btn_stop.setVisibility(View.INVISIBLE);

        }
    }

    @Override
    public void CoverSuccessfully(Bitmap resource, Palette palette) {


        int dominantColor, darkVibrantColor, darkMutedColor, vibrantColor, textVibrantColor;

        if (resource == null) {
            imageView_small.setImageResource(R.drawable.ic_music_note);
            imageView.setImageResource(R.drawable.ic_music_note);
            dominantColor = 0x000000;
            darkVibrantColor = dominantColor;
            darkMutedColor = darkVibrantColor;
            vibrantColor = 0xFFF06068;
            textVibrantColor = vibrantColor;
        } else {
            imageView_small.setImageBitmap(resource);
            imageView.setImageBitmap(resource);
            dominantColor = palette.getDominantColor(0x000000);
            darkVibrantColor = palette.getDarkVibrantColor(dominantColor);
            darkMutedColor = palette.getDarkMutedColor(palette.getMutedColor(darkVibrantColor));
            vibrantColor = palette.getVibrantColor(0xFFF06068);
            Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
            textVibrantColor = vibrantSwatch == null ? vibrantColor : vibrantSwatch.getTitleTextColor();

        }


        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{dominantColor, darkVibrantColor, darkMutedColor});

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(darkMutedColor);
        }
        backgroundGradient.setBackgroundDrawable(gradientDrawable);
        main_container_player.setBackgroundDrawable(gradientDrawable);
        fab.setBackgroundColor(vibrantColor);
        toolbar.setTitleTextColor(textVibrantColor);


    }


    @Override
    public void onSelect(RadioStation radioStation) {

        if (musicSrv.isPlaying()) {
            musicSrv.stopPlayer();
            musicSrv.resetPlayer(radioStation);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            musicSrv.resetPlayer(radioStation);
            progressBar.setVisibility(View.VISIBLE);
            musicSrv.playPlayer();
        }
    }
}
