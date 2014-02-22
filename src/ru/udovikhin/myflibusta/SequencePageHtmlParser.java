package ru.udovikhin.myflibusta;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.content.Context;
import android.util.Log;

public class SequencePageHtmlParser extends HtmlParser {
	
	String sequenceStr;
	
	public SequencePageHtmlParser(Context ctx, String seqStr) {
		super(ctx);
		sequenceStr = seqStr;
	}


	@Override
	public SearchResults parse(InputStream stream) {

		HtmlCleaner pageParser = new HtmlCleaner();
		
		SearchResults results = new SearchResults();
		
		try {
			TagNode rootNode = pageParser.clean(stream);
			if( stream != null )
				stream.close();
			
			// use Xpath syntax to find all nodes with name a 
			// with attr href having div with attr class=title as parent
			String xpathExpr = "//div[@id='main']//a[@href]";
			
			Log.i(SearchActivity.TAG, xpathExpr);
			
		    Object linkElements[] = rootNode.evaluateXPath(xpathExpr);
		    
		    for( Object obj : linkElements ) {
		    	
		    	TagNode node = (TagNode)obj;
		    	
		    	String link = node.getAttributeByName(ATTR_LOOKUP_NAME);
		    	
		    	//printNodePath(node);
		    	// now decide the type of the link based on its value
		    	for( Map.Entry<String, SearchResults.Type> entry : linkPrefixToType.entrySet() ) {
		    		
		    				    		
		    		String regex = entry.getKey();
		    		SearchResults.Type linkType = entry.getValue();

		    		// we are interested in books only on this kind of search
		    		if( linkType != SearchResults.Type.BOOK )
		    			continue;
		    		
		    		// group name is always a sequence str
		    		String groupName = sequenceStr;
		    		
		    		if( link.matches(regex) ) {
		    			// add this node to appropriate section
		    			ArrayList<SearchResults.ChildData> vals = results.results.get(groupName);
		    			if( vals == null) {
		    				vals = new ArrayList<SearchResults.ChildData>();
		    				results.results.put(groupName, vals);
		    			}
		    			
		    			String nodeText = node.getText().toString();
		    			Log.i(SearchActivity.TAG, "Text = " + nodeText);
		    			vals.add(new SearchResults.ChildData(nodeText, link, linkType));
		    		}
		    	}
		    }
		} catch (XPatherException e) {
			Log.e(SearchActivity.TAG, e.getMessage());
		}
		catch (IOException e) {
			Log.e(SearchActivity.TAG, e.getMessage());
		}
		
		return results;
	}
}
