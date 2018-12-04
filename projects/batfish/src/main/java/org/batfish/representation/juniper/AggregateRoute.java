package org.batfish.representation.juniper;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

@ParametersAreNonnullByDefault
public final class AggregateRoute extends AbstractAggregateRoute {

  private static final long serialVersionUID = 1L;

  public AggregateRoute(Prefix prefix) {
    super(prefix);
  }

  public void inheritUnsetFields(AggregateRoute parent) {
    super.inheritUnsetFieldsSuper(parent);
  }
}
