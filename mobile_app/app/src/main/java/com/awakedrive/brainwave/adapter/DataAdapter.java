package com.awakedrive.brainwave.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.awakedrive.brainwave.R;
import com.awakedrive.brainwave.model.BrainData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {
    private List<BrainData> dataList;
    private Context context;

    public DataAdapter(Context context, List<BrainData> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_brain_data, parent, false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        BrainData data = dataList.get(position);
        holder.txtData.setText("Timestamp: " + formatTimestamp(data.getTimestamp()) +
                "\nDelta: " + data.getDelta() +
                "\nHighAlpha: " + data.getHighAlpha() +
                "\nHighBetag: " + data.getHighBeta() +
                "\nLowAlpha: " + data.getLowAlpha() +
                "\nLowBeta: " + data.getLowBeta() +
                "\nLowGamma: " + data.getLowGamma() +
                "\nMiddleGamma: " + data.getMiddleGamma() +
                "\nTheta: " + data.getMiddleGamma());
    }

    private String formatTimestamp(long timestampMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestampMillis));
    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {
        private TextView txtData;
        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            txtData = itemView.findViewById(R.id.txtData);
        }
    }
}
