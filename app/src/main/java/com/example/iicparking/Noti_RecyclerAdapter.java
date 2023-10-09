package com.example.iicparking;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<String> itemList;
    private Context context;

    // Constructor to initialize the adapter with a list of items and a context.
    public MyAdapter(Context context, List<String> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    // ViewHolder class to hold references to your item's views.
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.noti_recycler);
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
        String item = itemList.get(position);
        holder.textView.setText(item);
    }

    // Return the size of your dataset (invoked by the layout manager).
    @Override
    public int getItemCount() {
        return itemList.size();
    }
}

