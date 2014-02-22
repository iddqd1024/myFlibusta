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
import android.app.ExpandableListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

public class PageDownloader extends AsyncTask<String, Void, HtmlParser.SearchResults> {
	private static final String HTTP_DOWNLOAD_ADDRESS = "http://flibusta.net";
	private static final String HTTP_SEARCH_SUFFIX = "/booksearch?ask=";

	Context context;
	HtmlParser parser;
	
	public PageDownloader(Context ctx, HtmlParser p) {
		context = ctx;
		parser = p;
	}
	
    @Override
    protected HtmlParser.SearchResults doInBackground(String... searchStrs) {
    	
    	
        try {
        	String encodedSearchStr = URLEncoder.encode(searchStrs[0], "UTF-8");
        	String url = HTTP_DOWNLOAD_ADDRESS + HTTP_SEARCH_SUFFIX + encodedSearchStr;
        	
        	Log.i("myflibusta", "DOWNLOAD URL is " + url);
        		
        	InputStream stream = downloadUrl(url);
            return parser.parse(stream);
            
        } catch (IOException e) {
        	HtmlParser.SearchResults result = new HtmlParser.SearchResults();
        	ArrayList<SearchResults.ChildData> strs = new ArrayList<SearchResults.ChildData>();
        	strs.add(new SearchResults.ChildData(e.toString(), null, SearchResults.Type.OTHER));
        	
        	result.results.put(context.getString(R.string.connection_error), strs);
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
        		context,
        		groupData,
        		android.R.layout.simple_expandable_list_item_1,
        		groupFrom,
        		groupTo,
        		childData,
        		android.R.layout.simple_list_item_1,
        		childFrom,
        		childTo);
        ExpandableListActivity activity = (ExpandableListActivity)context;
        activity.setListAdapter(adapter);
        
        Toast.makeText(context, "Search is done", Toast.LENGTH_LONG).show();

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
