package org.batfish.representation.juniper;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.ip.Prefix;

@ParametersAreNonnullByDefault
public final class GeneratedRoute extends AbstractAggregateRoute {

  public GeneratedRoute(Prefix prefix) {
    super(prefix);
  }

  public void inheritUnsetFields(GeneratedRoute parent) {
    super.inheritUnsetFieldsSuper(parent);
  }
}
