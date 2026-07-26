public class ShapeAreaCalculator {
public static void main(String[] args) {
System.out.println("----- Shape Area Calculator -----\n");

        Circle circle = new Circle(5.0);
        System.out.println("Circle:");
        System.out.println("Radius = " + circle.getRadius());
        System.out.println("Area of Circle = " + circle.calculateArea());

        System.out.println();

        Rectangle rectangle = new Rectangle(4.0, 6.0);
        System.out.println("Rectangle:");
        System.out.println("Length = " + rectangle.getLength() + ", Width = " + rectangle.getWidth());
        System.out.println("Area of Rectangle = " + rectangle.calculateArea());

        System.out.println();

        Triangle triangle = new Triangle(3.0, 8.0);
        System.out.println("Triangle:");
        System.out.println("Base = " + triangle.getBase() + ", Height = " + triangle.getHeight());
        System.out.println("Area of Triangle = " + triangle.calculateArea());
    }
}

interface Shape {
    double calculateArea();
}

class Circle implements Shape {
    private double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }
}

class Rectangle implements Shape {
    private double length;
    private double width;

    public Rectangle(double length, double width) {
        this.length = length;
        this.width = width;
    }

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    @Override
    public double calculateArea() {
        return length * width;
    }
}

class Triangle implements Shape {
    private double base;
    private double height;

    public Triangle(double base, double height) {
        this.base = base;
        this.height = height;
    }

    public double getBase() {
        return base;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public double calculateArea() {
        return 0.5 * base * height;
    }
}
