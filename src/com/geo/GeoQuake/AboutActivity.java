package com.geo.GeoQuake;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.w3c.dom.Text;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by gstinson on 15-08-03.
 */
public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.github_link)
    TextView mGithubTextView;

    @Bind(R.id.github_source_image)
    ImageButton mGitHubImage;

    @Bind(R.id.canada_license_text)
    TextView canadaLicense;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity_layout);
        ButterKnife.bind(this);

        android.support.v7.app.ActionBar bar = getSupportActionBar();
        if(bar != null) {
            bar.setHomeButtonEnabled(true);
        }

        mGithubTextView.setText(Html.fromHtml(this.getString(R.string.github_source_link)));
        mGithubTextView.setMovementMethod(LinkMovementMethod.getInstance());
        canadaLicense.setText(Html.fromHtml(this.getString(R.string.canada_quake_license_link)));
        canadaLicense.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Handling onClick events
     * @param v View passed from activity
     */
    @Override
    public void onClick(View v){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        switch(v.getId()){
            case R.id.github_source_image:
                intent.setData(Uri.parse(this.getString(R.string.github_source_image_link)));
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}