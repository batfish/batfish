package org.batfish.symbolic.abstraction;

import net.sf.javabdd.BDD;
import org.batfish.symbolic.utils.Tuple;

public class BDDReturn extends Tuple<BDDRecord, BDD> {

  BDDReturn(BDDRecord r, BDD b) {
    super(r,b);
  }

  public String debug() {
    return this.getFirst().getDot(this.getSecond());
  }
}
