package Adapter;

import android.app.Fragment;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.CloudieNetwork.GadgetFlow.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.ArrayList;
import Helper.ScalingUtilities;
import Model.CategoryItem;

/**Created by vassilis Fouroulis on 4/10/2015. **/
public class AdapterCategories extends RecyclerView.Adapter<AdapterCategories.ViewHolder1> {
    private ArrayList<CategoryItem> categoryList;
    private Fragment fragment;
    private int cellSize;

    public static OnItemClickListener listener;// Define listener member variable

    /** Defines the listener interface **/
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public AdapterCategories(Context context, ArrayList<CategoryItem> categoryList, Fragment pFragment) {
        this.categoryList = categoryList;
        this.fragment = pFragment;
        this.cellSize = ScalingUtilities.getScreenWidth(context);
    }

    public class ViewHolder1 extends RecyclerView.ViewHolder {
        private TextView Name, Number;
        private ImageView image;

        public ViewHolder1(View Item) {
            super(Item);
            Name = (TextView) Item.findViewById(R.id.name_category);
            Number = (TextView) Item.findViewById(R.id.number_products);
            image = (ImageView) Item.findViewById(R.id.image_category);
            Item.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null)
                        listener.onItemClick(itemView, getAdapterPosition());
                }
            });
        }
    }


    @Override
    public AdapterCategories.ViewHolder1 onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        ViewHolder1 commentViewHolder = new ViewHolder1(view);

        return commentViewHolder;
    }

    @Override
    public void onBindViewHolder(AdapterCategories.ViewHolder1 holder, int position) {
        CategoryItem item = categoryList.get(position);

        holder.Name.setText(item.getTitle().replace("&amp;","&"));
        holder.Number.setText(item.getPost_count());
        Glide.with(fragment).
                load(item.getImage())
                .override(cellSize,250)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.image);

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}



