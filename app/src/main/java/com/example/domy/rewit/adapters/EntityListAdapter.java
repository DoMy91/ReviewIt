package com.example.domy.rewit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.domy.rewit.googlePlaces.GooglePlaces;
import com.example.domy.rewit.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Domy on 09/01/15.
 */
public class EntityListAdapter extends ArrayAdapter<JSONObject> {
    private final Context context;
    private final JSONObject[] values;

    public EntityListAdapter(Context context, JSONObject [] values) {
        super(context, R.layout.entitylist_item,values);
        this.context=context;
        this.values=values;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        String URLimage=null;
        if(view==null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.entitylist_item, parent, false);
            holder = new ViewHolder();
            holder.textView1 = (TextView) view.findViewById(R.id.firstLine);
            holder.textView2 = (TextView) view.findViewById(R.id.secondLine);
            holder.image = (ImageView) view.findViewById(R.id.icon);
            view.setTag(holder);
        }
        else
            holder=(ViewHolder) view.getTag();
        try {
            holder.textView1.setText(values[position].getString("name"));
            if(values[position].has("formatted_address")) //textsearch
                holder.textView2.setText(values[position].getString("formatted_address"));
            else //nearbysearch
                holder.textView2.setText(values[position].getString("vicinity"));
            if (values[position].has("photos")) {
                URLimage = GooglePlaces.photoLink(values[position].getJSONArray("photos").
                        getJSONObject(0).getString("photo_reference"));
            }
        }
        catch (JSONException exc){};
        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(context)
                .load(URLimage)
                .placeholder(R.drawable.places)
                .error(R.drawable.places)
                .tag(context)
                .into(holder.image);
       return view;
    }

    static class ViewHolder {
        ImageView image;
        TextView textView1;
        TextView textView2;
    }

    public JSONObject getJSONobj(int position){
        return values[position];
    }
}
