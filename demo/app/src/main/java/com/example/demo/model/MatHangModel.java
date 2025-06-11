package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatHangModel {
    private String maHang;
    private String tenHang;
    private int soLuong;
    private double donGia;
}
