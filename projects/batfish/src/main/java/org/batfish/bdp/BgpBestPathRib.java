package org.batfish.bdp;

import java.util.HashMap;
import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;

public class BgpBestPathRib extends AbstractRib<BgpRoute> {

  /** */
  private static final long serialVersionUID = 1L;

  private BgpBestPathRib _prev;

  /**
   * Construct an instance with given owner and previous instance.
   *
   * @param owner The virtual router context for this RIB
   * @param prev The previous RIB used for tie-breaking based on age
   * @param clearOldPrev Whether to clear out the previous instance's reference to its own previous
   *     instance to enable GC. This is only safe if prev is no longer used for route preference
   *     comparison, but only for content inspection.
   */
  public BgpBestPathRib(VirtualRouter owner, BgpBestPathRib prev, boolean clearOldPrev) {
    super(owner);
    _prev = prev;
    if (clearOldPrev && _prev != null) {
      _prev._prev = null;
    }
  }

  /**
   * Construct an initial best-path RIB with no age information for tie-breaking
   *
   * @param owner The virtual router context for this RIB
   * @return A new instance
   */
  public static final BgpBestPathRib initial(VirtualRouter owner) {
    return new BgpBestPathRib(owner, new BgpBestPathRib(owner, null, false), false);
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
    if (res != 0) {
      return res;
    }

    /*
     * then compare MED
     *
     * TODO: handle presence/absence of always-compare-med, noting that
     * normally we only do this comparison if the first AS is the same in the
     * paths for both routes
     */
    res = Long.compare(rhs.getMetric(), lhs.getMetric());
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

    /*
     * Break tie with process's chosen tie-breaking mechanism
     */
    BgpTieBreaker tieBreaker = _owner._vrf.getBgpProcess().getTieBreaker();
    boolean bothEbgp =
        lhs.getProtocol() == RoutingProtocol.BGP && rhs.getProtocol() == RoutingProtocol.BGP;
    switch (tieBreaker) {
      case ARRIVAL_ORDER:
        if (!bothEbgp) {
          break;
        }
        boolean lhsOld = _prev.containsRoute(lhs);
        boolean rhsOld = _prev.containsRoute(rhs);
        if (lhsOld && !rhsOld) {
          return 1;
        } else if (!lhsOld && rhsOld) {
          return -1;
        }
        break;

      case ROUTER_ID:
        if (!bothEbgp) {
          break;
        }
        /** Prefer the route that comes from the BGP router with the lowest router ID. */
        res = rhs.getOriginatorIp().compareTo(lhs.getOriginatorIp());
        if (res != 0) {
          return res;
        }
        break;

      case CLUSTER_LIST_LENGTH:
      default:
        throw new BatfishException("Unhandled tie-breaker: " + tieBreaker);
    }

    /** Prefer the route that comes from the BGP router with the lowest router ID. */
    res = rhs.getOriginatorIp().compareTo(lhs.getOriginatorIp());
    if (res != 0) {
      return res;
    }

    /** Prefer the path with the minimum cluster list length. */
    res = Integer.compare(rhs.getClusterList().size(), lhs.getClusterList().size());
    if (res != 0) {
      return res;
    }

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

  public Map<Prefix, AsPath> getBestAsPaths() {
    Map<Prefix, AsPath> bestAsPaths = new HashMap<>();
    for (BgpRoute route : getRoutes()) {
      bestAsPaths.put(route.getNetwork(), route.getAsPath());
    }
    return bestAsPaths;
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
