package it.jaschke.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements ListOfBooks.Callback{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static boolean IS_TABLET = false;
    private BroadcastReceiver messageReceiver;

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";
    public static final String BOOK_ADDED_EVENT = "BOOK_ADDED_EVENT";
    public static final String BOOK_DELETED_EVENT = "BOOK_DELETED_EVENT";

    private static final String FRAG_TAG_LIST_OF_BOOKS = "LIST_OF_BOOKS";
    private static final String FRAG_TAG_ADD_BOOK = "ADD_BOOK";
    private static final String FRAG_TAG_BOOK_DETAILS = "BOOK_DETAILS";
    private static final String FRAG_TAG_ABOUT = "ABOUT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IS_TABLET = isTablet();
        //todo: fix layout for tablet
        if(IS_TABLET){
            setContentView(R.layout.activity_main_tablet);
        }else {
            setContentView(R.layout.activity_main);
        }

        //changed to using messages instead
        messageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        filter.addAction(BOOK_ADDED_EVENT);
        filter.addAction(BOOK_DELETED_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,filter);

        //wanted to try and use the toolbar instead of the actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set default values to settings, only done on entering the app for the first time
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        //load fragment if first launch
        if(savedInstanceState == null) {

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_container, new ListOfBooks(), FRAG_TAG_LIST_OF_BOOKS)
                    .addToBackStack(FRAG_TAG_LIST_OF_BOOKS)
                    .commit();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String pageStartOption = prefs.getString(getString(R.string.pref_startScreen_key), getString(R.string.pref_default_value));

            Log.d(LOG_TAG, "Preference: " + pageStartOption);

            if(pageStartOption.equalsIgnoreCase("1")){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_container, new AddBook(), FRAG_TAG_ADD_BOOK)
                        .addToBackStack(FRAG_TAG_ADD_BOOK)
                        .commit();

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        else if (id == R.id.action_about){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new About(), FRAG_TAG_ABOUT)
                    .addToBackStack(FRAG_TAG_ABOUT)
                    .commit();

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        //release resources
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        super.onDestroy();
    }

    //Callback from ListOfBooks fragment
    @Override
    public void onAddBook() {
        //todo: handle two-pane mode

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new AddBook(), FRAG_TAG_ADD_BOOK)
                .addToBackStack(FRAG_TAG_ADD_BOOK)
                .commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //Callback from ListOfBooks fragment
    @Override
    public void onBookSelected(String ean) {
        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);

        BookDetail fragment = new BookDetail();
        fragment.setArguments(args);

        int id = R.id.main_container;
        if(findViewById(R.id.right_container) != null){
            id = R.id.right_container;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(id, fragment)
                .addToBackStack(FRAG_TAG_BOOK_DETAILS)
                .commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(action.equalsIgnoreCase(MESSAGE_EVENT)) {
                if (intent.getStringExtra(MESSAGE_KEY) != null) {
                    Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();
                }
            }
            else if(action.equalsIgnoreCase(BOOK_ADDED_EVENT)){
                AddBook fragment = (AddBook) getSupportFragmentManager().findFragmentByTag(FRAG_TAG_ADD_BOOK);

                if(fragment != null)
                    fragment.restartLoader();
            }
            else if(action.equalsIgnoreCase(BOOK_DELETED_EVENT)){
                getSupportFragmentManager().popBackStack();
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()<2){
            finish();
        }
        else if(getSupportFragmentManager().getBackStackEntryCount() == 2)
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        super.onBackPressed();
    }
}























