package edu.udel.udmap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class UDDataActivity extends Activity 
{
	final static String TAG = "UDMapTest";
	
	private class UDDataItem
	{
		public String mCode;
		public String mName;
		public Double mLatitude;
		public Double mLongitude;
		
		public UDDataItem(String code, String name, Double latitude, Double longitude)
		{
			mCode = code;
			mName = name;
			mLatitude = latitude;
			mLongitude = longitude;
		}
		
		public UDDataItem(UDDataItem item)
		{
			mCode = item.mCode;
			mName = item.mName;
			mLatitude = item.mLatitude;
			mLongitude = item.mLongitude;
		}
	}
	
	private ArrayList<UDDataItem> getUDDataFromFile()
	{
	        final String UDDATA_PATH = "/sdcard/UDBuildingPositions";
                String sCurrentLine;
                BufferedReader br = null;
                String delims = ":";
                String[] tokens;
	        final int FIELDARRAYLEN = 256;
		
		ArrayList<UDDataItem> uddata_array = new ArrayList<UDDataItem>();

                try {
                  br = new BufferedReader(new FileReader(UDDATA_PATH));
                  while ((sCurrentLine = br.readLine()) != null) {
                      tokens = sCurrentLine.split(":");
	                  UDDataItem new_item = new UDDataItem(tokens[0], tokens[1], 
                                                  Double.parseDouble(tokens[2]),
                                                  Double.parseDouble(tokens[3]));
    	              uddata_array.add(new_item);
                  }
                  br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                  }
		
		return uddata_array;
	}

	private class UDDataListAdapter extends ArrayAdapter<UDDataItem>
	{
		private ArrayList<UDDataItem> mUDData_array;
		
		public UDDataListAdapter(Context context, int layoutResourceId, ArrayList<UDDataItem> item_array)
		{
			super(context, layoutResourceId, item_array);
			mUDData_array = item_array;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View cv = convertView;
			if (cv == null) 
			{
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				cv = inflater.inflate(R.layout.item_layout, null);
			}
			UDDataItem item = mUDData_array.get(position);
			if (item != null) 
			{
				TextView tvCode = (TextView)cv.findViewById(R.id.item_code);
				TextView tvName = (TextView)cv.findViewById(R.id.item_name);
				TextView tvLatitude = (TextView)cv.findViewById(R.id.item_latitude);
				TextView tvLongitude = (TextView)cv.findViewById(R.id.item_longitude);
				tvCode.setText("Code: " + item.mCode);
				tvName.setText("Name: " + item.mName);
				tvLatitude.setText("Latitude: " + item.mLatitude);
				tvLongitude.setText("Longitude: " + item.mLongitude);
			}
			return cv;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_uddata);
		/**
		 *  Tedious works start here
		 */
		ListView mUDDataList = (ListView)findViewById(R.id.uddatalist);
		ArrayList<UDDataItem> tmp_array = getUDDataFromFile();
		UDDataListAdapter uddata_adp = new UDDataListAdapter(this, android.R.layout.simple_list_item_1, tmp_array);
		mUDDataList.setAdapter(uddata_adp);
		/**
		 *  Tedious works end here
		 */
	}
}
