package com.thezec.coffeein.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.thezec.coffeein.Models.Recipe;
import com.thezec.coffeein.R;

import java.util.List;

public class GridAdapter extends BaseAdapter {

    Context context;
    private List<Recipe> list;
    private Typeface customFont;

    public GridAdapter(Context mContext, List<Recipe> list) {
        this.context = mContext;
        this.list = list;

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.grid_item, null);
            holder.text = (TextView) convertView.findViewById(R.id.gridRecipe);
            holder.image = (ImageView) convertView.findViewById(R.id.gridImage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Recipe item = (Recipe) getItem(position);
        customFont = Typeface.createFromAsset(context.getAssets(), "fonts/Mountain-Demo.otf");
        holder.text.setText(item.getCoffee());
        holder.text.setTypeface(customFont);

        Picasso.with(context)
                .load(item.getUrl())
                .fit()
                .centerCrop()
                .placeholder(R.id.gridImage)
                .into(holder.image);

        return convertView;
    }

    public class ViewHolder {
        public TextView text;
        public ImageView image;
    }

}
