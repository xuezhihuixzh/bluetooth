package com.renj.bluetoothchat.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.renj.bluetoothchat.R;
import com.renj.bluetoothchat.client.ClientConnUtils;
import com.renj.bluetoothchat.client.ClientUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * ======================================================================
 * <p>
 * 作者：Renj
 * <p>
 * 创建时间：2017-09-21   17:23
 * <p>
 * 描述：客户端页面
 * <p>
 * 修订历史：
 * <p>
 * ======================================================================
 */
public class ClientActivity extends Activity {
    private Button btOpen, btSearch;
    private ListView listView;

    private ClientUtils clientUtils;
    private ClientConnUtils clientConnUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        btOpen = (Button) findViewById(R.id.bt_open);
        btSearch = (Button) findViewById(R.id.bt_search);
        listView = (ListView) findViewById(R.id.listview);

        clientUtils = new ClientUtils(this);
        clientConnUtils = new ClientConnUtils();

        btOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientUtils.openBluetooth();
            }
        });

        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientUtils.startSearch();
            }
        });

        clientUtils.setOnBluetoothSearchFinishedListener(new ClientUtils.BluetoothSearchFinishedListener() {
            @Override
            public void finishedSearch(List<BluetoothDevice> devices) {
                listView.setAdapter(new MyAdapter(devices));
            }
        })
                .setOnBluetoothSearchResultListener(new ClientUtils.BluetoothSearchResultListener() {
                    @Override
                    public void findDevice(BluetoothDevice device) {
                        Log.i("MainActivity", "找到设备：" + device.toString());
                    }
                });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice itemAtPosition = (BluetoothDevice) parent.getItemAtPosition(position);
                clientUtils.bondAndConn(true, itemAtPosition);
            }
        });

        clientUtils.setOnBluetoothBondChangeListener(new ClientUtils.BluetoothBondChangeListener() {
            @Override
            public void bond() {
                Toast.makeText(ClientActivity.this, "未配对", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void bonding() {
                Toast.makeText(ClientActivity.this, "未配对", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void bonded() {
                Toast.makeText(ClientActivity.this, "已配对", Toast.LENGTH_SHORT).show();
            }
        });

        clientUtils.setOnClientConnListener(new ClientUtils.ClientConnListener() {
            @Override
            public void onSucceed(boolean secure, BluetoothSocket bluetoothSocket) {
                try {
                    OutputStream outputStream = bluetoothSocket.getOutputStream();
                    outputStream.write("封装好的测试数据...".getBytes());
                    //outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFialed(Exception e) {

            }
        });
    }


    // 适配器
    class MyAdapter extends BaseAdapter {
        private List<BluetoothDevice> devices;

        public MyAdapter(List<BluetoothDevice> devices) {
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View inflate = LayoutInflater.from(ClientActivity.this).inflate(R.layout.item_devices, null);
            TextView textview = (TextView) inflate.findViewById(R.id.textview);
            BluetoothDevice bluetoothDevice = devices.get(position);
            int bondState = bluetoothDevice.getBondState();
            String isBoud = "--";
            if (BluetoothDevice.BOND_NONE == bondState) {
                isBoud = "未配对";
            } else if (BluetoothDevice.BOND_BONDING == bondState) {
                isBoud = "正在配对";
            } else if (BluetoothDevice.BOND_BONDED == bondState) {
                isBoud = "已配对";
            }
            textview.setText(isBoud + " - " + bluetoothDevice.getName() + " : " + bluetoothDevice.getAddress());
            return inflate;
        }
    }
}