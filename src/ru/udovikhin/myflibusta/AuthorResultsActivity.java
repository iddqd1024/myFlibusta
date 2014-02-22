package ru.udovikhin.myflibusta;

import android.annotation.TargetApi;
import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

public class AuthorResultsActivity extends ExpandableListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Show the Up button in the action bar.
		setupActionBar();
		
        // fire downloader task
        Intent intent = getIntent();
        String authorStr = intent.getStringExtra(SearchActivity.EXTRA_MESSAGE);
        String linkStr = intent.getStringExtra(SearchActivity.EXTRA_MESSAGE2);
        
        setTitle( getString(R.string.title_activity_author_results) + " \"" + authorStr + "\"");
        
        String fullLink = SearchActivity.HTTP_DOWNLOAD_ADDRESS + linkStr;
        
        //Toast.makeText(this, "Downloading " + fullLink, Toast.LENGTH_LONG).show();
        
        new PageDownloader(this, new AuthorPageHtmlParser(this)).execute(fullLink);

	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}


}
