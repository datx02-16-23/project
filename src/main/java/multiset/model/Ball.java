package multiset.model;

public class Ball implements iValueContainer{
  private double x, y, vx, vy, r, value;
  private boolean gravity = false;
  private final RgbColor color;

  
  public Ball(double x, double y, double value) {
    this(x, y, Math.random(), Math.random(), 20, value);
  }

  public Ball(double x, double y, double vx, double vy, double r, double value){
    this.x = x;
    this.y = y;
    this.vx = vx;
    this.vy = vy;
    this.r = r;
    this.value = value;
    color = new RgbColor();
  }
  
  public boolean collidesWith(Ball ball){
    double xDiff = getX()-ball.getX();
    double yDiff = getY()-ball.getY();
    double distance = Math.sqrt(xDiff*xDiff + yDiff*yDiff);
    return (distance < getR() + ball.getR());
  }


  public void move(double deltaT){
    x += vx * deltaT;
    y += vy * deltaT;
  }


  public void setGravity(boolean value){
    gravity = value;
  }

  public RgbColor getColor(){
    return color;
  }
  
  public double getMass(){
    return r*r*Math.PI;
  }

  public String toString(){
    return x + ", " + y + "\t" + vx + ", " + vy;
  }
  
  public Vector getMovementVector(){
    return new Vector(vx, vy);
  }
  
  public double getX(){
    return x;
  }

  public double getY(){
    return y;
  }
  
  public double getR(){
    return r;
  }
  
  public double getVx(){
    return vx;
  }
  
  public double getVy(){
    return vy;
  }
  
  public void setX(double x){
    this.x = x;
  }
  
  public void setVx(double vx){
    this.vx = vx;
  }
  
  public void setVy(double vy){
    this.vy = vy;
  }


  @Override
  public double getValue() {
    return value;
  }
}
