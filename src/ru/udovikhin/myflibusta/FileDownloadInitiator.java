package ru.udovikhin.myflibusta;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

public class FileDownloadInitiator {
	
	private Context context;
	private String bookName;
	private String urlString;
	
	public FileDownloadInitiator(Context ctx, String bName, String urlStr) {
		context = ctx; 
		bookName = bName;
		urlString = urlStr;
	}
	
	public class FileDownloadClickListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which){
			case DialogInterface.BUTTON_POSITIVE:
				//Yes button clicked
				Log.i(SearchActivity.TAG, "User checked YES");
				
	        	String url = SearchActivity.HTTP_DOWNLOAD_ADDRESS + urlString + 
	        			SearchActivity.HTTP_DOWNLOAD_SUFFIX;

				
				new FileDownloader(context, bookName).execute(url);
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				//No button clicked
				Log.i(SearchActivity.TAG, "User checked NO");
				break;
			}
		}
	}
	
	public void initiateFileDownload() {
    	// ensure user wants to download this
		FileDownloadClickListener listener = new FileDownloadClickListener();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Download \"" + bookName + "\"?").setPositiveButton("Yes", listener)
            .setNegativeButton("No", listener).show();
	}

}
