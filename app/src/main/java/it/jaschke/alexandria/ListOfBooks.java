package it.jaschke.alexandria;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.data.AlexandriaContract;


public class ListOfBooks extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ListOfBooks.class.getSimpleName();

    private final String EAN_KEY = "ean_key";

    //changed to RecyclerView, wanted to give it a try
    //moved views to not need to get the view every time
    private RecyclerView bookList;
    private BookListAdapter bookListAdapter;
    private int position = RecyclerView.NO_POSITION;
    private EditText searchText;

    private final int LOADER_ID = 10;

    //added callback interface to communicate with activity
    public interface Callback {
        void onAddBook();
        void onBookSelected(String ean);
    }

    public ListOfBooks() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);

        searchText = (EditText) rootView.findViewById(R.id.searchText);
        rootView.findViewById(R.id.searchButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListOfBooks.this.restartLoader();
                    }
                }
        );

        bookList = (RecyclerView) rootView.findViewById(R.id.listOfBooks);
        bookList.setHasFixedSize(true);
        bookList.setLayoutManager(new LinearLayoutManager(getActivity()));

        //added view to give the user information
        View emptyView = rootView.findViewById(R.id.listOfBooks_empty);

        bookListAdapter = new BookListAdapter(getActivity(), new BookListAdapter.BookListAdapterOnClickHandler() {
            @Override
            public void onClick(String ean, BookListAdapter.ViewHolder viewHolder) {
                ((Callback) getActivity()).onBookSelected(ean);
                position = viewHolder.getAdapterPosition();
            }
        }, emptyView);

        bookList.setAdapter(bookListAdapter);

        //using a FAB to give the app a easier and more modern UI
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //callback to main activity method
                ((Callback) getActivity()).onAddBook();
            }
        });

        getActivity().setTitle(R.string.app_name);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(savedInstanceState != null) {
            if(savedInstanceState.containsKey(EAN_KEY)) {
                String ean = savedInstanceState.getString(EAN_KEY);
                searchText.setSelected(true);
                searchText.setText(ean);
                Log.d(LOG_TAG, "search text is: " + searchText.getText().toString());
            }
        }
        //moved and removed unnecessary restarting of loader
        restartLoader();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //handle saving if user has typed any search value
        if(searchText.getText().length() > 0) {
            outState.putString(EAN_KEY, searchText.getText().toString());
        }

        super.onSaveInstanceState(outState);
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String searchString =searchText.getText().toString();

        if(searchString.length() > 0) {
            final String selection = AlexandriaContract.BookEntry.TITLE + " LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";

            searchString = "%" + searchString + "%";
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[]{searchString, searchString},
                    null
            );
        }

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bookListAdapter.swapCursor(data);
        if (position != RecyclerView.NO_POSITION) {
            bookList.smoothScrollToPosition(position);
        }

        updateEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookListAdapter.swapCursor(null);
    }

    //user feedback if no books have been saved yet
    private void updateEmptyView() {
        if ( bookListAdapter.getItemCount() == 0 ) {
            TextView textView = (TextView) getView().findViewById(R.id.listOfBooks_empty);

            if ( null != textView ) {
                textView.setText(R.string.empty_list_of_books);
            }
        }
    }
}
