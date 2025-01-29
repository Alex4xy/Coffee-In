package com.thezec.coffeein.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.thezec.coffeein.Models.Recipe;
import com.thezec.coffeein.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aleksandar on 3/15/14.
 */
public class RecipesListViewAdapter extends ArrayAdapter<Recipe> implements Filterable {

    private Context context;
    private LayoutInflater inflater;
    private List<Recipe> list;
    private List<Recipe> mList;
    private List<String> mListItem;
    private ViewHolder viewHolder;
    private Typeface customFont;

    public RecipesListViewAdapter(Context context, List<Recipe> list) {
        super(context, R.layout.item_navigation_drawer, list);
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Recipe getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_navigation_drawer, null);
            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(R.id.item_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        Recipe item = getItem(position);
        customFont = Typeface.createFromAsset(context.getAssets(), "fonts/Mountain-Demo.otf");
        viewHolder.text.setText(item.getCoffee());
        viewHolder.text.setTypeface(customFont);

        return convertView;
    }

    @Override
    public Filter getFilter() {
        /**
         * A filter object which will
         * filter message key
         * */
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                list = (List<Recipe>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values. Only filtered values will be shown on the list
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation for publishing

                List<Recipe> FilteredArrList = new ArrayList<Recipe>();

                if (mList == null) {
                    mList = new ArrayList<Recipe>(list); // saves the original data in mOriginalValues
                }

                if (mListItem == null) {
                    mListItem = new ArrayList<String>();
                    for (Recipe recipe : mList) {
                        mListItem.add(recipe.getCoffee());
                    }
                }

                /**
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 **/

                if (constraint == null || constraint.length() == 0) {

                    /* CONTRACT FOR IMPLEMENTING FILTER : set the Original values to result which will be returned for publishing */
                    results.count = mList.size();
                    results.values = mList;
                } else {
                    /* Do the filtering */
                    constraint = constraint.toString().toLowerCase();

                    for (int i = 0; i < mListItem.size(); i++) {
                        String data = mListItem.get(i);
                        if (data.toLowerCase().startsWith(constraint.toString())) {
                            FilteredArrList.add(mList.get(i));
                        }
                    }

                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }

    private static class ViewHolder {
        private TextView text;
        private ImageView imgIcon;
        private TextView header;
    }
}


