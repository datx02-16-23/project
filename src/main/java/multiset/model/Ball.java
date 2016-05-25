package multiset.model;

import java.util.ArrayList;
import java.util.List;

public class Ball implements iValueContainer{
  private double x, y, vx, vy, r;
  private boolean gravity = false;
  private final RgbColor color;
  private List<Double> values;

  
  public Ball(double x, double y, List<Double> values) {
    this(x, y, Math.random()/2, Math.random()/2, 25, values);
  }

  public Ball(double x, double y, double vx, double vy, double r, List<Double> values){
    this.x = x;
    this.y = y;
    this.vx = vx;
    this.vy = vy;
    this.r = r;
    this.values = values;
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
  public List<Double> getValues() {
    return values;
  }
}
