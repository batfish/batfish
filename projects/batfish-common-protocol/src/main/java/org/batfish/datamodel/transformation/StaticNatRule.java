package org.batfish.datamodel.transformation;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;

// TODO javadoc
public class StaticNatRule extends Transformation {
  private static final String PROP_LOCAL_NETWORK = "localNetwork";
  private static final String PROP_GLOBAL_NETWORK = "globalNetwork";
  private static final long serialVersionUID = 1L;
  private Prefix _globalNetwork;
  private Prefix _localNetwork;

  private StaticNatRule(
      @JsonProperty(PROP_ACL) @Nullable IpAccessList acl,
      @JsonProperty(PROP_ACTION) @Nonnull RuleAction action,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description,
      @JsonProperty(PROP_GLOBAL_NETWORK) @Nonnull Prefix globalNetwork,
      @JsonProperty(PROP_LOCAL_NETWORK) @Nonnull Prefix localNetwork) {
    super(acl, action, description);
    _globalNetwork = globalNetwork;
    _localNetwork = localNetwork;
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static StaticNatRule create(
      @JsonProperty(PROP_ACL) @Nullable IpAccessList acl,
      @JsonProperty(PROP_ACTION) @Nonnull RuleAction action,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description,
      @JsonProperty(PROP_LOCAL_NETWORK) @Nonnull Prefix localNetwork,
      @JsonProperty(PROP_GLOBAL_NETWORK) @Nonnull Prefix globalNetwork) {
    return new StaticNatRule(
        acl,
        requireNonNull(action),
        description,
        requireNonNull(globalNetwork),
        requireNonNull(localNetwork));
  }

  @Override
  protected int compareSameClass(Transformation o) {
    return Comparator.comparing(StaticNatRule::getGlobalNetwork)
        .thenComparing(StaticNatRule::getLocalNetwork)
        .compare(this, (StaticNatRule) o);
  }

  @Override
  public <R> R accept(GenericTransformationRuleVisitor<R> visitor) {
    return visitor.visitStaticTransformationRule(this);
  }

  @JsonProperty(PROP_GLOBAL_NETWORK)
  public Prefix getGlobalNetwork() {
    return _globalNetwork;
  }

  @JsonProperty(PROP_LOCAL_NETWORK)
  public Prefix getLocalNetwork() {
    return _localNetwork;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_acl, _action, _description, _globalNetwork, _localNetwork);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_ACL, _acl)
        .add(PROP_ACTION, _action)
        .add(PROP_DESCRIPTION, _description)
        .add(PROP_GLOBAL_NETWORK, _globalNetwork)
        .add(PROP_LOCAL_NETWORK, _localNetwork)
        .toString();
  }

  @Override
  boolean transformEquals(Transformation o) {
    StaticNatRule rhs = (StaticNatRule) o;
    return Objects.equals(_globalNetwork, rhs._globalNetwork)
        && Objects.equals(_localNetwork, rhs._localNetwork);
  }

  public static class Builder {
    @Nullable IpAccessList _acl;
    @Nullable RuleAction _action;
    @Nullable String _description;
    @Nullable Prefix _globalNetwork;
    @Nullable Prefix _localNetwork;

    private Builder() {}

    @Nullable
    public StaticNatRule build() {
      if (_action == null || _globalNetwork == null || _localNetwork == null) {
        return null;
      }
      return new StaticNatRule(_acl, _action, _description, _globalNetwork, _localNetwork);
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

    public Builder setGlobalNetwork(Prefix globalNetwork) {
      _globalNetwork = globalNetwork;
      return this;
    }

    public Builder setLocalNetwork(Prefix localNetwork) {
      _localNetwork = localNetwork;
      return this;
    }
  }
}
