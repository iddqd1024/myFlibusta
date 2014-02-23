package ru.udovikhin.myflibusta;

import android.annotation.TargetApi;
import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

public class SequenceResultsActivity extends ExpandableListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Show the Up button in the action bar.
		setupActionBar();
		
        // fire downloader task
        Intent intent = getIntent();
        String sequenceStr = intent.getStringExtra(SearchActivity.EXTRA_MESSAGE);
        String linkStr = intent.getStringExtra(SearchActivity.EXTRA_MESSAGE2);
        
        setTitle( getString(R.string.title_activity_sequence_results) + " \"" + sequenceStr + "\"");
        
        String fullLink = SearchActivity.HTTP_DOWNLOAD_ADDRESS + linkStr;
                
        new PageDownloader(this, new SequencePageHtmlParser(this, sequenceStr)).execute(fullLink);
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
