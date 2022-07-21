package es.udc.psi.psproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.MyViewHolder>{
    private final ArrayList<Favourites> mDataset;
    private static OnItemClickListener clickListener;

    public FavouritesAdapter(ArrayList<Favourites> mDataset) {
        this.mDataset = mDataset;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favourites_tile, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void setClickListener(OnItemClickListener onItemClickListener) {
        clickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(View view, int position);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name;
        public TextView number;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tile_name_tv);
            number = itemView.findViewById(R.id.tile_number_tv);
            itemView.setOnClickListener(this);
        }

        public void bind(Favourites favourite){
            this.name.setText(favourite.getName());
            this.number.setText(favourite.getNumber());
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());        }
    }

    public void addFavourite(Favourites favourite) {
        mDataset.add(favourite);
    }
    public Favourites getFavourite(int numFavourite){
        return mDataset.get(numFavourite);
    }
}
