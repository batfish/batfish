package org.batfish.dataplane.rib;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.ConnectedRoute;

/** Rib for storing {@link ConnectedRoute}s */
@ParametersAreNonnullByDefault
public class ConnectedRib extends AnnotatedRib<ConnectedRoute> {

  private static final long serialVersionUID = 1L;

  public ConnectedRib() {
    super(null);
  }

  @Override
  public int comparePreference(
      AnnotatedRoute<ConnectedRoute> lhs, AnnotatedRoute<ConnectedRoute> rhs) {
    return 0;
  }
}
