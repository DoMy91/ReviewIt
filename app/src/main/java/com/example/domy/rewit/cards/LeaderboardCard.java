package com.example.domy.rewit.cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.domy.rewit.R;
import com.example.domy.rewit.myApi.model.JsonMap;
import com.squareup.picasso.Picasso;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by Domy on 02/02/15.
 */
public class LeaderboardCard extends Card {
    private String name;
    private String city;
    private Integer total_rev;
    private Integer position;
    private String photoLink;
    private Context context;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public LeaderboardCard(Context context,JsonMap item,int position) {
        this(context, R.layout.leaderboard_card_inner_content,item,position);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public LeaderboardCard(Context context, int innerLayout,JsonMap item,int position) {
        super(context, innerLayout);
        this.context=context;
        name=item.get("FULLNAME").toString();
        city=item.get("LOCATION_NAME").toString();
        if(item.containsKey("PICTURE_LINK"))
            photoLink=item.get("PICTURE_LINK").toString();
        total_rev=Integer.parseInt(item.get("NUMBER_REW").toString());
        this.position=position;
        init();
    }

    /**
     * Init
     */
    private void init(){

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        ImageView imageView=(ImageView) parent.findViewById(R.id.imageView11);
        TextView name=(TextView) parent.findViewById(R.id.textView32);
        TextView city=(TextView) parent.findViewById(R.id.textView34);
        TextView total_rev=(TextView) parent.findViewById(R.id.textView37);
        TextView position=(TextView) parent.findViewById(R.id.textView35);
        Picasso.with(context)
                .load(photoLink)
                .placeholder(R.drawable.com_facebook_profile_picture_blank_portrait)
                .error(R.drawable.com_facebook_profile_picture_blank_portrait)
                .tag(context)
                .into(imageView);
        if(name!=null){
            name.setText(this.name);
        }
        if(city!=null){
            city.setText(this.city);
        }
        if(total_rev!=null){
            total_rev.setText(this.total_rev.toString());

        }
        if(position!=null){
            position.setText(this.position.toString());
        }
    }

}
