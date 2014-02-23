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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import ru.udovikhin.myflibusta.HtmlParser.SearchResults;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

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
    	bookPath += attrs.authorF + " " + attrs.authorL;
    	
    	if( attrs.sequence != null || attrs.sequenceNum != null) {
    		bookPath += "/";
    		if( attrs.sequenceNum != null)
    			bookPath += attrs.sequenceNum + ". ";
    		if( attrs.sequence!= null)
    			bookPath += attrs.sequence;
    	}
    	
    	File dir = new File(bookPath);
    	dir.mkdirs();
    	
    	String newFileName = attrs.title + ".fb2.zip";
    	
    	// now move file under the created directory
    	File file = new File(fileName);
    	
    	Log.i(SearchActivity.TAG, "Moving file " + fileName + "under " + bookPath + "/" + newFileName);
    	
    	file.renameTo(new File(dir, newFileName));
    	
    	return null;
    }
    
    private class BookAttributes {
    	String title = null;
    	String authorF = null;
    	String authorL = null;
    	String sequence = null;
    	String sequenceNum = null;
    	
    	private String safeStr(String str) {
    		if( str == null)
    			return "<null>";
    		return str;
    	}
    	
    	public String toString() {
    		String result = "";
    		
    		result += "title: " + safeStr(title);
    		result += " author: " + safeStr(authorF) + " " + safeStr(authorL);
    		result += " sequence: " + safeStr(sequenceNum) + " " + safeStr(sequence);
    		return result;
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
		HtmlCleaner pageParser = new HtmlCleaner();
		
		SearchResults results = new SearchResults();
		
		try {
			TagNode rootNode = pageParser.clean(stream);
			if( stream != null )
				stream.close();
			
			String xpathExpr = "//description//title-info//author//first-name";
			Object linkElements[] = rootNode.evaluateXPath(xpathExpr);
			if( linkElements.length == 1) {
				TagNode node = (TagNode)linkElements[0];
				attrs.authorF = node.getText().toString();
			}
			xpathExpr = "//description//title-info//author//last-name";
			linkElements = rootNode.evaluateXPath(xpathExpr);
			if( linkElements.length == 1) {
				TagNode node = (TagNode)linkElements[0];
				attrs.authorL = node.getText().toString();
			}
			xpathExpr = "//description//title-info//book-title";
			linkElements = rootNode.evaluateXPath(xpathExpr);
			if( linkElements.length == 1) {
				TagNode node = (TagNode)linkElements[0];
				attrs.title = node.getText().toString();
			}
			xpathExpr = "//description//title-info//sequence";
			linkElements = rootNode.evaluateXPath(xpathExpr);
			if( linkElements.length == 1) {
				TagNode node = (TagNode)linkElements[0];
				
				attrs.sequence = node.getAttributeByName("name");
				attrs.sequenceNum = node.getAttributeByName("number");
			}
		} catch (XPatherException e) {
			Log.e(SearchActivity.TAG, e.getMessage());
		}
		catch (IOException e) {
			Log.e(SearchActivity.TAG, e.getMessage());
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
    	
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

}
