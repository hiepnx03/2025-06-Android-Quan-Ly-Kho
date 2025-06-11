-- Bật chế độ Foreign Key
PRAGMA foreign_keys = ON;

-- Xóa bảng cũ nếu tồn tại để tránh lỗi
DROP TABLE IF EXISTS ChiTietPhieu;
DROP TABLE IF EXISTS Phieu;

-- Tạo bảng Phieu
CREATE TABLE Phieu (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    maPhieu TEXT NOT NULL,
    loai TEXT NOT NULL,
    ngay TEXT NOT NULL,
    tongTien REAL NOT NULL
);

-- Tạo bảng ChiTietPhieu với ràng buộc Foreign Key
CREATE TABLE ChiTietPhieu (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    phieu_id INTEGER,
    maHang TEXT NOT NULL,
    tenHang TEXT NOT NULL,
    soLuong INTEGER NOT NULL,
    donGia REAL NOT NULL,
    FOREIGN KEY (phieu_id) REFERENCES Phieu(id) ON DELETE CASCADE
);

-- Chèn dữ liệu mẫu vào bảng Phieu
INSERT INTO Phieu (maPhieu, loai, ngay, tongTien) VALUES
('P001', 'nhap', '01/06/2025', 500000.0),
('P002', 'xuat', '02/06/2025', 300000.0),
('P003', 'nhap', '03/06/2025', 750000.0);

-- Chèn dữ liệu mẫu vào bảng ChiTietPhieu
INSERT INTO ChiTietPhieu (phieu_id, maHang, tenHang, soLuong, donGia) VALUES
(1, 'MH001', 'Gạo', 10, 50000.0),
(1, 'MH002', 'Đường', 5, 20000.0),
(2, 'MH001', 'Gạo', 6, 50000.0),
(3, 'MH003', 'Muối', 15, 5000.0),
(3, 'MH004', 'Dầu ăn', 10, 70000.0);