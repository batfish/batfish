package org.batfish.representation.fortios;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
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
  public Set<Interface> getSrcIntf() {
    return _srcIntf;
  }

  @Nonnull
  public Set<Interface> getDstIntf() {
    return _dstIntf;
  }

  @Nonnull
  public Set<Address> getSrcAddr() {
    return _srcAddr;
  }

  @Nonnull
  public Set<Address> getDstAddr() {
    return _dstAddr;
  }

  @Nonnull
  public Set<Service> getService() {
    return _service;
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
  }

  @Nonnull private String _number;
  @Nullable private String _name;
  @Nonnull private final Set<Interface> _srcIntf;
  @Nonnull private final Set<Interface> _dstIntf;
  @Nonnull private final Set<Address> _srcAddr;
  @Nonnull private final Set<Address> _dstAddr;
  @Nonnull private final Set<Service> _service;
  @Nullable private Status _status;
  @Nullable private String _comments;
  @Nullable private Action _action;

  public IpAccessList toIpAccessList() {
    // TODO Incorporate _name, _comments. Probably return null or empty if not enabled.
    // TODO May need to confirm that all interfaces/addresses are defined. They were checked when
    //  these fields  were set, but technically could have been deleted since.
    ImmutableList.Builder<AclLineMatchExpr> matchConjuncts = ImmutableList.builder();

    // Match source interfaces
    // TODO May have to restructure to keep MatchSrcInterface separate from main IpAccessList.
    matchConjuncts.add(new MatchSrcInterface(_srcIntf));
    if (_srcAddr.size() > 1 || _dstAddr.size() > 1) {
      // TODO Support using multiple src/dst addresses.
      throw new UnsupportedOperationException();
    }

    // Match src and dst addresses
    String srcAddr = Iterables.getOnlyElement(_srcAddr);
    String dstAddr = Iterables.getOnlyElement(_dstAddr);
    matchConjuncts.add(
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setSrcIps(new IpSpaceReference(srcAddr))
                .setDstIps(new IpSpaceReference(dstAddr))
                .build()));

    // Match services. TODO confirm services should be disjuncted
    matchConjuncts.add(
        or(_service.stream().map(Service::toMatchExpr).collect(ImmutableList.toImmutableList())));

    // construct line
    ExprAclLine.Builder line =
        getActionEffective() == Action.ALLOW ? ExprAclLine.accepting() : ExprAclLine.rejecting();
    line.setMatchCondition(and(matchConjuncts.build()));
    return IpAccessList.builder().setName(_number).setLines(line.build()).build();
  }
}
