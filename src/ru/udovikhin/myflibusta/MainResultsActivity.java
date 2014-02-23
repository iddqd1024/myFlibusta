package ru.udovikhin.myflibusta;

import java.util.Map;

import ru.udovikhin.myflibusta.HtmlParser.SearchResults;
import android.annotation.TargetApi;
import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

public class MainResultsActivity extends ExpandableListActivity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		//setContentView(R.layout.activity_results);
		// Show the Up button in the action bar.
		setupActionBar();
				                
        // fire downloader task
        Intent intent = getIntent();
        String searchStr = intent.getStringExtra(SearchActivity.EXTRA_MESSAGE);
        String linkStr = intent.getStringExtra(SearchActivity.EXTRA_MESSAGE2);

        setTitle( getString(R.string.title_activity_mainsearch_results) + " \"" + searchStr + "\"");
        
        new PageDownloader(this, new MainSearchHtmlParser(this)).execute(linkStr);
        
	}
		
    @Override
    public boolean onChildClick(ExpandableListView l, View v, int groupPosition, int childPosition, long id) {
        @SuppressWarnings("unchecked")
        Map<String, String> item = (Map<String, String>) 
        		getExpandableListAdapter().getChild(groupPosition, childPosition);
        
        // based on the item type, decide the action required
        SearchResults.Type itemType = SearchResults.Type.valueOf(item.get("childType"));
        switch(itemType) {
        case AUTHOR:
        	// open up next activity with author search result
        	Intent intentA = new Intent(this, AuthorResultsActivity.class);
        	intentA.putExtra(SearchActivity.EXTRA_MESSAGE, item.get("childText"));
        	intentA.putExtra(SearchActivity.EXTRA_MESSAGE2, item.get("childLink"));
            startActivity(intentA);
            break;
        case SEQUENCE:
        	// open up next activity with sequence search results
        	Intent intentS = new Intent(this, SequenceResultsActivity.class);
        	intentS.putExtra(SearchActivity.EXTRA_MESSAGE, item.get("childText"));
        	intentS.putExtra(SearchActivity.EXTRA_MESSAGE2, item.get("childLink"));
            startActivity(intentS);
       	
        	break;
        case BOOK:
        	// run FileDownloader
        	new FileDownloadInitiator(this, item.get("childText"), item.get("childLink")).initiateFileDownload();
        	
            break;
        case OTHER:
        	// do nothing
        	break;
        default:
        	Log.e(SearchActivity.TAG, "Unsupported item type specified: " + itemType.name());
        	return false;
        }
                
        return true;
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
