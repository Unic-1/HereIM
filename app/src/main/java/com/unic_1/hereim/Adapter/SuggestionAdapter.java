package com.unic_1.hereim.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.unic_1.hereim.Model.ContactModel;
import com.unic_1.hereim.R;

import java.util.ArrayList;

/**
 * Created by unic-1 on 28/11/17.
 */

public class SuggestionAdapter extends ArrayAdapter<ContactModel> {

    private ArrayList<ContactModel> contactList;
    private ArrayList<ContactModel> suggestions;

    public SuggestionAdapter(@NonNull Context context, int resource, @NonNull ArrayList<ContactModel> objects) {
        super(context, resource, objects);
        contactList = objects;
        suggestions = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.suggestion_item, null);
        TextView numberSuggestion = (TextView) view.findViewById(R.id.numberSuggestion);
        TextView nameSuggestion = (TextView) view.findViewById(R.id.nameSuggestion);
        numberSuggestion.setText(contactList.get(position).getNumber());
        nameSuggestion.setText(contactList.get(position).getName());
        return view;
    }


    @NonNull
    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            String str = ((ContactModel)(resultValue)).getNumber();
            return str;
        }
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.i("Filter", "performFiltering: ");
            if(constraint != null) {
                suggestions.clear();
                for (ContactModel contact : contactList) {
                    if(Character.isAlphabetic(constraint.charAt(0))) {
                        if(contact.getName().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                            suggestions.add(contact);
                        }
                    } else {
                        if (contact.getNumber().startsWith(constraint.toString())) {
                            suggestions.add(contact);
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<ContactModel> filteredList = (ArrayList<ContactModel>) results.values;
            if(results != null && results.count > 0) {
                clear();
                for (ContactModel c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    };
}
