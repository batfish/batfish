package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.datamodel.acl.TraceElements.matchedByAclLine;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.vendor.VendorStructureId;

/** A line in an IpAccessList */
public final class ExprAclLine extends AclLine {

  public static class Builder {

    private LineAction _action;

    private AclLineMatchExpr _matchCondition;

    private String _name;

    private TraceElement _traceElement;

    private VendorStructureId _vendorStructureId;

    private boolean _setTraceElement;

    private Builder() {}

    public Builder accepting() {
      _action = LineAction.PERMIT;
      return this;
    }

    public ExprAclLine build() {
      // If traceElement has not been set, create a default one from the name
      TraceElement traceElement =
          (!_setTraceElement && _name != null)
              ? matchedByAclLine(_name, _vendorStructureId)
              : _traceElement;
      return new ExprAclLine(_action, _matchCondition, _name, traceElement, _vendorStructureId);
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

    public Builder setName(@Nullable String name) {
      _name = name;
      return this;
    }

    public Builder setTraceElement(@Nullable TraceElement traceElement) {
      _setTraceElement = true;
      _traceElement = traceElement;
      return this;
    }

    public Builder setVendorStructureId(@Nullable VendorStructureId vendorStructureId) {
      _vendorStructureId = vendorStructureId;
      return this;
    }
  }

  public static final ExprAclLine ACCEPT_ALL = accepting("ACCEPT_ALL", TrueExpr.INSTANCE);
  private static final String PROP_ACTION = "action";
  private static final String PROP_MATCH_CONDITION = "matchCondition";

  public static final ExprAclLine REJECT_ALL = rejecting("REJECT_ALL", TrueExpr.INSTANCE);

  public static Builder accepting() {
    return new Builder().setAction(LineAction.PERMIT);
  }

  /** Prefer {@link #accepting(String, AclLineMatchExpr)}. */
  @VisibleForTesting
  public static ExprAclLine accepting(AclLineMatchExpr expr) {
    return accepting().setMatchCondition(expr).build();
  }

  public static ExprAclLine accepting(@Nonnull String name, AclLineMatchExpr expr) {
    return accepting().setMatchCondition(expr).setName(name).build();
  }

  public static ExprAclLine accepting(@Nonnull TraceElement traceElement, AclLineMatchExpr expr) {
    return accepting().setMatchCondition(expr).setTraceElement(traceElement).build();
  }

  /** Prefer {@link #acceptingHeaderSpace(String, HeaderSpace)}. */
  @VisibleForTesting
  public static ExprAclLine acceptingHeaderSpace(HeaderSpace headerSpace) {
    return accepting(new MatchHeaderSpace(headerSpace));
  }

  public static ExprAclLine acceptingHeaderSpace(@Nonnull String name, HeaderSpace headerSpace) {
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
  public static ExprAclLine rejecting(AclLineMatchExpr expr) {
    return rejecting().setMatchCondition(expr).build();
  }

  public static ExprAclLine rejecting(String name, AclLineMatchExpr expr) {
    return rejecting().setMatchCondition(expr).setName(name).build();
  }

  public static ExprAclLine rejecting(@Nonnull TraceElement traceElement, AclLineMatchExpr expr) {
    return rejecting().setMatchCondition(expr).setTraceElement(traceElement).build();
  }

  /** Prefer {@link #rejectingHeaderSpace(String, HeaderSpace)}. */
  @VisibleForTesting
  public static ExprAclLine rejectingHeaderSpace(HeaderSpace headerSpace) {
    return rejecting(new MatchHeaderSpace(headerSpace));
  }

  public static ExprAclLine rejectingHeaderSpace(String name, HeaderSpace headerSpace) {
    return rejecting(name, new MatchHeaderSpace(headerSpace));
  }

  private final LineAction _action;

  private final AclLineMatchExpr _matchCondition;

  @JsonCreator
  private static ExprAclLine jsonCreator(
      @JsonProperty(PROP_ACTION) @Nullable LineAction action,
      @JsonProperty(PROP_MATCH_CONDITION) @Nullable AclLineMatchExpr matchCondition,
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_TRACE_ELEMENT) @Nullable TraceElement traceElement,
      @JsonProperty(PROP_VENDOR_STRUCTURE_ID) @Nullable VendorStructureId vendorStructureId) {
    return new ExprAclLine(
        checkNotNull(action, "%s cannot be null", PROP_ACTION),
        checkNotNull(matchCondition, "%s cannot be null", PROP_MATCH_CONDITION),
        name,
        traceElement,
        vendorStructureId);
  }

  public ExprAclLine(
      @Nonnull LineAction action, @Nonnull AclLineMatchExpr matchCondition, String name) {
    this(action, matchCondition, name, null, null);
  }

  public ExprAclLine(
      @Nonnull LineAction action,
      @Nonnull AclLineMatchExpr matchCondition,
      String name,
      TraceElement traceElement,
      VendorStructureId vendorStructureId) {
    super(name, traceElement, vendorStructureId);
    _action = action;
    _matchCondition = matchCondition;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ExprAclLine)) {
      return false;
    }
    ExprAclLine other = (ExprAclLine) obj;
    return _action == other._action
        && Objects.equals(_matchCondition, other._matchCondition)
        && Objects.equals(_name, other._name)
        && Objects.equals(_traceElement, other._traceElement)
        && Objects.equals(_vendorStructureId, other._vendorStructureId);
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
    return visitor.visitExprAclLine(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_action, _matchCondition, _name, _traceElement, _vendorStructureId);
  }

  public Builder toBuilder() {
    return builder()
        .setAction(_action)
        .setMatchCondition(_matchCondition)
        .setName(_name)
        .setTraceElement(_traceElement)
        .setVendorStructureId(_vendorStructureId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_ACTION, _action)
        .add(PROP_MATCH_CONDITION, _matchCondition)
        .add(PROP_NAME, _name)
        .add(
            PROP_TRACE_ELEMENT,
            Optional.ofNullable(_traceElement).map(TraceElement::toString).orElse(null))
        .add(
            PROP_VENDOR_STRUCTURE_ID,
            Optional.ofNullable(_vendorStructureId).map(VendorStructureId::toString).orElse(null))
        .toString();
  }
}
