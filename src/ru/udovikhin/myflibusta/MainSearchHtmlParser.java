package ru.udovikhin.myflibusta;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import android.content.Context;
import android.util.Log;

public class MainSearchHtmlParser extends HtmlParser {
	
	public MainSearchHtmlParser(Context ctx) {
		super(ctx);
	}
	
	public SearchResults parse(InputStream stream) {		
		HtmlCleaner pageParser = new HtmlCleaner();
		
		SearchResults results = new SearchResults();
		
		try {
			TagNode rootNode = pageParser.clean(stream);
			if( stream != null )
				stream.close();
		    TagNode linkElements[] = rootNode.getElementsByName("li", true);
		    
		    for( TagNode node : linkElements ) {
		    	
		    	// list item should have parent with id = main
		    	if( node.getParent() == null )
		    		continue;
		    	String parentAttrVal = node.getParent().getAttributeByName(ATTR_ID_NAME); 
		    	if( parentAttrVal == null || !parentAttrVal.equals(ATTR_PARENT_ID_VALUE) )
		    		continue;

		    	// first child of list item with href attr - the node we are searching for
		    	List<TagNode> children = node.getChildTagList();
		    	
		    	if( children.size() < 1)
		    		continue;
		    	
		    	TagNode child = children.get(0);
		    	
		    	// query the expected href attr
		    	String link = child.getAttributeByName(ATTR_LOOKUP_NAME);

		    	// skip if child has no such attr
		    	if( link == null )
		    		continue;
		    	
		    	
		    	// now decide the type of the link based on its value
		    	for( Map.Entry<String, SearchResults.Type> entry : linkPrefixToType.entrySet() ) {
		    		String prefix = entry.getKey();
		    		SearchResults.Type linkType = entry.getValue();
		    		String groupName= linkTypeToSectionName.get(linkType);
		    		
		    		if( link.startsWith(prefix) ) {
		    			// add this node to appropriate section
		    			ArrayList<SearchResults.ChildData> vals = results.results.get(groupName);
		    			if( vals == null) {
		    				vals = new ArrayList<SearchResults.ChildData>();
		    				results.results.put(groupName, vals);
		    			}
		    			
		    			String nodeText = child.getText().toString();
		    			Log.i("myflibusta", "Text = " + nodeText);
		    			vals.add(new SearchResults.ChildData(nodeText, link, linkType));
		    		}
		    	}
		    }
		} catch (IOException e) {
			Log.e("ERROR", e.getMessage());
		}
		
		return results;
	}
	

}
