package com.thezec.coffeein.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.thezec.coffeein.Models.NavigationDrawerListModel;
import com.thezec.coffeein.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Aleksandar on 9/18/2014.
 */
public class NavigationdDrawerAdapter extends BaseAdapter implements Filterable{

    private Context context;
    private List<NavigationDrawerListModel> list;
    private List<NavigationDrawerListModel> mList;
    private List<String> mListItem;
    private Typeface customFont;

    public NavigationdDrawerAdapter(Context context, ArrayList<NavigationDrawerListModel> navDrawerItems) {
        this.context = context;
        this.list = navDrawerItems;
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
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.item_navigation_drawer, null);
        }

        customFont = Typeface.createFromAsset(context.getAssets(), "fonts/Mountain-Demo.otf");
        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.item_icon);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.item_title);

        imgIcon.setImageResource(list.get(position).getIcon());
        txtTitle.setText(list.get(position).getTitle());
        txtTitle.setTypeface(customFont);

        // displaying count
        // check whether it set visible or not

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

                list = (List<NavigationDrawerListModel>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values. Only filtered values will be shown on the list
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation for publishing

                List<NavigationDrawerListModel> FilteredArrList = new ArrayList<NavigationDrawerListModel>();

                if (mList == null) {
                    mList = new ArrayList<NavigationDrawerListModel>(list); // saves the original data in mOriginalValues
                }

                if (mListItem == null) {
                    mListItem = new ArrayList<String>();
                    for (NavigationDrawerListModel recipe : mList) {
                        mListItem.add(recipe.getTitle());
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
}