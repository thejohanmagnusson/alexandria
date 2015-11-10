package it.jaschke.alexandria.api;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;

/**
 * Created by saj on 11/01/15.
 */
public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.ViewHolder> {

    public static final String LOG_TAG = BookListAdapter.class.getSimpleName();

    private Cursor cursor;
    private final Context context;
    private final BookListAdapterOnClickHandler clickHandler;
    private final View emptyView;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final ImageView bookCover;
        public final TextView bookTitle;
        public final TextView bookSubTitle;

        public ViewHolder(View itemView) {
            super(itemView);

            bookCover = (ImageView) itemView.findViewById(R.id.fullBookCover);
            bookTitle = (TextView) itemView.findViewById(R.id.listBookTitle);
            bookSubTitle = (TextView) itemView.findViewById(R.id.listBookSubTitle);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //get item position from adapter
            int position = getAdapterPosition();

            //move to the position on the cursor, get data and handle click
            cursor.moveToPosition(position);
            String ean = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID));
            clickHandler.onClick(ean, this);
        }
    }

    public interface BookListAdapterOnClickHandler {
        void onClick(String ean, ViewHolder viewHolder);
    }

    public BookListAdapter(Context context, BookListAdapterOnClickHandler clickHandler, View emptyView) {
        this.context = context;
        this.clickHandler = clickHandler;
        this.emptyView = emptyView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(parent instanceof RecyclerView){
            int layoutId = R.layout.book_list_item;

            View view =LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);

            return new ViewHolder(view);
        }
        else
            throw new RuntimeException("Not bound to RecyclerViewSelection");
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);

        String imgUrl = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        Picasso.with(context)
                .load(imgUrl)
                .fit()
                .centerCrop()
                .into(holder.bookCover);

        String bookTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        holder.bookTitle.setText(bookTitle);

        String bookSubTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        holder.bookSubTitle.setText(bookSubTitle);
    }

    @Override
    public int getItemCount() {
        if(cursor == null)
            return 0;

        return cursor.getCount();
    }

    public void swapCursor(Cursor newCursor){
        cursor = newCursor;
        notifyDataSetChanged();
        emptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
}
