package com.example.smartcourseschedule.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartcourseschedule.R;
import com.example.smartcourseschedule.databinding.ActivityScheduleListBinding;
import com.example.smartcourseschedule.model.Schedule;
import com.example.smartcourseschedule.viewmodel.CourseViewModel;
import java.util.ArrayList;
import java.util.List;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ScheduleListActivity extends AppCompatActivity {

    private ActivityScheduleListBinding binding;
    private CourseViewModel viewModel;
    private ScheduleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CourseViewModel.class);

        setupRecyclerView();

        // 观察课表数据
        viewModel.getAllSchedules().observe(this, schedules -> {
            if (schedules == null || schedules.isEmpty()) {
                binding.llEmptyState.setVisibility(View.VISIBLE);
                binding.rvSchedules.setVisibility(View.GONE);
                binding.fabAddSchedule.setVisibility(View.GONE);
            } else {
                binding.llEmptyState.setVisibility(View.GONE);
                binding.rvSchedules.setVisibility(View.VISIBLE);
                binding.fabAddSchedule.setVisibility(View.VISIBLE);
                adapter.setList(schedules);
            }
        });

        // 创建按钮点击
        binding.btnCreateFirst.setOnClickListener(v -> showCreateDialog());
        binding.fabAddSchedule.setOnClickListener(v -> showCreateDialog());
    }

    private void setupRecyclerView() {
        adapter = new ScheduleAdapter(schedule -> {
            // 点击进入 MainActivity，并传递选中的 ID
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("schedule_id", schedule.scheduleId);
            intent.putExtra("schedule_name", schedule.scheduleName);
            startActivity(intent);
        });
        binding.rvSchedules.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSchedules.setAdapter(adapter);
    }

    private void showCreateDialog() {
        EditText et = new EditText(this);
        et.setHint("例如：大二下学期");
        new AlertDialog.Builder(this)
                .setTitle("新建课表")
                .setView(et)
                .setPositiveButton("创建", (d, w) -> {
                    String name = et.getText().toString().trim();
                    if (!name.isEmpty()) viewModel.insertSchedule(name);
                })
                .setNegativeButton("取消", null).show();
    }

    // 内部适配器
    private class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.VH> {
        private List<Schedule> list = new ArrayList<>();
        private final OnItemClickListener listener;

        public ScheduleAdapter(OnItemClickListener l) { this.listener = l; }
        public void setList(List<Schedule> l) { this.list = l; notifyDataSetChanged(); }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_schedule_card, p, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Schedule s = list.get(pos);
            h.tv.setText(s.scheduleName);
            h.itemView.setOnClickListener(v -> listener.onClick(s));
        }
        @Override public int getItemCount() { return list.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tv;
            VH(View v) { super(v); tv = v.findViewById(R.id.tv_name); }
        }
    }

    interface OnItemClickListener { void onClick(Schedule s); }
}
