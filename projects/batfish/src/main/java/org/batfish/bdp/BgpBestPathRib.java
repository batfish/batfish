package org.batfish.bdp;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.RoutingProtocol;

public class BgpBestPathRib extends AbstractRib<BgpRoute> {

  /** */
  private static final long serialVersionUID = 1L;

  public BgpBestPathRib(VirtualRouter owner) {
    super(owner);
  }

  @Override
  public int comparePreference(BgpRoute lhs, BgpRoute rhs) {

    int res;

    /*
     * first compare local preference
     */
    res = Integer.compare(lhs.getLocalPreference(), rhs.getLocalPreference());
    if (res != 0) {
      return res;
    }

    /*
     * on non-juniper, prefer aggregates (these routes won't appear on
     * juniper)
     */

    res =
        Integer.compare(
            getAggregatePreference(lhs.getProtocol()), getAggregatePreference(rhs.getProtocol()));
    if (res != 0) {
      return res;
    }

    /*
     * then compare as path size (shorter is better, hence reversal)
     */
    res = Integer.compare(rhs.getAsPath().size(), lhs.getAsPath().size());
    if (res != 0) {
      return res;
    }

    /*
     * origin type (IGP better than EGP, which is better than INCOMPLETE)
     */
    res = Integer.compare(lhs.getOriginType().getPreference(), rhs.getOriginType().getPreference());

    /*
     * then compare MED
     *
     * TODO: handle presence/absence of always-compare-med, noting that
     * normally we only do this comparison if the first AS is the same in the
     * paths for both routes
     */
    res = Integer.compare(rhs.getMetric(), lhs.getMetric());
    if (res != 0) {
      return res;
    }

    /*
     * next prefer eBGP over iBGP
     */
    res = Integer.compare(getTypeCost(rhs.getProtocol()), getTypeCost(lhs.getProtocol()));
    if (res != 0) {
      return res;
    }

    /*
     * The remaining criteria are used for tie-breaking to end up with a
     * single best-path.
     */

    /** Prefer the route that comes from the BGP router with the lowest router ID. */
    res = rhs.getOriginatorIp().compareTo(lhs.getOriginatorIp());
    if (res != 0) {
      return res;
    }

    /** TODO: Prefer the path with the minimum cluster list length. */

    /** TODO: Prefer the path that comes from the lowest neighbor address. */
    return res;
  }

  private int getAggregatePreference(RoutingProtocol protocol) {
    if (protocol == RoutingProtocol.AGGREGATE) {
      return 1;
    } else {
      return 0;
    }
  }

  private int getTypeCost(RoutingProtocol protocol) {
    switch (protocol) {
      case AGGREGATE:
        return 0;
      case BGP: // eBGP
        return 1;
      case IBGP:
        return 2;
        // $CASES-OMITTED$
      default:
        throw new BatfishException("Invalid BGP protocol: '" + protocol + "'");
    }
  }
}
