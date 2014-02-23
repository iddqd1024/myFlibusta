package ru.udovikhin.myflibusta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
