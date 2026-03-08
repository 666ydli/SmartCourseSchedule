package com.example.smartcourseschedule.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcourseschedule.R;
import com.example.smartcourseschedule.model.Homework;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeworkAdapter extends RecyclerView.Adapter<HomeworkAdapter.VH> {

    public interface OnHomeworkDeleteListener {
        void onDelete(Homework homework);
    }

    private List<Homework> hList = new ArrayList<>();
    private final OnHomeworkDeleteListener deleteListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

    public HomeworkAdapter(OnHomeworkDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setList(List<Homework> l) {
        this.hList = l;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_homework, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Homework h = hList.get(position);
        ((TextView) holder.itemView.findViewById(R.id.tv_homework_content)).setText(h.content);

        TextView tvTime = holder.itemView.findViewById(R.id.tv_homework_time);
        if (h.reminderTime > 0) {
            tvTime.setText("提醒: " + dateFormat.format(h.reminderTime));
            tvTime.setVisibility(View.VISIBLE);
        } else {
            tvTime.setVisibility(View.GONE);
        }

        holder.itemView.findViewById(R.id.iv_delete_homework).setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(h);
        });
    }

    @Override
    public int getItemCount() {
        return hList.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        VH(View v) { super(v); }
    }
}
