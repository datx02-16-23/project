package multiset.model;

public class RgbColor {
    private final double r;
    private final double g;
    private final double b;

    public RgbColor () {
        this(Math.random(), Math.random(), Math.random());
    }

    public RgbColor (double r, double g, double b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public double getR () {
        return this.r;
    }

    public double getG () {
        return this.g;
    }

    public double getB () {
        return this.b;
    }
}
