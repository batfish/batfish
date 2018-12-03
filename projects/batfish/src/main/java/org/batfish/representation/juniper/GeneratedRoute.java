package org.batfish.representation.juniper;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

@ParametersAreNonnullByDefault
public final class GeneratedRoute extends AbstractAggregateRoute {

  private static final long serialVersionUID = 1L;

  public GeneratedRoute(Prefix prefix) {
    super(prefix);
  }

  public void inheritUnsetFields(GeneratedRoute parent) {
    super.inheritUnsetFieldsSuper(parent);
  }
}
