package org.batfish.common.util;

import java.util.Comparator;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVisitor;
import org.batfish.datamodel.route.nh.NextHopVrf;
import org.batfish.datamodel.route.nh.NextHopVtep;

/** A {@link Comparator} implementation for next hops.. */
@ParametersAreNonnullByDefault
public final class NextHopComparator implements Comparator<NextHop> {

  private static final NextHopComparator INSTANCE = new NextHopComparator();

  public static @Nonnull NextHopComparator instance() {
    return INSTANCE;
  }

  @Override
  public int compare(NextHop o1, NextHop o2) {
    // First order by type
    if (!o1.getClass().equals(o2.getClass())) {
      return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
    }
    return COMPARATOR_SELECTOR.visit(o1).compare(o1, o2);
  }

  private static final Comparator<NextHop> COMPARATOR_IP =
      Comparator.comparing(nh -> ((NextHopIp) nh).getIp());

  private static final Comparator<NextHop> COMPARATOR_INTERFACE =
      Comparator.<NextHop, Ip>comparing(
              nh -> ((NextHopInterface) nh).getIp(),
              Comparator.nullsFirst(Comparator.naturalOrder()))
          .thenComparing(
              nh -> ((NextHopInterface) nh).getInterfaceName(), InterfaceNameComparator.instance());

  private static final Comparator<NextHop> COMPARATOR_DISCARD = (nh1, nh2) -> 0;

  private static final Comparator<NextHop> COMPARATOR_VRF =
      Comparator.comparing(nh -> ((NextHopVrf) nh).getVrfName());

  private static final Comparator<NextHop> COMPARATOR_VTEP =
      Comparator.<NextHop, Integer>comparing(nh -> ((NextHopVtep) nh).getVni())
          .thenComparing(nh -> ((NextHopVtep) nh).getVtepIp());

  private static final ComparatorSelector COMPARATOR_SELECTOR = new ComparatorSelector();

  private static final class ComparatorSelector implements NextHopVisitor<Comparator<NextHop>> {

    @Override
    public Comparator<NextHop> visitNextHopIp(NextHopIp nextHopIp) {
      return COMPARATOR_IP;
    }

    @Override
    public Comparator<NextHop> visitNextHopInterface(NextHopInterface nextHopInterface) {
      return COMPARATOR_INTERFACE;
    }

    @Override
    public Comparator<NextHop> visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
      return COMPARATOR_DISCARD;
    }

    @Override
    public Comparator<NextHop> visitNextHopVrf(NextHopVrf nextHopVrf) {
      return COMPARATOR_VRF;
    }

    @Override
    public Comparator<NextHop> visitNextHopVtep(NextHopVtep nextHopVtep) {
      return COMPARATOR_VTEP;
    }
  }

  private NextHopComparator() {} // prevent instantiation of utility class
}
