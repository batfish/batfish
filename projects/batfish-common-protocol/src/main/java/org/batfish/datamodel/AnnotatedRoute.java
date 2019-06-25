package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents an {@link AbstractRoute} annotated with additional information. */
@ParametersAreNonnullByDefault
public final class AnnotatedRoute<R extends AbstractRoute>
    implements AbstractRouteDecorator, Serializable {

  @Nonnull private final R _route;
  @Nonnull private final String _sourceVrf;
  private int _hashCode;

  public AnnotatedRoute(R route, String sourceVrf) {
    _route = route;
    _sourceVrf = sourceVrf;
  }

  @Override
  public AbstractRoute getAbstractRoute() {
    return _route;
  }

  @Override
  @Nonnull
  public Prefix getNetwork() {
    return _route.getNetwork();
  }

  @Nonnull
  public R getRoute() {
    return _route;
  }

  @Nonnull
  public String getSourceVrf() {
    return _sourceVrf;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof AnnotatedRoute)) {
      return false;
    }
    AnnotatedRoute<?> o = (AnnotatedRoute<?>) obj;
    return _route.equals(o._route) && _sourceVrf.equals(o._sourceVrf);
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = 31 * _route.hashCode() + _sourceVrf.hashCode();
      _hashCode = h;
    }
    return h;
  }

  @Override
  public String toString() {
    return toStringHelper(getClass()).add("sourceVrf", _sourceVrf).add("route", _route).toString();
  }
}
