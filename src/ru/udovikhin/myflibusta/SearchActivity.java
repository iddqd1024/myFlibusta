package ru.udovikhin.myflibusta;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class SearchActivity extends Activity {
	public final static String EXTRA_MESSAGE = "ru.udovikhin.myflibusta.MESSAGE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
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
    	Intent intent = new Intent(this, ResultsActivity.class);
    	EditText editText = (EditText) findViewById(R.id.search_message);
    	String message = editText.getText().toString();
    	intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

}
