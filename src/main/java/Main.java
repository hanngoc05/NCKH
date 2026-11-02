import java.util.List;
import java.util.Scanner;

import simulation.SimulationController;
import util.ExcelWriter;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        SimulationController controller = new SimulationController(sc);

        System.out.println("=================================");
        System.out.println("   WSN SIMULATION SYSTEM");
        System.out.println("=================================");
        System.out.println("1. Nhap tham so tu ban phim");
        System.out.println("2. Sinh du lieu ngau nhien");
        System.out.println("3. Doc du lieu tu file");
        System.out.print("Lua chon cua ban: ");

        int choice;
        try {
            choice = sc.nextInt();
        } catch (Exception e) {
            System.out.println("Lua chon khong hop le!");
            sc.close();
            return;
        }

        try {
            switch (choice) {
                case 1 -> controller.loadFromKeyboard();
                case 2 -> controller.loadRandom();
                case 3 -> {
                    sc.nextLine();
                    System.out.print("Nhap duong dan file input: ");
                    String path = sc.nextLine();
                    controller.loadFromFile(path);
                }
                default -> {
                    System.out.println("Lua chon khong hop le!");
                    sc.close();
                    return;
                }
            }

            List<ExcelWriter.Result> results = controller.runAllAndGetResults();
            String fileName = "ketqua_" + controller.getDatasetName().replace("/", "_").replace("\\", "_") + ".csv";
            ExcelWriter.write(fileName, results, controller.getRunDetails());
            System.out.println("Hoan tat. Xem log tai resultWSN.txt va file Excel vua tao.");
        } catch (Exception e) {
            System.err.println("Loi khi chay mo phong!");
            e.printStackTrace();
        } finally {
            sc.close();
        }
    }
}
