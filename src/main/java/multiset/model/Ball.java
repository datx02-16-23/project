package multiset.model;

public class Ball implements iValueContainer {
    private double         x, y, vx, vy;
    private final double   r;
    private final double   value;
    private final RgbColor color;

    public Ball (double x, double y, double value) {
        this(x, y, Math.random() / 2, Math.random() / 2, 25, value);
    }

    public Ball (double x, double y, double vx, double vy, double r, double value) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.r = r;
        this.value = value;
        this.color = new RgbColor();
    }

    public boolean collidesWith (Ball ball) {
        double xDiff = this.getX() - ball.getX();
        double yDiff = this.getY() - ball.getY();
        double distance = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
        return distance < this.getR() + ball.getR();
    }

    public void move (double deltaT) {
        this.x += this.vx * deltaT;
        this.y += this.vy * deltaT;
    }

    public void setGravity (boolean value) {
    }

    public RgbColor getColor () {
        return this.color;
    }

    public double getMass () {
        return this.r * this.r * Math.PI;
    }

    @Override
    public String toString () {
        return this.x + ", " + this.y + "\t" + this.vx + ", " + this.vy;
    }

    public Vector getMovementVector () {
        return new Vector(this.vx, this.vy);
    }

    public double getX () {
        return this.x;
    }

    public double getY () {
        return this.y;
    }

    public double getR () {
        return this.r;
    }

    public double getVx () {
        return this.vx;
    }

    public double getVy () {
        return this.vy;
    }

    public void setX (double x) {
        this.x = x;
    }

    public void setVx (double vx) {
        this.vx = vx;
    }

    public void setVy (double vy) {
        this.vy = vy;
    }

    @Override
    public double getValue () {
        return this.value;
    }
}
