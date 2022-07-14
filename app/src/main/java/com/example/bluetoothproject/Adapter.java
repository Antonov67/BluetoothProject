package com.example.bluetoothproject;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;
    private int iconType;

    private ItemClickListener mClickListener;

    public Adapter(ArrayList<BluetoothDevice> bluetoothDevices, Context context, int iconType) {
        this.bluetoothDevices = bluetoothDevices;
        this.context = context;
        this.iconType = iconType;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.layout_item,parent,false);
        return new ViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice bluetoothDevice = bluetoothDevices.get(position);

        if (bluetoothDevice != null) {
            holder.nameDevice.setText(bluetoothDevice.getName());
            holder.macDevice.setText(bluetoothDevice.getAddress());
            holder.deviceIcon.setImageResource(iconType);
        }

    }

    @Override
    public int getItemCount() {
        return bluetoothDevices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nameDevice, macDevice;
        ImageView deviceIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameDevice = itemView.findViewById(R.id.nameDeviceText);
            macDevice = itemView.findViewById(R.id.macDeviceText);
            deviceIcon = itemView.findViewById(R.id.btDeviceIcon);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null) mClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    void setClickListener(ItemClickListener itemClickListener){
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
