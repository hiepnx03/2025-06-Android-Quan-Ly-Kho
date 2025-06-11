package com.example.demo.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.demo.model.MatHangModel;
import com.example.demo.model.PhieuModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "QuanLyKho.db";
    private static final int DATABASE_VERSION = 6;
    public static final String TABLE_PHIEU = "Phieu";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MA_PHIEU = "maPhieu";
    public static final String COLUMN_LOAI = "loai";
    public static final String COLUMN_NGAY = "ngay";
    public static final String COLUMN_TONG_TIEN = "tongTien";
    public static final String TABLE_CHI_TIET = "ChiTietPhieu";
    public static final String COLUMN_PHIEU_ID = "phieu_id";
    public static final String COLUMN_MA_HANG = "maHang";
    public static final String COLUMN_TEN_HANG = "tenHang";
    public static final String COLUMN_SO_LUONG = "soLuong";
    public static final String COLUMN_DON_GIA = "donGia";

    private final Context context;
    private static final String DB_PATH = "/data/data/com.example.demo/databases/";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
//        initializeDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Phieu (id INTEGER PRIMARY KEY AUTOINCREMENT, maPhieu TEXT NOT NULL, loai TEXT NOT NULL, ngay TEXT NOT NULL, tongTien REAL NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS sanpham (maHang TEXT PRIMARY KEY, tenHang TEXT NOT NULL, soLuong INTEGER NOT NULL, donGia REAL NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS ChiTietPhieu (id INTEGER PRIMARY KEY AUTOINCREMENT, maPhieu TEXT NOT NULL, maHang TEXT NOT NULL, soLuong INTEGER NOT NULL, donGia REAL NOT NULL, FOREIGN KEY (maPhieu) REFERENCES Phieu(maPhieu), FOREIGN KEY (maHang) REFERENCES sanpham(maHang))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ChiTietPhieu");
        db.execSQL("DROP TABLE IF EXISTS sanpham");
        db.execSQL("DROP TABLE IF EXISTS Phieu");
        onCreate(db);
    }

//    private void initializeDatabase() {
//        File dbFile = new File(DB_PATH + DATABASE_NAME);
//        SQLiteDatabase db = null;
//
//        try {
//            // Kiểm tra file trong assets
//            InputStream inputStream = context.getAssets().open(DATABASE_NAME);
//            Log.d(TAG, "Đã tìm thấy file " + DATABASE_NAME + " trong assets");
//
//            // Nếu file chưa tồn tại, sao chép từ assets
//            if (!dbFile.exists()) {
//                Log.d(TAG, "Sao chép database từ assets...");
//                File dbDir = new File(DB_PATH);
//                if (!dbDir.exists() && !dbDir.mkdirs()) {
//                    Log.e(TAG, "Không thể tạo thư mục " + DB_PATH);
//                    return;
//                }
//
//                try (FileOutputStream outputStream = new FileOutputStream(dbFile)) {
//                    byte[] buffer = new byte[1024];
//                    int length;
//                    while ((length = inputStream.read(buffer)) > 0) {
//                        outputStream.write(buffer, 0, length);
//                    }
//                    outputStream.flush();
//                    Log.d(TAG, "Sao chép thành công vào " + dbFile.getAbsolutePath());
//                }
//            } else {
//                Log.d(TAG, "Database đã tồn tại tại " + dbFile.getAbsolutePath());
//                // Mở database để kiểm tra
//                db = this.getReadableDatabase();
//                Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{TABLE_PHIEU});
//                if (cursor.moveToFirst() && cursor.getCount() > 0) {
//                    Log.d(TAG, "Database hợp lệ với bảng Phieu");
//                } else {
//                    Log.w(TAG, "Bảng Phieu không tồn tại, xóa và sao chép lại");
//                    db.close();
//                    if (dbFile.delete()) {
//                        initializeDatabase(); // Thử sao chép lại
//                    } else {
//                        Log.e(TAG, "Không thể xóa file database hiện tại");
//                    }
//                }
//                cursor.close();
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "Lỗi khi sao chép database: " + e.getMessage());
//            e.printStackTrace();
//            // Nếu không sao chép được, tạo database mới
//            try {
//                db = this.getWritableDatabase();
//                onCreate(db);
//                Log.d(TAG, "Đã tạo database mới vì sao chép thất bại");
//            } catch (Exception ex) {
//                Log.e(TAG, "Lỗi khi tạo database mới: " + ex.getMessage());
//                ex.printStackTrace();
//            }
//        } finally {
//            if (db != null) {
//                db.close();
//            }
//        }
//    }

    private void initializeDatabase() {
        SQLiteDatabase db = null;
        try {
            // Xóa database cũ nếu cần (chỉ dùng khi gỡ lỗi)
            // context.deleteDatabase(DATABASE_NAME);
            // Log.d(TAG, "Đã xóa database cũ");

            db = this.getWritableDatabase();
            onCreate(db); // Đảm bảo các bảng được tạo
            themDuLieuMau(); // Thêm dữ liệu mẫu
            standardizeLoaiColumn(); // Chuẩn hóa cột loai
//            removeDuplicatePhieu(); // Xóa bản ghi trùng lặp
            Log.d(TAG, "Đã tạo database mới và thêm dữ liệu mẫu");
        } catch (Exception ex) {
            Log.e(TAG, "Lỗi khi tạo database mới: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
    public void standardizeLoaiColumn() {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL("UPDATE " + TABLE_PHIEU + " SET " + COLUMN_LOAI + " = 'nhap' WHERE LOWER(" + COLUMN_LOAI + ") = 'nhập'");
            db.execSQL("UPDATE " + TABLE_PHIEU + " SET " + COLUMN_LOAI + " = 'xuat' WHERE LOWER(" + COLUMN_LOAI + ") = 'xuất'");
            Log.d(TAG, "Chuẩn hóa cột loai thành công");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi chuẩn hóa cột loai: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.close();
        }
    }
    public void removeDuplicatePhieu() {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " + TABLE_PHIEU + " WHERE id NOT IN (" +
                    "SELECT MIN(id) FROM " + TABLE_PHIEU + " GROUP BY maPhieu, loai, ngay, tongTien)");
            Log.d(TAG, "Xóa bản ghi trùng lặp thành công");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xóa bản ghi trùng lặp: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public void exportDatabase() {
        File dbFile = new File(DB_PATH + DATABASE_NAME);
        File exportDir = new File(context.getExternalFilesDir(null), "DatabaseBackup");
        if (!exportDir.exists() && !exportDir.mkdirs()) {
            Log.e(TAG, "Không thể tạo thư mục backup");
            return;
        }
        File exportFile = new File(exportDir, DATABASE_NAME);

        try (FileInputStream inputStream = new FileInputStream(dbFile);
             FileOutputStream outputStream = new FileOutputStream(exportFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            Log.d(TAG, "Xuất database thành công vào " + exportFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Lỗi khi xuất database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void importDatabase(File importFile) {
        File dbFile = new File(DB_PATH + DATABASE_NAME);
        if (importFile.exists()) {
            try (FileInputStream inputStream = new FileInputStream(importFile);
                 FileOutputStream outputStream = new FileOutputStream(dbFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                Log.d(TAG, "Nhập database thành công từ " + importFile.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Lỗi khi nhập database: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "File nhập không tồn tại: " + importFile.getAbsolutePath());
        }
    }


    public boolean insertPhieu(PhieuModel phieu) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Lưu thông tin phiếu
            ContentValues phieuValues = new ContentValues();
            phieuValues.put(COLUMN_MA_PHIEU, phieu.getMaPhieu());
            String loai = phieu.getLoai().toLowerCase().equals("nhập") ? "nhap" : "xuat";
            phieuValues.put(COLUMN_LOAI, loai);
            phieuValues.put(COLUMN_NGAY, chuanHoaNgay(phieu.getNgay()));
            phieuValues.put(COLUMN_TONG_TIEN, phieu.getTongTien());
            long phieuResult = db.insert(TABLE_PHIEU, null, phieuValues);

            if (phieuResult == -1) {
                Log.e(TAG, "Lỗi khi chèn phiếu vào bảng Phieu");
                return false;
            }

            // Lấy ID của phiếu vừa chèn
            int phieuId = getPhieuId(phieu.getMaPhieu());
            if (phieuId == -1) {
                Log.e(TAG, "Không tìm thấy ID của phiếu vừa chèn");
                return false;
            }

            // Cập nhật số lượng tồn kho và lưu chi tiết mặt hàng
            for (MatHangModel matHang : phieu.getDanhSachMatHang()) {
                String maHang = matHang.getMaHang();
                int soLuong = matHang.getSoLuong();

                // Kiểm tra sản phẩm tồn tại
                Cursor cursor = db.rawQuery("SELECT soLuong FROM sanpham WHERE maHang = ?", new String[]{maHang});
                boolean sanPhamExists = cursor.moveToFirst();
                int soLuongHienTai = sanPhamExists ? cursor.getInt(0) : 0;
                cursor.close();

                if (loai.equals("xuat") && (!sanPhamExists || soLuongHienTai < soLuong)) {
                    Log.e(TAG, "Số lượng tồn kho không đủ cho mặt hàng: " + maHang);
                    throw new Exception("Số lượng tồn kho không đủ cho mặt hàng: " + maHang);
                }

                // Cập nhật hoặc thêm sản phẩm
                ContentValues spValues = new ContentValues();
                if (sanPhamExists) {
                    int soLuongMoi = loai.equals("nhap") ? soLuongHienTai + soLuong : soLuongHienTai - soLuong;
                    spValues.put("soLuong", soLuongMoi);
                    int rows = db.update("sanpham", spValues, "maHang = ?", new String[]{maHang});
                    if (rows == 0) {
                        Log.e(TAG, "Lỗi khi cập nhật số lượng tồn kho cho: " + maHang);
                        return false;
                    }
                } else if (loai.equals("nhap")) {
                    spValues.put("maHang", maHang);
                    spValues.put("tenHang", matHang.getTenHang());
                    spValues.put("soLuong", soLuong);
                    spValues.put("donGia", matHang.getDonGia());
                    long spResult = db.insert("sanpham", null, spValues);
                    if (spResult == -1) {
                        Log.e(TAG, "Lỗi khi thêm sản phẩm mới: " + maHang);
                        return false;
                    }
                }

                // Lưu chi tiết mặt hàng
                ContentValues chiTietValues = new ContentValues();
                chiTietValues.put(COLUMN_PHIEU_ID, phieuId);
                chiTietValues.put(COLUMN_MA_HANG, maHang);
                chiTietValues.put(COLUMN_TEN_HANG, matHang.getTenHang());
                chiTietValues.put(COLUMN_SO_LUONG, soLuong);
                chiTietValues.put(COLUMN_DON_GIA, matHang.getDonGia());
                long chiTietResult = db.insert(TABLE_CHI_TIET, null, chiTietValues);
                if (chiTietResult == -1) {
                    Log.e(TAG, "Lỗi khi chèn chi tiết mặt hàng: " + maHang);
                    return false;
                }
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lưu phiếu: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }


    public boolean updatePhieu(PhieuModel phieu) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MA_PHIEU, phieu.getMaPhieu());
        values.put(COLUMN_LOAI, phieu.getLoai());
        values.put(COLUMN_NGAY, chuanHoaNgay(phieu.getNgay()));
        values.put(COLUMN_TONG_TIEN, phieu.getTongTien());

        int rows = db.update(TABLE_PHIEU, values, COLUMN_ID + " = ?", new String[]{String.valueOf(getPhieuId(phieu.getMaPhieu()))});
        db.close();
        return rows > 0;
    }

    public boolean deletePhieuById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Lấy thông tin phiếu
            Cursor phieuCursor = db.rawQuery("SELECT " + COLUMN_MA_PHIEU + ", " + COLUMN_LOAI +
                    " FROM " + TABLE_PHIEU + " WHERE " + COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            if (!phieuCursor.moveToFirst()) {
                Log.e(TAG, "Không tìm thấy phiếu với ID: " + id);
                phieuCursor.close();
                return false;
            }

            String maPhieu = phieuCursor.getString(phieuCursor.getColumnIndexOrThrow(COLUMN_MA_PHIEU));
            String loai = phieuCursor.getString(phieuCursor.getColumnIndexOrThrow(COLUMN_LOAI)).toLowerCase();
            phieuCursor.close();

            // Lấy chi tiết mặt hàng
            Cursor chiTietCursor = db.rawQuery("SELECT " + COLUMN_MA_HANG + ", " + COLUMN_SO_LUONG +
                    " FROM " + TABLE_CHI_TIET + " WHERE " + COLUMN_PHIEU_ID + " = ?", new String[]{String.valueOf(id)});
            while (chiTietCursor.moveToNext()) {
                String maHang = chiTietCursor.getString(chiTietCursor.getColumnIndexOrThrow(COLUMN_MA_HANG));
                int soLuong = chiTietCursor.getInt(chiTietCursor.getColumnIndexOrThrow(COLUMN_SO_LUONG));

                // Kiểm tra sản phẩm tồn tại
                Cursor spCursor = db.rawQuery("SELECT soLuong FROM sanpham WHERE maHang = ?", new String[]{maHang});
                if (!spCursor.moveToFirst()) {
                    Log.e(TAG, "Sản phẩm không tồn tại: " + maHang);
                    spCursor.close();
                    chiTietCursor.close();
                    throw new Exception("Sản phẩm không tồn tại: " + maHang);
                }

                int soLuongHienTai = spCursor.getInt(0);
                spCursor.close();

                // Cập nhật số lượng tồn kho
                ContentValues spValues = new ContentValues();
                int soLuongMoi = loai.equals("nhap") ? soLuongHienTai - soLuong : soLuongHienTai + soLuong;
                if (soLuongMoi < 0) {
                    Log.e(TAG, "Số lượng tồn kho âm sau khi xóa phiếu: " + maHang);
                    chiTietCursor.close();
                    throw new Exception("Số lượng tồn kho âm sau khi xóa phiếu: " + maHang);
                }
                spValues.put("soLuong", soLuongMoi);
                int rows = db.update("sanpham", spValues, "maHang = ?", new String[]{maHang});
                if (rows == 0) {
                    Log.e(TAG, "Lỗi khi cập nhật số lượng tồn kho cho: " + maHang);
                    chiTietCursor.close();
                    return false;
                }
            }
            chiTietCursor.close();

            // Xóa chi tiết phiếu và phiếu
            db.delete(TABLE_CHI_TIET, COLUMN_PHIEU_ID + " = ?", new String[]{String.valueOf(id)});
            int rows = db.delete(TABLE_PHIEU, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            if (rows == 0) {
                Log.e(TAG, "Lỗi khi xóa phiếu với ID: " + id);
                return false;
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xóa phiếu: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public ArrayList<PhieuModel> getAllPhieu() {
        ArrayList<PhieuModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PHIEU, null)) {
            if (cursor.moveToFirst()) {
                do {
                    int phieuId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String maPhieu = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MA_PHIEU));
                    String loai = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOAI));
                    String ngay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NGAY));
                    double tongTien = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TONG_TIEN));

                    ArrayList<MatHangModel> dsMatHang = new ArrayList<>();
                    try (Cursor cMatHang = db.rawQuery("SELECT * FROM " + TABLE_CHI_TIET +
                            " WHERE " + COLUMN_PHIEU_ID + " = ?", new String[]{String.valueOf(phieuId)})) {
                        if (cMatHang.moveToFirst()) {
                            do {
                                String maHang = cMatHang.getString(cMatHang.getColumnIndexOrThrow(COLUMN_MA_HANG));
                                String tenHang = cMatHang.getString(cMatHang.getColumnIndexOrThrow(COLUMN_TEN_HANG));
                                int soLuong = cMatHang.getInt(cMatHang.getColumnIndexOrThrow(COLUMN_SO_LUONG));
                                double donGia = cMatHang.getDouble(cMatHang.getColumnIndexOrThrow(COLUMN_DON_GIA));
                                dsMatHang.add(new MatHangModel(maHang, tenHang, soLuong, donGia));
                            } while (cMatHang.moveToNext());
                        }
                    }

                    PhieuModel phieu = new PhieuModel(maPhieu, loai, ngay, tongTien, dsMatHang);
                    list.add(phieu);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lấy danh sách phiếu: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }


    public void themDuLieuMau() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Kiểm tra và thêm sản phẩm mẫu
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sanpham WHERE maHang = ?", new String[]{"SP001"});
            if (cursor.moveToFirst() && cursor.getInt(0) == 0) { // Chỉ thêm nếu chưa tồn tại
                ContentValues sp = new ContentValues();
                sp.put("maHang", "SP001");
                sp.put("tenHang", "Sản phẩm 1");
                sp.put("soLuong", 100);
                sp.put("donGia", 50000);
                db.insertOrThrow("sanpham", null, sp);

                sp.clear();
                sp.put("maHang", "SP002");
                sp.put("tenHang", "Sản phẩm 2");
                sp.put("soLuong", 50);
                sp.put("donGia", 75000);
                db.insertOrThrow("sanpham", null, sp);
                Log.d(TAG, "Đã thêm sản phẩm mẫu");
            }
            cursor.close();

            // Kiểm tra và thêm phiếu mẫu
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PHIEU + " WHERE maPhieu = ?", new String[]{"PN001"});
            if (cursor.moveToFirst() && cursor.getInt(0) == 0) { // Chỉ thêm nếu chưa tồn tại
                ContentValues phieu = new ContentValues();
                phieu.put("maPhieu", "PN001");
                phieu.put("loai", "nhap");
                phieu.put("ngay", "01/06/2025");
                phieu.put("tongTien", 5000000);
                db.insertOrThrow(TABLE_PHIEU, null, phieu);

                phieu.clear();
                phieu.put("maPhieu", "PX001");
                phieu.put("loai", "xuat");
                phieu.put("ngay", "02/06/2025");
                phieu.put("tongTien", 3750000);
                db.insertOrThrow(TABLE_PHIEU, null, phieu);
                Log.d(TAG, "Đã thêm phiếu mẫu");
            }
            cursor.close();

            db.setTransactionSuccessful();
            Log.d(TAG, "Đã thêm dữ liệu mẫu thành công");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi thêm dữ liệu mẫu: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public double getTongGiaTriGiaoDichTheoThang(String thangNam) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT SUM(" + COLUMN_TONG_TIEN + ") FROM " + TABLE_PHIEU +
                " WHERE " + COLUMN_NGAY + " LIKE ?";
        try (Cursor cursor = db.rawQuery(sql, new String[]{"%/" + thangNam})) {
            double tong = 0;
            if (cursor.moveToFirst()) {
                tong = cursor.isNull(0) ? 0 : cursor.getDouble(0);
            }
            Log.d(TAG, "Tổng giá trị giao dịch tháng " + thangNam + ": " + tong);
            return tong;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tính tổng giá trị giao dịch: " + e.getMessage());
            return 0;
        }
    }

    public int getSoLuongPhieuTheoLoai(String loai, String thangNam) {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d(TAG, "Querying for loai: " + loai + ", thangNam: " + thangNam);
        String sql = "SELECT COUNT(*) FROM " + TABLE_PHIEU +
                " WHERE LOWER(" + COLUMN_LOAI + ") = ? AND " + COLUMN_NGAY + " LIKE ?";
        try (Cursor cursor = db.rawQuery(sql, new String[]{loai.toLowerCase(), "%/" + thangNam})) {
            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            Log.d(TAG, "Số lượng phiếu " + loai + " tháng " + thangNam + ": " + count);
            return count;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi đếm phiếu theo loại: " + e.getMessage());
            return 0;
        }
    }
    public int getTongSoLuongTon() {
        int tong = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(soLuong) FROM sanpham", null);
        if (cursor.moveToFirst()) {
            tong = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return tong;
    }

    public void resetTonKho() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("soLuong", 0);
            int rows = db.update("sanpham", values, null, null);
            Log.d(TAG, "Đã reset số lượng tồn kho, ảnh hưởng đến " + rows + " bản ghi");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi reset số lượng tồn kho: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void resetAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Reset số lượng tồn kho trong sanpham
            ContentValues values = new ContentValues();
            values.put("soLuong", 0);
            int sanPhamRows = db.update("sanpham", values, null, null);
            Log.d(TAG, "Đã reset số lượng tồn kho, ảnh hưởng đến " + sanPhamRows + " bản ghi");

            // Xóa tất cả bản ghi trong ChiTietPhieu
            int chiTietRows = db.delete(TABLE_CHI_TIET, null, null);
            // Xóa tất cả bản ghi trong Phieu
            int phieuRows = db.delete(TABLE_PHIEU, null, null);
            Log.d(TAG, "Đã xóa " + chiTietRows + " bản ghi trong ChiTietPhieu và " + phieuRows + " bản ghi trong Phieu");

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi reset toàn bộ dữ liệu: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void logPhieuData() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT maPhieu, loai, ngay, tongTien FROM " + TABLE_PHIEU, null);
        Log.d(TAG, "Phieu table data:");
        while (cursor.moveToNext()) {
            String maPhieu = cursor.getString(0);
            String loai = cursor.getString(1);
            String ngay = cursor.getString(2);
            double tongTien = cursor.getDouble(3);
            Log.d(TAG, maPhieu + " - " + loai + " - " + ngay + " - " + tongTien);
        }
        cursor.close();
        db.close();
    }
    public void logSanPhamData() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT maHang, tenHang, soLuong FROM sanpham", null);
        Log.d(TAG, "SanPham table data:");
        while (cursor.moveToNext()) {
            String maHang = cursor.getString(0);
            String tenHang = cursor.getString(1);
            int soLuong = cursor.getInt(2);
            Log.d(TAG, maHang + " - " + tenHang + " - " + soLuong);
        }
        cursor.close();
        db.close();
    }




    private String chuanHoaNgay(String ngayGoc) {
        try {
            if (ngayGoc != null && ngayGoc.contains("-")) {
                SimpleDateFormat input = new SimpleDateFormat("dd-MM-yyyy");
                Date date = input.parse(ngayGoc);
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy");
                return output.format(date);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Lỗi khi chuẩn hóa ngày: " + e.getMessage());
            e.printStackTrace();
        }
        return ngayGoc != null ? ngayGoc : "";
    }

    public int getPhieuId(String maPhieu) {
        SQLiteDatabase db = this.getReadableDatabase();
        int id = -1;
        try (Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + TABLE_PHIEU +
                " WHERE " + COLUMN_MA_PHIEU + " = ?", new String[]{maPhieu})) {
            if (cursor.moveToFirst()) {
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lấy ID phiếu: " + e.getMessage());
            e.printStackTrace();
        }
        return id;
    }
}