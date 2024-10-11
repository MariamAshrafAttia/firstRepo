/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Mariam
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */

/*Main Method:
Sets up the JFrame for the GUI.
Creates a JComboBox for selecting COM ports.
Adds JTextArea components for displaying received string data and binary data.
Adds a custom LogicAnalyzerGUI component to the bottom of the frame.
Sets up the action listener for the JComboBox to handle port selection
*/

import com.fazecast.jSerialComm.SerialPort;//for serial communication
import javax.swing.*;//for GUI components
import java.awt.*;//for layout and event handling
import java.awt.event.ActionEvent;//for handling actions
import java.awt.event.ActionListener;
import java.util.ArrayList;//for managing a list of available COM ports

public class DisplaySerialData {
    private static volatile boolean stopReading = false;
    private static SerialPort comPort = null;
    private static String lastBinaryData = ""; // Instance variable for binary data

    public static void main(String[] args) {
        // Create and set up the JFrame
        JFrame frame = new JFrame("Data Display");
        frame.setSize(800, 400); // Increased size for better visibility
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create a JComboBox to list available COM ports
        ArrayList<String> portNames = new ArrayList<>();
        
        // Add all available ports
        SerialPort[] portList = SerialPort.getCommPorts();
        for (SerialPort port : portList) {
            portNames.add(port.getSystemPortName());
        }

        // Add COM10 if it's not already in the list
        if (!portNames.contains("COM10")) {
            portNames.add("COM10");
        }

        JComboBox<String> portSelector = new JComboBox<>(portNames.toArray(new String[0]));
        frame.add(portSelector, BorderLayout.NORTH);

        // Create a JTextArea to display data in string format
        JTextArea textArea = new JTextArea("Waiting for data...", 10, 50);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Create a JTextArea to display binary data
        JTextArea binaryArea = new JTextArea("Binary Data...", 10, 50);
        binaryArea.setEditable(false);
        JScrollPane binaryScrollPane = new JScrollPane(binaryArea);
        frame.add(binaryScrollPane, BorderLayout.EAST);

        // Create an instance of LogicAnalyzerGUI
        LogicAnalyzerGUI logicAnalyzer = new LogicAnalyzerGUI();
        frame.add(logicAnalyzer, BorderLayout.SOUTH);

        // Make the frame visible
        frame.setVisible(true);

        // Add ActionListener to start communication when a port is selected
        portSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopReading = true; // Stop any previous reading thread
                
                // Close the existing port if it's open
                if (comPort != null && comPort.isOpen()) {
                    comPort.closePort();
                }
                
                // Start new thread for the selected port
                new Thread(() -> {
                    try {
                        // Wait for 100ms
                        Thread.sleep(100);

                        // Get the selected port name from the JComboBox
                        String selectedPortName = (String) portSelector.getSelectedItem();

                        // Configure and open the serial port
                        comPort = SerialPort.getCommPort(selectedPortName);
                        comPort.setBaudRate(9600);
                        comPort.openPort();

                        // Ensure the serial port is open
                        if (comPort.isOpen()) {
                            System.out.println("Serial port is open on " + selectedPortName);
                        } else {
                            System.err.println("Failed to open serial port " + selectedPortName);
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
                                                    textArea.setText("Received: " + result);
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
                                                        binaryArea.setText("Binary Data: " + lastBinaryData);
                                                        System.out.println("Binary Data: " + lastBinaryData); // Print to console
                                                        // Update the LogicAnalyzerGUI with the binary data
                                                        String[] binaryFramesStr = binaryString.split(" ");
                                                        int[] binaryFrames = new int[binaryFramesStr.length * 8]; // 8 bits per byte
                                                        for (int i = 0; i < binaryFramesStr.length; i++) {
                                                            String binaryByte = binaryFramesStr[i];
                                                            for (int j = 0; j < binaryByte.length(); j++) {
                                                                binaryFrames[i * 8 + j] = Character.getNumericValue(binaryByte.charAt(j));
                                                            }
                                                        }
                                                        logicAnalyzer.setDataFrames(binaryFrames);
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

                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
    }
}



/*
import com.fazecast.jSerialComm.SerialPort;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class DisplaySerialData {
    private static volatile boolean stopReading = false;
    private static SerialPort comPort = null;
    private static String lastBinaryData = ""; // Instance variable for binary data

    public static void main(String[] args) {
        // Create and set up the JFrame
        JFrame frame = new JFrame("Data Display");
        frame.setSize(600, 400); // Increased size for better visibility
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create a JPanel with BoxLayout to stack textArea and binaryArea vertically
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));

        // Create a JComboBox to list available COM ports
        ArrayList<String> portNames = new ArrayList<>();
        
        // Add all available ports
        SerialPort[] portList = SerialPort.getCommPorts();
        for (SerialPort port : portList) {
            portNames.add(port.getSystemPortName());
        }

        // Add COM10 if it's not already in the list
        if (!portNames.contains("COM10")) {
            portNames.add("COM10");
        }

        JComboBox<String> portSelector = new JComboBox<>(portNames.toArray(new String[0]));
        frame.add(portSelector, BorderLayout.NORTH);

        // Create a JTextArea to display data in string format
        JTextArea textArea = new JTextArea("Waiting for data...", 10, 50);
        textArea.setEditable(false);
        JScrollPane textScrollPane = new JScrollPane(textArea);
        dataPanel.add(textScrollPane);

        // Create a JTextArea to display binary data
        JTextArea binaryArea = new JTextArea("Binary Data...", 10, 50);
        binaryArea.setEditable(false);
        JScrollPane binaryScrollPane = new JScrollPane(binaryArea);
        dataPanel.add(binaryScrollPane);

        // Add dataPanel to the frame
        frame.add(dataPanel, BorderLayout.CENTER);

        // Make the frame visible
        frame.setVisible(true);

        // Add ActionListener to start communication when a port is selected
        portSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopReading = true; // Stop any previous reading thread
                
                // Close the existing port if it's open
                if (comPort != null && comPort.isOpen()) {
                    comPort.closePort();
                }
                
                // Start new thread for the selected port
                new Thread(() -> {
                    try {
                        // Wait for 100ms
                        Thread.sleep(100);

                        // Get the selected port name from the JComboBox
                        String selectedPortName = (String) portSelector.getSelectedItem();

                        // Configure and open the serial port
                        comPort = SerialPort.getCommPort(selectedPortName);
                        comPort.setBaudRate(9600);
                        comPort.openPort();

                        // Ensure the serial port is open
                        if (comPort.isOpen()) {
                            System.out.println("Serial port is open on " + selectedPortName);
                        } else {
                            System.err.println("Failed to open serial port " + selectedPortName);
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
                                                    textArea.setText("Received: " + result);
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
                                                        binaryArea.setText("Binary Data: " + lastBinaryData);
                                                        System.out.println("Binary Data: " + lastBinaryData); // Print to console
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

                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
    }
}*/





/*
import com.fazecast.jSerialComm.SerialPort;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class DisplaySerialData {
    private static volatile boolean stopReading = false;
    private static SerialPort comPort = null;
    private static LogicAnalyzerGUI logicAnalyzer = new LogicAnalyzerGUI(); // Reference to the GUI

    public static void main(String[] args) {
        // Create and set up the JFrame
        JFrame frame = new JFrame("Data Display");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new java.awt.BorderLayout());

        // Create a JComboBox to list available COM ports
        ArrayList<String> portNames = new ArrayList<>();
        
        // Add all available ports
        SerialPort[] portList = SerialPort.getCommPorts();
        for (SerialPort port : portList) {
            portNames.add(port.getSystemPortName());
        }

        // Add COM10 if it's not already in the list
        if (!portNames.contains("COM10")) {
            portNames.add("COM10");
        }

        JComboBox<String> portSelector = new JComboBox<>(portNames.toArray(new String[0]));
        frame.add(portSelector, java.awt.BorderLayout.NORTH);

        // Add LogicAnalyzerGUI to the frame
        frame.add(logicAnalyzer, java.awt.BorderLayout.CENTER);

        // Make the frame visible
        frame.setVisible(true);

        // Add ActionListener to start communication when a port is selected
        portSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopReading = true; // Stop any previous reading thread
                
                // Close the existing port if it's open
                if (comPort != null && comPort.isOpen()) {
                    comPort.closePort();
                }
                
                // Start new thread for the selected port
                new Thread(() -> {
                    try {
                        // Wait for 100ms
                        Thread.sleep(100);

                        // Get the selected port name from the JComboBox
                        String selectedPortName = (String) portSelector.getSelectedItem();

                        // Configure and open the serial port
                        comPort = SerialPort.getCommPort(selectedPortName);
                        comPort.setBaudRate(9600);
                        comPort.openPort();

                        // Ensure the serial port is open
                        if (comPort.isOpen()) {
                            System.out.println("Serial port is open on " + selectedPortName);
                        } else {
                            System.err.println("Failed to open serial port " + selectedPortName);
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
                                    
                                    // Convert received bytes to binary and then to int array
                                    StringBuilder binaryData = new StringBuilder();
                                    for (byte b : readBuffer) {
                                        String binaryString = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
                                        binaryData.append(binaryString);
                                    }
                                    
                                    // Convert binary string to int array
                                    int[] binaryArray = binaryData.toString().chars().map(c -> c - '0').toArray();

                                    // Update the GUI with binary data
                                    SwingUtilities.invokeLater(() -> logicAnalyzer.setDataFrames(binaryArray));

                                    // Optionally, print the binary data for debugging
                                    System.out.println("Received binary data: " + binaryData.toString());
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

                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
    }
}
*/

/*
import com.fazecast.jSerialComm.SerialPort;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class DisplaySerialData {
    private static volatile boolean stopReading = false;
    private static SerialPort comPort = null;
    private static String lastBinaryData = ""; // Instance variable for binary data

    public static void main(String[] args) {
        // Create and set up the JFrame
        JFrame frame = new JFrame("Data Display");
        frame.setSize(600, 400); // Increased size for better visibility
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create a JComboBox to list available COM ports
        ArrayList<String> portNames = new ArrayList<>();
        
        // Add all available ports
        SerialPort[] portList = SerialPort.getCommPorts();
        for (SerialPort port : portList) {
            portNames.add(port.getSystemPortName());
        }

        // Add COM10 if it's not already in the list
        if (!portNames.contains("COM10")) {
            portNames.add("COM10");
        }

        JComboBox<String> portSelector = new JComboBox<>(portNames.toArray(new String[0]));
        frame.add(portSelector, BorderLayout.NORTH);

        // Create a JTextArea to display data in string format
        JTextArea textArea = new JTextArea("Waiting for data...", 10, 50);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Create a JTextArea to display binary data
        JTextArea binaryArea = new JTextArea("Binary Data...", 10, 50);
        binaryArea.setEditable(false);
        JScrollPane binaryScrollPane = new JScrollPane(binaryArea);
        frame.add(binaryScrollPane, BorderLayout.EAST);

        // Make the frame visible
        frame.setVisible(true);

        // Add ActionListener to start communication when a port is selected
        portSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopReading = true; // Stop any previous reading thread
                
                // Close the existing port if it's open
                if (comPort != null && comPort.isOpen()) {
                    comPort.closePort();
                }
                
                // Start new thread for the selected port
                new Thread(() -> {
                    try {
                        // Wait for 100ms
                        Thread.sleep(100);

                        // Get the selected port name from the JComboBox
                        String selectedPortName = (String) portSelector.getSelectedItem();

                        // Configure and open the serial port
                        comPort = SerialPort.getCommPort(selectedPortName);
                        comPort.setBaudRate(9600);
                        comPort.openPort();

                        // Ensure the serial port is open
                        if (comPort.isOpen()) {
                            System.out.println("Serial port is open on " + selectedPortName);
                        } else {
                            System.err.println("Failed to open serial port " + selectedPortName);
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
                                                    textArea.setText("Received: " + result);
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
                                                        binaryArea.setText("Binary Data: " + lastBinaryData);
                                                        System.out.println("Binary Data: " + lastBinaryData); // Print to console
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

                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
    }
}//correctly run no.2 not for all
*/

/*
import com.fazecast.jSerialComm.SerialPort;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class DisplaySerialData {
    private static volatile boolean stopReading = false;
    private static SerialPort comPort = null;

    public static void main(String[] args) {
        // Create and set up the JFrame
        JFrame frame = new JFrame("Data Display");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new java.awt.BorderLayout());

        // Create a JComboBox to list available COM ports
        ArrayList<String> portNames = new ArrayList<>();
        
        // Add all available ports
        SerialPort[] portList = SerialPort.getCommPorts();
        for (SerialPort port : portList) {
            portNames.add(port.getSystemPortName());
        }

        // Add COM10 if it's not already in the list
        if (!portNames.contains("COM10")) {
            portNames.add("COM10");
        }

        JComboBox<String> portSelector = new JComboBox<>(portNames.toArray(new String[0]));
        frame.add(portSelector, java.awt.BorderLayout.NORTH);

        // Create a JLabel to display data
        JLabel label = new JLabel("Waiting for data...", SwingConstants.CENTER);
        frame.add(label, java.awt.BorderLayout.CENTER);

        // Make the frame visible
        frame.setVisible(true);

        // Add ActionListener to start communication when a port is selected
        portSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopReading = true; // Stop any previous reading thread
                
                // Close the existing port if it's open
                if (comPort != null && comPort.isOpen()) {
                    comPort.closePort();
                }
                
                // Start new thread for the selected port
                new Thread(() -> {
                    try {
                        // Wait for 100ms
                        Thread.sleep(100);

                        // Get the selected port name from the JComboBox
                        String selectedPortName = (String) portSelector.getSelectedItem();

                        // Configure and open the serial port
                        comPort = SerialPort.getCommPort(selectedPortName);
                        comPort.setBaudRate(9600);
                        comPort.openPort();

                        // Ensure the serial port is open
                        if (comPort.isOpen()) {
                            System.out.println("Serial port is open on " + selectedPortName);
                        } else {
                            System.err.println("Failed to open serial port " + selectedPortName);
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
                                    String data = new String(readBuffer, 0, numRead);

                                    for (char c : data.toCharArray()) {
                                        if (c == '#') {
                                            if (recording) {
                                                // If recording, print the buffer and reset it
                                                String result = buffer.toString();
                                                System.out.println("Received: " + result);
                                                SwingUtilities.invokeLater(() -> label.setText("Received: " + result));
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

                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
    }
}//run correctly
*/

/*
import com.fazecast.jSerialComm.SerialPort;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class DisplaySerialData {
    private static volatile boolean stopReading = false;

    public static void main(String[] args) {
        // Create and set up the JFrame
        JFrame frame = new JFrame("Data Display");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new java.awt.BorderLayout());

        // Create a JComboBox to list available COM ports
        ArrayList<String> portNames = new ArrayList<>();
        
        // Add all available ports
        SerialPort[] portList = SerialPort.getCommPorts();
        for (SerialPort port : portList) {
            portNames.add(port.getSystemPortName());
        }

        // Add COM10 if it's not already in the list
        if (!portNames.contains("COM10")) {
            portNames.add("COM10");
        }

        JComboBox<String> portSelector = new JComboBox<>(portNames.toArray(new String[0]));
        frame.add(portSelector, java.awt.BorderLayout.NORTH);

        // Create a JLabel to display data
        JLabel label = new JLabel("Waiting for data...", SwingConstants.CENTER);
        frame.add(label, java.awt.BorderLayout.CENTER);

        // Make the frame visible
        frame.setVisible(true);

        // Add ActionListener to start communication when a port is selected
        portSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopReading = true; // Stop any previous reading thread
                new Thread(() -> {
                    try {
                        // Wait for 100ms
                        Thread.sleep(100);

                        // Get the selected port name from the JComboBox
                        String selectedPortName = (String) portSelector.getSelectedItem();

                        // Configure and open the serial port
                        SerialPort comPort = SerialPort.getCommPort(selectedPortName);
                        comPort.setBaudRate(9600);
                        comPort.openPort();

                        // Ensure the serial port is open
                        if (comPort.isOpen()) {
                            System.out.println("Serial port is open on " + selectedPortName);
                        } else {
                            System.err.println("Failed to open serial port " + selectedPortName);
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
                                    String data = new String(readBuffer, 0, numRead);

                                    for (char c : data.toCharArray()) {
                                        if (c == '#') {
                                            if (recording) {
                                                // If recording, print the buffer and reset it
                                                String result = buffer.toString();
                                                System.out.println("Received: " + result);
                                                SwingUtilities.invokeLater(() -> label.setText("Received: " + result));
                                                buffer.setLength(0); // Clear the buffer
                                                // Optionally, check if result is valid
                                                if (!result.contains("Java Test")) { 
                                                    stopReading = true; 
                                                    comPort.closePort(); 
                                                    SwingUtilities.invokeLater(() -> label.setText("Invalid data. Port closed."));
                                                    return;
                                                }
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

                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
    }
}

*/

/*
import com.fazecast.jSerialComm.SerialPort;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class DisplaySerialData {
    public static void main(String[] args) {
        // Create and set up the JFrame
        JFrame frame = new JFrame("Data Display");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new java.awt.BorderLayout());

        // Create a JComboBox to list available COM ports
        ArrayList<String> portNames = new ArrayList<>();
        
        // Add all available ports
        SerialPort[] portList = SerialPort.getCommPorts();
        for (SerialPort port : portList) {
            portNames.add(port.getSystemPortName());
        }

        // Add COM10 if it's not already in the list
        if (!portNames.contains("COM10")) {
            portNames.add("COM10");
        }

        JComboBox<String> portSelector = new JComboBox<>(portNames.toArray(new String[0]));
        frame.add(portSelector, java.awt.BorderLayout.NORTH);

        // Create a JLabel to display data
        JLabel label = new JLabel("Waiting for data...", SwingConstants.CENTER);
        frame.add(label, java.awt.BorderLayout.CENTER);

        // Make the frame visible
        frame.setVisible(true);

        // Add ActionListener to start communication when a port is selected
        portSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    try {
                        // Wait for 100ms
                        Thread.sleep(100);

                        // Get the selected port name from the JComboBox
                        String selectedPortName = (String) portSelector.getSelectedItem();

                        // Configure and open the serial port
                        SerialPort comPort = SerialPort.getCommPort(selectedPortName);
                        comPort.setBaudRate(9600);
                        comPort.openPort();

                        // Ensure the serial port is open
                        if (comPort.isOpen()) {
                            System.out.println("Serial port is open on " + selectedPortName);
                        } else {
                            System.err.println("Failed to open serial port " + selectedPortName);
                            return;
                        }

                        // Buffer to accumulate data
                        StringBuilder buffer = new StringBuilder();
                        boolean recording = false;

                        // Read data from the serial port
                        while (true) {
                            try {
                                if (comPort.bytesAvailable() > 0) {
                                    byte[] readBuffer = new byte[comPort.bytesAvailable()];
                                    int numRead = comPort.readBytes(readBuffer, readBuffer.length);
                                    String data = new String(readBuffer, 0, numRead);

                                    for (char c : data.toCharArray()) {
                                        if (c == '#') {
                                            if (recording) {
                                                // If recording, print the buffer and reset it
                                                String result = buffer.toString();
                                                System.out.println("Received: " + result);
                                                SwingUtilities.invokeLater(() -> label.setText("Received: " + result));
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
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
    }
}
*/


/*
import com.fazecast.jSerialComm.SerialPort;
import javax.swing.*;

public class DisplaySerialData {
    public static void main(String[] args) {
        // Create and set up the JFrame
        JFrame frame = new JFrame("Data Display");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new java.awt.BorderLayout());

        // Create a JLabel to display data
        JLabel label = new JLabel("Waiting for data...", SwingConstants.CENTER);
        frame.add(label, java.awt.BorderLayout.CENTER);

        // Make the frame visible
        frame.setVisible(true);

        // Configure and open the serial port
        SerialPort comPort = SerialPort.getCommPort("COM10"); // Adjust COM port name to match the Arduino's port
        comPort.setBaudRate(9600);
        comPort.openPort();

        // Ensure the serial port is open
        if (comPort.isOpen()) {
            System.out.println("Serial port is open");
        } else {
            System.err.println("Failed to open serial port");
            return;
        }

        // Buffer to accumulate data
        StringBuilder buffer = new StringBuilder();
        boolean recording = false;

        // Read data from the serial port
        while (true) {
            try {
                if (comPort.bytesAvailable() > 0) {
                    byte[] readBuffer = new byte[comPort.bytesAvailable()];
                    int numRead = comPort.readBytes(readBuffer, readBuffer.length);
                    String data = new String(readBuffer, 0, numRead);

                    for (char c : data.toCharArray()) {
                        if (c == '#') {
                            if (recording) {
                                // If recording, print the buffer and reset it
                                String result = buffer.toString();
                                System.out.println("Received: " + result);
                                SwingUtilities.invokeLater(() -> label.setText("Received: " + result));
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
            } catch (Exception e) {
                e.printStackTrace(); // Print any exceptions for debugging
            }
        }
    }
}
*/


