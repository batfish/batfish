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

    private final @Nullable String _description;

    private final @Nullable String _reason;

    private final @Nonnull HealthState _state;

    @JsonCreator
    private static TargetHealth create(
        @JsonProperty(JSON_KEY_DESCRIPTION) @Nullable String description,
        @JsonProperty(JSON_KEY_REASON) @Nullable String reason,
        @JsonProperty(JSON_KEY_STATE) @Nullable String state) {
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

    public @Nullable String getDescription() {
      return _description;
    }

    public @Nullable String getReason() {
      return _reason;
    }

    public @Nonnull HealthState getState() {
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
          && _state == that._state;
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

    private final @Nonnull LoadBalancerTarget _target;

    private final @Nonnull TargetHealth _targetHealth;

    @JsonCreator
    private static TargetHealthDescription create(
        @JsonProperty(JSON_KEY_TARGET) @Nullable LoadBalancerTarget target,
        @JsonProperty(JSON_KEY_TARGET_HEALTH) @Nullable TargetHealth targetHealth) {
      checkNonNull(target, JSON_KEY_TARGET, "Load balancer target health");
      checkNonNull(targetHealth, JSON_KEY_TARGET_HEALTH, "Load balancer target health");

      return new TargetHealthDescription(target, targetHealth);
    }

    TargetHealthDescription(LoadBalancerTarget target, TargetHealth targetHealth) {
      _target = target;
      _targetHealth = targetHealth;
    }

    public @Nonnull LoadBalancerTarget getTarget() {
      return _target;
    }

    public @Nonnull TargetHealth getTargetHealth() {
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

  private final @Nonnull String _targetGroupArn;

  private final @Nonnull List<TargetHealthDescription> _targetHealthDescriptions;

  @JsonCreator
  private static LoadBalancerTargetHealth create(
      @JsonProperty(JSON_KEY_TARGET_GROUP_ARN) @Nullable String targetGroupArn,
      @JsonProperty(JSON_KEY_TARGET_HEALTH_DESCRIPTIONS) @Nullable
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

  public @Nonnull List<TargetHealthDescription> getTargetHealthDescriptions() {
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
