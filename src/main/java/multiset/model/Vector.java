package multiset.model;

public class Vector {
    private final double x, y;

    public Vector (double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector (Vector v, double length) {
        this.x = v.getX() * length;
        this.y = v.getY() * length;
    }

    public double dotProduct (Vector b) {
        return this.x * b.getX() + this.y * b.getY();
    }

    public double getLength () {
        return Math.hypot(this.x, this.y);
    }

    public Vector normalize () {
        double len = Math.hypot(this.x, this.y);
        return new Vector(this.x / len, this.y / len);
    }

    public Vector projection (Vector b) {
        double length = this.dotProduct(b) / b.dotProduct(b);
        return new Vector(length * b.getX(), length * b.getY());
    }

    public double projectionLength (Vector b) {
        return this.dotProduct(b) / b.dotProduct(b);
    }

    public Vector rejection (Vector b) {
        return this.projection(new Vector(b.getY() * -1, b.getX()));
    }

    public Vector add (Vector b) {
        return new Vector(this.x + b.getX(), this.y + b.getY());
    }

    @Override public String toString () {
        return "X: " + this.x + "\tY: " + this.y;
    }

    public boolean equals (Vector v) {
        return this.x == v.getX() && this.y == v.getY();
    }

    public double getX () {
        return this.x;
    }

    public double getY () {
        return this.y;
    }
}
