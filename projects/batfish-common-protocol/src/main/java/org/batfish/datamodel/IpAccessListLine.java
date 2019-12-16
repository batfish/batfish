package org.batfish.datamodel;

import static org.batfish.datamodel.acl.AclLineMatchExprs.not;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;

/** A line in an IpAccessList */
public final class IpAccessListLine extends AbstractAclLine {

  public static class Builder {

    private LineAction _action;

    private AclLineMatchExpr _matchCondition;

    private String _name;

    private Builder() {}

    public Builder accepting() {
      _action = LineAction.PERMIT;
      return this;
    }

    public IpAccessListLine build() {
      return new IpAccessListLine(_action, _matchCondition, _name);
    }

    public Builder rejecting() {
      _action = LineAction.DENY;
      return this;
    }

    public Builder setAction(LineAction action) {
      _action = action;
      return this;
    }

    public Builder setMatchCondition(AclLineMatchExpr matchCondition) {
      _matchCondition = matchCondition;
      return this;
    }

    public Builder setName(String name) {
      _name = name;
      return this;
    }
  }

  public static final IpAccessListLine ACCEPT_ALL = accepting("ACCEPT_ALL", TrueExpr.INSTANCE);
  private static final String PROP_ACTION = "action";
  private static final String PROP_MATCH_CONDITION = "matchCondition";

  public static final IpAccessListLine REJECT_ALL = rejecting("REJECT_ALL", TrueExpr.INSTANCE);

  public static Builder accepting() {
    return new Builder().setAction(LineAction.PERMIT);
  }

  /** Prefer {@link #accepting(String, AclLineMatchExpr)}. */
  @VisibleForTesting
  public static IpAccessListLine accepting(AclLineMatchExpr expr) {
    return accepting().setMatchCondition(expr).build();
  }

  public static IpAccessListLine accepting(@Nonnull String name, AclLineMatchExpr expr) {
    return accepting().setMatchCondition(expr).setName(name).build();
  }

  /** Prefer {@link #acceptingHeaderSpace(String, HeaderSpace)}. */
  @VisibleForTesting
  public static IpAccessListLine acceptingHeaderSpace(HeaderSpace headerSpace) {
    return accepting(new MatchHeaderSpace(headerSpace));
  }

  public static IpAccessListLine acceptingHeaderSpace(
      @Nonnull String name, HeaderSpace headerSpace) {
    return accepting(name, new MatchHeaderSpace(headerSpace));
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder rejecting() {
    return new Builder().setAction(LineAction.DENY);
  }

  /** Prefer {@link #rejecting(String, AclLineMatchExpr)}. */
  @VisibleForTesting
  public static IpAccessListLine rejecting(AclLineMatchExpr expr) {
    return rejecting().setMatchCondition(expr).build();
  }

  public static IpAccessListLine rejecting(String name, AclLineMatchExpr expr) {
    return rejecting().setMatchCondition(expr).setName(name).build();
  }

  /** Prefer {@link #rejectingHeaderSpace(String, HeaderSpace)}. */
  @VisibleForTesting
  public static IpAccessListLine rejectingHeaderSpace(HeaderSpace headerSpace) {
    return rejecting(new MatchHeaderSpace(headerSpace));
  }

  public static IpAccessListLine rejectingHeaderSpace(String name, HeaderSpace headerSpace) {
    return rejecting(name, new MatchHeaderSpace(headerSpace));
  }

  /**
   * Returns the {@link IpAccessListLine lines} necessary to take the explicit actions of the named
   * ACL.
   */
  public static Stream<IpAccessListLine> takingExplicitActionsOf(String aclName) {
    return Stream.of(
        IpAccessListLine.accepting()
            .setMatchCondition(new PermittedByAcl(aclName, false))
            .setName(aclName + "-EXPLICITLY-PERMITTED")
            .build(),
        IpAccessListLine.rejecting()
            .setMatchCondition(not(new PermittedByAcl(aclName, true)))
            .setName(aclName + "-EXPLICITLY-DENIED")
            .build());
  }

  private final LineAction _action;

  private final AclLineMatchExpr _matchCondition;

  @JsonCreator
  public IpAccessListLine(
      @JsonProperty(PROP_ACTION) @Nonnull LineAction action,
      @JsonProperty(PROP_MATCH_CONDITION) @Nonnull AclLineMatchExpr matchCondition,
      @JsonProperty(PROP_NAME) String name) {
    super(name);
    _action = Objects.requireNonNull(action);
    _matchCondition = Objects.requireNonNull(matchCondition);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IpAccessListLine)) {
      return false;
    }
    IpAccessListLine other = (IpAccessListLine) obj;
    return _action == other._action
        && Objects.equals(_matchCondition, other._matchCondition)
        && Objects.equals(_name, other._name);
  }

  /** The action the underlying access-list will take when this line matches an IPV4 packet. */
  @JsonProperty(PROP_ACTION)
  public @Nonnull LineAction getAction() {
    return _action;
  }

  @JsonProperty(PROP_MATCH_CONDITION)
  public @Nonnull AclLineMatchExpr getMatchCondition() {
    return _matchCondition;
  }

  @Override
  public <R> R accept(GenericAclLineVisitor<R> visitor) {
    return visitor.visitIpAccessListLine(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_action, _matchCondition, _name);
  }

  public Builder toBuilder() {
    return builder().setAction(_action).setMatchCondition(_matchCondition).setName(_name);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_ACTION, _action)
        .add(PROP_MATCH_CONDITION, _matchCondition)
        .add(PROP_NAME, _name)
        .toString();
  }
}
