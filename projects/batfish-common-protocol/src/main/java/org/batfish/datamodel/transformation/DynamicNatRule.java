package org.batfish.datamodel.transformation;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;

// TODO javadoc
public class DynamicNatRule extends Transformation {
  private static final String PROP_POOL_IP_FIRST = "poolIpFirst";
  private static final String PROP_POOL_IP_LAST = "poolIpLast";
  private static final long serialVersionUID = 1L;
  private Ip _poolIpFirst;
  private Ip _poolIpLast;

  private DynamicNatRule(
      @Nullable IpAccessList acl,
      @Nonnull RuleAction action,
      @Nullable String description,
      @Nonnull Ip poolIpFirst,
      @Nonnull Ip poolIpLast){
    super(acl, action, description);
    _poolIpFirst = poolIpFirst;
    _poolIpLast = poolIpLast;
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static DynamicNatRule create(
      @JsonProperty(PROP_ACL) @Nullable IpAccessList acl,
      @JsonProperty(PROP_ACTION) @Nonnull RuleAction action,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description,
      @JsonProperty(PROP_POOL_IP_FIRST) @Nonnull Ip poolIpFirst,
      @JsonProperty(PROP_POOL_IP_LAST) @Nonnull Ip poolIpLast) {
    return new DynamicNatRule(
        acl,
        requireNonNull(action),
        description,
        requireNonNull(poolIpFirst),
        requireNonNull(poolIpLast));
  }

  @Override
  public <R> R accept(GenericTransformationRuleVisitor<R> visitor) {
    return visitor.visitDynamicTransformationRule(this);
  }

  @Override
  protected int compareSameClass(Transformation o) {
    return Comparator.comparing(DynamicNatRule::getPoolIpFirst)
        .thenComparing(DynamicNatRule::getPoolIpLast)
        .compare(this, (DynamicNatRule) o);
  }

  @JsonProperty(PROP_POOL_IP_FIRST)
  public Ip getPoolIpFirst() {
    return _poolIpFirst;
  }

  @JsonProperty(PROP_POOL_IP_LAST)
  public Ip getPoolIpLast() {
    return _poolIpLast;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_acl, _action, _description, _poolIpFirst, _poolIpLast);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_ACL, _acl)
        .add(PROP_ACTION, _action)
        .add(PROP_DESCRIPTION, _description)
        .add(PROP_POOL_IP_FIRST, _poolIpFirst)
        .add(PROP_POOL_IP_LAST, _poolIpLast)
        .toString();
  }

  @Override
  boolean transformEquals(Transformation o) {
    DynamicNatRule rhs = (DynamicNatRule) o;
    return Objects.equals(_poolIpFirst, rhs._poolIpFirst)
        && Objects.equals(_poolIpLast, rhs._poolIpLast);
  }

  public static class Builder {
    @Nullable IpAccessList _acl;
    @Nullable RuleAction _action;
    @Nullable String _description;
    @Nullable private Ip _poolIpFirst;
    @Nullable private Ip _poolIpLast;

    private Builder() {}

    @Nullable
    public DynamicNatRule build() {
      if (_action == null || _poolIpFirst == null || _poolIpLast == null) {
        return null;
      }
      return new DynamicNatRule(
          _acl, _action, _description, _poolIpFirst, _poolIpLast);
    }

    public Builder setAcl(IpAccessList acl) {
      _acl = acl;
      return this;
    }

    public Builder setAction(RuleAction action) {
      _action = action;
      return this;
    }

    public Builder setDescription(String description) {
      _description = description;
      return this;
    }

    public Builder setPoolIpFirst(Ip poolIpFirst) {
      _poolIpFirst = poolIpFirst;
      return this;
    }

    public Builder setPoolIpLast(Ip poolIpLast) {
      _poolIpLast = poolIpLast;
      return this;
    }
  }
}
