import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ProcessMonitoringDashboard {
    private JFrame frame;
    private JLabel cpuUsageLabel;
    private JLabel memoryUsageLabel;
    private JTable processTable;
    private DefaultTableModel tableModel;

    public ProcessMonitoringDashboard() {
        // Create main frame
        frame = new JFrame("Real-Time Process Monitoring Dashboard");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Panel for CPU & Memory Usage
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        cpuUsageLabel = new JLabel("CPU Usage: Fetching...");
        memoryUsageLabel = new JLabel("Memory Usage: Fetching...");
        infoPanel.add(cpuUsageLabel);
        infoPanel.add(memoryUsageLabel);

        // Table for Process List
        tableModel = new DefaultTableModel();
        tableModel.addColumn("PID");
        tableModel.addColumn("Process Name");
        processTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(processTable);

        // Add components to frame
        frame.add(infoPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Start updating process and system info
        startMonitoring();

        // Show frame
        frame.setVisible(true);
    }

    private void startMonitoring() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateSystemUsage();
                updateProcessList();
            }
        }, 0, 2000); // Update every 2 seconds
    }

    private void updateSystemUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getSystemLoadAverage(); // Get CPU Load
        long freeMemory = Runtime.getRuntime().freeMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        long usedMemory = totalMemory - freeMemory;

        // Update labels
        SwingUtilities.invokeLater(() -> {
            cpuUsageLabel.setText(String.format("CPU Usage: %.2f%%", cpuLoad * 100));
            memoryUsageLabel.setText(String.format("Memory Usage: %.2f MB", usedMemory / (1024 * 1024)));
        });
    }

    private void updateProcessList() {
        List<String[]> processes = getProcessList();
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0); // Clear table
            for (String[] process : processes) {
                tableModel.addRow(process);
            }
        });
    }

    private List<String[]> getProcessList() {
        List<String[]> processList = new ArrayList<>();
        try {
            Process process;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                process = Runtime.getRuntime().exec("tasklist");
            } else {
                process = Runtime.getRuntime().exec("ps -e");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { // Skip headers
                    firstLine = false;
                    continue;
                }
                String[] parts = line.trim().split("\\s+");
                if (parts.length > 1) {
                    String pid = parts[0];
                    String processName = parts[parts.length - 1];
                    processList.add(new String[]{pid, processName});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processList;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ProcessMonitoringDashboard::new);
    }
}
