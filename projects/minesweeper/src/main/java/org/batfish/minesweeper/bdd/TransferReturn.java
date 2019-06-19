package org.batfish.minesweeper.bdd;

import net.sf.javabdd.BDD;
import org.batfish.minesweeper.utils.Tuple;

public class TransferReturn extends Tuple<BDDRoute, BDD> {

  TransferReturn(BDDRoute r, BDD b) {
    super(r, b);
  }

  public String debug() {
    return this.getFirst().dot(this.getSecond());
  }
}
