package org.batfish.dataplane;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;

@ParametersAreNonnullByDefault
public class AnnotatedRoute<R extends AbstractRoute>
    implements Serializable, Comparable<AnnotatedRoute<R>> {

  private static final long serialVersionUID = 1L;

  private final R _route;
  private final String _sourceVrf;

  public AnnotatedRoute(R route, String sourceVrf) {
    _route = route;
    _sourceVrf = sourceVrf;
  }

  @Override
  public final int compareTo(AnnotatedRoute<R> other) {
    return _route.compareTo(other._route);
  }

  @Nonnull
  public R getRoute() {
    return _route;
  }

  @Nonnull
  public String getSourceVrf() {
    return _sourceVrf;
  }
}
