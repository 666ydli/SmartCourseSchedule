package com.example.smartcourseschedule.view;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.smartcourseschedule.R;
import com.example.smartcourseschedule.databinding.ActivityMainBinding;
import com.example.smartcourseschedule.databinding.DialogAddCourseBinding;
import com.example.smartcourseschedule.databinding.DialogAddHomeworkBinding;
import com.example.smartcourseschedule.databinding.DialogHomeworkManagerBinding;
import com.example.smartcourseschedule.databinding.ItemGridCourseBinding;
import com.example.smartcourseschedule.model.Course;
import com.example.smartcourseschedule.model.CourseTime;
import com.example.smartcourseschedule.model.CourseWithTimes;
import com.example.smartcourseschedule.model.Homework;
import com.example.smartcourseschedule.model.Schedule;
import com.example.smartcourseschedule.utils.AlarmReceiver;
import com.example.smartcourseschedule.viewmodel.ConflictUtils;
import com.example.smartcourseschedule.viewmodel.CourseViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;
import jp.wasabeef.glide.transformations.BlurTransformation;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CourseViewModel courseViewModel;

    private List<CourseWithTimes> currentCoursesSnapshot = new ArrayList<>();
    private int currentScheduleId = -1;
    private String currentScheduleName = "我的课表";

    private int zoomLevel = 75;
    private static final String PREFS = "AppPrefs";
    private static final String KEY_ZOOM = "zoom_level";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        enableImmersiveMode();
        setContentView(binding.getRoot());
        applyWindowInsets();

        zoomLevel = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_ZOOM, 75);
        checkNotificationPermission();
        courseViewModel = new ViewModelProvider(this).get(CourseViewModel.class);

        int passedId = getIntent().getIntExtra("schedule_id", -1);
        String passedName = getIntent().getStringExtra("schedule_name");

        if (passedId != -1) {
            currentScheduleId = passedId;
            currentScheduleName = passedName;
            binding.tvTitle.setText(currentScheduleName);
            courseViewModel.getCoursesBySchedule(currentScheduleId).observe(this, list -> {
                this.currentCoursesSnapshot = list;
                renderGridView(list);
            });
        } else {
            initScheduleData();
        }

        loadBlurredBackground();

        binding.tvTitle.setOnClickListener(v -> showScheduleListManageDialog());
        binding.tvTitle.setOnLongClickListener(v -> {
            showCreateScheduleDialog();
            return true;
        });

        binding.glCourseContainer.setOnLongClickListener(v -> {
            showZoomControlDialog();
            return true;
        });

        binding.fabAddCourse.setOnClickListener(v -> {
            if (currentScheduleId != -1) showAddOptionsPopupMenu(v);
            else Toast.makeText(this, "请先创建课表", Toast.LENGTH_SHORT).show();
        });
    }

    private void initScheduleData() {
        courseViewModel.getAllSchedules().observe(this, schedules -> {
            if (schedules == null || schedules.isEmpty()) {
                courseViewModel.insertSchedule("默认课表");
            } else {
                boolean exists = false;
                for (Schedule s : schedules) { if (s.scheduleId == currentScheduleId) exists = true; }
                if (!exists) switchToSchedule(schedules.get(0));
            }
        });
    }

    private void switchToSchedule(Schedule schedule) {
        currentScheduleId = schedule.scheduleId;
        currentScheduleName = schedule.scheduleName;
        binding.tvTitle.setText(currentScheduleName);
        courseViewModel.getCoursesBySchedule(currentScheduleId).observe(this, list -> {
            this.currentCoursesSnapshot = list;
            renderGridView(list);
        });
    }

    private void renderGridView(List<CourseWithTimes> list) {
        binding.glCourseContainer.removeAllViews();
        binding.llPeriods.removeAllViews();
        binding.llWeekHeader.removeAllViews();

        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int periodWidth = (int) (40 * dm.density);
        int cellWidth = (dm.widthPixels - periodWidth) / 7;
        int cellHeight = (int) (zoomLevel * dm.density);

        // 1. 渲染星期表头
        String[] weeks = {"一", "二", "三", "四", "五", "六", "日"};
        binding.llWeekHeader.setPadding(periodWidth, 0, 0, 0);
        int weekFontSize = isLandscape ? 16 : (zoomLevel < 60 ? 11 : 13);
        for (String w : weeks) {
            TextView tv = new TextView(this);
            tv.setText(w); tv.setWidth(cellWidth); tv.setGravity(Gravity.CENTER);
            tv.setTextColor(Color.WHITE); tv.setTextSize(weekFontSize);
            binding.llWeekHeader.addView(tv);
        }

        binding.glCourseContainer.setColumnCount(7);
        binding.glCourseContainer.setRowCount(12);

        // --- 【关键修复：添加列宽占位符】 ---
        // 为 7 列每一列都添加一个高度为 0、宽度为 cellWidth 的 View
        // 确保即使中间某天没课，列也不会塌陷前移
        for (int i = 0; i < 7; i++) {
            View columnSpacer = new View(this);
            GridLayout.LayoutParams spacerLp = new GridLayout.LayoutParams(
                    GridLayout.spec(0), // 放在第一行
                    GridLayout.spec(i)  // 每一列
            );
            spacerLp.width = cellWidth;
            spacerLp.height = 0; // 不占高度
            binding.glCourseContainer.addView(columnSpacer, spacerLp);
        }

        // 2. 渲染左侧节次
        int periodFontSize = isLandscape ? 14 : (zoomLevel < 60 ? 10 : 12);
        for (int i = 1; i <= 12; i++) {
            TextView tv = new TextView(this);
            tv.setText(String.valueOf(i)); tv.setHeight(cellHeight); tv.setGravity(Gravity.CENTER);
            tv.setTextColor(Color.WHITE); tv.setTextSize(periodFontSize);
            binding.llPeriods.addView(tv);

            // 原代码此处的 placeholder 已不再需要，由上方的 columnSpacer 代替
        }

        // 3. 渲染课程格
        if (list == null) return;
        for (CourseWithTimes item : list) {
            for (CourseTime time : item.times) {
                ItemGridCourseBinding itemBinding = ItemGridCourseBinding.inflate(getLayoutInflater());
                itemBinding.tvGridCourseName.setText(item.course.courseName);
                itemBinding.tvGridLocation.setText(time.location);
                itemBinding.tvTeacherName.setText(item.course.teacherName);

                int nameSize = isLandscape ? 14 : (zoomLevel < 60 ? 8 : 10);
                itemBinding.tvGridCourseName.setTextSize(nameSize);
                itemBinding.tvGridLocation.setTextSize(isLandscape ? 11 : 9);
                itemBinding.tvGridLocation.setVisibility(zoomLevel < 55 && !isLandscape ? View.GONE : View.VISIBLE);

                try { itemBinding.llGridBg.setBackgroundColor(Color.parseColor(item.course.colorCode)); }
                catch (Exception e) { itemBinding.llGridBg.setBackgroundColor(Color.parseColor("#4CAF50")); }

                // 计算跨行
                int rowSpan = time.endPeriod - time.startPeriod + 1;
                // 注意：dayOfWeek 如果是 1-7，则减 1 对应 0-6 列
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(time.startPeriod - 1, rowSpan),
                        GridLayout.spec(time.dayOfWeek - 1));

                params.width = cellWidth;
                params.height = cellHeight * rowSpan;
                params.setGravity(Gravity.FILL);
                params.setMargins(2, 2, 2, 2);

                itemBinding.getRoot().setOnClickListener(v -> showHomeworkManagerDialog(item.course));
                itemBinding.getRoot().setOnLongClickListener(v -> { showEditCourseDialog(item, time); return true; });
                itemBinding.ivGridNav.setOnClickListener(v -> {
                    Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(time.location));
                    try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) {}
                });
                binding.glCourseContainer.addView(itemBinding.getRoot(), params);
            }
        }
    }

    private void showScheduleListManageDialog() {
        RecyclerView rv = new RecyclerView(this);
        rv.setLayoutManager(new LinearLayoutManager(this));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("管理课表库")
                .setView(rv)
                .setPositiveButton("新建课表", (d, w) -> showCreateScheduleDialog())
                .setNegativeButton("关闭", null)
                .create();

        ScheduleManageAdapter adapter = new ScheduleManageAdapter(new ScheduleManageAdapter.OnScheduleActionListener() {
            @Override
            public void onSelect(Schedule s) {
                switchToSchedule(s);
                dialog.dismiss();
            }

            @Override
            public void onDelete(Schedule s) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("确认删除")
                        .setMessage("确定删除课表 [" + s.scheduleName + "] 吗？")
                        .setPositiveButton("删除", (d2, w2) -> courseViewModel.deleteSchedule(s))
                        .setNegativeButton("取消", null).show();
            }
        });

        rv.setAdapter(adapter);
        courseViewModel.getAllSchedules().observe(this, adapter::setList);
        dialog.show();
    }

    private void showAddOptionsPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add(0, 1, 0, "📝 手动添加课程");
        popup.getMenu().add(0, 2, 1, "📥 从外部导入课程");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) showAddCourseDialog();
            else showImportCoursePlaceholder();
            return true;
        });
        popup.show();
    }

    private void showImportCoursePlaceholder() {
        new AlertDialog.Builder(this).setTitle("外部导入").setMessage("导入功能开发中，敬请期待。").setPositiveButton("知道了", null).show();
    }

    private void showCreateScheduleDialog() {
        EditText et = new EditText(this); et.setHint("输入课表名称");
        new AlertDialog.Builder(this).setTitle("新建课表").setView(et).setPositiveButton("创建", (d, w) -> {
            String name = et.getText().toString().trim();
            if (!name.isEmpty()) courseViewModel.insertSchedule(name);
        }).show();
    }

    private void showAddCourseDialog() {
        DialogAddCourseBinding dBinding = DialogAddCourseBinding.inflate(getLayoutInflater());
        new AlertDialog.Builder(this).setTitle("手动添加").setView(dBinding.getRoot()).setPositiveButton("保存", (dialog, which) -> {
            try {
                String name = dBinding.etCourseName.getText().toString().trim();
                String teacher = dBinding.etTeacherName.getText().toString().trim();
                int day = Integer.parseInt(dBinding.etDayOfWeek.getText().toString());
                int start = Integer.parseInt(dBinding.etStartPeriod.getText().toString());
                int end = Integer.parseInt(dBinding.etEndPeriod.getText().toString());

                boolean conflict = false;
                for (CourseWithTimes it : currentCoursesSnapshot) {
                    for (CourseTime ct : it.times) { if (ConflictUtils.isConflicting(day, start, end, ct)) { conflict = true; break; } }
                }
                if (conflict) Toast.makeText(this, "时间冲突", Toast.LENGTH_SHORT).show();
                else {
                    Course c = new Course(); c.courseName = name; c.teacherName = teacher;
                    c.scheduleId = currentScheduleId; c.colorCode = generateRandomColor();
                    CourseTime t = new CourseTime(); t.dayOfWeek = day; t.startPeriod = start; t.endPeriod = end;
                    t.location = dBinding.etLocation.getText().toString().trim();
                    courseViewModel.insertCourseWithTime(c, t);
                }
            } catch (Exception e) { Toast.makeText(this, "输入错误", Toast.LENGTH_SHORT).show(); }
        }).show();
    }

    private void showEditCourseDialog(CourseWithTimes item, CourseTime currentTime) {
        DialogAddCourseBinding eBinding = DialogAddCourseBinding.inflate(getLayoutInflater());
        eBinding.etCourseName.setText(item.course.courseName);
        eBinding.etTeacherName.setText(item.course.teacherName);
        eBinding.etLocation.setText(currentTime.location);
        eBinding.etDayOfWeek.setText(String.valueOf(currentTime.dayOfWeek));
        eBinding.etStartPeriod.setText(String.valueOf(currentTime.startPeriod));
        eBinding.etEndPeriod.setText(String.valueOf(currentTime.endPeriod));

        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("管理课程").setView(eBinding.getRoot())
                .setPositiveButton("更新", null).setNeutralButton("删除", (d, w) -> courseViewModel.deleteCourse(item.course)).create();

        dialog.setOnShowListener(di -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                int day = Integer.parseInt(eBinding.etDayOfWeek.getText().toString());
                int start = Integer.parseInt(eBinding.etStartPeriod.getText().toString());
                int end = Integer.parseInt(eBinding.etEndPeriod.getText().toString());
                boolean conflict = false;
                for (CourseWithTimes other : currentCoursesSnapshot) {
                    if (other.course.courseId != item.course.courseId) {
                        for (CourseTime ct : other.times) { if (ConflictUtils.isConflicting(day, start, end, ct)) { conflict = true; break; } }
                    }
                }
                if (conflict) Toast.makeText(this, "时间冲突", Toast.LENGTH_SHORT).show();
                else {
                    item.course.courseName = eBinding.etCourseName.getText().toString().trim();
                    item.course.teacherName = eBinding.etTeacherName.getText().toString().trim();
                    currentTime.dayOfWeek = day; currentTime.startPeriod = start; currentTime.endPeriod = end;
                    currentTime.location = eBinding.etLocation.getText().toString();
                    courseViewModel.updateCourseWithTime(item.course, currentTime);
                    dialog.dismiss();
                }
            } catch (Exception e) { Toast.makeText(this, "输入错误", Toast.LENGTH_SHORT).show(); }
        }));
        dialog.show();
    }

    private void showHomeworkManagerDialog(Course course) {
        DialogHomeworkManagerBinding mBinding = DialogHomeworkManagerBinding.inflate(getLayoutInflater());
        mBinding.rvHomeworkList.setLayoutManager(new LinearLayoutManager(this));

        HomeworkAdapter adapter = new HomeworkAdapter(homework -> courseViewModel.deleteHomework(homework));
        mBinding.rvHomeworkList.setAdapter(adapter);

        courseViewModel.getHomeworkByCourse(course.courseId).observe(this, adapter::setList);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(mBinding.getRoot()).setPositiveButton("关闭", null).create();
        mBinding.btnAddNewHomework.setOnClickListener(v -> { dialog.dismiss(); showAddHomeworkDialog(course); });
        dialog.show();
    }

    private void showAddHomeworkDialog(Course course) {
        DialogAddHomeworkBinding hBinding = DialogAddHomeworkBinding.inflate(getLayoutInflater());
        Calendar cal = Calendar.getInstance(); cal.add(Calendar.HOUR_OF_DAY, 1);
        hBinding.swReminder.setOnCheckedChangeListener((b, isChecked) -> {
            hBinding.btnPickTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            hBinding.tvSelectedTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        hBinding.btnPickTime.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, day) -> {
                cal.set(Calendar.YEAR, year); cal.set(Calendar.MONTH, month); cal.set(Calendar.DAY_OF_MONTH, day);
                new TimePickerDialog(this, (view1, h, m) -> {
                    cal.set(Calendar.HOUR_OF_DAY, h); cal.set(Calendar.MINUTE, m); cal.set(Calendar.SECOND, 0);
                    hBinding.tvSelectedTime.setText("提醒: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(cal.getTime()));
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
        new AlertDialog.Builder(this).setTitle("添加作业").setView(hBinding.getRoot()).setPositiveButton("添加", (d, w) -> {
            String content = hBinding.etHomeworkContent.getText().toString().trim();
            if (content.isEmpty()) return;
            long reminder = hBinding.swReminder.isChecked() ? cal.getTimeInMillis() : 0;
            courseViewModel.insertHomework(new Homework(course.courseId, content, reminder));
            if (reminder > System.currentTimeMillis()) scheduleNotification(course.courseName, content, reminder);
        }).show();
    }

    private void showZoomControlDialog() {
        SeekBar sb = new SeekBar(this); sb.setMax(150);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) sb.setMin(45);
        sb.setProgress(zoomLevel); sb.setPadding(60, 60, 60, 60);
        new AlertDialog.Builder(this).setTitle("调整缩放 (" + zoomLevel + "dp)").setView(sb)
                .setPositiveButton("确定", (d, w) -> getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(KEY_ZOOM, zoomLevel).apply()).show();
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) { if(p>45){zoomLevel=p; renderGridView(currentCoursesSnapshot);} }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
    }

    private void enableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) getWindow().setDecorFitsSystemWindows(false);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.tvTitle, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, insets.top, 0, 0);
            return windowInsets;
        });
    }

    private void loadBlurredBackground() {
        Glide.with(this).load("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1080")
                .centerCrop().apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3))).into(binding.ivBackground);
    }

    private void scheduleNotification(String title, String content, long time) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && am != null && !am.canScheduleExactAlarms()) {
            startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)); return;
        }
        Intent i = new Intent(this, AlarmReceiver.class);
        i.putExtra("title", title); i.putExtra("content", "作业提醒: " + content);
        PendingIntent pi = PendingIntent.getBroadcast(this, (int)System.currentTimeMillis(), i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (am != null) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi);
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
    }

    private String generateRandomColor() {
        String[] colors = {"#EF5350", "#EC407A", "#AB47BC", "#7E57C2", "#5C6BC0", "#42A5F5", "#26A69A", "#66BB6A", "#FFA726", "#8D6E63"};
        return colors[(int) (Math.random() * colors.length)];
    }
}
