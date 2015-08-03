package com.geo.GeoQuake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by gstinson on 15-08-03.
 */
public class AboutActivity extends Activity implements View.OnClickListener {

    Context mContext;
    Resources mResources;
    TextView mGithubTextView;
    TextView mGPlusLink;
    TextView mLinkedInLink;
    ImageButton mGitHubImage;
    ImageButton mGPlusImage;
    ImageButton mLinkedinImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity_layout);

        mContext = getApplicationContext();
        mResources = mContext.getResources();

        mGithubTextView = (TextView) findViewById(R.id.github_link);
        mGithubTextView.setText(Html.fromHtml(mResources.getString(R.string.github_source_link)));
        mGithubTextView.setMovementMethod(LinkMovementMethod.getInstance());

        mGPlusLink = (TextView) findViewById(R.id.g_plus_link);
        mGPlusLink.setText(Html.fromHtml(mResources.getString(R.string.google_plus_link)));
        mGPlusLink.setMovementMethod(LinkMovementMethod.getInstance());

        mLinkedInLink = (TextView) findViewById(R.id.linkedin_link);
        mLinkedInLink.setText(Html.fromHtml(mResources.getString(R.string.linkedin_link)));
        mLinkedInLink.setMovementMethod(LinkMovementMethod.getInstance());

        mGitHubImage = (ImageButton) findViewById(R.id.github_source_image);
        mGPlusImage = (ImageButton) findViewById(R.id.gplus_image);
        mLinkedinImage = (ImageButton) findViewById(R.id.linkedin_image);

    }

    /**
     * Handling onClick events
     * @param v
     */
    @Override
    public void onClick(View v){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        switch(v.getId()){
            case R.id.github_source_image:
                intent.setData(Uri.parse(mResources.getString(R.string.github_source_image_link)));
                startActivity(intent);
                break;
            case R.id.gplus_image:
                intent.setData(Uri.parse(mResources.getString(R.string.google_plus_image_link)));
                startActivity(intent);
                break;
            case R.id.linkedin_image:
                intent.setData(Uri.parse(mResources.getString(R.string.linkedin_image_link)));
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}