package org.batfish.datamodel;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ospf.OspfMetricType;

/** A route or route builder with writeable OSPF metric-type. */
@ParametersAreNonnullByDefault
public interface HasWritableOspfMetricType<
    B extends AbstractRouteBuilder<B, R>, R extends AbstractRoute> {

  @Nonnull
  B setOspfMetricType(@Nonnull OspfMetricType ospfMetricType);
}
