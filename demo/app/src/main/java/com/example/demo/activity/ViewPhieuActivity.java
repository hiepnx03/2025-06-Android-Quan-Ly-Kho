package com.example.demo.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.R;
import com.example.demo.adapter.PhieuAdapter;
import com.example.demo.model.MatHangModel;
import com.example.demo.model.PhieuModel;
import com.example.demo.util.DatabaseHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ViewPhieuActivity extends AppCompatActivity {

    private ListView transactionsListView;
    private EditText searchEditText;
    private PhieuAdapter adapter;
    private List<PhieuModel> danhSachPhieu;
    private List<PhieuModel> filteredPhieuList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_phieu);

        // Khởi tạo các thành phần
        initializeComponents();

        // Thiết lập bộ lọc tìm kiếm
        dbHelper.exportDatabase();
        setupSearchListener();
    }

    private void initializeComponents() {
        transactionsListView = findViewById(R.id.lv_phieu);
        searchEditText = findViewById(R.id.edtSearch);
        dbHelper = new DatabaseHelper(this);

        danhSachPhieu = dbHelper.getAllPhieu();
        filteredPhieuList = new ArrayList<>(danhSachPhieu);
        adapter = new PhieuAdapter(this, filteredPhieuList, this::deletePhieu); // Truyền callback xóa
        transactionsListView.setAdapter(adapter);

        if (transactionsListView == null || searchEditText == null) {
            throw new IllegalStateException("Một hoặc nhiều thành phần giao diện không được tìm thấy trong layout");
        }
    }

    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String keyword) {
        filteredPhieuList.clear();
        if (keyword.isEmpty()) {
            filteredPhieuList.addAll(danhSachPhieu);
        } else {
            keyword = keyword.toLowerCase();
            for (PhieuModel p : danhSachPhieu) {
                for (MatHangModel c : p.getDanhSachMatHang()) {
                    if (c.getMaHang().toLowerCase().contains(keyword)) {
                        filteredPhieuList.add(p);
                        break;
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void deletePhieu(int position) {
        if (position >= 0 && position < filteredPhieuList.size()) {
            PhieuModel phieu = filteredPhieuList.get(position);
            int originalIndex = danhSachPhieu.indexOf(phieu);
            if (originalIndex >= 0) {
                PhieuModel originalPhieu = danhSachPhieu.get(originalIndex);
                int phieuId = dbHelper.getPhieuId(originalPhieu.getMaPhieu());
                if (phieuId != -1) {
                    if (dbHelper.deletePhieuById(phieuId)) {
                        danhSachPhieu.remove(originalIndex);
                        filteredPhieuList.remove(position);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Đã xóa phiếu thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lỗi khi xóa phiếu! Kiểm tra số lượng tồn kho.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy ID phiếu để xóa!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private List<PhieuModel> getDummyPhieuData() {
        List<MatHangModel> chiTiet1 = Arrays.asList(
                new MatHangModel("B01", "Bút chì", 50, 5000),
                new MatHangModel("B02", "Bút", 30, 20000),
                new MatHangModel("S01", "Sách giáo khoa", 10, 30000),
                new MatHangModel("V01", "Vở kẻ ngang", 20, 7000)
        );
        PhieuModel p1 = new PhieuModel("1", "Nhập", "10/05/2025", 1290000, chiTiet1);

        List<MatHangModel> chiTiet2 = Collections.singletonList(
                new MatHangModel("V01", "Vở kẻ ngang", 5, 7000)
        );
        PhieuModel p2 = new PhieuModel("2", "Xuất", "15/05/2025", 35000, chiTiet2);

        List<MatHangModel> chiTiet3 = Collections.singletonList(
                new MatHangModel("B01", "Bút chì", 5, 5000)
        );
        PhieuModel p3 = new PhieuModel("3", "Xuất", "13/05/2025", 25000, chiTiet3);

        return Arrays.asList(p1, p2, p3);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}