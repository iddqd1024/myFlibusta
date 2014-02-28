package ru.udovikhin.myflibusta;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class FileDownloader extends AsyncTask<String, Void, String> {
	
	private final static String BOOK_DOWNLOAD_LOCATION = "/Books/";
	
	
	Context context;
	String bookName;
	
	public FileDownloader(Context ctx, String bName) {
		context = ctx;
		bookName = bName;
	}
	
    @Override
    protected String doInBackground(String... urls) {
    	    	    	
    	Log.i(SearchActivity.TAG, "Download of \"" + bookName + "\" is started");
    	
    	HttpURLConnection conn = null;
    	InputStream stream = null;

        try {        	
        	Log.i(SearchActivity.TAG, "DOWNLOAD URL is " + urls[0]);
        	
        	conn = PageDownloader.downloadUrl(urls[0]);
        	stream = conn.getInputStream();
        	String fileName = getSaveFileName(urls[0]);
        	String result = saveFile(stream, fileName);     
        	if( result != null)
        		return result;
        	
        	BookAttributes attrs = extractBookAttibutes(fileName);
        	result = moveFile(fileName, attrs); 
        	// return null on ok
            return result;
            
        } catch (IOException e) {
        	String msg = "Could not download \"" + bookName + "\" - " + e.getMessage(); 
            return msg;
        } finally {
        	try { 
        		if( stream != null )
        			stream.close();
        	} catch( IOException e) {}
        	
        	if( conn != null )
        		conn.disconnect();
        }
    }
    
    private String getSaveFileName(String url) {
    	
    	// assign default name
    	String fileExt = ".fb2.zip";
    	
    	Pattern regex = Pattern.compile(".*/(\\d+)/.*");
    	Matcher match = regex.matcher(url);
    	
    	String name = "tmp";
    	if( match.find() )
    		name = match.group(1);
    	
    	// make full name
    	String fullName = Environment.getExternalStorageDirectory() + BOOK_DOWNLOAD_LOCATION
    			+ name + fileExt; 
    			
		Log.i(SearchActivity.TAG, "Book save filename is: " + fullName);
    	return fullName;
    }
    
    private String saveFile(InputStream stream, String fileName) {
    	
    	OutputStream output = null;
    	try {
    		File file = new File(fileName);
    		//if( file.exists() )
    		//	file.delete();
    	
    		output = new FileOutputStream(file);

    		byte data[] = new byte[4096];
    		int count;
    		while ((count = stream.read(data)) != -1) {
    			output.write(data, 0, count);
    		}
    	} catch (Exception e) {
    		return e.toString();
    	} finally {
    		try {
    			if (output != null)
    				output.close();
    		} catch (IOException ignored) {
    		}
    	}  
    	return null;
    }
    
    private String moveFile(String fileName, BookAttributes attrs) {
    	// construct new file name out of the extracted attributes
    	Log.i(SearchActivity.TAG, "Book attributes: " + attrs.toString());
    	    	
    	// always use authors
    	String bookPath = Environment.getExternalStorageDirectory() 
    			+ BOOK_DOWNLOAD_LOCATION;
    	bookPath += attrs.authorString();
    	
    	if( attrs.sequences.size() != 0 ) {
    		bookPath += "/";
    		bookPath += attrs.sequenceNameString();
    	}
    	
    	File dir = new File(bookPath);
    	dir.mkdirs();
    	
    	String newFileName = attrs.sequenceNumString() + attrs.titleString() + ".fb2.zip";
    	
    	// now move file under the created directory
    	File file = new File(fileName);
    	
    	Log.i(SearchActivity.TAG, "Moving file " + fileName + "under " + bookPath + "/" + newFileName);
    	
    	file.renameTo(new File(dir, newFileName));
    	
    	return null;
    }
    
    public class BookAttributes {
    	public class Author {
    		String firstName = null;
    		String lastName = null;
    	}
    	public class Sequence {
    		String name = null;
    		String num = null;
    	}
    	
    	ArrayList<String> titles = new ArrayList<String>();
    	ArrayList<Author> authors = new ArrayList<Author>();
    	ArrayList<Sequence> sequences = new ArrayList<Sequence>();
    	String sequence = null;
    	String sequenceNum = null;
    	
    	private String safeStr(String str) {
    		if( str == null)
    			return "null";
    		return str;
    	}
    	
    	public String toString() {
    		String result = "";
    		
    		for( String title : titles ) {
    			result += "title: \"" + safeStr(title) + "\"";
    		}
    		
    		for( Author author : authors ) {
    			result += " author: " + safeStr(author.firstName) + " " + safeStr(author.lastName);
    		}
    		for( Sequence seq : sequences ) {
    			result += " sequence: " + safeStr(seq.name) + " " + safeStr(seq.num);
    		}
    		return result;
    	} 
    	
    	public String authorString() {
    		ArrayList<String> names = new ArrayList<String>();
    		for( Author author : authors ) {
    			names.add(safeStr(author.firstName) + " " + safeStr(author.lastName));
    		}

    		return TextUtils.join(", ", names);
    	}
    	
    	public String titleString() {
    		return TextUtils.join(", ", titles);
    	}
    	
    	public String sequenceNameString() {
    		ArrayList<String> names = new ArrayList<String>();
    		for( Sequence seq : sequences ) {
    			names.add(safeStr(seq.name));
    		}

    		return TextUtils.join(", ", names);
    	}
    	
    	public String sequenceNumString() {
    		ArrayList<String> nums = new ArrayList<String>();
    		for( Sequence seq : sequences ) {
    			if( seq.num != null)
    				nums.add(safeStr(seq.num) + ".");
    		}

    		return TextUtils.join(" ", nums);
    	}

    }
    	
    private BookAttributes extractBookAttibutes(String fileName) {
    	// open zip file and read its data in order to properly determine book attributes

        InputStream is = null;
        ZipInputStream zis = null;
        BookAttributes attrs = null;
        try {
            is = new FileInputStream(fileName);
            zis = new ZipInputStream(new BufferedInputStream(is));          
            
            ZipEntry ze = zis.getNextEntry();
            if( ze == null )
            	return null;
            
            Log.i(SearchActivity.TAG, ze.getName());

            attrs = extractBookAttributesFromStream(zis);
            Log.i(SearchActivity.TAG, attrs.toString());
            //zis.closeEntry();
        } 
        catch(IOException e)
        {
        	Log.e(SearchActivity.TAG, "Could not open zip: " + e.toString());
            return null;
        } finally {
        	try {
        		if( zis != null )
        			zis.close();
        		if( is != null )
        			is.close();
        	} catch ( IOException unhandled ){}
        }
    	return attrs;
    }
            
    private BookAttributes extractBookAttributesFromStream(InputStream stream) {
    	
    	Log.i(SearchActivity.TAG, "Parsing book attributes from stream...");
    	
    	BookAttributes attrs = new BookAttributes();
    	
    	try {
    		XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
    		XmlPullParser parser = xmlFactoryObject.newPullParser();

    		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
    		parser.setInput(stream, null);
    		    		
            int event = parser.getEventType();
            String lastText = null;
            boolean parsingAuthor = false;
            while (event != XmlPullParser.END_DOCUMENT) {
               String name=parser.getName();
               Log.i(SearchActivity.TAG, "Tag name: " + name + " event: " + event);
               switch (event){
                  case XmlPullParser.START_TAG:
                	  if( name.equals("author")) {
                		  // allocate new author struct
                		  attrs.authors.add(attrs.new Author());
                		  parsingAuthor = true;
                	  } else if( name.equals("sequence") ) {
                		 // get attributes
                		 BookAttributes.Sequence seq = attrs.new Sequence();
                		 attrs.sequences.add(seq);
                		 seq.name = parser.getAttributeValue(null, "name");
                		 seq.num = parser.getAttributeValue(null, "number");
                	  }   
                	  break;
                  case XmlPullParser.TEXT:
                	  lastText = parser.getText();
                	  break;

                  case XmlPullParser.END_TAG:
                	  if( name.equals("first-name") && parsingAuthor )
                		  attrs.authors.get(attrs.authors.size()-1).firstName = lastText;
                	  else if( name.equals("last-name") && parsingAuthor )
                		  attrs.authors.get(attrs.authors.size()-1).lastName = lastText;
                	  else if( name.equals("book-title"))
                		  attrs.titles.add(lastText);
                	  else if( name.equals("author"))
                		  parsingAuthor = false;
                	  else if( name.equals("title-info"))
                		  // stop parsing prematurely
                		  return attrs;
                	  break;
               }
               event = parser.next(); 
            }
    	} catch( Exception e ) {
    		Log.e(SearchActivity.TAG, e.toString());
    	}
		
		return attrs;
    }
    
    @Override
    protected void onPostExecute(String result) {
    	// notify user download is done
    	String msg = result;
    	if( result == null )
    		msg = "Download of \"" + bookName + "\" finished successfully";
    	else
    		Log.e(SearchActivity.TAG, "Book download failed with: " + result);

    	Log.i(SearchActivity.TAG, "Download of \"" + bookName + "\" is finished");
    	
    	SearchActivity.showMsg(context, msg);
    }

}
