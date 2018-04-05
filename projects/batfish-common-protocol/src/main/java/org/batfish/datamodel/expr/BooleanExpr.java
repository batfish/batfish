package org.batfish.datamodel.expr;

import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;

public abstract class BooleanExpr {
  public abstract boolean match(Flow flow, String srcInterface, Set<IpAccessList> availableAcls);

  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    /*if (!getClass().equals(o.getClass())) {
      return false;
    }*/
    return getClass().equals(o.getClass());

    // I don't think this is applicable anymore
    // return exprEquals((Expr) o);
  }

  public abstract int hashCode();

  public abstract String toString();
}
