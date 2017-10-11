package org.batfish.symbolic.abstraction;

import net.sf.javabdd.BDD;
import org.batfish.symbolic.utils.Tuple;

public class TransferReturn extends Tuple<BDDRecord, BDD> {

  TransferReturn(BDDRecord r, BDD b) {
    super(r,b);
  }

  public String debug() {
    return this.getFirst().getDot(this.getSecond());
  }
}
