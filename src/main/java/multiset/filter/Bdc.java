package multiset.filter;

/**
 * Created by cb on 04/05/16.
 */
public enum Bdc {
  LESS ("<", (double a, double b) -> a < b),
  LESSOREQUALS ("<=", (double a, double b) -> a <= b),
  GREATER (">", (double a, double b) -> a > b);

  private final String representation;
  private final iBdc bdc;

  Bdc(String representation, iBdc bdc){
    this.representation = representation;
    this.bdc = bdc;
  }

  private String getRepresentation(){
    return representation;
  }

  private iBdc getBdc(){
    return bdc;
  }

  public static iBdc getBDC(String representation){
    System.out.println(representation);
    for (Bdc bdc: Bdc.values()){
      System.out.println(bdc.getRepresentation());
      if (bdc.getRepresentation().equals(representation)){
        return bdc.getBdc();
      }
    }
    throw new IllegalArgumentException("Unrecognized token");
  }

  public interface iBdc {
    boolean compare(double a, double b);
  }


}

