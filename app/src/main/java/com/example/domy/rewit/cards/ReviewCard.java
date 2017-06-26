package com.example.domy.rewit.cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.domy.rewit.R;
import com.example.domy.rewit.myApi.model.JsonMap;
import com.squareup.picasso.Picasso;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by Domy on 19/01/15.
 */
public class ReviewCard extends Card {
    private String userName;
    private String dateRev;
    private Float valutation;
    private String description;
    private String photoLink;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public ReviewCard(Context context,JsonMap item) {
        this(context, R.layout.review_card_inner_content,item);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public ReviewCard(Context context, int innerLayout,JsonMap item) {
        super(context, innerLayout);
        photoLink=item.get("PICTURE_LINK").toString();
        userName=item.get("FULLNAME").toString();
        dateRev=item.get("DATE_REW").toString();
        valutation=Float.parseFloat((String) item.get("VALUTATION"));
        description=item.get("DESCRIPTION").toString();
        init();
    }

    /**
     * Init
     */
    private void init(){

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        ImageView imageView=(ImageView) parent.findViewById(R.id.imageView7);
        TextView userName=(TextView) parent.findViewById(R.id.textView19);
        TextView dateRev=(TextView) parent.findViewById(R.id.textView21);
        RatingBar valutation=(RatingBar) parent.findViewById(R.id.ratingBar3);
        TextView description=(TextView) parent.findViewById(R.id.textView20);
        Picasso.with(getContext())
                .load(photoLink)
                .placeholder(R.drawable.com_facebook_profile_picture_blank_portrait)
                .error(R.drawable.com_facebook_profile_picture_blank_portrait)
                .tag(getContext())
                .into(imageView);
        if(userName!=null){
            userName.setText(this.userName);
        }
        if(dateRev!=null){
            dateRev.setText(this.dateRev);
        }
        if(valutation!=null){
            valutation.setRating(this.valutation);
        }
        if(description!=null){
            description.setText(this.description);
        }
    }
    @Override
    public int getType() {
        //Very important with different inner layouts
        return 1;
    }
}
