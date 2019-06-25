package org.batfish.dataplane.rib;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.LocalRoute;

@ParametersAreNonnullByDefault
public class LocalRib extends AnnotatedRib<LocalRoute> {

  public LocalRib() {
    super();
  }

  @Override
  public int comparePreference(AnnotatedRoute<LocalRoute> lhs, AnnotatedRoute<LocalRoute> rhs) {
    return 0;
  }
}
