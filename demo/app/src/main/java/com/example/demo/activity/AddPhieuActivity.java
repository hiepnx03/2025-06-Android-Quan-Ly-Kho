package com.example.demo.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.R;
import com.example.demo.model.MatHangModel;
import com.example.demo.model.PhieuModel;
import com.example.demo.util.DatabaseHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddPhieuActivity extends AppCompatActivity {

    private Spinner spinnerLoai;
    private EditText edtNgay, edtMaHang, edtTenHang, edtSoLuong, edtDonGia;
    private ImageButton btnDatePicker;
    private Button btnThemMatHang, btnLuu;
    private DatabaseHelper dbHelper;
    private List<MatHangModel> danhSachMatHang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Khởi tạo các thành phần
        initializeComponents();

        // Thiết lập dữ liệu cho Spinner
        setupSpinner();

        // Thiết lập sự kiện click
        setupClickListeners();
    }

    private void initializeComponents() {
        spinnerLoai = findViewById(R.id.spinnerLoaiGiaoDich);
        edtNgay = findViewById(R.id.edtNgay);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        edtMaHang = findViewById(R.id.edtMaHang);
        edtTenHang = findViewById(R.id.edtTenHang);
        edtSoLuong = findViewById(R.id.edtSoLuong);
        edtDonGia = findViewById(R.id.edtDonGia);
        btnThemMatHang = findViewById(R.id.btnThemMatHang);
        btnLuu = findViewById(R.id.btnLuu);
        dbHelper = new DatabaseHelper(this);
        danhSachMatHang = new ArrayList<>();

        // Kiểm tra null để tránh NullPointerException
        if (spinnerLoai == null || edtNgay == null || btnDatePicker == null || edtMaHang == null ||
                edtTenHang == null || edtSoLuong == null || edtDonGia == null || btnThemMatHang == null || btnLuu == null) {
            throw new IllegalStateException("Một hoặc nhiều thành phần giao diện không được tìm thấy trong layout");
        }
    }

    private void setupSpinner() {
        String[] loai = {"Nhập", "Xuất"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, loai);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLoai.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnThemMatHang.setOnClickListener(v -> themMatHang());
        btnLuu.setOnClickListener(v -> luuPhieu());
        btnDatePicker.setOnClickListener(v -> showDatePickerDialog());
    }

    private void themMatHang() {
        try {
            String maHang = edtMaHang.getText().toString().trim();
            String tenHang = edtTenHang.getText().toString().trim();
            String soLuongStr = edtSoLuong.getText().toString().trim();
            String donGiaStr = edtDonGia.getText().toString().trim();

            if (maHang.isEmpty() || tenHang.isEmpty() || soLuongStr.isEmpty() || donGiaStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            int soLuong = Integer.parseInt(soLuongStr);
            double donGia = Double.parseDouble(donGiaStr);

            if (soLuong <= 0 || donGia <= 0) {
                Toast.makeText(this, "Số lượng và đơn giá phải lớn hơn 0!", Toast.LENGTH_SHORT).show();
                return;
            }

            danhSachMatHang.add(new MatHangModel(maHang, tenHang, soLuong, donGia));
            Toast.makeText(this, "Đã thêm mặt hàng thành công!", Toast.LENGTH_SHORT).show();

            clearInputFields();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Lỗi: Số lượng hoặc đơn giá không hợp lệ!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void luuPhieu() {
        try {
            String loaiGiaoDich = spinnerLoai.getSelectedItem().toString();
            String ngay = edtNgay.getText().toString().trim();

            if (ngay.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập hoặc chọn ngày giao dịch!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (danhSachMatHang.isEmpty()) {
                Toast.makeText(this, "Vui lòng thêm ít nhất 1 mặt hàng!", Toast.LENGTH_SHORT).show();
                return;
            }

            double tongTien = calculateTotalAmount();
            String maPhieu = "P" + System.currentTimeMillis();

            PhieuModel phieu = new PhieuModel(maPhieu, loaiGiaoDich, ngay, tongTien, new ArrayList<>(danhSachMatHang));

            boolean success = dbHelper.insertPhieu(phieu);
            if (success) {
                Toast.makeText(this, "Đã lưu phiếu giao dịch thành công!", Toast.LENGTH_SHORT).show();
                clearAllFields();
            } else {
                Toast.makeText(this, "Lỗi khi lưu phiếu! Kiểm tra số lượng tồn kho.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi lưu phiếu: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                    edtNgay.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private double calculateTotalAmount() {
        double tongTien = 0;
        for (MatHangModel mh : danhSachMatHang) {
            tongTien += mh.getSoLuong() * mh.getDonGia();
        }
        return tongTien;
    }

    private void clearInputFields() {
        edtMaHang.setText("");
        edtTenHang.setText("");
        edtSoLuong.setText("");
        edtDonGia.setText("");
    }

    private void clearAllFields() {
        edtNgay.setText("");
        danhSachMatHang.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}