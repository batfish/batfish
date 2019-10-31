package org.batfish.common.bdd;

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;

@ParametersAreNonnullByDefault
public final class BDDRepresentativePicker {

  public static BDD pickRepresentative(BDD bdd, List<BDD> preference) {
    if (bdd.isZero()) {
      return bdd;
    }

    for (BDD preferedBDD : preference) {
      BDD newBDD = preferedBDD.and(bdd);
      if (!newBDD.isZero()) {
        return newBDD.fullSatOne();
      }
    }

    return bdd.fullSatOne();
  }

  private BDDRepresentativePicker() {}
}
