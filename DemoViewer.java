import java.awt.*;
import javax.swing.*;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.ArrayList;

public class DemoViewer {
    public static void main(String[] args) {
        //initialize frame(window) and the pane everything will be added in
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());
        
        //horizontal slider
        JSlider headingSlider = new JSlider(0,360,180);  //value=180 is slider's starting position, the middle
        pane.add(headingSlider, BorderLayout.SOUTH);

        //vertical slider
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);
        
        //panel for render results
        JPanel renderPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                //turning Graphics object to Graphics2D which is more often used for more advanced 2D graphics operations
                Graphics2D g2 = (Graphics2D) g;

                //fills the panel with black, getWidth() and setWidth() are methods available in JPanel
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                //make the center the point where things will be placed (it's set to the top left corner by default)
                g2.translate(getWidth() / 2, getHeight() / 2);
                
                //the values of the sliders
                double heading = Math.toRadians(headingSlider.getValue());
                double pitch = Math.toRadians(pitchSlider.getValue());
                
                //XZ rotation matrix
                Matrix3 headingTransform = new Matrix3(new double[] {
                    Math.cos(heading), 0, Math.sin(heading),
                    0, 1, 0,
                    -Math.sin(heading), 0, Math.cos(heading)
                });

                //YZ rotation matrix
                Matrix3 pitchTransform = new Matrix3(new double[] {
                    1, 0, 0,
                    0, Math.cos(pitch), Math.sin(pitch),
                    0, -Math.sin(pitch), Math.cos(pitch)
                });

                //transformation matrix for both XZ and YZ rotations
                Matrix3 transform = headingTransform.multiply(pitchTransform);

                //make List<Triangle> and add 4 Triangles forming a basic tetrahedron
                List<Triangle> tris = new ArrayList<Triangle>();
                tris.add(new Triangle(new Vertex(100, 100, 100),
                                    new Vertex(-100, -100, 100),
                                    new Vertex(-100, 100, -100),
                                    Color.WHITE));
                tris.add(new Triangle(new Vertex(100, 100, 100),
                                    new Vertex(-100, -100, 100),
                                    new Vertex(100, -100, -100),
                                    Color.RED));
                tris.add(new Triangle(new Vertex(-100, 100, -100),
                                    new Vertex(100, -100, -100),
                                    new Vertex(100, 100, 100),
                                    Color.GREEN));
                tris.add(new Triangle(new Vertex(-100, 100, -100),
                                    new Vertex(100, -100, -100),
                                    new Vertex(-100, -100, 100),
                                    Color.BLUE));
                
                //set drawing color to white
                g2.setColor(Color.WHITE);

                for (Triangle t : tris) {
                    //transform all Triangle vertices based on current rotation 
                    Vertex v1 = transform.transform(t.v1);
                    Vertex v2 = transform.transform(t.v2);
                    Vertex v3 = transform.transform(t.v3);

                    //draw them using orthographic projection
                    Path2D path = new Path2D.Double();
                    path.moveTo(v1.x, v1.y);
                    path.lineTo(v2.x, v2.y);
                    path.lineTo(v3.x, v3.y);
                    path.closePath();
                    g2.draw(path);
                }
            }
        };

        pane.add(renderPanel, BorderLayout.CENTER);
        
        //final settings
        frame.setSize(400, 400);
        frame.setVisible(true);
        
        //add action listeners to the sliders
        headingSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());
    }
}

class Vertex {
    //x is left-right, y is up-down, z is depth (larger z means closer to observer)
    double x, y, z;
    Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    } 
}

class Triangle {
    Vertex v1, v2, v3;
    Color color;
    Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.color = color;
    }
    @Override
    public String toString() {
        return String.format("%s,\n%s,\n%s\n", v1.toString(), v2.toString(), v3.toString());
    } 
}

class Matrix3 {
    double[] values;
    Matrix3 (double[] values) {
        this.values = values;
    }
    //returns the matrix that results from the multiplication of 3x3 matrices this and other
    Matrix3 multiply(Matrix3 other) {
        double[] result = new double[9];
        for (int row=0; row<3; row++) {         //rows of this matrix
            for (int col=0; col<3; col++) {     //columns of other matrix
                for (int k=0; k<3; k++) {       //use k to traverse the current row and col simultaneously 
                    result[row*3 + col] += this.values[row*3 + k] * other.values[k*3 + col];
                }
            }
        }
        return new Matrix3(result);
    }
    //returns the Vertex that results from multiplying Vertex v (1x3) with double[9] values (3x3)
    Vertex transform(Vertex v) {
        return new Vertex(
            v.x * values[0] + v.y * values[3] + v.z * values[6],
            v.x * values[1] + v.y * values[4] + v.z * values[7],
            v.x * values[2] + v.y * values[5] + v.z * values[8]
        );
    }
}
