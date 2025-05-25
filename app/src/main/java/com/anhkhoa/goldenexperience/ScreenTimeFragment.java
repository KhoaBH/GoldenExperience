package com.anhkhoa.goldenexperience;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ScreenTimeFragment extends Fragment {

    private UsageStatsManager usageStatsManager;
    private BarChart barChart;
    private View rootView;
    private Button btnToday, btnWeek, btnMonth;
    private Handler handler;
    private Runnable updateRunnable;
    private String currentPeriod = "today";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_screen_time, container, false);

        usageStatsManager =
                (UsageStatsManager) requireContext().getSystemService(Context.USAGE_STATS_SERVICE);
        barChart = rootView.findViewById(R.id.barChart);

        btnToday = rootView.findViewById(R.id.btnToday);
        btnWeek  = rootView.findViewById(R.id.btnWeek);
        btnMonth = rootView.findViewById(R.id.btnMonth);

        btnToday.setOnClickListener(v -> selectPeriod("today"));
        btnWeek.setOnClickListener(v -> selectPeriod("week"));
        btnMonth.setOnClickListener(v -> selectPeriod("month"));

        selectPeriod("today");

        handler = new Handler();
        updateRunnable = () -> {
            loadUsageData(currentPeriod);
            handler.postDelayed(updateRunnable, 60_000);
        };
        handler.post(updateRunnable);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) handler.removeCallbacks(updateRunnable);
    }

    private void selectPeriod(String period) {
        currentPeriod = period;
        loadUsageData(period);
        updateButtonColors(period);
    }

    private void loadUsageData(String period) {
        long endTime = System.currentTimeMillis();
        long startTime;
        TextView txtAvg = rootView.findViewById(R.id.txtAverageTime);
        List<String> xLabels = new ArrayList<>();
        Map<String, Float> usageMap = new LinkedHashMap<>();
        long todayTotalMs = 0L;

        switch (period) {
            case "today":
                startTime = getStartOfDay();
                txtAvg.setVisibility(View.GONE);
                String todayLabel = new SimpleDateFormat("dd/MM", Locale.getDefault())
                        .format(new Date());
                xLabels.add(todayLabel);
                usageMap.put(todayLabel, 0f);
                break;

            case "week":
                startTime = getStartOfWeek();
                txtAvg.setVisibility(View.VISIBLE);
                xLabels = List.of("Su","M","Tu","W","Th","F","Sa");
                for (String d : xLabels) usageMap.put(d, 0f);
                break;

            case "month":
                startTime = get30DaysAgo();
                txtAvg.setVisibility(View.VISIBLE);
                for (int i = 29; i >= 0; i--) {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_YEAR, -i);
                    String date = new SimpleDateFormat("dd/MM", Locale.getDefault())
                            .format(cal.getTime());
                    xLabels.add(date);
                    usageMap.put(date, 0f);
                }
                break;

            default:
                startTime = getStartOfDay();
        }

        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        if (stats == null || stats.isEmpty()) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            return;
        }

        for (UsageStats s : stats) {
            long usedMs = s.getTotalTimeInForeground();
            if (usedMs <= 0) continue;
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(s.getLastTimeUsed());

            switch (period) {
                case "today":
                    String key = xLabels.get(0);
                    usageMap.put(key, usageMap.get(key) + usedMs/60000f);
                    todayTotalMs += usedMs;
                    break;

                case "week":
                    int dow = cal.get(Calendar.DAY_OF_WEEK)-1;
                    int todayIdx = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1;
                    if (dow <= todayIdx) {
                        String wk = xLabels.get(dow);
                        usageMap.put(wk, usageMap.get(wk) + usedMs/60000f);
                    }
                    break;

                case "month":
                    String dk = new SimpleDateFormat("dd/MM", Locale.getDefault())
                            .format(cal.getTime());
                    if (usageMap.containsKey(dk)) {
                        usageMap.put(dk, usageMap.get(dk) + usedMs/60000f);
                    }
                    break;
            }
        }

        if ("today".equals(period)) {
            ((TextView)rootView.findViewById(R.id.txtTodayTime))
                    .setText(formatMillis(todayTotalMs));
        }

        if ("week".equals(period)) {
            int daysIncluded = Calendar.getInstance()
                    .get(Calendar.DAY_OF_WEEK);
            updateAverageTime(usageMap, daysIncluded);
        } else if ("month".equals(period)) {
            updateAverageTime(usageMap);
        }

        updateChart(usageMap, xLabels);
    }

    private void updateButtonColors(String sel) {
        int selC = Color.parseColor("#4FC3F7"), selT = Color.WHITE;
        int defC = Color.parseColor("#EEEEEE"), defT = Color.BLACK;

        btnToday.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        sel.equals("today") ? selC : defC));
        btnToday.setTextColor(
                sel.equals("today") ? selT : defT);

        btnWeek.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        sel.equals("week") ? selC : defC));
        btnWeek.setTextColor(
                sel.equals("week") ? selT : defT);

        btnMonth.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        sel.equals("month") ? selC : defC));
        btnMonth.setTextColor(
                sel.equals("month") ? selT : defT);
    }

    private void updateAverageTime(Map<String, Float> data) {
        float sum = 0f;
        for (float v : data.values()) sum += v;
        long avgMin = data.isEmpty() ? 0L : (long)(sum / data.size());
        ((TextView)rootView.findViewById(R.id.txtAverageTime))
                .setText(formatMinutes(avgMin));
    }

    private void updateAverageTime(Map<String, Float> data, int daysIncluded) {
        float sum = 0f;
        for (float v : data.values()) sum += v;
        long avgMin = daysIncluded == 0 ? 0L : (long)(sum / daysIncluded);
        ((TextView)rootView.findViewById(R.id.txtAverageTime))
                .setText(formatMinutes(avgMin));
    }

    private void updateChart(Map<String, Float> data, List<String> labels) {
        List<BarEntry> entries = new ArrayList<>();
        float sum = 0f;
        for (int i = 0; i < labels.size(); i++) {
            float val = data.getOrDefault(labels.get(i), 0f);
            entries.add(new BarEntry(i, val));
            sum += val;
        }

        BarDataSet set = new BarDataSet(entries, "Screen Time");
        set.setColor(Color.parseColor("#4FC3F7"));
        BarData bd = new BarData(set);
        bd.setBarWidth(0.5f);
        barChart.setData(bd);

        XAxis x = barChart.getXAxis();
        x.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) {
                int idx = (int)v;
                return (idx >= 0 && idx < labels.size()) ? labels.get(idx) : "";
            }
        });
        x.setGranularity(1f);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);

        YAxis y = barChart.getAxisLeft();
        y.removeAllLimitLines();
        if (!"today".equals(currentPeriod)) {
            long avgMin = 0L;
            if ("week".equals(currentPeriod)) {
                int daysIncl = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                avgMin = daysIncl > 0 ? (long)(sum / daysIncl) : 0L;
            } else if ("month".equals(currentPeriod)) {
                int daysCount = labels.size();
                avgMin = daysCount > 0 ? (long)(sum / daysCount) : 0L;
            }
            LimitLine ll = new LimitLine(avgMin, "Avg");
            ll.setLineColor(Color.RED);
            ll.setLineWidth(2f);
            ll.enableDashedLine(10f,10f,0f);
            ll.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            ll.setTextColor(Color.RED);
            ll.setTextSize(12f);
            y.addLimitLine(ll);
        }
        y.setAxisMinimum(0f);
        y.setGranularity(60f);
        y.setLabelCount(5, true);
        y.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) {
                return Math.round(v / 60) + "h";
            }
        });

        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.invalidate();
    }

    private String formatMillis(long ms) {
        long h = TimeUnit.MILLISECONDS.toHours(ms);
        long m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60;
        return h + "h " + m + "min";
    }

    private String formatMinutes(long mins) {
        long h = mins / 60, m = mins % 60;
        return h + "h " + m + "min";
    }

    private long getStartOfDay() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        return c.getTimeInMillis();
    }

    private long getStartOfWeek() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        c.set(Calendar.HOUR_OF_DAY,0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        return c.getTimeInMillis();
    }

    private long get30DaysAgo() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR,-30);
        c.set(Calendar.HOUR_OF_DAY,0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        return c.getTimeInMillis();
    }
}
