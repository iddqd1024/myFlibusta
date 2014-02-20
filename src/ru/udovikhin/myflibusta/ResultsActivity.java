package ru.udovikhin.myflibusta;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.udovikhin.myflibusta.HtmlParser.SearchResults;

import android.annotation.TargetApi;
import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

public class ResultsActivity extends ExpandableListActivity {
	
	private static final String HTTP_DOWNLOAD_ADDRESS = "http://flibusta.net";
	private static final String HTTP_SEARCH_SUFFIX = "/booksearch?ask=";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_results);
		// Show the Up button in the action bar.
		setupActionBar();
				                
        // fire downloader task
        Intent intent = getIntent();
        String searchStr = intent.getStringExtra(SearchActivity.EXTRA_MESSAGE);
        
        new SearchAndProcessResults().execute(searchStr);
        
	}

    @Override
    public boolean onChildClick(ExpandableListView l, View v, int groupPosition, int childPosition, long id) {
        @SuppressWarnings("unchecked")
        Map<String, String> item = (Map<String, String>) 
        		getExpandableListAdapter().getChild(groupPosition, childPosition);
        Toast.makeText(this, item + " выбран", Toast.LENGTH_LONG).show();
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
	
	// Implementation of async task which does site search and preprocess results
    private class SearchAndProcessResults extends AsyncTask<String, Void, HtmlParser.SearchResults> {

        @Override
        protected HtmlParser.SearchResults doInBackground(String... searchStrs) {
        	
        	
            try {
            	String encodedSearchStr = URLEncoder.encode(searchStrs[0], "UTF-8");
            	String url = HTTP_DOWNLOAD_ADDRESS + HTTP_SEARCH_SUFFIX + encodedSearchStr;
            	
            	Log.i("myflibusta", "DOWNLOAD URL is " + url);
            		
            	InputStream stream = downloadUrl(url);
                return new HtmlParser(ResultsActivity.this).parse(stream);
                
            } catch (IOException e) {
            	HtmlParser.SearchResults result = new HtmlParser.SearchResults();
            	ArrayList<SearchResults.ChildData> strs = new ArrayList<SearchResults.ChildData>();
            	strs.add(new SearchResults.ChildData(e.toString(), null));
            	
            	result.results.put(getString(R.string.connection_error), strs);
                return result;
            }
        }

        @Override
        protected void onPostExecute(HtmlParser.SearchResults result) {
            ArrayList<Map<String, String>> groupData = new ArrayList<Map<String, String>>();;
            ArrayList<ArrayList<Map<String, String>>> childData =
            	new ArrayList<ArrayList<Map<String, String>>>();
            
            String groupAttrName = "categoryName";
            String childText = "childText";
            String childLink = "childLink";
            
            // translate parsing results into adapter data
            for (Map.Entry<String, ArrayList<SearchResults.ChildData>> entry : result.results.entrySet()) {
            	String groupName = entry.getKey();
            	
            	// add group
            	Map<String, String> m = new HashMap<String, String>();
            	m.put(groupAttrName, groupName);
            	groupData.add(m);
            	
            	// add children
            	ArrayList<Map<String, String>> childItemData = new ArrayList<Map<String, String>>();
            	for( SearchResults.ChildData value : entry.getValue() ) {
            		Map<String, String> m2 = new HashMap<String, String>();
            		m2.put(childText, value.text);
            		m2.put(childLink, value.url);
            		childItemData.add(m2);
            	}
            	childData.add(childItemData);
            }
            
            String groupFrom[] = new String[] {groupAttrName};
            int groupTo[] = new int[] {android.R.id.text1};
            
            String childFrom[] = new String[] {childText};
            int childTo[] = new int[] {android.R.id.text1};

            SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
            		ResultsActivity.this,
            		groupData,
            		android.R.layout.simple_expandable_list_item_1,
            		groupFrom,
            		groupTo,
            		childData,
            		android.R.layout.simple_list_item_1,
            		childFrom,
            		childTo);
            setListAdapter(adapter);
            
            Toast.makeText(ResultsActivity.this, "Search is done", Toast.LENGTH_LONG).show();

        }
    }
    
    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }

}
