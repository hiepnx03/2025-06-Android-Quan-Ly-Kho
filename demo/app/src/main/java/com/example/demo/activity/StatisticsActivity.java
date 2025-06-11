package com.example.demo.activity;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.R;
import com.example.demo.util.DatabaseHelper;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private TextView txtThangHienTai, tvTongGiaTriThang, tvTongSoLuongTon, tvSoLuongGiaoDichNhap,
            tvSoLuongGiaoDichXuat, tvTongGiaoDichThang;
    private Spinner spinnerMonthYear;
    private Button btnReset;
    private DatabaseHelper dbHelper;
    private String currentThangNam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thongke);

        // Khởi tạo các thành phần
        initializeComponents();

        // Tải dữ liệu ban đầu
        loadInitialData();
    }

    private void initializeComponents() {
        txtThangHienTai = findViewById(R.id.txtThangHienTai);
        tvTongGiaTriThang = findViewById(R.id.tvTongGiaTriThang);
        tvTongSoLuongTon = findViewById(R.id.tvTongSoLuongTon);
        tvSoLuongGiaoDichNhap = findViewById(R.id.tvSoLuongGiaoDichNhap);
        tvSoLuongGiaoDichXuat = findViewById(R.id.tvSoLuongGiaoDichXuat);
        tvTongGiaoDichThang = findViewById(R.id.tvTongGiaoDichThang);
        btnReset = findViewById(R.id.btnReset);

        dbHelper = new DatabaseHelper(this);
        setupInitialMonth();
        setupMonthPicker();

        if (txtThangHienTai == null || tvTongGiaTriThang == null || tvTongSoLuongTon == null ||
                tvSoLuongGiaoDichNhap == null || tvSoLuongGiaoDichXuat == null || tvTongGiaoDichThang == null) {
            throw new IllegalStateException("Một hoặc nhiều TextView không được tìm thấy trong layout");
        }

        // Xử lý sự kiện cho nút Reset
        btnReset.setOnClickListener(v -> {
            new AlertDialog.Builder(StatisticsActivity.this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc muốn reset toàn bộ dữ liệu (tồn kho, phiếu, chi tiết phiếu)? Thao tác này không thể hoàn tác.")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        dbHelper.resetAllData();
                        loadThongKeTheoThang(currentThangNam);
                        Toast.makeText(StatisticsActivity.this, "Đã reset toàn bộ dữ liệu!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void setupInitialMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        currentThangNam = sdf.format(new Date());
        txtThangHienTai.setText(getString(R.string.statistics_month, currentThangNam));
    }

    private void setupMonthPicker() {
        txtThangHienTai.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    StatisticsActivity.this,
                    (view, selectedYear, selectedMonth, dayOfMonth) -> {
                        currentThangNam = String.format(Locale.getDefault(), "%02d/%04d", selectedMonth + 1, selectedYear);
                        txtThangHienTai.setText(getString(R.string.statistics_month, currentThangNam));
                        loadThongKeTheoThang(currentThangNam);
                    },
                    year,
                    month,
                    1
            );

            try {
                int dayId = getResources().getIdentifier("day", "id", "android");
                if (dayId != 0) {
                    View dayView = datePickerDialog.getDatePicker().findViewById(dayId);
                    if (dayView != null) {
                        dayView.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            datePickerDialog.show();
        });
    }

    private void loadInitialData() {
        loadThongKeTheoThang(currentThangNam);
    }

    private void loadThongKeTheoThang(String thangNam) {
        if (dbHelper == null) {
            showErrorState();
            Log.e(TAG, "DatabaseHelper is null");
            return;
        }

        try {
            Log.d(TAG, "Bắt đầu tải dữ liệu thống kê cho tháng: " + thangNam);
            dbHelper.logPhieuData();
            dbHelper.logSanPhamData();

            double tongGiaTri = dbHelper.getTongGiaTriGiaoDichTheoThang(thangNam);
            int tongTonKho = dbHelper.getTongSoLuongTon();
            int soLuongNhap = dbHelper.getSoLuongPhieuTheoLoai("nhap", thangNam);
            int soLuongXuat = dbHelper.getSoLuongPhieuTheoLoai("xuat", thangNam);
            int tongGiaoDich = soLuongNhap + soLuongXuat;

            Log.d(TAG, "Dữ liệu thống kê tháng " + thangNam + ":");
            Log.d(TAG, "Tổng giá trị: " + tongGiaTri);
            Log.d(TAG, "Tổng tồn kho: " + tongTonKho);
            Log.d(TAG, "Số lượng nhập: " + soLuongNhap);
            Log.d(TAG, "Số lượng xuất: " + soLuongXuat);
            Log.d(TAG, "Tổng giao dịch: " + tongGiaoDich);

            DecimalFormat formatter = new DecimalFormat("#,###");
            String formattedGiaTri = formatter.format(tongGiaTri);

            runOnUiThread(() -> {
                try {
                    tvTongGiaTriThang.setText(getString(R.string.total_transaction_value, formattedGiaTri));
                    tvTongSoLuongTon.setText(getString(R.string.total_inventory, tongTonKho));
                    tvSoLuongGiaoDichNhap.setText(getString(R.string.import_transaction_count, soLuongNhap));
                    tvSoLuongGiaoDichXuat.setText(getString(R.string.export_transaction_count, soLuongXuat));
                    tvTongGiaoDichThang.setText(getString(R.string.total_transactions, tongGiaoDich));
                    Log.d(TAG, "Cập nhật giao diện thành công");

                    // Thông báo nếu không có giao dịch
                    if (tongGiaTri == 0 && soLuongNhap == 0 && soLuongXuat == 0) {
                        Toast.makeText(StatisticsActivity.this, "Không có giao dịch trong tháng " + thangNam, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi khi cập nhật giao diện: " + e.getMessage());
                    e.printStackTrace();
                    showErrorState();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tải thống kê: " + e.getMessage());
            e.printStackTrace();
            showErrorState();
        }
    }

    private void showErrorState() {
        tvTongGiaTriThang.setText(R.string.error_loading_data);
        tvTongSoLuongTon.setText("");
        tvSoLuongGiaoDichNhap.setText("");
        tvSoLuongGiaoDichXuat.setText("");
        tvTongGiaoDichThang.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}