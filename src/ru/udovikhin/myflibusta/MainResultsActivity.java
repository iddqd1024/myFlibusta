package ru.udovikhin.myflibusta;

import java.util.Map;

import ru.udovikhin.myflibusta.HtmlParser.SearchResults;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

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
	
	public class DownLoadClickListener implements DialogInterface.OnClickListener {
		private Map<String, String> downloadItem;
		
		public DownLoadClickListener(Map<String, String> item) {
			downloadItem = item;
		}
		
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                //Yes button clicked
            	
            	Toast.makeText(MainResultsActivity.this, "Downloading " + downloadItem.get("childLink"), 
            		Toast.LENGTH_LONG).show();
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                //No button clicked
                break;
            }
        }
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
        	break;
        case SEQUENCE:
        	// open up next activity with sequence search results
        	Intent intent = new Intent(this, SequenceResultsActivity.class);
        	intent.putExtra(SearchActivity.EXTRA_MESSAGE, item.get("childText"));
        	intent.putExtra(SearchActivity.EXTRA_MESSAGE2, item.get("childLink"));
            startActivity(intent);
       	
        	break;
        case BOOK:
        	// query book download
            DownLoadClickListener dialogClickListener = new DownLoadClickListener(item);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Download " + item.get("childText") + "?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
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
