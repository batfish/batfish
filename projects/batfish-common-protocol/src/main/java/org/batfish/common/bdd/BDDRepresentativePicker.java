package org.batfish.common.bdd;

import io.opentracing.ActiveSpan;
import io.opentracing.util.GlobalTracer;
import java.util.List;
import net.sf.javabdd.BDD;

public class BDDRepresentativePicker {
  private final List<BDD> _preference;

  BDDRepresentativePicker(List<BDD> preference) {
    _preference = preference;
  }

  public BDD pickRepresentative(BDD bdd) {
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("BDDRepresentativePicker.pickRepresentative").startActive()) {
      assert span != null; // avoid unused warning
      if (bdd.isZero()) {
        return bdd;
      }

      for (BDD preferedBDD : _preference) {
        BDD newBDD = preferedBDD.and(bdd);
        if (!newBDD.isZero()) {
          return newBDD.fullSatOne();
        }
      }

      return bdd.fullSatOne();
    }
  }
}
