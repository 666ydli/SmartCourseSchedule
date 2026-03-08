package com.example.smartcourseschedule.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcourseschedule.R;
import com.example.smartcourseschedule.model.Schedule;

import java.util.ArrayList;
import java.util.List;

public class ScheduleManageAdapter extends RecyclerView.Adapter<ScheduleManageAdapter.VH> {

    public interface OnScheduleActionListener {
        void onSelect(Schedule s);
        void onDelete(Schedule s);
    }

    private List<Schedule> list = new ArrayList<>();
    private final OnScheduleActionListener listener;

    public ScheduleManageAdapter(OnScheduleActionListener l) {
        this.listener = l;
    }

    public void setList(List<Schedule> l) {
        this.list = l;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_schedule_manage, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Schedule s = list.get(position);
        TextView tv = holder.itemView.findViewById(R.id.tv_schedule_name);
        tv.setText(s.scheduleName);

        // 点击名称选择课表
        tv.setOnClickListener(v -> listener.onSelect(s));

        // 点击删除图标
        holder.itemView.findViewById(R.id.iv_delete_schedule).setOnClickListener(v -> {
            if (list.size() <= 1) {
                Toast.makeText(v.getContext(), "最后一个课表不能删除", Toast.LENGTH_SHORT).show();
            } else {
                listener.onDelete(s);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        VH(View v) { super(v); }
    }
}
