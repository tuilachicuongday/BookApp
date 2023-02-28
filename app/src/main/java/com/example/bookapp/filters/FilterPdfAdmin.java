package com.example.bookapp.filters;

import android.widget.Filter;

import com.example.bookapp.adapters.AdapterCategory;
import com.example.bookapp.adapters.AdapterPdfAdmin;
import com.example.bookapp.models.Category;
import com.example.bookapp.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {
    ArrayList<ModelPdf> filterList;
    AdapterPdfAdmin adapterPdfAdmin;

    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        if (charSequence != null && charSequence.length() > 0 ){
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelPdf> filterdModel = new ArrayList<>();
            for(int i =0; i <filterList.size(); i ++)
            {
                if(filterList.get(i).getTitle().toUpperCase().contains(charSequence)){
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
        adapterPdfAdmin.pdfArrayList = (ArrayList<ModelPdf>)filterResults.values;
        adapterPdfAdmin.notifyDataSetChanged();
    }
}
