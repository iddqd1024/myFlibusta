package ru.udovikhin.myflibusta;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import android.util.Log;



public class HtmlParser {
	private static final String ATTR_LOOKUP_NAME = "href";
	private static final String ATTR_ID_NAME = "id";
	private static final String ATTR_PARENT_ID_VALUE = "main";
	private static final Map<String, Integer> LINK_PREFIX_TO_NAME_IDX = 
			new HashMap<String, Integer>() {{
				put("/a/", R.string.author_section_name);
				put("/b/", R.string.book_section_name);
				put("/sequence/", R.string.serial_section_name);
				put("/s/", R.string.serial_section_name);
			}};
					
	// search values returned by html downloader/parser
	public static class SearchResults {
		public Map<Integer, ArrayList<String>> results = new HashMap<Integer, ArrayList<String>>();
	}

	public SearchResults parse(InputStream stream) {		
		HtmlCleaner pageParser = new HtmlCleaner();
		
		SearchResults results = new SearchResults();
		
		try {
			TagNode rootNode = pageParser.clean(stream);
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
		    	for( Map.Entry<String, Integer> entry : LINK_PREFIX_TO_NAME_IDX.entrySet() ) {
		    		String prefix = entry.getKey();
		    		Integer groupIdx = entry.getValue();
		    		
		    		if( link.startsWith(prefix) ) {
		    			// add this node to appropriate section
		    			ArrayList<String> vals = results.results.get(groupIdx);
		    			if( vals == null) {
		    				vals = new ArrayList<String>();
		    				results.results.put(groupIdx, vals);
		    			}
		    			
		    			String nodeText = node.getText().toString();
		    			vals.add(nodeText);
		    		}
		    	}
		    }
		} catch (IOException e) {
			Log.e("ERROR", e.getMessage());
		}
		
		return results;
	}
	
}
