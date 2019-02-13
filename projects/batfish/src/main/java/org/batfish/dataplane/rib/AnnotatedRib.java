package org.batfish.dataplane.rib;

import java.io.Serializable;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.dataplane.AnnotatedRoute;

public class AnnotatedRib<R extends AbstractRoute> extends AbstractRib<AnnotatedRoute<R>>
    implements Serializable {

  private static final long serialVersionUID = 1L;

  public AnnotatedRib() {
    super(null);
  }

  @Override
  public AbstractRoute getAbstractRoute(AnnotatedRoute<R> route) {
    return route.getRoute();
  }

  @Override
  public int comparePreference(AnnotatedRoute<R> lhs, AnnotatedRoute<R> rhs) {
    return lhs.getRoute().compareTo(rhs.getRoute());
  }
}
