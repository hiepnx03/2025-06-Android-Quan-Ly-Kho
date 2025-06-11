package com.example.demo.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhieuModel {
    private String maPhieu;
    private String loai;
    private String ngay;
    private double tongTien;
    private List<MatHangModel> danhSachMatHang;
}

