package ru.udovikhin.myflibusta;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.content.Context;
import android.util.Log;

public class AuthorPageHtmlParser extends HtmlParser {
	
	public AuthorPageHtmlParser(Context ctx) {
		super(ctx);
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
			String xpathExpr = "//div[@id='main']//form[@name='bk']//a[@href]";
			
			Log.i(SearchActivity.TAG, xpathExpr);
			
		    Object linkElements[] = rootNode.evaluateXPath(xpathExpr);
		    
		    String curSeqName = "<null>";
		    
		    for( Object obj : linkElements ) {
		    	
		    	TagNode node = (TagNode)obj;
		    	
		    	String link = node.getAttributeByName(ATTR_LOOKUP_NAME);
		    	
		    	//printNodePath(node);
		    	// now decide the type of the link based on its value
		    	for( Map.Entry<String, SearchResults.Type> entry : linkPrefixToType.entrySet() ) {
		    		
		    				    		
		    		String regex = entry.getKey();
		    		SearchResults.Type linkType = entry.getValue();

		    		// we are interested in books and sequences only
		    		if( linkType != SearchResults.Type.BOOK && linkType != SearchResults.Type.SEQUENCE )
		    			continue;
		    		
		    		if( link.matches(regex) ) {
		    			if( linkType == SearchResults.Type.SEQUENCE) {
		    				// update current sequence name
		    				curSeqName = node.getText().toString();
		    			}
		    				
		    			if( linkType == SearchResults.Type.BOOK ) {
		    				// add this node to appropriate section
		    				ArrayList<SearchResults.ChildData> vals = results.results.get(curSeqName);
		    				if( vals == null) {
		    					vals = new ArrayList<SearchResults.ChildData>();
		    					results.results.put(curSeqName, vals);
		    				}
		    			
		    				String nodeText = node.getText().toString();
		    		    	nodeText = StringEscapeUtils.unescapeHtml3(nodeText);
		    		    	Log.i(SearchActivity.TAG, "Text = " + nodeText);
		    				//printNodePath(node);
		    				vals.add(new SearchResults.ChildData(nodeText, link, linkType));
		    			}
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
