package multiset.model;

public class Vector {
	private final double x, y;
	
	public Vector (double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public Vector (Vector v, double length){
		x = v.getX() * length;
		y = v.getY() * length;
	}
	
	public double dotProduct(Vector b){
		return x*b.getX()+y*b.getY();
	}
	
	public double getLength(){
		return Math.hypot(x, y);
	}
	
	public Vector normalize(){
		double len = Math.hypot(x, y);
		return new Vector(x/len, y/len);
	}
	
	public Vector projection(Vector b){
		double length = this.dotProduct(b)/b.dotProduct(b);
		return new Vector(length*b.getX(), length*b.getY());
	}

  public double projectionLength(Vector b){
    return this.dotProduct(b)/b.dotProduct(b);
  }
	
	public Vector rejection(Vector b){
		return projection(new Vector(b.getY()*-1, b.getX()));
	}
	
	public Vector add(Vector b){
		return new Vector(x + b.getX(), y + b.getY());
	}
	
	public String toString(){
		return "X: " + x + "\tY: " + y;
	}

  public boolean equals(Vector v){
    return x == v.getX() && y == v.getY();
  }
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
}
