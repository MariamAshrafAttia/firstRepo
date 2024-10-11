/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Mariam
 */
/*This application is designed to monitor and display serial data in both text and binary formats,
providing a visual representation of the data . It also includes basic error handling.*/


import javax.swing.*;
import java.awt.*;

public class LogicAnalyzerGUI extends JPanel {

    private int[] dataFrames = {}; // Initialize with an empty array

    public LogicAnalyzerGUI() {
        setPreferredSize(new Dimension(800, 200)); // Set preferred size
    }

    // Method to set data frames and update the display
    public void setDataFrames(int[] dataFrames) {
        this.dataFrames = dataFrames;
        repaint(); // Refresh the display
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawDataFrames(g);
    }

    private void drawDataFrames(Graphics g) {
        int x = 50; // Start position
        int y = 100; // Y position
        int width = 30; // Width of each frame
        int height = 50; // Height of the frame

        int prevBit = -1; // Initialize with a value that won't match the first bit

        for (int bit : dataFrames) {
            if (bit == 1) {
                if (prevBit == -1) {
                    // Draw the start bit for the first frame as a horizontal line only
                    g.drawLine(x, y, x + width, y);  // High level horizontal line
                } else if (bit == prevBit) {
                    // Continue the horizontal line at the high level
                    g.drawLine(x - width, y, x + width, y);
                } else {
                    g.drawLine(x, y + height, x, y);  // Vertical line from low to high
                    g.drawLine(x, y, x + width, y);   // High level
                }
            } else {
                if (prevBit == -1) {
                    // Draw the start bit for the first frame as a horizontal line only
                    g.drawLine(x, y + height, x + width, y + height);  // Low level horizontal line
                } else if (bit == prevBit) {
                    // Continue the horizontal line at the low level
                    g.drawLine(x - width, y + height, x + width, y + height);
                } else {
                    g.drawLine(x, y, x, y + height);  // Vertical line from high to low
                    g.drawLine(x, y + height, x + width, y + height); // Low level
                }
            }

            prevBit = bit; // Update the previous bit to the current bit
            x += width; // Move to the next frame
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Logic Analyzer");
        LogicAnalyzerGUI logicAnalyzer = new LogicAnalyzerGUI();
        frame.add(logicAnalyzer);
        frame.setSize(800, 200); // Set size to match preferred size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}




/*
import javax.swing.*;
import java.awt.*;

public class LogicAnalyzerGUI extends JPanel {

    private int[] dataFrames = {};  // Data frames to display
    private boolean addSeparator = false; // Flag to add a separator

    public LogicAnalyzerGUI() {
        setPreferredSize(new Dimension(800, 200)); // Adjust size as needed
    }

    public void setDataFrames(int[] dataFrames) {
        this.dataFrames = dataFrames;
        repaint(); // Refresh the display
    }

    public void addSeparator() {
        this.addSeparator = true;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawDataFrames(g);
    }

    private void drawDataFrames(Graphics g) {
        int x = 50; // Start position
        int y = 100; // Y position
        int width = 30; // Width of each frame
        int height = 50; // Height of the frame

        int prevBit = -1; // Initialize with a value that won't match the first bit

        // Draw a separator if needed
        if (addSeparator) {
            g.setColor(Color.BLUE); // Use gray color for the separator
            g.drawLine(x, y - height, x, y + height); // Vertical separator line
            x += width; // Add space after the separator
            addSeparator = false; // Reset the separator flag
        }

        // Draw the data frames
        for (int bit : dataFrames) {
            g.setColor(Color.BLACK); // Reset color to black for data frames
            if (bit == 1) {
                if (prevBit == -1) {
                    // Draw the start bit for the first frame as a horizontal line only
                    g.drawLine(x, y, x + width, y);  // High level horizontal line
                } else if (bit == prevBit) {
                    // Continue the horizontal line at the high level
                    g.drawLine(x - width, y, x + width, y);
                } else {
                    g.drawLine(x, y + height, x, y);  // Vertical line from low to high
                    g.drawLine(x, y, x + width, y);   // High level
                }
            } else {
                if (prevBit == -1) {
                    // Draw the start bit for the first frame as a horizontal line only
                    g.drawLine(x, y + height, x + width, y + height);  // Low level horizontal line
                } else if (bit == prevBit) {
                    // Continue the horizontal line at the low level
                    g.drawLine(x - width, y + height, x + width, y + height);
                } else {
                    g.drawLine(x, y, x, y + height);  // Vertical line from high to low
                    g.drawLine(x, y + height, x + width, y + height); // Low level
                }
            }

            prevBit = bit; // Update the previous bit to the current bit
            x += width; // Move to the next frame
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Logic Analyzer");
        LogicAnalyzerGUI logicAnalyzer = new LogicAnalyzerGUI();
        frame.add(logicAnalyzer);
        frame.setSize(800, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
*/


/*
import javax.swing.*;
import java.awt.*;

public class LogicAnalyzerGUI extends JPanel {

    private int[] dataFrames = {};  // Default data frames 0, 0, 1, 0, 1, 0, 1, 0

    public LogicAnalyzerGUI() {
        setPreferredSize(new Dimension(800, 200)); // Adjust size as needed
    }

    public void setDataFrames(int[] dataFrames) {
        this.dataFrames = dataFrames;
        repaint(); // Refresh the display
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawDataFrames(g);
    }

    private void drawDataFrames(Graphics g) {
        int x = 50; // Start position
        int y = 100; // Y position
        int width = 30; // Width of each frame
        int height = 50; // Height of the frame

        int prevBit = -1; // Initialize with a value that won't match the first bit

        for (int bit : dataFrames) {
            if (bit == 1) {
                if (prevBit == -1) {
                    // Draw the start bit for the first frame as a horizontal line only
                    g.drawLine(x, y, x + width, y);  // High level horizontal line
                } else if (bit == prevBit) {
                    // Continue the horizontal line at the high level
                    g.drawLine(x - width, y, x + width, y);
                } else {
                    g.drawLine(x, y + height, x, y);  // Vertical line from low to high
                    g.drawLine(x, y, x + width, y);   // High level
                }
            } else {
                if (prevBit == -1) {
                    // Draw the start bit for the first frame as a horizontal line only
                    g.drawLine(x, y + height, x + width, y + height);  // Low level horizontal line
                } else if (bit == prevBit) {
                    // Continue the horizontal line at the low level
                    g.drawLine(x - width, y + height, x + width, y + height);
                } else {
                    g.drawLine(x, y, x, y + height);  // Vertical line from high to low
                    g.drawLine(x, y + height, x + width, y + height); // Low level
                }
            }

            prevBit = bit; // Update the previous bit to the current bit
            x += width; // Move to the next frame
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Logic Analyzer");
        LogicAnalyzerGUI logicAnalyzer = new LogicAnalyzerGUI();
        frame.add(logicAnalyzer);
        frame.setSize(800, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
*/

/*
import javax.swing.*;
import java.awt.*;

public class LogicAnalyzerGUI extends JPanel {

    private int[] dataFrames = {};  // Default data frames 0, 0, 1, 0, 1, 0, 1, 0
    private JLabel textLabel = new JLabel("Received data: ", SwingConstants.CENTER);

    public LogicAnalyzerGUI() {
        setLayout(new BorderLayout());
        add(textLabel, BorderLayout.NORTH); // Add label to the top
    }

    public void setDataFrames(int[] dataFrames) {
        this.dataFrames = dataFrames;
        repaint(); // Refresh the display
    }

    public void setText(String text) {
        textLabel.setText(text); // Update text on the label
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawDataFrames(g);
    }

    private void drawDataFrames(Graphics g) {
        int x = 50; // Start position
        int y = 100; // Y position
        int width = 30; // Width of each frame
        int height = 50; // Height of the frame

        int prevBit = -1; // Initialize with a value that won't match the first bit

        for (int bit : dataFrames) {
            if (bit == 1) {
                if (prevBit == -1) {
                    // Draw the start bit for the first frame as a horizontal line only
                    g.drawLine(x, y, x + width, y);  // High level horizontal line
                } else if (bit == prevBit) {
                    // Continue the horizontal line at the high level
                    g.drawLine(x - width, y, x + width, y);
                } else {
                    g.drawLine(x, y + height, x, y);  // Vertical line from low to high
                    g.drawLine(x, y, x + width, y);   // High level
                }
            } else {
                if (prevBit == -1) {
                    // Draw the start bit for the first frame as a horizontal line only
                    g.drawLine(x, y + height, x + width, y + height);  // Low level horizontal line
                } else if (bit == prevBit) {
                    // Continue the horizontal line at the low level
                    g.drawLine(x - width, y + height, x + width, y + height);
                } else {
                    g.drawLine(x, y, x, y + height);  // Vertical line from high to low
                    g.drawLine(x, y + height, x + width, y + height); // Low level
                }
            }

            prevBit = bit; // Update the previous bit to the current bit
            x += width; // Move to the next frame
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Logic Analyzer");
        LogicAnalyzerGUI logicAnalyzer = new LogicAnalyzerGUI();
        frame.add(logicAnalyzer);
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
*/


/*
import javax.swing.*;
import java.awt.*;

public class LogicAnalyzerGUI extends JPanel {

    private int[] dataFrames = {0, 0, 1, 0, 1, 0, 1, 0};  // Example data frames

    public void setDataFrames(int[] dataFrames) {
        this.dataFrames = dataFrames;
        repaint(); // Refresh the display
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawDataFrames(g);
    }

    private void drawDataFrames(Graphics g) {
        int x = 50; // Start position
        int y = 100; // Y position
        int width = 30; // Width of each frame
        int height = 50; // Height of the frame

        int prevBit = -1; // Initialize with a value that won't match the first bit

        for (int bit : dataFrames) {
            if (bit == 1) {
                if (prevBit == -1) {
                    // Draw the start bit for the first frame as a horizontal line only
                    g.drawLine(x, y, x + width, y);  // High level horizontal line
                } else if (bit == prevBit) {
                    // Continue the horizontal line at the high level
                    g.drawLine(x - width, y, x + width, y);
                } else {
                    g.drawLine(x, y + height, x, y);  // Vertical line from low to high
                    g.drawLine(x, y, x + width, y);   // High level
                }
            } else {
                if (prevBit == -1) {
                    // Draw the start bit for the first frame as a horizontal line only
                    g.drawLine(x, y + height, x + width, y + height);  // Low level horizontal line
                } else if (bit == prevBit) {
                    // Continue the horizontal line at the low level
                    g.drawLine(x - width, y + height, x + width, y + height);
                } else {
                    g.drawLine(x, y, x, y + height);  // Vertical line from high to low
                    g.drawLine(x, y + height, x + width, y + height); // Low level
                }
            }

            prevBit = bit; // Update the previous bit to the current bit
            x += width; // Move to the next frame
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Logic Analyzer");
        LogicAnalyzerGUI logicAnalyzer = new LogicAnalyzerGUI();
        frame.add(logicAnalyzer);
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
*/





/* //run correctly
import javax.swing.*;
import java.awt.*;

public class LogicAnalyzerGUI extends JPanel {

    private int[] dataFrames = {0, 0, 1, 0, 1, 0, 1, 0};  // Example data frames

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawDataFrames(g);
    }
    
    private void drawDataFrames(Graphics g) {
    int x = 50; // Start position
    int y = 100; // Y position
    int width = 30; // Width of each frame
    int height = 50; // Height of the frame

    int prevBit = -1; // Initialize with a value that won't match the first bit

    for (int bit : dataFrames) {
        if (bit == 1) {
            if (prevBit == -1) {
                // Draw the start bit for the first frame as a horizontal line only
                g.drawLine(x, y, x + width, y);  // High level horizontal line
            } else if (bit == prevBit) {
                // Continue the horizontal line at the high level
                g.drawLine(x - width, y, x + width, y);
            } else {
                g.drawLine(x, y + height, x, y);  // Vertical line from low to high
                g.drawLine(x, y, x + width, y);   // High level
            }
        } else {
            if (prevBit == -1) {
                // Draw the start bit for the first frame as a horizontal line only
                g.drawLine(x, y + height, x + width, y + height);  // Low level horizontal line
            } else if (bit == prevBit) {
                // Continue the horizontal line at the low level
                g.drawLine(x - width, y + height, x + width, y + height);
            } else {
                g.drawLine(x, y, x, y + height);  // Vertical line from high to low
                g.drawLine(x, y + height, x + width, y + height); // Low level
            }
        }

        prevBit = bit; // Update the previous bit to the current bit
        x += width; // Move to the next frame
    }
}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Logic Analyzer");
        LogicAnalyzerGUI logicAnalyzer = new LogicAnalyzerGUI();
        frame.add(logicAnalyzer);
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}*/
 