package it.jaschke.alexandria;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;

public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private final int LOADER_ID = 1;
    private final String EAN_CONTENT="eanContent";

    //moved views to not need to get the view every time
    private EditText ean;
    private CardView bookCard;
    private TextView bookTitle;
    private TextView bookSubTitle;
    private TextView bookAuthors;
    private TextView bookCategories;
    private ImageView bookCover;

    public AddBook(){
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_add_book, container, false);

        ean = (EditText) rootView.findViewById(R.id.ean);
        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean =s.toString();

                if(Utility.isEanFormatValid(ean))
                    fetchBook(ean);

                //todo: remove
                //changed to using messaging instead of reloading every time
//                AddBook.this.restartLoader();
            }
        });

        bookCard = (CardView) rootView.findViewById(R.id.book_card);
        bookTitle = (TextView) rootView.findViewById(R.id.bookTitle);
        bookSubTitle = (TextView) rootView.findViewById(R.id.bookSubTitle);
        bookAuthors = (TextView) rootView.findViewById(R.id.authors);
        bookCategories = (TextView) rootView.findViewById(R.id.categories);
        bookCover = (ImageView) rootView.findViewById(R.id.bookCover);

        hideCard();

        Button nextButton = (Button) rootView.findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ean.setText("");
                hideCard();
            }
        });

        if(getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            rootView.findViewById(R.id.fab_scan).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //added barcode scanning
                    IntentIntegrator integrator = IntentIntegrator.forSupportFragment(AddBook.this);
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
                    integrator.setPrompt("Scan a barcode");
                    integrator.initiateScan();
                }
            });
        }
        else
            rootView.findViewById(R.id.fab_scan).setEnabled(false);

        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey(EAN_CONTENT)) {
                ean.setText(savedInstanceState.getString(EAN_CONTENT));
                ean.setHint("");
            }
        }

        getActivity().setTitle(R.string.add_book);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        //save any typed ean code
        if(ean!=null)
            outState.putString(EAN_CONTENT, ean.getText().toString());

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //handle result for barcode scanning
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(result != null) {
            if(result.getContents() != null) {
                String ean = result.getContents();

                if(Utility.isEanFormatValid(ean)) {
                    fetchBook(ean);
                }
                else
                    Toast.makeText(getActivity(), "Scanned number is not valid.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void fetchBook(String ean){

        //check so network is available first
        if(Utility.isNetworkAvailable(getActivity())) {
            Intent bookIntent = new Intent(getActivity(), BookService.class);
            bookIntent.putExtra(BookService.EAN, ean);
            bookIntent.setAction(BookService.FETCH_BOOK);
            getActivity().startService(bookIntent);
        }

        //give user info if no network
        updateEmptyView();
    }

    //changed to public so activity can reload if book was added
    public void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String ean= this.ean.getText().toString();

        if(!Utility.isEanFormatValid(ean))
            return null;

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        updateEmptyView();

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        this.bookTitle.setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        this.bookSubTitle.setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if(authors != null) {
            String[] authorsArr = authors.split(",");
            this.bookAuthors.setLines(authorsArr.length);
            this.bookAuthors.setText(authors.replace(",", "\n"));
        }
        else
            this.bookAuthors.setText("-");

        //changed to use Picasso instead, asyncTask is not a good solution for this.
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            Log.d(TAG, "Image Url: " + imgUrl);
            Picasso.with(getActivity())
                    .load(imgUrl)
                    .fit()
                    .centerCrop()
                    .into(bookCover);
        }
        else
            Log.d(TAG, "No valid image Url: " + imgUrl); //todo: add placeholder with picasso

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        bookCategories.setText(categories);

        showCard();
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    //changed to just show and hide card instead of every view part for the book
    private void showCard(){
        bookCard.setVisibility(View.VISIBLE);
    }

    private void hideCard(){
        bookCard.setVisibility(View.INVISIBLE);
    }

    //feedback to the user
    private void updateEmptyView(){
        TextView textView = (TextView) getView().findViewById(R.id.user_info);

        if(textView != null){
            if(!Utility.isNetworkAvailable(getActivity())) {
                textView.setText(R.string.no_network);
                textView.setVisibility(View.VISIBLE);
            }
            else
                textView.setVisibility(View.GONE);
        }
    }
}




























