package uk.ac.wlv.cw;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private final Context context;
    Activity activity;

    private final ArrayList<String> message_id, message_text;
    private final ArrayList<byte[]> message_image;

    private final ArrayList<Boolean> selectedItems;

    CustomAdapter(Activity activity,
                  Context context,
                  ArrayList<String> message_id,
                  ArrayList<String> message_text,
                  ArrayList<byte[]> message_image){

        this.activity = activity;
        this.context = context;
        this.message_id = message_id;
        this.message_text = message_text;
        this.message_image = message_image;

        // an array used for storing the ids of the selected items set initially to false as no item is selected at launch
        selectedItems = new ArrayList<>();
        for (int i = 0; i < message_id.size(); i++){
            selectedItems.add(false);
        }
    }

    @NonNull
    @Override
    // creates a view holder, uses my_row layout to return the rv as an object in the app
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    // sets the data to each item in the list
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.message_id_txt.setText(String.valueOf(message_id.get(position)));
        holder.message_text_txt.setText(String.valueOf(message_text.get(position)));

        byte[] imageBytes = message_image.get(position);
        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.message_image_view.setImageBitmap(bitmap);
        }

        // when an item is selected darken the background for user feedback
        if (selectedItems.get(position)){
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.selectedItemBackground));
        } else {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.normalItemBackground));
        }

        // if a item in the rv is clicked add it to the selected items array
        holder.mainLayout.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION){
                selectedItems.set(currentPosition, !selectedItems.get(currentPosition));
                notifyItemChanged(currentPosition);
            }
        });
    }

    // gets the ids from the selected items array
    public ArrayList<String> getSelectedItems(){
        ArrayList<String> selectedIds = new ArrayList<>();
        for (int i = 0; i <selectedItems.size(); i++){
            if (selectedItems.get(i)) {
                selectedIds.add(message_id.get(i));
            }
        }
        return selectedIds;
    }

    @Override
    public int getItemCount() {
        return message_id.size();
    }

    // matches the values from my_row to the recycler view
    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView message_id_txt, message_text_txt;
        ImageView message_image_view;
        LinearLayout mainLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            message_id_txt = itemView.findViewById(R.id.message_id_txt);
            message_text_txt = itemView.findViewById(R.id.message_text_txt);
            message_image_view = itemView.findViewById(R.id.message_image_txt);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}
