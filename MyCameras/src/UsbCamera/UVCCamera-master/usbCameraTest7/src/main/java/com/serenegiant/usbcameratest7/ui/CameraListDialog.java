/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */

package com.serenegiant.usbcameratest7.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usbcameratest7.R;

import java.util.ArrayList;
import java.util.List;

public class CameraListDialog extends DialogFragment {
	private static final String TAG = CameraListDialog.class.getSimpleName();

	private static final String KEY_CAMERA_WINDOW_NAME = "CAMERA_WINDOW_NAME";

	public interface CameraDialogParent {
		//public USBMonitor getUSBMonitor();
		public void onDialogResult(boolean canceled, UsbDevice device);
	}

	/**
	 * Helper method
	 * @param parent FragmentActivity
	 * @return
	 */
	public static CameraListDialog showDialog(final Activity parent, List<UsbDevice> devices, String windowName) {
		CameraListDialog dialog = newInstance(devices, windowName);
		try {
			dialog.show(parent.getFragmentManager(), TAG);
		} catch (final IllegalStateException e) {
			dialog = null;
		}
    	return dialog;
	}

	public static CameraListDialog newInstance(List<UsbDevice> devices, String windowName) {
		final CameraListDialog dialog = new CameraListDialog();
		final Bundle args = new Bundle();
		args.putCharSequence(KEY_CAMERA_WINDOW_NAME, windowName);
		dialog.setArguments(args);
		dialog.mDeviceList = devices;
		return dialog;
	}

	protected USBMonitor mUSBMonitor;
	public List<UsbDevice> mDeviceList;
	private DeviceListAdapter mDeviceListAdapter;
	private ListView mListView;
	private String mCameraWindowName = "";

	public CameraListDialog() {
		// Fragment need default constructor
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
//       if (mUSBMonitor == null)
//        try {
//    		mUSBMonitor = ((CameraDialogParent)activity).getUSBMonitor();
//        } catch (final ClassCastException e) {
//    	} catch (final NullPointerException e) {
//        }
//		if (mUSBMonitor == null) {
//        	throw new ClassCastException(activity.toString() + " must implement CameraDialogParent#getUSBController");
//		}
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			savedInstanceState = getArguments();
			mCameraWindowName = savedInstanceState.getCharSequence(KEY_CAMERA_WINDOW_NAME).toString();
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle saveInstanceState) {
		final Bundle args = getArguments();
		if (args != null) {
			saveInstanceState.putAll(args);
		}
		super.onSaveInstanceState(saveInstanceState);
	}

	@Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(initView());
        builder.setTitle("Select USB Camera For Window #" + mCameraWindowName);
	    //builder.setPositiveButton(android.R.string.ok, mOnDialogClickListener);
	    builder.setNegativeButton(android.R.string.cancel , mOnDialogClickListener);
	    final Dialog dialog = builder.create();
	    dialog.setCancelable(true);
	    dialog.setCanceledOnTouchOutside(true);
        return dialog;
	}

	/**
	 * create view that this fragment shows
	 * @return
	 */
	private final View initView() {
		final View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_camera_list, null);
		mListView = rootView.findViewById(R.id.lv_camera_list);
		mDeviceListAdapter = new DeviceListAdapter(getActivity(), mDeviceList);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				Toast.makeText(getActivity(), "click on id" + mDeviceList.get(i).getDeviceId(),Toast.LENGTH_LONG).show();
				((CameraDialogParent)getActivity()).onDialogResult(false, mDeviceList.get(i));
				CameraListDialog.this.dismiss();
			}
		});
		mListView.setAdapter(mDeviceListAdapter);
		return rootView;
	}

	private final DialogInterface.OnClickListener mOnDialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			switch (which) {
				//###@:diu
//			case DialogInterface.BUTTON_POSITIVE:
//				final Object item = mListView.getSelectedItem();
//				if (item instanceof UsbDevice) {
//					mUSBMonitor.requestPermission((UsbDevice)item);
//					((CameraDialogParent)getActivity()).onDialogResult(false);
//				}
//				break;
			case DialogInterface.BUTTON_NEGATIVE:
				((CameraDialogParent)getActivity()).onDialogResult(true, null);
				break;
			}
		}
	};

	@Override
	public void onCancel(final DialogInterface dialog) {
		((CameraDialogParent)getActivity()).onDialogResult(true, null);
		super.onCancel(dialog);
	}

	private static final class DeviceListAdapter extends BaseAdapter {

		private final LayoutInflater mInflater;
		private final List<UsbDevice> mList;

		public DeviceListAdapter(final Context context, final List<UsbDevice>list) {
			mInflater = LayoutInflater.from(context);
			mList = list != null ? list : new ArrayList<UsbDevice>();
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public UsbDevice getItem(final int position) {
			if ((position >= 0) && (position < mList.size()))
				return mList.get(position);
			else
				return null;
		}

		@Override
		public long getItemId(final int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.dialog_camera_list_item, parent, false);
			}
			if (convertView instanceof CheckedTextView) {
				final UsbDevice device = getItem(position);
				((CheckedTextView)convertView).setText(
					String.format("%s: (%x:%x) %s", device.getDeviceId(), device.getVendorId(), device.getProductId(), device.getProductName()));
			}
			return convertView;
		}
	}
}
