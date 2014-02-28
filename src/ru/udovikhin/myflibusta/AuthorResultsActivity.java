package ru.udovikhin.myflibusta;

import java.util.Map;

import ru.udovikhin.myflibusta.HtmlParser.SearchResults;
import android.annotation.TargetApi;
import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

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
                
        new PageDownloader(this, new AuthorPageHtmlParser(this)).execute(fullLink);

	}
	
    @Override
    public boolean onChildClick(ExpandableListView l, View v, int groupPosition, int childPosition, long id) {
        @SuppressWarnings("unchecked")
        Map<String, String> item = (Map<String, String>) 
        		getExpandableListAdapter().getChild(groupPosition, childPosition);
        
        // based on the item type, decide the action required
        SearchResults.Type itemType = SearchResults.Type.valueOf(item.get("childType"));
        switch(itemType) {
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


}
