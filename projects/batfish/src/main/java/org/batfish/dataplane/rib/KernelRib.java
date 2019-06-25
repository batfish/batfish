package org.batfish.dataplane.rib;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.KernelRoute;

/** RIB for storing {@link KernelRoute}s, which are identified solely by their network. */
@ParametersAreNonnullByDefault
public class KernelRib extends AnnotatedRib<KernelRoute> {

  public KernelRib() {
    super();
  }

  @Override
  public int comparePreference(AnnotatedRoute<KernelRoute> lhs, AnnotatedRoute<KernelRoute> rhs) {
    return 0;
  }
}
