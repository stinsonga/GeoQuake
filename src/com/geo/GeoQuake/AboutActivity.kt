package com.geo.GeoQuake

import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ImageButton
import android.widget.TextView

import org.w3c.dom.Text

import butterknife.Bind
import butterknife.ButterKnife

/**
 * Created by gstinson on 15-08-03.
 */
class AboutActivity : AppCompatActivity(), View.OnClickListener {

    @Bind(R.id.github_link)
    internal var mGithubTextView: TextView? = null

    @Bind(R.id.github_source_image)
    internal var mGitHubImage: ImageButton? = null

    @Bind(R.id.canada_license_text)
    internal var canadaLicense: TextView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_activity_layout)
        ButterKnife.bind(this)

        val bar = supportActionBar
        bar?.setHomeButtonEnabled(true)

        //TODO: fix null exception
//        mGithubTextView!!.text = Html.fromHtml(this.getString(R.string.github_source_link))
//        mGithubTextView!!.movementMethod = LinkMovementMethod.getInstance()
//        canadaLicense!!.text = Html.fromHtml(this.getString(R.string.canada_quake_license_link))
//        canadaLicense!!.movementMethod = LinkMovementMethod.getInstance()
    }

    /**
     * Handling onClick events
     * @param v View passed from activity
     */
    override fun onClick(v: View) {
        val intent = Intent(Intent.ACTION_VIEW)
        when (v.id) {
            R.id.github_source_image -> {
                intent.data = Uri.parse(this.getString(R.string.github_source_image_link))
                startActivity(intent)
            }
            else -> {
            }
        }
    }
}