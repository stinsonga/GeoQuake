package com.geo.GeoQuake

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


/**
 * Created by gstinson on 15-08-03.
 */
class AboutActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mGithubTextView: TextView
    private lateinit var canadaLicense: TextView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_activity_layout)

        val bar = supportActionBar
        bar?.setHomeButtonEnabled(true)

        mGithubTextView = findViewById<TextView>(R.id.github_link)
        canadaLicense = findViewById<TextView>(R.id.canada_license_text)

        mGithubTextView.text = Html.fromHtml(this.getString(R.string.github_source_link))
        mGithubTextView.movementMethod = LinkMovementMethod.getInstance()
        canadaLicense.text = Html.fromHtml(this.getString(R.string.canada_quake_license_link))
        canadaLicense.movementMethod = LinkMovementMethod.getInstance()
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
        }
    }
}