package com.example.demo.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.demo.R;
import com.example.demo.model.MatHangModel;
import com.example.demo.model.PhieuModel;

import java.text.DecimalFormat;
import java.util.List;

public class PhieuAdapter extends BaseAdapter {
    private final Context context;
    private final List<PhieuModel> phieuList;
    private final OnDeleteListener deleteListener;

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    private static class ViewHolder {
        TextView tvLoaiNgay;
        TextView layoutDanhSachMatHang;
        TextView tvTong;
        Button btnXoa;
    }

    public PhieuAdapter(Context context, List<PhieuModel> phieuList, OnDeleteListener deleteListener) {
        this.context = context;
        this.phieuList = phieuList;
        this.deleteListener = deleteListener;
    }

    @Override
    public int getCount() {
        return phieuList.size();
    }

    @Override
    public Object getItem(int position) {
        return phieuList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return phieuList.get(position).getMaPhieu().hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        PhieuModel phieu = phieuList.get(position);
        DecimalFormat formatter = new DecimalFormat("#,###.##");

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.list_item_phieu, parent, false);
            holder = new ViewHolder();
            holder.tvLoaiNgay = convertView.findViewById(R.id.tvLoaiNgay);
            holder.layoutDanhSachMatHang = convertView.findViewById(R.id.layoutDanhSachMatHang);
            holder.tvTong = convertView.findViewById(R.id.tvTong);
            holder.btnXoa = convertView.findViewById(R.id.btnXoa);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvLoaiNgay.setText(phieu.getLoai() + " - " + phieu.getNgay());

        StringBuilder chiTiet = new StringBuilder();
        List<MatHangModel> matHangList = phieu.getDanhSachMatHang();
        if (matHangList.isEmpty()) {
            chiTiet.append("Không có mặt hàng");
        } else {
            for (MatHangModel item : matHangList) {
                chiTiet.append("- Mã Hàng: ").append(item.getMaHang())
                        .append(" | Tên Hàng: ").append(item.getTenHang()).append("\n")
                        .append("  Số Lượng: ").append(item.getSoLuong())
                        .append(" | Đơn Giá: ").append(formatter.format(item.getDonGia())).append(" đ\n\n");
            }
        }
        holder.layoutDanhSachMatHang.setText(chiTiet.toString().trim());

        holder.tvTong.setText("Thành tiền: " + formatter.format(phieu.getTongTien()) + " đ");

        holder.btnXoa.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc muốn xóa phiếu này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        if (deleteListener != null) {
                            deleteListener.onDelete(position);
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        return convertView;
    }
}