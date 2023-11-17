import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

public class DemoViewer {
    public static void main(String[] args) {
        //initialize frame (window)
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setVisible(true);
        
        //the pane everything will be added in
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        //horizontal slider
        JSlider headingSlider = new JSlider(-180, 180, 0);
        pane.add(headingSlider, BorderLayout.SOUTH);

        //vertical slider
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, 90, 270, 180);
        pane.add(pitchSlider, BorderLayout.EAST);
        
        //panel for render results
        JPanel renderPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                //turning Graphics object to Graphics2D which is more often used for more advanced 2D graphics operations
                Graphics2D g2 = (Graphics2D) g;

                //fill the panel with black, getWidth() and setWidth() are methods available in JPanel
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
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

                //store necessary objects and values in Data class so they are accessible by other classes
                Data.store(transform, g2, getWidth(), getHeight());

                int choice = 2;
                switch (choice) {
                    //tetrahedron
                    case 1:
                        List<Triangle> tris = Shape3D.tetrahedron(100);
                        Draw.drawTriangles(tris);
                        break;

                    //cube
                    case 2:
                        List<Rectangle> recs = Shape3D.cube(100);
                        Draw.drawRectangles(recs);
                        break;
                    
                    //cuboid
                    case 3:
                        List<Rectangle> recs2 = Shape3D.cuboid(50,75,100);
                        Draw.drawRectangles(recs2);
                        break;
                }
            }
        };

        pane.add(renderPanel, BorderLayout.CENTER);
        
        //add action listeners to the sliders
        headingSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());
    }
}

class Vertex {
    //x is left-right, y is up-down, z is depth (positive z means away from observer)
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

class Shape2D {
    Color color;
    Shape2D(Color color) {
        this.color = color;
    }
}

class Triangle extends Shape2D {
    Vertex v1, v2, v3;
    Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
        super(color);
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }
    //return a deep copy of a Triangle object
    Triangle copy() {
        Vertex v1Copy = new Vertex(v1.x, v1.y, v1.z);
        Vertex v2Copy = new Vertex(v2.x, v2.y, v2.z); 
        Vertex v3Copy = new Vertex(v3.x, v3.y, v3.z); 
        Color colorCopy = new Color(color.getRGB());
        return new Triangle(v1Copy, v2Copy, v3Copy, colorCopy);
    }
    void scaleX(double a) {
        this.v1.x *= a;
        this.v2.x *= a;
        this.v3.x *= a;
    }
    void scaleY(double b) {
        this.v1.y *= b;
        this.v2.y *= b;
        this.v3.y *= b;
    }
    void scaleZ(double c) {
        this.v1.z *= c;
        this.v2.z *= c;
        this.v3.z *= c;
    }
    void scale(double a, double b, double c) {
        scaleX(a);
        scaleY(b);
        scaleZ(c);
    }
    void scale(double x) {
        scaleX(x);
        scaleY(x);
        scaleZ(x);
    }
    @Override
    public String toString() {
        return String.format("%s,\n%s,\n%s\n", v1.toString(), v2.toString(), v3.toString());
    } 
}

class Rectangle extends Shape2D {
    Vertex v1, v2, v3, v4;
    Rectangle(Vertex v1, Vertex v2, Vertex v3, Vertex v4, Color color) {
        super(color);
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.v4 = v4;
    }
    //return a deep copy of the Rectangle object
    Rectangle copy() {
        Vertex v1Copy = new Vertex(v1.x, v1.y, v1.z);
        Vertex v2Copy = new Vertex(v2.x, v2.y, v2.z); 
        Vertex v3Copy = new Vertex(v3.x, v3.y, v3.z); 
        Vertex v4Copy = new Vertex(v4.x, v4.y, v4.z);
        Color colorCopy = new Color(color.getRGB());
        return new Rectangle(v1Copy, v2Copy, v3Copy, v4Copy, colorCopy);
    }
    //turn Rectangle object into 2 Triangles
    List<Triangle> triangulate() {
        List<Vertex> vers = new ArrayList<Vertex>();
        vers.add(v1);
        vers.add(v2);
        vers.add(v3);
        vers.add(v4);
        double maxDistance = 0;
        Vertex diagAcross = v1;
        for (Vertex v : vers) {
            if (maxDistance < Math.sqrt(Math.pow(v1.x - v.x, 2) + Math.pow(v1.y - v.y, 2) + Math.pow(v1.z - v.z, 2))) {
                maxDistance = Math.sqrt(Math.pow(v1.x - v.x, 2) + Math.pow(v1.y - v.y, 2) + Math.pow(v1.z - v.z, 2));
                diagAcross = v;
            }
        }
        vers.remove(v1);
        vers.remove(diagAcross);
        List<Triangle> tris = new ArrayList<Triangle>();
        tris.add(new Triangle(v1, vers.get(0), diagAcross, color));
        tris.add(new Triangle(diagAcross, vers.get(1), v1, color));
        return tris;
    }
    void scaleX(double a) {
        this.v1.x *= a;
        this.v2.x *= a;
        this.v3.x *= a;
        this.v4.x *= a;
    }
    void scaleY(double b) {
        this.v1.y *= b;
        this.v2.y *= b;
        this.v3.y *= b;
        this.v4.y *= b;
    }
    void scaleZ(double c) {
        this.v1.z *= c;
        this.v2.z *= c;
        this.v3.z *= c;
        this.v4.z *= c;
    }
    void scale(double a, double b, double c) {
        scaleX(a);
        scaleY(b);
        scaleZ(c);
    }
    void scale(double x) {
        scaleX(x);
        scaleY(x);
        scaleZ(x);
    }
    @Override
    public String toString() {
        return String.format("%s,\n%s,\n%s,\n%s\n", v1.toString(), v2.toString(), v3.toString(), v4.toString());
    }
}

class Shape3D {
    //returns a List of Triangles that form a standard tetrahedron
    static List<Triangle> tetrahedron(double size) {
        List<Triangle> tris = new ArrayList<Triangle>();
        tris.add(new Triangle(new Vertex(size, size, size),
                            new Vertex(-size, -size, size),
                            new Vertex(-size, size, -size),
                            Color.RED));
        tris.add(new Triangle(new Vertex(size, size, size),
                            new Vertex(-size, -size, size),
                            new Vertex(size, -size, -size),
                            Color.GREEN));
        tris.add(new Triangle(new Vertex(-size, size, -size),
                            new Vertex(size, -size, -size),
                            new Vertex(size, size, size),
                            Color.BLUE));
        tris.add(new Triangle(new Vertex(-size, size, -size),
                            new Vertex(size, -size, -size),
                            new Vertex(-size, -size, size),
                            Color.YELLOW));
        return tris;
    }
    //returns a List of Rectangles that form a cube
    static List<Rectangle> cube(double size) {
        List<Rectangle> rects = new ArrayList<Rectangle>();
        Rectangle r1 = new Rectangle(new Vertex(-size, size, size),     //back face (furtherst away from observer)
                                    new Vertex(-size, -size, size),
                                    new Vertex(size, -size, size),
                                    new Vertex(size, size, size),
                                    Color.RED);
        rects.add(r1);
        Rectangle r2 = r1.copy();
        r2.scaleZ(-1);                                                  //front face
        rects.add(r2);
        Rectangle r3 = new Rectangle(new Vertex(-size, size, size),     //left face
                                    new Vertex(-size, -size, size),
                                    new Vertex(-size, -size, -size),
                                    new Vertex(-size, size, -size),
                                    Color.GREEN);
        rects.add(r3);
        Rectangle r4 = r3.copy();
        r4.scaleX(-1);                                                  //right face
        rects.add(r4);
        Rectangle r5 = new Rectangle(new Vertex(-size, size, size),     //upper face
                                    new Vertex(size, size, size),
                                    new Vertex(size, size, -size),
                                    new Vertex(-size, size, -size),
                                    Color.BLUE);
        rects.add(r5);
        Rectangle r6 = r5.copy(); 
        r6.scaleY(-1);                                                  //lower face
        rects.add(r6);
        return rects;
    }
    //returns a List of Rectangles that form a cuboid
    static List<Rectangle> cuboid(int length, int width, int height) {
        List<Rectangle> rects = cube(1);
        for (Rectangle r : rects) {
            r.scale(length, width, height);
        }
        return rects;
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
        for (int row = 0; row < 3; row++) {         //rows of this matrix
            for (int col = 0; col < 3; col++) {     //columns of other matrix
                for (int k = 0; k < 3; k++) {       //use k to traverse the current row and col simultaneously 
                    result[row * 3 + col] += this.values[row * 3 + k] * other.values[k * 3 + col];
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

class Data {
    //store all objects and values needed outside the JPanel paintComponent method
    static Matrix3 transform;
    static Graphics2D g2;
    static double width, height;
    static void store(Matrix3 transform, Graphics2D g2, double width, double height) {
        Data.transform = transform;
        Data.g2 = g2;
        Data.width = width;
        Data.height = height;
    }
}

class Draw {
    //receives Triangles List
    static void drawTriangles(List<Triangle> tris) {
        BufferedImage img = new BufferedImage((int) Data.width, (int) Data.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = Data.g2;
        Matrix3 transform = Data.transform;

        //depth buffer array for storing the depth of each pixel 
        double[] zBuffer = new double[img.getWidth() * img.getHeight()];
        for (int i = 0; i < zBuffer.length; i++) {
            zBuffer[i] = Double.NEGATIVE_INFINITY;
        }

        for (Triangle t : tris) {
            Vertex v1 = transform.transform(t.v1);
            Vertex v2 = transform.transform(t.v2);
            Vertex v3 = transform.transform(t.v3);

            //manual translation to the center without using Graphics2D
            v1.x += Data.width / 2;
            v1.y += Data.height / 2;
            v2.x += Data.width / 2;
            v2.y += Data.height / 2;
            v3.x += Data.width / 2;
            v3.y += Data.height / 2;

            //rectangular bounds of triangle
            int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
            int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
            int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
            int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

            //the area the rectangular bounds take up
            //(basically double the area of the triangle)
            double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

            //rasterize triangles pixel-by-pixel using barycentric coordinates and depth buffer
            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                    double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                    double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;
                    if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                        double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
                        int zIndex = y * img.getWidth() + x;
                        if (zBuffer[zIndex] < depth) {
                            img.setRGB(x, y, t.color.getRGB());
                            zBuffer[zIndex] = depth;
                        }
                    }
                }
            }
        }

        //finally, draw the Buffered Image on the renderPanel
        g2.drawImage(img, 0, 0, null);
    }
    //receives Rectangles List
    static void drawRectangles(List<Rectangle> recs) {
        List<Triangle> tris = new ArrayList<Triangle>();
        List<Triangle> temp = new ArrayList<Triangle>();
        for (Rectangle r : recs) {
            temp = r.triangulate();
            tris.add(temp.get(0));
            tris.add(temp.get(1));
        }
        drawTriangles(tris);
    }
}