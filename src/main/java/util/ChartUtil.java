package util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ChartUtil {

    // ================= SINGLE LINE =================
    public static void saveEnergyChart(List<Double> energy, String fileName) {
        try {
            if (energy == null || energy.isEmpty()) {
                System.out.println("⚠ Energy rong: " + fileName);
                return;
            }

            XYSeries series = new XYSeries("Energy");

            for (int i = 0; i < energy.size(); i++) {
                series.add(i + 1, energy.get(i));
            }

            XYSeriesCollection dataset = new XYSeriesCollection(series);

            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Energy over Rounds",
                    "Round",
                    "Energy",
                    dataset
            );

            XYPlot plot = chart.getXYPlot();
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

            renderer.setSeriesStroke(0, new BasicStroke(2.0f));
            renderer.setSeriesShapesVisible(0, true); //có chấm

            plot.setRenderer(renderer);

            ChartUtils.saveChartAsPNG(new File(fileName), chart, 800, 600);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= MULTI LINE =================
    public static void saveMultiEnergyChart(
            Map<String, List<Double>> data,
            String fileName) {

        if (data == null || data.isEmpty()) {
            System.out.println("⚠ Khong co data de ve: " + fileName);
            return;
        }

        try {
            XYSeriesCollection dataset = new XYSeriesCollection();

            int seriesIndex = 0;

            for (Map.Entry<String, List<Double>> entry : data.entrySet()) {

                String name = entry.getKey();
                List<Double> energy = entry.getValue();

                if (energy == null || energy.isEmpty()) {
                    System.out.println("⚠ Bo qua series rong: " + name);
                    continue;
                }

                System.out.println("Draw: " + name + " | size=" + energy.size());

                XYSeries series = new XYSeries(name);

                for (int i = 0; i < energy.size(); i++) {

                    double y = energy.get(i);

                    // FIX TRÙNG LINE (LEACH = DEAL)
                    // thêm nhiễu cực nhỏ để không bị đè line
                    y += (seriesIndex * 1e-6);

                    series.add(i + 1, y);
                }

                dataset.addSeries(series);
                seriesIndex++;
            }

            if (dataset.getSeriesCount() == 0) {
                System.out.println("⚠ Tat ca series rong: " + fileName);
                return;
            }

            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Energy Comparison",
                    "Round",
                    "Energy",
                    dataset
            );

            XYPlot plot = chart.getXYPlot();
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

            for (int i = 0; i < dataset.getSeriesCount(); i++) {

                renderer.setSeriesStroke(i, new BasicStroke(2.5f));

                // 👇 HIỆN MARKER để thấy line trùng
                renderer.setSeriesShapesVisible(i, true);

                // màu giống paper
                switch (i % 6) {
                    case 0 -> renderer.setSeriesPaint(i, Color.RED);
                    case 1 -> renderer.setSeriesPaint(i, Color.BLUE);
                    case 2 -> renderer.setSeriesPaint(i, Color.GREEN);
                    case 3 -> renderer.setSeriesPaint(i, Color.ORANGE);
                    case 4 -> renderer.setSeriesPaint(i, Color.MAGENTA);
                    case 5 -> renderer.setSeriesPaint(i, Color.BLACK);
                }
            }

            plot.setRenderer(renderer);

            ChartUtils.saveChartAsPNG(new File(fileName), chart, 1000, 700);

            System.out.println("✔ Ve xong: " + fileName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}