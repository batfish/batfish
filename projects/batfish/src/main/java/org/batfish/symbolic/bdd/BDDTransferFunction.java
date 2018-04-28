package org.batfish.symbolic.bdd;

import net.sf.javabdd.BDD;
import org.batfish.symbolic.utils.Tuple;

public class BDDTransferFunction extends Tuple<BDDRoute, BDD> {

  BDDTransferFunction(BDDRoute r, BDD b) {
    super(r, b);
  }

  public String debug() {
    return this.getFirst().dot(this.getSecond());
  }
}
