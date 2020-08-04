package org.batfish.common.bdd;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;

/**
 * Class for picking a representative flow from a BDD according to the given preference as a list of
 * BDDs
 */
@ParametersAreNonnullByDefault
public final class BDDRepresentativePicker {

  /**
   * Picks a representative assignment, possibly from a combination of the given preference BDDs.
   */
  public static @Nonnull BDD pickRepresentative(BDD bdd, List<BDD> preference) {
    if (bdd.isZero()) {
      return bdd;
    }

    BDD curBDD = bdd.id(); // clone so we can free.
    for (BDD preferredBDD : preference) {
      BDD newBDD = preferredBDD.and(curBDD);
      if (newBDD.isZero()) {
        continue;
      }
      curBDD.free();
      curBDD = newBDD;
    }

    return curBDD.satOne();
  }

  private BDDRepresentativePicker() {}
}
