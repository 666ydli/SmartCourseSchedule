package com.example.smartcourseschedule.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcourseschedule.databinding.ItemCourseBinding; // 自动生成的类
import com.example.smartcourseschedule.model.Course;
import com.example.smartcourseschedule.model.CourseTime;
import com.example.smartcourseschedule.model.CourseWithTimes;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<CourseWithTimes> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CourseWithTimes item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setCourses(List<CourseWithTimes> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public CourseWithTimes getItemAt(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 使用 ViewBinding 加载布局
        ItemCourseBinding binding = ItemCourseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CourseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        // 调用 ViewHolder 的绑定方法
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // 商业级写法：将 UI 绑定逻辑封装在 ViewHolder 内
    static class CourseViewHolder extends RecyclerView.ViewHolder {
        private final ItemCourseBinding binding;

        public CourseViewHolder(ItemCourseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CourseWithTimes current, OnItemClickListener listener) {
            Course course = current.course;
            binding.tvItemCourseName.setText(course.courseName);
            binding.tvItemTeacher.setText(course.teacherName);

            if (current.times != null && !current.times.isEmpty()) {
                CourseTime time = current.times.get(0);
                binding.tvItemTime.setText(String.format("周%d 第%d-%d节",
                        time.dayOfWeek, time.startPeriod, time.endPeriod));
                binding.tvItemLocation.setText("📍 " + time.location);
                binding.ivNavigate.setVisibility(View.VISIBLE);
                binding.ivNavigate.setOnClickListener(v -> launchMap(v.getContext(), time.location));
            } else {
                binding.tvItemTime.setText("暂无时间");
                binding.tvItemLocation.setText("📍 未知地点");
                binding.ivNavigate.setVisibility(View.GONE);
            }

            try {
                binding.llCardBg.setBackgroundColor(Color.parseColor(course.colorCode));
            } catch (Exception e) {
                binding.llCardBg.setBackgroundColor(Color.parseColor("#4CAF50"));
            }

            // 设置点击回调
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(current);
            });
        }

        private void launchMap(Context context, String location) {
            try {
                Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(location));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "地图跳转失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
