package ru.udovikhin.myflibusta;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;



abstract public class HtmlParser {
	protected static final String ATTR_LOOKUP_NAME = "href";
	protected static final String ATTR_ID_NAME = "id";
	protected static final String ATTR_PARENT_ID_VALUE = "main";
	protected Map<String, SearchResults.Type> linkPrefixToType;
	protected Map<SearchResults.Type, String> linkTypeToSectionName;
	protected Context context;
			
	@SuppressWarnings("serial")
	public HtmlParser(Context ctx) {
		context = ctx;
		linkPrefixToType = new HashMap<String, SearchResults.Type>() {{
			put("/a/", SearchResults.Type.AUTHOR);
			put("/b/", SearchResults.Type.BOOK);
			put("/sequence/", SearchResults.Type.SEQUENCE);
			put("/s/", SearchResults.Type.SEQUENCE);
		}};
		
		
		linkTypeToSectionName = new HashMap<SearchResults.Type, String>() {{
			put(SearchResults.Type.AUTHOR, context.getString(R.string.author_section_name));
			put(SearchResults.Type.BOOK, context.getString(R.string.book_section_name));
			put(SearchResults.Type.SEQUENCE, context.getString(R.string.serial_section_name));
		}};

	}
					
	// search values returned by html downloader/parser
	public static class SearchResults {
		enum Type {AUTHOR, BOOK, SEQUENCE, OTHER};
		
		public static class ChildData {
			String text;
			String url;
			Type type;
			public ChildData(String t, String u, Type tt) {
				text = t;
				url = u;
				type = tt;
			}
		}
		
		public Map<String, ArrayList<ChildData>> results = new HashMap<String, ArrayList<ChildData>>();
		
	}

	abstract public SearchResults parse(InputStream stream);
}
