package com.sih.qr_scannerandgenrator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<String> historyList;
    private List<String> qrBitmapList; // To store Base64 QR code images

    public HistoryAdapter(List<String> historyList, List<String> qrBitmapList) {
        this.historyList = historyList;
        this.qrBitmapList = qrBitmapList;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        // Set the data for each item
        String history = historyList.get(position);
        String qrBitmapBase64 = qrBitmapList.get(position);

        holder.historyText.setText(history);
        Bitmap qrBitmap = decodeBase64ToBitmap(qrBitmapBase64);
        holder.qrImage.setImageBitmap(qrBitmap); // Set the QR code image to the ImageView
    }
    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedByteArray = Base64.decode(base64String, Base64.DEFAULT);
        InputStream inputStream = new ByteArrayInputStream(decodedByteArray);
        return BitmapFactory.decodeStream(inputStream);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    // ViewHolder class to hold the views for each item
    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView historyText;
        ImageView qrImage;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            historyText = itemView.findViewById(R.id.historyText);
            qrImage = itemView.findViewById(R.id.historyQrImage); // ImageView for QR Code
        }
    }
}
