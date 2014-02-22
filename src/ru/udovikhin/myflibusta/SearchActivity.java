package ru.udovikhin.myflibusta;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SearchActivity extends Activity {
	public final static String EXTRA_MESSAGE = "ru.udovikhin.myflibusta.MESSAGE";
    private static final String TAG = "ru.udovikhin.myflibusta";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		

		Log.i(TAG, "OnCreate");
		
		EditText editText = (EditText) findViewById(R.id.search_message);
		editText.setOnEditorActionListener(new OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        boolean handled = false;
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		            startSearch(v);
		            handled = true;
		        }
		        return handled;
		    }
		});
	}
	
    public void startSearch(View view) {
        // Do something in response to button
    	Intent intent = new Intent(this, MainResultsActivity.class);
    	EditText editText = (EditText) findViewById(R.id.search_message);
    	String message = editText.getText().toString();
    	intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
    
	protected void onStart() {
		super.onStart();
		Log.i(TAG, "onStart");
	}
	
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
	}
	
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");
	}
	
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop");
	}
	
	protected void onRestart() {
		super.onRestart();
		Log.i(TAG, "onRestart");
	}

	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.i(TAG, "onSaveInstanceState");
	}
	
	protected void onRestoreInstanceState(Bundle savedState) {		
		Log.i(TAG, "onRestoreInstanceState");
	}
}
