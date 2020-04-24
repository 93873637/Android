package com.liz;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Button mBtnSwitch;
	private TextView mTextTimeCount = null;
	private View mLayoutSettings = null;
	
	///////////////////////////////////////////////////////////////////////////
	//To in case:
	//This Handler class should be static or leaks might occur
	
	/**
	 * Instances of static inner classes do not hold an implicit reference to
	 * their outer class.
	 */
	private static class UIHandler extends Handler {
		private final WeakReference<MainActivity> mActivity;

		UIHandler(MainActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			MainActivity activity = mActivity.get();
			activity.handleMessage(msg);
		}
	}

	private final UIHandler mUIHandler = new UIHandler(this);
	///////////////////////////////////////////////////////////////////////////

	public void handleMessage(Message msg) {
		switch (msg.what) {
		case NumReader.MSG_NUMBER_UPDATED:
			updateUI();
			break;
		default:
			break;
		}
	}

	public void updateUI() {
		mTextTimeCount.setText(NumReader.getFormatTimeStr());
		mBtnSwitch.setText(NumReader.getNumberString());
		mBtnSwitch.setBackgroundResource(NumReader.isPlaying()? R.drawable.bg_circle_green : R.drawable.bg_circle_red);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mBtnSwitch = findViewById(R.id.btn_switch);
		mBtnSwitch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				NumReader.switchPlayPause();
				updateUI();
			}
		});

		mTextTimeCount = findViewById(R.id.textTimeCount);

		findViewById(R.id.ib_replay).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				NumReader.replay();
				updateUI();
			}
		});

		findViewById(R.id.ib_reset).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				NumReader.reset();
				updateUI();
			}
		});

		final EditText editNum = findViewById(R.id.edit_test_num);
		findViewById(R.id.btn_play_num).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SoundPoolPlayer.playNumberString(editNum.getText().toString());
			}
		});

		NumReader.init(this);
		NumReader.setUIHandler(mUIHandler);
		updateUI();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	protected void onResume() {
		super.onResume();
		updateUI();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			prepareSettings();
			new AlertDialog.Builder(this)
					.setTitle("Settings")
					.setView(mLayoutSettings)
					.setNegativeButton("Cancel", null)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									onSettingsOK();
								}
							}).show();
			return true;
		}
		else if (id == R.id.action_exit) {
			NumReader.release();
			this.finish();
			System.exit(0);
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void prepareSettings() {
		
		LayoutInflater inflater = getLayoutInflater();
		mLayoutSettings = inflater.inflate(R.layout.layout_settings,
		     (ViewGroup) findViewById(R.id.dialog));

        EditText etCountTimeSpan = mLayoutSettings.findViewById(R.id.etCountTimeSpan);
        EditText etCountReadSpan = mLayoutSettings.findViewById(R.id.etCountReadSpan);
        EditText editDigitSpan = mLayoutSettings.findViewById(R.id.edit_digits_span);
        EditText editPlayRate = mLayoutSettings.findViewById(R.id.edit_play_rate);

		etCountTimeSpan.setText(NumReader.getTimeSpanString());
        etCountReadSpan.setText(NumReader.getReadSpanString());
        editDigitSpan.setText(NumReader.getDigitSpanString());
        editPlayRate.setText(NumReader.getPlayRateString());
	}
	
	public void onSettingsOK() {
		   
		EditText etCountTimeSpan = mLayoutSettings.findViewById(R.id.etCountTimeSpan);
		EditText etCountReadSpan = mLayoutSettings.findViewById(R.id.etCountReadSpan);
		EditText editDigitSpan = mLayoutSettings.findViewById(R.id.edit_digits_span);
		EditText editPlayRate = mLayoutSettings.findViewById(R.id.edit_play_rate);

		//set origin value
		int newTimeSpan;
		int newReadSpan;
		int newDigitSpan;
		float newPlayRate;

		try {
			newTimeSpan = Integer.parseInt(etCountTimeSpan.getText().toString());
			newReadSpan = Integer.parseInt(etCountReadSpan.getText().toString());
			newDigitSpan = Integer.parseInt(editDigitSpan.getText().toString());
			newPlayRate = Float.parseFloat(editPlayRate.getText().toString());
		}
		catch (NumberFormatException ex) {
			Toast.makeText(MainActivity.this, "NumberFormatException: " + ex.toString(), Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (newTimeSpan != NumReader.mTimeSpan
				|| newReadSpan != NumReader.mReadSpan
				|| newDigitSpan != NumReader.mDigitSpan
				|| newPlayRate != NumReader.mPlayRate) {
			
			//setting change, save new settings
			SharedPreferences.Editor editor = this.getSharedPreferences(NumReader.SP_SETTINGS, Context.MODE_PRIVATE).edit();
			editor.clear();
			editor.putInt(NumReader.SP_TIME_SPAN, newTimeSpan);
			editor.putInt(NumReader.SP_READ_SPAN, newReadSpan);
			editor.putInt(NumReader.SP_DIGIT_SPAN, newDigitSpan);
			editor.putFloat(NumReader.SP_PLAY_RATE, newPlayRate);
			editor.commit();
			
			//update value and pause current reading
			NumReader.mTimeSpan = newTimeSpan;
			NumReader.mReadSpan = newReadSpan;
			NumReader.mDigitSpan = newDigitSpan;
			NumReader.mPlayRate = newPlayRate;
 		}
	}
}
