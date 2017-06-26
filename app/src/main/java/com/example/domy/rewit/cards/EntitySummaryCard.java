package com.example.domy.rewit.cards;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.example.domy.rewit.googlePlaces.GooglePlaces;
import com.example.domy.rewit.R;
import com.example.domy.rewit.myApi.model.JsonMap;
import com.example.domy.rewit.myApi.model.ReviewListBean;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by Domy on 19/01/15.
 */
public class EntitySummaryCard extends Card {
    private String name;
    private String address;
    private Float avg;
    private int totalRev;
    private JsonMap ratings;
    private String photoLink;
    protected TextView mTitle;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public EntitySummaryCard(Context context,JSONObject entity,ReviewListBean response) {
        this(context, R.layout.entity_summary_card_inner_content,entity,response);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public EntitySummaryCard(Context context, int innerLayout,JSONObject entity,ReviewListBean response) {
        super(context, innerLayout);
        try{
            name=entity.getString("name");
            if(entity.has("formatted_address"))
                address=entity.getString("formatted_address");
            else
                address=entity.getString("vicinity");
            avg=response.getAvg();
            if (entity.has("photos")) {
                photoLink = GooglePlaces.photoLink(entity.getJSONArray("photos").
                        getJSONObject(0).getString("photo_reference"));
            }
            ratings=response.getRatings();
            totalRev=response.getTotalRev();
        }
        catch (JSONException exc){
            Log.e("EntitySummaryCard",exc.toString());
        }
        init();
    }

    /**
     * Init
     */
    private void init(){

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        ImageView imageView=(ImageView) parent.findViewById(R.id.imageView);
        mTitle = (TextView) parent.findViewById(R.id.textView3);
        TextView addressTV= (TextView) parent.findViewById(R.id.textView4);
        TextView avgTV= (TextView) parent.findViewById(R.id.textView18);
        RatingBar mRatingBar = (RatingBar) parent.findViewById(R.id.ratingBar2);
        RoundCornerProgressBar[] progressBars=new RoundCornerProgressBar[5];
        progressBars[0]=(RoundCornerProgressBar) parent.findViewById(R.id.progressBar4);
        progressBars[1]=(RoundCornerProgressBar) parent.findViewById(R.id.progressBar5);
        progressBars[2]=(RoundCornerProgressBar) parent.findViewById(R.id.progressBar6);
        progressBars[3]=(RoundCornerProgressBar) parent.findViewById(R.id.progressBar7);
        progressBars[4]=(RoundCornerProgressBar) parent.findViewById(R.id.progressBar8);
        TextView[] textViews=new TextView[5];
        textViews[0]=(TextView) parent.findViewById(R.id.textView9);
        textViews[1]=(TextView) parent.findViewById(R.id.textView11);
        textViews[2]=(TextView) parent.findViewById(R.id.textView13);
        textViews[3]=(TextView) parent.findViewById(R.id.textView15);
        textViews[4]=(TextView) parent.findViewById(R.id.textView17);
        Picasso.with(getContext())
                .load(photoLink)
                .placeholder(R.drawable.places)
                .error(R.drawable.places)
                .tag(getContext())
                .into(imageView);
        if (name!=null)
            mTitle.setText(name);

        if (address!=null)
            addressTV.setText(address);

        if(avg!=null){
            avgTV.setText(avg.toString());
        }

        if (mRatingBar!=null) {
            mRatingBar.setNumStars(5);
            mRatingBar.setMax(5);
            mRatingBar.setStepSize(0.1f);
            mRatingBar.setRating(avg);
        }
       for(Integer i=1;i<=5;i++){
           progressBars[i-1].setMax(totalRev);
           BigDecimal progress=(BigDecimal) ratings.get(i.toString());
           progressBars[i-1].setProgress(progress.intValue());
           textViews[i-1].setText(progress.toString());
       }

    }
    @Override
    public int getType() {
        //Very important with different inner layouts
        return 0;
    }
}