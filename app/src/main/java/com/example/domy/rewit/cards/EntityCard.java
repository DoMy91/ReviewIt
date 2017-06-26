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
 * Created by Domy on 30/01/15.
 */
public class EntityCard extends Card {
    private String name;
    private String address;
    private Float avg;
    private Integer total_rev;
    private String photoLink;
    private Context context;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public EntityCard(Context context,JsonMap item) {
        this(context, R.layout.entity_card_inner_content,item);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public EntityCard(Context context, int innerLayout,JsonMap item) {
        super(context, innerLayout);
        this.context=context;
        name=item.get("name").toString();
        address=item.get("address").toString();
        if(item.containsKey("photoLink"))
            photoLink=item.get("photoLink").toString();
        avg=Float.parseFloat(item.get("AVG_VALUTATION").toString());
        total_rev=Integer.parseInt(item.get("COUNT").toString());
        init();
    }

    /**
     * Init
     */
    private void init(){

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        ImageView imageView=(ImageView) parent.findViewById(R.id.imageView10);
        TextView name=(TextView) parent.findViewById(R.id.textView27);
        TextView address=(TextView) parent.findViewById(R.id.textView29);
        RatingBar avg=(RatingBar) parent.findViewById(R.id.ratingBar4);
        TextView avgTV=(TextView) parent.findViewById(R.id.textView28);
        TextView total=(TextView) parent.findViewById(R.id.textView30);
        Picasso.with(getContext())
                .load(photoLink)
                .placeholder(R.drawable.places)
                .error(R.drawable.places)
                .tag(getContext())
                .into(imageView);
        if(name!=null){
            name.setText(this.name);
        }
        if(address!=null){
            address.setText(this.address);
        }
        if(avg!=null){
            avg.setRating(this.avg);
            avgTV.setText("("+this.avg.toString()+")");

        }
        if(total!=null){
            total.setText(this.total_rev.toString());
        }
    }
}
