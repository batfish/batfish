package org.batfish.representation.fortios;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.representation.fortios.FortiosConfiguration.computePolicyName;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;

/** FortiOS datamodel component containing firewall policy configuration */
public final class Policy implements Serializable {
  public static final String ALL_ADDRESSES = "all";
  public static final String ALL_SERVICE = "ALL";
  public static final String ANY_INTERFACE = "any";

  public static final Action DEFAULT_ACTION = Action.DENY;
  public static final Status DEFAULT_STATUS = Status.ENABLE;

  public enum Status {
    ENABLE,
    DISABLE,
  }

  public enum Action {
    ALLOW,
    DENY,
    IPSEC,
  }

  @Nonnull
  public String getNumber() {
    return _number;
  }

  @Nullable
  public String getName() {
    return _name;
  }

  @VisibleForTesting
  @Nullable
  public Action getAction() {
    return _action;
  }

  @Nonnull
  public Action getActionEffective() {
    return _action == null ? DEFAULT_ACTION : _action;
  }

  @Nullable
  public String getComments() {
    return _comments;
  }

  @VisibleForTesting
  @Nullable
  public Status getStatus() {
    return _status;
  }

  @Nonnull
  public Status getStatusEffective() {
    return _status == null ? DEFAULT_STATUS : _status;
  }

  @Nonnull
  public Set<String> getSrcIntf() {
    return _srcIntf;
  }

  @Nonnull
  public Set<String> getDstIntf() {
    return _dstIntf;
  }

  public @Nullable Set<String> getSrcAddr() {
    return _srcAddr;
  }

  /** Set of Batfish-internal UUIDs associated with srcaddr references. */
  @Nonnull
  public Set<BatfishUUID> getSrcAddrUUIDs() {
    return _srcAddrUuids;
  }

  public @Nullable Set<String> getDstAddr() {
    return _dstAddr;
  }

  /** Set of Batfish-internal UUIDs associated with dstaddr references. */
  @Nonnull
  public Set<BatfishUUID> getDstAddrUUIDs() {
    return _dstAddrUuids;
  }

  public @Nullable Set<String> getService() {
    return _service;
  }

  public void setDstAddr(Set<String> dstAddr) {
    _dstAddr = ImmutableSet.copyOf(dstAddr);
  }

  /** Set of Batfish-internal UUIDs associated with service references. */
  @Nonnull
  public Set<BatfishUUID> getServiceUUIDs() {
    return _serviceUuids;
  }

  public void setAction(Action action) {
    _action = action;
  }

  public void setComments(String comments) {
    _comments = comments;
  }

  public void setName(String name) {
    _name = name;
  }

  public void setService(Set<String> service) {
    _service = ImmutableSet.copyOf(service);
  }

  public void setSrcAddr(Set<String> srcAddr) {
    _srcAddr = ImmutableSet.copyOf(srcAddr);
  }

  public void setStatus(Status status) {
    _status = status;
  }

  public Policy(String number) {
    _number = number;
    _srcIntf = new HashSet<>();
    _dstIntf = new HashSet<>();
    _srcAddr = new HashSet<>();
    _dstAddr = new HashSet<>();
    _service = new HashSet<>();

    _srcAddrUuids = new HashSet<>();
    _dstAddrUuids = new HashSet<>();
    _serviceUuids = new HashSet<>();
  }

  @Nonnull private String _number;
  @Nullable private String _name;
  @Nonnull private final Set<String> _srcIntf;
  @Nonnull private final Set<String> _dstIntf;
  @Nullable private Set<String> _srcAddr;
  @Nonnull private final Set<BatfishUUID> _srcAddrUuids;
  @Nullable private Set<String> _dstAddr;
  @Nonnull private final Set<BatfishUUID> _dstAddrUuids;
  @Nullable private Set<String> _service;
  @Nonnull private final Set<BatfishUUID> _serviceUuids;
  @Nullable private Status _status;
  @Nullable private String _comments;
  @Nullable private Action _action;

  public IpAccessList toIpAccessList(
      Map<String, IpSpace> namedIpSpaces, Map<String, AclLineMatchExpr> convertedServices) {
    // Make sure references were finalized
    assert _srcAddr != null && _dstAddr != null && _service != null;

    // TODO Incorporate _name, _comments. Probably return null or empty if not enabled.
    // TODO May need to confirm that all interfaces/addresses are defined. They were checked when
    //  these fields  were set, but technically could have been deleted since.
    ImmutableList.Builder<AclLineMatchExpr> matchConjuncts = ImmutableList.builder();

    // Match source interfaces
    // TODO May have to restructure to keep MatchSrcInterface separate from main IpAccessList.
    matchConjuncts.add(new MatchSrcInterface(_srcIntf));

    // Match src and dst addresses
    ImmutableList.Builder<AclLineMatchExpr> srcAddrExprs = ImmutableList.builder();
    ImmutableList.Builder<AclLineMatchExpr> dstAddrExprs = ImmutableList.builder();
    for (String src : Sets.intersection(_srcAddr, namedIpSpaces.keySet())) {
      HeaderSpace hs = HeaderSpace.builder().setSrcIps(new IpSpaceReference(src)).build();
      srcAddrExprs.add(new MatchHeaderSpace(hs, String.format("Match source address %s", src)));
    }
    for (String dst : Sets.intersection(_dstAddr, namedIpSpaces.keySet())) {
      HeaderSpace hs = HeaderSpace.builder().setDstIps(new IpSpaceReference(dst)).build();
      dstAddrExprs.add(
          new MatchHeaderSpace(hs, String.format("Match destination address %s", dst)));
    }
    matchConjuncts.add(or(srcAddrExprs.build()));
    matchConjuncts.add(or(dstAddrExprs.build()));

    // Match services. TODO confirm services should be disjuncted
    List<AclLineMatchExpr> svcExprs =
        Sets.intersection(_service, convertedServices.keySet()).stream()
            .map(convertedServices::get)
            .collect(ImmutableList.toImmutableList());
    matchConjuncts.add(or(svcExprs));

    // construct line
    ExprAclLine.Builder line =
        getActionEffective() == Action.ALLOW ? ExprAclLine.accepting() : ExprAclLine.rejecting();
    line.setMatchCondition(and(matchConjuncts.build()));
    return IpAccessList.builder().setName(computePolicyName(this)).setLines(line.build()).build();
  }
}
