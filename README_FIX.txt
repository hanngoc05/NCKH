Chay ban da sua
================

1) Neu dung Maven:
   mvn clean compile
   mvn exec:java -Dexec.mainClass=Main

2) Neu chua co Maven, ban van co the chay nhu cu, nhung phan Excel co the van thieu jar neu classpath lib/ chua du.

Cac diem da sua:
- Fix option 3: doc file co toa do node that
- Fix round: moi vong network.incrementRound()
- Fix assignMembers: khong con phu thuoc constants null
- Them seed random mac dinh = 42 de tai lap ket qua
- Bao ve phan ve chart va ghi Excel: loi thi khong lam dung mo phong
