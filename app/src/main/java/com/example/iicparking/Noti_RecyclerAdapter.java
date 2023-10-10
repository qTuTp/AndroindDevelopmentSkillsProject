package com.example.iicparking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iicparking.Class.Notification;

import java.util.List;

public class Noti_RecyclerAdapter extends RecyclerView.Adapter<Noti_RecyclerAdapter.ViewHolder> {

    private List<Notification> itemList;
    private Context context;
    private OnItemClickListener onItemClickListener; // Interface for item click callbacks

    // Constructor to initialize the adapter with a list of items and a context.
    public Noti_RecyclerAdapter(Context context, List<Notification> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    // Define an interface to handle item click events
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    // Setter method for the click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    // ViewHolder class to hold references to your item's views.
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView date;
        public TextView description;
        public ConstraintLayout card;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            date = view.findViewById(R.id.dateTextView);
            description = view.findViewById(R.id.description);
            card = view.findViewById(R.id.notifCard);

            // Set an item click listener
            card.setOnClickListener(v -> {
                //Open notification detail popup
            });
        }
    }

    // Create new views (invoked by the layout manager).
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.noti_recycler, parent, false);
        return new ViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager).
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Notification item = itemList.get(position);
        holder.title.setText(item.getTitle());
        holder.date.setText(item.getDate());
        holder.description.setText(item.getDescription());
    }

    // Return the number of items in the data set.
    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
