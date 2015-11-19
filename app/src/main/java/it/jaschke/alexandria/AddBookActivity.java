package it.jaschke.alexandria;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import it.jaschke.alexandria.services.BookService;

public class AddBookActivity extends AppCompatActivity{

    private static final String FRAG_TAG_ADD_BOOK = "ADD_BOOK";

    private BroadcastReceiver messageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_book);

        //wanted to try and use the toolbar instead of the actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null){
            AddBook fragment = new AddBook();
            getSupportFragmentManager().beginTransaction().add(R.id.add_book_container, fragment, FRAG_TAG_ADD_BOOK).commit();
        }

        messageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter(BookService.MESSAGE_EVENT);
        filter.addAction(BookService.BOOK_ADDED_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, filter);
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

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(action.equalsIgnoreCase(BookService.MESSAGE_EVENT)) {
                if (intent.getStringExtra(BookService.MESSAGE_KEY) != null) {
                    Toast.makeText(AddBookActivity.this, intent.getStringExtra(BookService.MESSAGE_KEY), Toast.LENGTH_LONG).show();
                }
            }
            else if(action.equalsIgnoreCase(BookService.BOOK_ADDED_EVENT)){
                    AddBook fragment = (AddBook) getSupportFragmentManager().findFragmentByTag(FRAG_TAG_ADD_BOOK);

                    if (fragment != null)
                        fragment.restartLoader();
            }
        }
    }
}
