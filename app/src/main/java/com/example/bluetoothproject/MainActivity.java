package com.example.bluetoothproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements Adapter.ItemClickListener {

    private FrameLayout messageFrame;
    private LinearLayout setupFrame;
    private LinearLayout ledFrame;

    private SwitchMaterial btSwitch;

    private BluetoothAdapter bluetoothAdapter;

    private static final int REQ_ENABLE_CODE = 7;
    private static final int TYPE_ONE = 100;
    private static final int TYPE_TWO = 200;

    private Adapter listAdapter;

    private ArrayList<BluetoothDevice> bluetoothDevices;

    private RecyclerView btDevicesList;

    private Button searchButton;
    private ProgressBar progressBar;

    private ProgressDialog progressDialog;

    private SwitchMaterial ledSwitch;

    private Button disconnectButton;

    private EditText consoleField;

    private ConnectedThread connectedThread;

    protected ConnectThread connectThread;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageFrame = findViewById(R.id.messageFrame);
        setupFrame = findViewById(R.id.setupFrame);
        ledFrame = findViewById(R.id.ledLayout);

        btSwitch = findViewById(R.id.btSwitch);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        btDevicesList = findViewById(R.id.recycleView);
        btDevicesList.setLayoutManager(new LinearLayoutManager(this));
        btDevicesList.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        bluetoothDevices = new ArrayList<>();

        searchButton = findViewById(R.id.searchButton);
        progressBar = findViewById(R.id.progressBar);

        disconnectButton = findViewById(R.id.disconnectButton);
        ledSwitch = findViewById(R.id.onOffLed);
        consoleField = findViewById(R.id.consoleField);


        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Соединение...");
        progressDialog.setMessage("Ждите...");

        registerReceiver(receiver, filter);

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectedThread != null){
                    connectedThread.cancel();
                }

                if (connectThread != null){
                    connectThread.cancel();
                }

                showSetupFrame();

            }
        });

        ledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ledOnOff(isChecked);
            }
        });




        if (bluetoothAdapter == null){
            Toast.makeText(getApplicationContext(),"Модуль Bluetooth не найден", Toast.LENGTH_LONG).show();
            finish();
        }

        if (bluetoothAdapter.isEnabled()){
            btSwitch.setChecked(true);
            showSetupFrame();
            setListAdapter(TYPE_ONE);
        }

        btSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                blEnable(isChecked);

                if (!isChecked){
                    showMessageFrame();
                }
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("777", "поиск");
                startSearchBtDevices();
            }
        });

       }


    private void ledOnOff(boolean isChecked){

        if (connectedThread != null && connectThread.isConnect()){
            String command = "";
            command = (isChecked) ? "1*" : "0*";
            connectedThread.write(command);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);

        if (connectedThread != null){
            connectedThread.cancel();
        }

        if (connectThread != null){
            connectThread.cancel();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ENABLE_CODE){
            if (resultCode == RESULT_OK && bluetoothAdapter.isEnabled()){
                showSetupFrame();
                setListAdapter(TYPE_ONE);

            } else if (requestCode == RESULT_CANCELED){
                blEnable(true);
            }
        }

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            switch (action){
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    searchButton.setText("остановить поиск");
                    progressBar.setVisibility(View.VISIBLE);
                    setListAdapter(TYPE_TWO);
                break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    searchButton.setText("начать поиск");
                    progressBar.setVisibility(View.GONE);
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null){
                        bluetoothDevices.add(device);
                        listAdapter.notifyDataSetChanged();
                    }
                    break;
            }


        }
    };

    @SuppressLint("MissingPermission")
       private void blEnable(boolean condition){

        if(condition) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,REQ_ENABLE_CODE);
        }else {
            bluetoothAdapter.disable();
        }

       }

       void showMessageFrame(){
            messageFrame.setVisibility(View.VISIBLE);
            setupFrame.setVisibility(View.GONE);
            ledFrame.setVisibility(View.GONE);
       }

    void showSetupFrame(){
        messageFrame.setVisibility(View.GONE);
        setupFrame.setVisibility(View.VISIBLE);
        ledFrame.setVisibility(View.GONE);
    }

    void showLedFrame(){
        messageFrame.setVisibility(View.GONE);
        setupFrame.setVisibility(View.GONE);
        ledFrame.setVisibility(View.VISIBLE);
    }

    // получим список уже подключенных устройств
    private ArrayList<BluetoothDevice> getBluetoothDevices(){

        @SuppressLint("MissingPermission")
        Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();

        ArrayList<BluetoothDevice> tmpArrayList = new ArrayList<>();

        if(deviceSet.size() > 0){
            for (BluetoothDevice device:  deviceSet){
                tmpArrayList.add(device);
            }
        }
        return tmpArrayList;
    }

    // заполним адаптер данными
    private void setListAdapter(int type){
        bluetoothDevices.clear();
        switch (type){
            case TYPE_ONE:
                bluetoothDevices = getBluetoothDevices();
                listAdapter = new Adapter(bluetoothDevices, this, R.drawable.ic_bluetooth_green);
                break;
            case TYPE_TWO:
                listAdapter = new Adapter(bluetoothDevices, this, R.drawable.ic_bluetooth_red);
        }

        btDevicesList.setAdapter(listAdapter);
        listAdapter.setClickListener(this);
    }

    // поиск устройств
    @SuppressLint("MissingPermission")
    private void startSearchBtDevices(){
        if (bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }else {
            bluetoothAdapter.startDiscovery();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        BluetoothDevice device = bluetoothDevices.get(position);

        if (device != null){
            connectThread = new ConnectThread(device);
            connectThread.start();
        }
    }




    // класс потока для обмена данными с блютуз устройством

    private class ConnectedThread extends Thread{

        private final InputStream inputStream;
        private final OutputStream outputStream;

        private boolean isConnected = false;

        public ConnectedThread(BluetoothSocket bluetoothSocket) {

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.inputStream = inputStream;
            this.outputStream = outputStream;

            isConnected = true;
        }

        @Override
        public void run() {

            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            StringBuffer buffer = new StringBuffer();
            StringBuffer stringBufferconsole = new StringBuffer();

            while (isConnected) {
                try {
                    int bytes = bufferedInputStream.read();
                    buffer.append((char) bytes);
                    int eof = buffer.indexOf("\r\n");
                    if (eof > 0){
                        stringBufferconsole.append(buffer);
                        buffer.delete(0,buffer.length());
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            consoleField.setText(stringBufferconsole.toString());
                            consoleField.setMovementMethod(new ScrollingMovementMethod());
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        // метод отправки команды
        public void write(String command){

            byte[] bytes = command.getBytes();

            if (outputStream != null){
                try {
                    outputStream.write(bytes);
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // метод сброса подключения
        public void cancel(){
            try {
                inputStream.close();
                outputStream.close();
                isConnected = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    // класс потока для подлкючения к блютуз устройству

    private class ConnectThread extends Thread{

        private BluetoothSocket bluetoothSocket = null;
        private boolean isSuccess = false;

        public ConnectThread(BluetoothDevice bluetoothDevice) {

            Method method = null;

            try {
                method = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                bluetoothSocket = (BluetoothSocket) method.invoke(bluetoothDevice, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            try {
                bluetoothSocket.connect();
                isSuccess = true;

                progressDialog.dismiss();
            } catch (IOException e) {
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Не могу соединиться", Toast.LENGTH_SHORT).show();
                    }
                });
                cancel();
            }

            if (isSuccess) {
                connectedThread = new ConnectedThread(bluetoothSocket);
                connectedThread.start();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLedFrame();
                    }
                });

            }
        }

        public boolean isConnect(){
            return bluetoothSocket.isConnected();
        }

        public  void  cancel(){
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }




}