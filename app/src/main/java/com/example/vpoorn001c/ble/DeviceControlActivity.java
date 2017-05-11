package com.example.vpoorn001c.ble;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    ListView listView;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };
    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
            };
    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    NotesDatabaseAdapter helper;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        helper = new NotesDatabaseAdapter(this);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        if (mDeviceAddress.equals("78:A5:04:8C:15:77")) {

            //Toast.makeText(this, mDeviceAddress, Toast.LENGTH_SHORT).show();
            helper.deleteData();
            insertData(1, "78:A5:04:8C:15:77", "Macy's: 25% Off Sitewide", "Get 25% off storewide with this Macy's promo code: snag shoes, handbags, watches, bath goods, linens, and more.", "25%", "5/22/15");
            insertData(2, "78:A5:04:8C:15:77", "AEO Shorts: Buy One, Get One 50% Off", "For a limited time, jeans are buy one, get one 50% off at American Eagle online! No promo code required. Offer excludes clearance.", "50%", "5/18/15");
            insertData(3, "78:A5:04:8C:15:77", "Macy's: Up To 75% Off on Clearance & Closeout Items", "Enjoy a storewide selection of Macy''s sales, clearances, and closeouts, including everything from apparel to housewares.", "75%", "5/23/15");
            insertData(4, "78:A5:04:8C:15:77", "AEO: Get Free Shipping On $50+ Order", "Save on the hottest fashions at American Eagle with this special offer! Get Free Shipping on orders $50 and up when you shop the wide selection of styles and sizes for men and women now!", "50%", "5/29/15");
            insertData(5, "78:A5:04:8C:15:77", "JCPenny: Up To 70% Off Clearance Items", "Save up to 70% on already marked down clearance items.", "70%", "5/14/15");
            insertData(6, "78:A5:04:8C:15:77", "JCPenny: Up To 77% Off Women's Clothing", "Check out JCPenney''s women''s clothing sale, which offers up to 77% off dresses, tops, skirts, and much more.", "77%", "5/26/15");
            insertData(7, "78:A5:04:8C:15:77", "JCPenny: Extra 15% Off Apparel, Shoes, Accessories, Fine Jewelry & Home", "Shop at JCPenney.com for clothing, accessories, jewelry, and home goods and save 15% with this promo code.", "15%", "5/30/15");
            //helper.deleteData();
        } else if (mDeviceAddress.equals("78:A5:04:8C:27:6F")) {
           // Toast.makeText(this, mDeviceAddress, Toast.LENGTH_SHORT).show();
            helper.deleteData();
            insertData(1, "78:A5:04:8C:27:6F", "Dell Home Spring Clearance Event", "Dell Home cuts up to $642 off a selection of laptops, tablets, desktops, monitors, and accessories as part of its Spring Clearance Event", "25%", "5/22/15");
            insertData(2, "78:A5:04:8C:27:6F", "Dell XPS i5 Dual 2.2GHz 13inch 1080p laptop", "Microsoft Store offers the 2.6-lb. Dell XPS 13 Signature Edition Intel Broadwell Core i5 2.2GHz 13.3, 1080p Laptop for $899", "50%", "5/18/15");
            insertData(3, "78:A5:04:8C:27:6F", "Lenovo G50-80 Broadwell i5 Dual 16inch Laptop", "Microsoft Store offers the 4.9-lb. Lenovo G50-80 Intel Broadwell Core i5 2.2GHz 15.6\" Signature Edition Laptop, model no. 80E501U3US, for $399 with free shipping.", "75%", "5/23/15");
            insertData(4, "78:A5:04:8C:27:6F", "Asus Haswell i7 16inch 1080p Laptop w/ 4GB GPU", "Newegg via eBay offers the 6-lb. Asus Intel Haswell Core i7 2.5GHz 15.6\" Gaming Laptop in Black Aluminum, model no. N56JK-DB72, for $749.99 with free shipping. ", "50%", "5/29/15");
            insertData(5, "78:A5:04:8C:27:6F", "Microsoft Store Spring PC Sale", "Microsoft Store cuts up to $401 off a selection laptops, tablets, and desktop PCs during its Spring PC Sale. Plus, all orders qualify for free shipping. ", "70%", "5/14/15");
                      //listView.setVisibility(View.GONE);
           // helper.deleteData();



        } else {
            //Toast.makeText(this, mDeviceAddress, Toast.LENGTH_SHORT).show();
            TextView textView = (TextView)findViewById(R.id.data_value);
            textView.setText("");
            TextView dataNotFound = (TextView) findViewById(R.id.dataNotFound);
            dataNotFound.setText("Data Not Found");
            //listView.setVisibility(View.GONE);

            helper.deleteData();
        }


    }

    private void insertData( int id, String deviceAddress, String couponName, String couponDesc, String discountPerc, String couponExpiry) {
        Data data = new Data(id, deviceAddress, couponName, couponDesc, discountPerc, couponExpiry);
        helper.insertData(data);
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
        ArrayList<Data> dataList = helper.getData();
        CustomAdapter adapter = new CustomAdapter(this, dataList);
         listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WebView webView = (WebView)findViewById(R.id.webView);
                if(mDeviceAddress.equals("78:A5:04:8C:15:77")) {
                    switch (position) {

                        case 0:
                            webView.loadUrl("http://www1.macys.com/");
                            break;
                        case 1:
                            webView.loadUrl("http://www.ae.com/web/index.jsp");
                            break;
                        case 2:
                            webView.loadUrl("http://www1.macys.com/shop/sale/clearance-closeout?id=54698");
                            break;
                        case 3:
                            webView.loadUrl("http://www.ae.com/web/index.jsp");
                            break;
                        case 4:
                            webView.loadUrl("http://www.jcpenney.com/");
                            break;
                        case 5:
                            webView.loadUrl("http://www.jcpenney.com/");
                            break;
                        case 6:
                            webView.loadUrl("http://www.jcpenney.com/");
                            break;

                    }
                }
                else if(mDeviceAddress.equals("78:A5:04:8C:27:6F")) {
                    switch (position) {


                        case 0:
                            webView.loadUrl("http://www.bestbuy.com/");
                            break;
                        case 1:
                            webView.loadUrl("http://www.target.com/");
                            break;
                        case 2:
                            webView.loadUrl("http://support.lenovo.com/in/hi/");
                            break;
                        case 3:
                            webView.loadUrl("http://www.ebay.com/itm/291442771827?rmvSB=true");
                            break;
                        case 4:
                            webView.loadUrl("http://www.amazon.com/");
                            break;

                    }
                }

                //Toast toast =Toast.makeText(DeviceControlActivity.this,"Nothing in here", Toast.LENGTH_SHORT);
                //toast.show();
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }
    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }
    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}