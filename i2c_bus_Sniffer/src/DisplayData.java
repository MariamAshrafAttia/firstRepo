/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/***************************************
 * I2C Sniffer Application
 * Description:
 * author: MariamAshraf
 ***************************************/

import com.fazecast.jSerialComm.SerialPort; // For serial communication
import javax.swing.*; // For GUI components
import java.awt.*; // For layout and event handling
import java.awt.event.ActionEvent; // For handling actions
import java.awt.event.ActionListener;
import java.util.ArrayList; // For managing a list of available COM ports
public class DisplayData extends javax.swing.JFrame {
    private static volatile boolean stopReading = false;
    private static SerialPort comPort = null;
    private static String lastBinaryData = ""; // Instance variable for binary data
    
    /**
     * Creates new form DisplayData
     */
   
    private LogicAnalyzerGUI logicAnalyzer;
    public DisplayData() {
        initComponents();
        logicAnalyzer = new LogicAnalyzerGUI();
        jPanel2.setBackground(Color.LIGHT_GRAY); // Set color
        // Initially hide jPanel2
        jPanel2.setVisible(false);
        // Add logicAnalyzer to jPanel2
        jPanel2.setLayout(new BorderLayout()); // Ensure jPanel2 has a layout
        jPanel2.add(logicAnalyzer, BorderLayout.CENTER); // Add logicAnalyzer to jPanel2
        
        setupActions();
    }
    
    
   // Method to set up custom actions and logic
    private void setupActions() {
        // Hide labels initially
        setLabelsVisible(false);
        
         // Action listener for jComboBox1
        jComboBox1.addActionListener(e -> {
            stopReading = true; // Stop any previous reading thread

            // Close the existing port if it's open
            if (comPort != null && comPort.isOpen()) {
                comPort.closePort();
                updateConnectionStatus(false); // Update status to disconnected
            }

            // Start new thread for the selected port
            new Thread(() -> {
                try {
                    // Wait for 100ms
                    Thread.sleep(100);

                    // Get the selected port name from the JComboBox
                    String selectedPortName = (String) jComboBox1.getSelectedItem();
                    
                    // Get the selected baud rate from jComboBox2
                String selectedBaudRateStr = (String) jComboBox2.getSelectedItem();
                int selectedBaudRate = Integer.parseInt(selectedBaudRateStr); // Parse to integer
                    
                    
                    // Configure and open the serial port
                    comPort = SerialPort.getCommPort(selectedPortName);
                    comPort.setBaudRate(selectedBaudRate);
                    comPort.openPort();

                    // Ensure the serial port is open
                    if (comPort.isOpen()) 
                    {
                        System.out.println("Serial port is open on " + selectedPortName);
                        SwingUtilities.invokeLater(() -> {
                        jPanel2.setVisible(true); // Show jPanel2
                        jPanel2.setBackground(Color.LIGHT_GRAY); // Set background color when visible
                        // Ensure labels are opaque before setting background color
                        jLabel2.setOpaque(true);
                        jLabel3.setOpaque(true);
                        jLabel4.setOpaque(true);
                        jLabel2.setBackground(Color.LIGHT_GRAY); // Set background color for jLabel2
                        jLabel3.setBackground(Color.LIGHT_GRAY); // Set background color for jLabel3
                        jLabel4.setBackground(Color.LIGHT_GRAY); // Set background color for jLabel4
                        updateConnectionStatus(true); // Update status to connected
                        setLabelsVisible(true); // Ensure labels are visible
                        });
                        
                    } 
                    else 
                    {
                        System.err.println("Failed to open serial port " + selectedPortName);
                        SwingUtilities.invokeLater(() -> {
                        updateConnectionStatus(false); // Update status to disconnected
                        JOptionPane.showMessageDialog(this, "Failed to open serial port " + selectedPortName, "Error", JOptionPane.ERROR_MESSAGE);
                        //jLabel2.setVisible(false); // Hide jLabel2
                        //jLabel3.setVisible(false); // Hide jLabel3
                        //jLabel4.setVisible(false); // Hide jLabel4
                        setLabelsVisible(false); // Hide labels if connection fails
                        });
                        return;
                    }

                    stopReading = false; // Allow the thread to start reading

                    // Buffer to accumulate data
                    StringBuilder buffer = new StringBuilder();
                    boolean recording = false;

                    // Read data from the serial port
                    while (!stopReading) {
                        try {
                            if (comPort.bytesAvailable() > 0) {
                                byte[] readBuffer = new byte[comPort.bytesAvailable()];
                                int numRead = comPort.readBytes(readBuffer, readBuffer.length);

                                // Process data based on delimiters
                                for (byte b : readBuffer) {
                                    char c = (char) b;
                                    if (c == '#') {
                                        if (recording) {
                                            // If recording, print the buffer and reset it
                                            String result = buffer.toString();
                                            SwingUtilities.invokeLater(() -> {
                                                jTextArea1.setText("Received: " + result);
                                                System.out.println("Received: " + result); // Print to console
                                            });

                                            // Convert buffer to binary string
                                            StringBuilder binaryData = new StringBuilder();
                                            for (char dataChar : result.toCharArray()) {
                                                binaryData.append(String.format("%8s", Integer.toBinaryString(dataChar & 0xFF)).replace(' ', '0')).append(" ");
                                            }

                                            // Check if binary data is different from last data
                                            String binaryString = binaryData.toString().trim();
                                            if (!binaryString.equals(lastBinaryData)) {
                                                lastBinaryData = binaryString;
                                                SwingUtilities.invokeLater(() -> {
                                                    jTextArea2.setText("Binary Data: " + lastBinaryData);
                                                    System.out.println("Binary Data: " + lastBinaryData); // Print to console
                                                    updateLogicAnalyzer(lastBinaryData); // Update logic analyzer
                                                });
                                            }

                                            buffer.setLength(0); // Clear the buffer
                                        }
                                        recording = !recording; // Toggle recording
                                    } else if (recording) {
                                        // Append character to the buffer only if recording
                                        buffer.append(c);
                                    }
                                }
                            }

                            // Sleep to avoid overwhelming the CPU
                            Thread.sleep(100);
                        } catch (Exception ex) {
                            ex.printStackTrace(); // Print any exceptions for debugging
                        }
                    }

                    // Close the port when finished
                    comPort.closePort();
                    System.out.println("Serial port closed on " + selectedPortName);
                    // Hide jPanel2 when port is closed
                // Hide jPanel2 when port is closed
                SwingUtilities.invokeLater(() -> {
                    jPanel2.setVisible(false);
                    jLabel3.setVisible(false); // Hide jLabel3
                    jLabel4.setVisible(false); // Hide jLabel4
                    jPanel2.setBackground(null); // Reset background color when hidden
                    jLabel3.setBackground(null); // Reset background color for jLabel3
                    jLabel4.setBackground(null); // Reset background color for jLabel4
                    updateConnectionStatus(false); // Update status to disconnected
                    setLabelsVisible(false); // Hide labels when disconnected
                });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });
    }
    
    
    
    
    private void updateConnectionStatus(boolean isConnected) {
    if (isConnected) {
        jLabel1.setText("Connected");
        jLabel1.setOpaque(true);
        jLabel1.setBackground(Color.GREEN);  // Optional: Change color to green for connected
    } else {
        jLabel1.setText("Disconnected");
        jLabel1.setOpaque(true);
        jLabel1.setBackground(Color.RED);// Optional: Change color to red for disconnected
    }
}

    
   /* 
    private void checkComboBoxes() 
    {
    // Check if both combo boxes have a selected item
    boolean bothSelected = jComboBox1.getSelectedIndex() != -1 && jComboBox2.getSelectedIndex() != -1;

    // Show labels only if both combo boxes have a selection
    setLabelsVisible(bothSelected);
    }
    */
    
    //to Toggle Visibility of high,low and data
    private void setLabelsVisible(boolean visible) {
    jLabel2.setVisible(visible);
    jLabel3.setVisible(visible);
    jLabel4.setVisible(visible);
}

    
    
    // Ensure logicAnalyzer is not null before using it
private void updateLogicAnalyzer(String binaryString) {
    
    
    if (logicAnalyzer != null) { // Null check
        String[] binaryFramesStr = binaryString.split(" ");
        int[] binaryFrames = new int[binaryFramesStr.length * 8]; // 8 bits per byte
        StringBuilder hexData = new StringBuilder(); // Initialize StringBuilder for hexadecimal data
        
        for (int i = 0; i < binaryFramesStr.length; i++) {
            String binaryByte = binaryFramesStr[i];
            for (int j = 0; j < binaryByte.length(); j++) {
                binaryFrames[i * 8 + j] = Character.getNumericValue(binaryByte.charAt(j));
            }
            // Convert each 8-bit binary string to hexadecimal
            int decimalValue = Integer.parseInt(binaryByte, 2);
            String hexValue = String.format("%02X", decimalValue); // Format as 2-digit hexadecimal
            hexData.append(hexValue).append(" "); // Append hex value to the hexData StringBuilder
        }
        logicAnalyzer.setDataFrames(binaryFrames);
        logicAnalyzer.repaint(); // Ensure the GUI is updated
        
        // Display the hex data in jTextArea3
        String hexString = hexData.toString().trim();
        SwingUtilities.invokeLater(() -> {
            jTextArea3.setText("Hex Data: " + hexString);
            System.out.println("Hex Data: " + hexString); // Print to console for debugging
        
            // Show the labels after the update is complete
            setLabelsVisible(true);
        });
        
        
        
        
        
    } else {
        System.err.println("LogicAnalyzerGUI is not initialized.");
    }
}
        
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "COM11", "COM10" }));
        jPanel1.add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 20, 73, -1));

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 230, 100));

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        jPanel1.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 50, 230, 100));

        jPanel2.setBackground(new java.awt.Color(204, 255, 255));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 950, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 186, Short.MAX_VALUE)
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 240, 950, -1));

        jTextArea3.setColumns(20);
        jTextArea3.setRows(5);
        jScrollPane3.setViewportView(jTextArea3);

        jPanel1.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 50, 230, 100));

        jLabel1.setText("Disconnect");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 20, 79, -1));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "4800", "9600", "19200", "38400", "57600" }));
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });
        jPanel1.add(jComboBox2, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 20, 61, -1));

        jLabel2.setText("Data");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 444, 37, -1));

        jLabel3.setText("Low");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 380, 37, -1));

        jLabel4.setBackground(new java.awt.Color(204, 204, 204));
        jLabel4.setText("High");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 330, 37, -1));

        jLabel5.setText("Status ");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 20, 43, -1));

        jLabel6.setText("COMPort");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 20, 53, -1));

        jLabel7.setText("BaudRate");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 20, 62, -1));

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/backgroundedited.jpg"))); // NOI18N
        jLabel8.setText("jLabel8");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1080, 640));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            java.awt.EventQueue.invokeLater(() -> new DisplayData().setVisible(true));

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    // End of variables declaration//GEN-END:variables
}
