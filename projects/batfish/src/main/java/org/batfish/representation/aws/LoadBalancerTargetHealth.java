package org.batfish.representation.aws;

import static org.batfish.representation.aws.Utils.checkNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents target health information for an elastic load balancer v2
 * https://docs.aws.amazon.com/elasticloadbalancing/.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public final class LoadBalancerTargetHealth implements AwsVpcEntity, Serializable {

  enum HealthState {
    DRAINING,
    INITIAL,
    HEALTHY,
    UNAVAILABLE,
    UNHEALTHY,
    UNUSED,
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static class TargetHealth implements Serializable {

    @Nullable private final String _description;

    @Nullable private final String _reason;

    @Nonnull private final HealthState _state;

    @JsonCreator
    private static TargetHealth create(
        @Nullable @JsonProperty(JSON_KEY_DESCRIPTION) String description,
        @Nullable @JsonProperty(JSON_KEY_REASON) String reason,
        @Nullable @JsonProperty(JSON_KEY_STATE) String state) {
      checkNonNull(state, JSON_KEY_STATE, "Load balancer target health");

      return new TargetHealth(description, reason, HealthState.valueOf(state.toUpperCase()));
    }

    TargetHealth(HealthState state) {
      this(null, null, state);
    }

    TargetHealth(@Nullable String description, @Nullable String reason, HealthState state) {
      _description = description;
      _reason = reason;
      _state = state;
    }

    @Nullable
    public String getDescription() {
      return _description;
    }

    @Nullable
    public String getReason() {
      return _reason;
    }

    @Nonnull
    public HealthState getState() {
      return _state;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof TargetHealth)) {
        return false;
      }
      TargetHealth that = (TargetHealth) o;
      return Objects.equals(_description, that._description)
          && Objects.equals(_reason, that._reason)
          && _state.equals(that._state);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_description, _reason, _state);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .omitNullValues()
          .add("_description", _description)
          .add("_reason", _reason)
          .add("_state", _state)
          .toString();
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  public static class TargetHealthDescription implements Serializable {

    @Nonnull private final LoadBalancerTarget _target;

    @Nonnull private final TargetHealth _targetHealth;

    @JsonCreator
    private static TargetHealthDescription create(
        @Nullable @JsonProperty(JSON_KEY_TARGET) LoadBalancerTarget target,
        @Nullable @JsonProperty(JSON_KEY_TARGET_HEALTH) TargetHealth targetHealth) {
      checkNonNull(target, JSON_KEY_TARGET, "Load balancer target health");
      checkNonNull(targetHealth, JSON_KEY_TARGET_HEALTH, "Load balancer target health");

      return new TargetHealthDescription(target, targetHealth);
    }

    TargetHealthDescription(LoadBalancerTarget target, TargetHealth targetHealth) {
      _target = target;
      _targetHealth = targetHealth;
    }

    @Nonnull
    public LoadBalancerTarget getTarget() {
      return _target;
    }

    @Nonnull
    public TargetHealth getTargetHealth() {
      return _targetHealth;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof TargetHealthDescription)) {
        return false;
      }
      TargetHealthDescription that = (TargetHealthDescription) o;
      return _target.equals(that._target) && _targetHealth.equals(that._targetHealth);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_target, _targetHealth);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("target", _target)
          .add("targetHealth", _targetHealth)
          .toString();
    }
  }

  @Nonnull private final String _targetGroupArn;

  @Nonnull private final List<TargetHealthDescription> _targetHealthDescriptions;

  @JsonCreator
  private static LoadBalancerTargetHealth create(
      @Nullable @JsonProperty(JSON_KEY_TARGET_GROUP_ARN) String targetGroupArn,
      @Nullable @JsonProperty(JSON_KEY_TARGET_HEALTH_DESCRIPTIONS)
          List<TargetHealthDescription> targetHealthDescriptions) {
    checkNonNull(targetGroupArn, JSON_KEY_TARGET_GROUP_ARN, "LoadBalancer target health");
    checkNonNull(
        targetHealthDescriptions,
        JSON_KEY_TARGET_HEALTH_DESCRIPTIONS,
        "LoadBalancer target health");

    return new LoadBalancerTargetHealth(targetGroupArn, targetHealthDescriptions);
  }

  public LoadBalancerTargetHealth(
      String targetGroupArn, List<TargetHealthDescription> targetHealthDescriptions) {
    _targetGroupArn = targetGroupArn;
    _targetHealthDescriptions = targetHealthDescriptions;
  }

  @Override
  public String getId() {
    return _targetGroupArn;
  }

  @Nonnull
  public List<TargetHealthDescription> getTargetHealthDescriptions() {
    return _targetHealthDescriptions;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LoadBalancerTargetHealth)) {
      return false;
    }
    LoadBalancerTargetHealth that = (LoadBalancerTargetHealth) o;
    return _targetGroupArn.equals(that._targetGroupArn)
        && _targetHealthDescriptions.equals(that._targetHealthDescriptions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_targetGroupArn, _targetHealthDescriptions);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_targetGroupArn", _targetGroupArn)
        .add("_targetHealthDescriptions", _targetHealthDescriptions)
        .toString();
  }
}
