package com.example.bookapp.filters;

import android.widget.Filter;

import com.example.bookapp.adapters.AdapterCategory;
import com.example.bookapp.models.Category;

import java.util.ArrayList;

public class FilterCate extends Filter {
    ArrayList<Category> filterList;
    AdapterCategory cateAdapter;

    public FilterCate(ArrayList<Category> filterList, AdapterCategory cateAdapter) {
        this.filterList = filterList;
        this.cateAdapter = cateAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        if (charSequence != null && charSequence.length() > 0 ){
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<Category> filterdModel = new ArrayList<>();
            for(int i =0; i <filterList.size(); i ++)
            {
                if(filterList.get(i).getCategory().toUpperCase().contains(charSequence)){
                    filterdModel.add(filterList.get(i));
                }
            }
                results.count = filterdModel.size();
                results.values = filterdModel;
        }
        else
        {
            results.count = filterList.size();
            results.values = filterList;
        }
        return  results;
    }
    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        cateAdapter.categoryArrayList = (ArrayList<Category>)filterResults.values;
        cateAdapter.notifyDataSetChanged();
    }
}
