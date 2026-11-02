package util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.geom.Ellipse2D;
import java.io.File;
import network.*;
import java.util.ArrayList;
import java.util.List;

public class Visualizer extends JFrame {
    public Visualizer(Network net, String title) {
        super(title);
        XYSeriesCollection dataset = createDataset(net);
        JFreeChart chart = createChart(dataset, title);
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new java.awt.Dimension(800, 800));
        setContentPane(panel);
        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private static XYSeriesCollection createDataset(Network net) {
        // Khởi tạo dataset ngay đầu hàm
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        // Khai báo các series dữ liệu
        XYSeries nodeToCHLinks = new XYSeries("Node-CH Links");
        XYSeries chToSinkLinks = new XYSeries("CH-Sink Links");
        XYSeries nodesSeries = new XYSeries("Nodes");
        XYSeries chSeries = new XYSeries("Cluster Heads");
        XYSeries sinkSeries = new XYSeries("Sink");

        // Kiểm tra an toàn: Nếu mạng rỗng vẫn phải return dataset (rỗng)
        if (net == null || net.nodes == null) {
            return dataset;
        }

        // 1. Vẽ Nodes và Cluster Heads
        for (Node n : net.nodes) {
            if (n == null) continue;
            try {
                double dx = Double.parseDouble(String.valueOf(n.x));
                double dy = Double.parseDouble(String.valueOf(n.y));

                if (n.isCH) {
                    chSeries.add(dx, dy);
                } else {
                    nodesSeries.add(dx, dy);
                    
                    // Vẽ mạng nhện xám (Node -> CH gần nhất)
                    Node nearestCH = null;
                    double minDist = Double.MAX_VALUE;
                    for (Node other : net.nodes) {
                        if (other != null && other.isCH) {
                            double d = Math.sqrt(Math.pow(dx - other.x, 2) + Math.pow(dy - other.y, 2));
                            if (d < minDist) {
                                minDist = d;
                                nearestCH = other;
                            }
                        }
                    }
                    if (nearestCH != null) {
                        nodeToCHLinks.add(dx, dy);
                        nodeToCHLinks.add(nearestCH.x, nearestCH.y);
                        nodeToCHLinks.add(null, null);
                    }
                }
            } catch (Exception e) {
                // Bỏ qua điểm lỗi tọa độ
            }
        }

        // 2. Vẽ đường nối đa chặng CH -> CH -> Sink (Dây xanh nối tiếp)
        for (Node n : net.nodes) {
            if (n != null && n.isCH) {
                try {
                    // Trường hợp 1: Có lộ trình đa chặng (Multi-hop)
                    if (n.route != null && n.route.path != null && n.route.path.size() > 1) {
                        List<Node> path = n.route.path;
                        for (int i = 0; i < path.size() - 1; i++) {
                            Node u = path.get(i);
                            Node v = path.get(i + 1);
                            chToSinkLinks.add(Double.parseDouble(String.valueOf(u.x)), Double.parseDouble(String.valueOf(u.y)));
                            chToSinkLinks.add(Double.parseDouble(String.valueOf(v.x)), Double.parseDouble(String.valueOf(v.y)));
                            chToSinkLinks.add(null, null);
                        }
                    } 
                    // Trường hợp 2: Bảo hiểm (Nếu route null, vẽ thẳng tới Sink)
                    else {
                        chToSinkLinks.add(Double.parseDouble(String.valueOf(n.x)), Double.parseDouble(String.valueOf(n.y)));
                        chToSinkLinks.add(Double.parseDouble(String.valueOf(net.sink.x)), Double.parseDouble(String.valueOf(net.sink.y)));
                        chToSinkLinks.add(null, null);
                    }
                } catch (Exception e) {}
            }
        }

        // 3. Vẽ Sink (Nút đỏ)
        if (net.sink != null) {
            try {
                sinkSeries.add(Double.parseDouble(String.valueOf(net.sink.x)), 
                               Double.parseDouble(String.valueOf(net.sink.y)));
            } catch (Exception e) {}
        }

        // THÊM TẤT CẢ VÀO DATASET TRƯỚC KHI RETURN
        dataset.addSeries(nodeToCHLinks);
        dataset.addSeries(chToSinkLinks);
        dataset.addSeries(nodesSeries);
        dataset.addSeries(chSeries);
        dataset.addSeries(sinkSeries);
        
        // LUÔN LUÔN CÓ DÒNG NÀY Ở CUỐI CÙNG
        return dataset;
    }
    private static JFreeChart createChart(XYSeriesCollection dataset, String title) {
        JFreeChart chart = ChartFactory.createScatterPlot(title, "X", "Y", dataset, PlotOrientation.VERTICAL, true, true, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        XYLineAndShapeRenderer r = new XYLineAndShapeRenderer();
        
        r.setSeriesLinesVisible(0, true); r.setSeriesShapesVisible(0, false); r.setSeriesPaint(0, new Color(230, 230, 230));
        r.setSeriesLinesVisible(1, true); r.setSeriesShapesVisible(1, false); r.setSeriesPaint(1, Color.BLUE); r.setSeriesStroke(1, new BasicStroke(1.2f));
        r.setSeriesLinesVisible(2, false); r.setSeriesShapesVisible(2, true); r.setSeriesPaint(2, Color.GRAY); r.setSeriesShape(2, new Ellipse2D.Double(-1.5,-1.5,3,3));
        r.setSeriesLinesVisible(3, false); r.setSeriesShapesVisible(3, true); r.setSeriesPaint(3, Color.BLUE); r.setSeriesShape(3, new Ellipse2D.Double(-4,-4,8,8));
        r.setSeriesLinesVisible(4, false); r.setSeriesShapesVisible(4, true); r.setSeriesPaint(4, Color.RED); r.setSeriesShape(4, new java.awt.Rectangle(-6,-6,12,12));
        
        plot.setRenderer(r);
        return chart;
    }

    public static void saveChart(Network net, String fileName) {
        try {
            File dir = new File("charts");
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (created) System.out.println("--- Da tao moi thu muc charts ---");
            }
            
            File outFile = new File(dir, fileName + ".png");
            ChartUtils.saveChartAsPNG(outFile, createChart(createDataset(net), fileName), 800, 800);
            
        } catch (Exception e) {
            System.err.println("Loi ghi file: " + e.getMessage());
        }
    }
}