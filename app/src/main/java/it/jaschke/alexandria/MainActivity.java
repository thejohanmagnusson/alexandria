package it.jaschke.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import it.jaschke.alexandria.services.BookService;


public class MainActivity extends AppCompatActivity implements ListOfBooks.Callback{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private boolean twoPane;
    private BroadcastReceiver messageReceiver;

    private static final String FRAG_TAG_LIST_OF_BOOKS = "LIST_OF_BOOKS";
    private static final String FRAG_TAG_ADD_BOOK = "ADD_BOOK";
    private static final String FRAG_TAG_BOOK_DETAILS = "BOOK_DETAILS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //wanted to try and use the toolbar instead of the actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //using a FAB to give the app an easier and more modern UI
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddBook();
            }
        });

        //set default values to settings, only done on entering the app for the first time
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        //check if using tablet specific views
        if(findViewById(R.id.book_details_container) != null)
            twoPane = true;
        else
            twoPane = false;

        //if first launch
        if(savedInstanceState == null) {
            //add fragment(s)
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_container, new ListOfBooks(), FRAG_TAG_LIST_OF_BOOKS)
                    .commit();
        }

        //changed to using messages instead
        messageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter(BookService.MESSAGE_EVENT);
        filter.addAction(BookService.BOOK_ADDED_EVENT);
        filter.addAction(BookService.BOOK_DELETED_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,filter);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if(savedInstanceState == null){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String pageStartOption = prefs.getString(getString(R.string.pref_startScreen_key), getString(R.string.pref_default_value));

            if(pageStartOption.equalsIgnoreCase("1")){
                onAddBook();
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

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        //release resources
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        super.onDestroy();
    }

    //Callback from ListOfBooks(main) fragment
    @Override
    public void onAddBook() {

        //show fragment as dialog if large device
        if(getResources().getBoolean(R.bool.is_large_device)){
            AddBook dialogFragment = new AddBook();
            dialogFragment.show(getSupportFragmentManager(), FRAG_TAG_ADD_BOOK);
        }
        else{
            Intent intent = new Intent(this, AddBookActivity.class);
            startActivity(intent);
        }
    }

    //Callback from ListOfBooks fragment
    @Override
    public void onBookSelected(String ean) {

        if(twoPane) {
            Log.d(LOG_TAG, "TwoPane, ean: " + ean);

            Bundle args = new Bundle();
            args.putString(BookDetail.EAN_KEY, ean);

            BookDetail fragment = new BookDetail();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.book_details_container, fragment, FRAG_TAG_BOOK_DETAILS).commit();
        }
        else{
            //new activity and set data with extra
            Intent intent = new Intent(this, BookDetailActivity.class).putExtra(BookDetail.EAN_KEY, ean);
            startActivity(intent);
        }
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(action.equalsIgnoreCase(BookService.MESSAGE_EVENT)) {
                if (intent.getStringExtra(BookService.MESSAGE_KEY) != null) {
                    Toast.makeText(MainActivity.this, intent.getStringExtra(BookService.MESSAGE_KEY), Toast.LENGTH_LONG).show();
                }
            }
            else if(action.equalsIgnoreCase(BookService.BOOK_ADDED_EVENT)){
                AddBook dialogFragment = (AddBook) getSupportFragmentManager().findFragmentByTag(FRAG_TAG_ADD_BOOK);
                ListOfBooks fragment = (ListOfBooks) getSupportFragmentManager().findFragmentByTag(FRAG_TAG_LIST_OF_BOOKS);

                if (dialogFragment != null)
                    dialogFragment.restartLoader();

                if (fragment != null)
                    fragment.restartLoader();
            }
            else if(action.equalsIgnoreCase(BookService.BOOK_DELETED_EVENT)){
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAG_TAG_BOOK_DETAILS);

                if(fragment != null)
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }
    }
}























