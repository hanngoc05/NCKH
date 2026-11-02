# WSN_node1 - Final upgraded version
# Báo cáo Đề tài Nghiên cứu Khoa học

📁 **Tài liệu & Báo cáo chi tiết (Google Drive):**
[Xem toàn bộ tài liệu và dữ liệu nghiên cứu tại đây](https://drive.google.com/drive/folders/1ey-nQ4-dbGkrEok7GZuUttDQNfEHVITU)
## Những gì đã nâng cấp
- Fix lỗi xuất Excel bằng Maven dependency đầy đủ.
- Fix `round` trong mô phỏng để LEACH/DEAL/SFCH dùng đúng vòng lặp.
- Fix `option 3` để đọc file có tọa độ node thật.
- Thêm `seed` để kết quả tái lập được.
- Thêm chạy lặp nhiều lần cho mỗi tổ hợp thuật toán và lấy trung bình + độ lệch chuẩn.
- Excel có 2 sheet: `Summary` và `PerRun`.
- Ghi log sạch hơn vào `resultWSN.txt`.
- Chỉ vẽ chart ở lần chạy đầu mỗi tổ hợp để tránh quá nhiều ảnh.
- Cải thiện routing để GA/DE/PSO tạo khác biệt rõ hơn thay vì gần như giống nhau.

## Yêu cầu
- Java JDK 17
- Maven 3.9+

Kiểm tra:
```powershell
java -version
mvn -version
```

## Cách chạy
Mở terminal tại thư mục project:
```powershell
mvn clean compile
mvn exec:java
```

### Cách dùng nhanh
- Chọn `2` để sinh dữ liệu ngẫu nhiên.
- Nhập số lần lặp, seed, có vẽ chart hay không.
- Sau khi chạy xong xem:
  - `ketqua_*.csv`
  - `resultWSN.txt`
  - thư mục `charts/`

### Chạy bằng file
Chọn `3`, nhập ví dụ:
```text
data\5node_coords.txt
```

Format file:
- 10 dòng đầu là config
- tiếp theo là `N` dòng tọa độ `x y`

Ví dụ:
```text
100
100
5
50
150
0.2
4000
5.0E-8
1.0E-11
1.3E-15
10 10
20 30
40 50
70 80
90 20
```

## Test nhanh
### Test 1: smoke test random
```powershell
mvn clean compile
mvn exec:java
```
Gõ:
```text
2
1
42
n
```
Kỳ vọng:
- chạy đủ 9 tổ hợp
- có `ketqua_Random.csv`
- có `resultWSN.txt`

### Test 2: file input có tọa độ thật
```powershell
mvn exec:java
```
Gõ:
```text
3
data\5node_coords.txt
1
42
n
```
Kỳ vọng:
- không fallback random
- có `ketqua_data_5node_coords.txt.csv`

### Test 3: có chart
```text
2
1
42
y
20
```
Kỳ vọng:
- ảnh PNG xuất trong `charts/`

## Các file sửa chính
- `src/main/java/Main.java`
- `src/main/java/config/SimulationConfig.java`
- `src/main/java/network/Node.java`
- `src/main/java/network/Network.java`
- `src/main/java/util/DataGenerator.java`
- `src/main/java/util/RandomManager.java`
- `src/main/java/util/ExcelWriter.java`
- `src/main/java/util/Logger.java`
- `src/main/java/simulation/SimulationController.java`
- `src/main/java/simulation/Statistics.java`
- `src/main/java/clustering/LEACHClustering.java`
- `src/main/java/clustering/DEALClustering.java`
- `src/main/java/clustering/SFCHClustering.java`
- `src/main/java/routing/Route.java`
- `src/main/java/routing/FitnessFunction.java`
- `src/main/java/routing/GARouting.java`
- `src/main/java/routing/DERouting.java`
- `src/main/java/routing/PSORouting.java`
- `pom.xml`
- `data/5node_coords.txt`

