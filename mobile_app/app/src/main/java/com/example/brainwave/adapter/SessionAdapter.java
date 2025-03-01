package com.example.brainwave.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brainwave.R;
import com.example.brainwave.model.Session;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {
    private List<Session> sessionList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Session session);
    }

    public SessionAdapter(Context context, List<Session> sessionList, OnItemClickListener listener) {
        this.context = context;
        this.sessionList = sessionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        Session session = sessionList.get(position);
        holder.bind(session, listener);
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public static class SessionViewHolder extends RecyclerView.ViewHolder {
        private TextView txtSession, tv_session_name;
        private ImageView img_session;
        private FirebaseAuth firebaseAuth;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSession = itemView.findViewById(R.id.txtSession);
            tv_session_name = itemView.findViewById(R.id.tv_session_name);
            img_session = itemView.findViewById(R.id.img_session);
            int nightModeFlags = itemView.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                img_session.setColorFilter(Color.WHITE);
            } else {
                img_session.setColorFilter(Color.BLACK);
            }
        }

        public void bind(Session session, OnItemClickListener listener) {
            txtSession.setText("Bắt đầu: " + formatTimestamp(new Date(session.getStartTime()).getTime()) +
                    "\nKết thúc: " + formatTimestamp(new Date(session.getEndTime()).getTime()));
            firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser user = firebaseAuth.getCurrentUser();
            tv_session_name.setText(user.getDisplayName());
            itemView.setOnClickListener(v -> listener.onItemClick(session));
        }

        private String formatTimestamp(long timestampMillis) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            return sdf.format(new Date(timestampMillis));
        }
    }
}
