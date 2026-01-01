package jdd.bdd;

import jdd.util.NodeName;

/** BDD-style node naming: v1..vn */
public class BDDNames implements NodeName {
  public BDDNames() {}

  public String zero() {
    return "FALSE";
  }

  public String one() {
    return "TRUE";
  }

  public String zeroShort() {
    return "0";
  }

  public String oneShort() {
    return "1";
  }

  public String variable(int n) {
    if (n < 0) return "(none)";
    return String.format("v%d", n + 1);
  }
}
