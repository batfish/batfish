package org.batfish.vendor.check_point_management;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A single access-rule in an {@link AccessLayer}. */
public final class AccessRule extends NamedManagementObject implements AccessRuleOrSection {

  @VisibleForTesting
  public static final class Builder {
    public @Nonnull Builder setAction(Uid action) {
      _action = action;
      return this;
    }

    public @Nonnull Builder setComments(String comments) {
      _comments = comments;
      return this;
    }

    public @Nonnull Builder setContent(List<Uid> content) {
      _content = content;
      return this;
    }

    public @Nonnull Builder setContentDirection(String contentDirection) {
      _contentDirection = contentDirection;
      return this;
    }

    public @Nonnull Builder setContentNegate(Boolean contentNegate) {
      _contentNegate = contentNegate;
      return this;
    }

    public @Nonnull Builder setDestination(List<Uid> destination) {
      _destination = destination;
      return this;
    }

    public @Nonnull Builder setDestinationNegate(Boolean destinationNegate) {
      _destinationNegate = destinationNegate;
      return this;
    }

    public @Nonnull Builder setEnabled(Boolean enabled) {
      _enabled = enabled;
      return this;
    }

    public @Nonnull Builder setInstallOn(List<Uid> installOn) {
      _installOn = installOn;
      return this;
    }

    public @Nonnull Builder setName(String name) {
      _name = name;
      return this;
    }

    public @Nonnull Builder setRuleNumber(Integer ruleNumber) {
      _ruleNumber = ruleNumber;
      return this;
    }

    public @Nonnull Builder setService(List<Uid> service) {
      _service = service;
      return this;
    }

    public @Nonnull Builder setServiceNegate(Boolean serviceNegate) {
      _serviceNegate = serviceNegate;
      return this;
    }

    public @Nonnull Builder setSource(List<Uid> source) {
      _source = source;
      return this;
    }

    public @Nonnull Builder setSourceNegate(Boolean sourceNegate) {
      _sourceNegate = sourceNegate;
      return this;
    }

    public @Nonnull Builder setUid(Uid uid) {
      _uid = uid;
      return this;
    }

    public @Nonnull Builder setVpn(List<Uid> vpn) {
      _vpn = vpn;
      return this;
    }

    public @Nonnull AccessRule build() {
      checkArgument(_action != null, "Missing %s", PROP_ACTION);
      checkArgument(_comments != null, "Missing %s", PROP_COMMENTS);
      checkArgument(_content != null, "Missing %s", PROP_CONTENT);
      checkArgument(_contentDirection != null, "Missing %s", PROP_CONTENT_DIRECTION);
      checkArgument(_contentNegate != null, "Missing %s", PROP_CONTENT_NEGATE);
      checkArgument(_destination != null, "Missing %s", PROP_DESTINATION);
      checkArgument(_destinationNegate != null, "Missing %s", PROP_DESTINATION_NEGATE);
      checkArgument(_enabled != null, "Missing %s", PROP_ENABLED);
      checkArgument(_installOn != null, "Missing %s", PROP_INSTALL_ON);
      // name may not be set by user, see below.
      checkArgument(_ruleNumber != null, "Missing %s", PROP_RULE_NUMBER);
      checkArgument(_service != null, "Missing %s", PROP_SERVICE);
      checkArgument(_serviceNegate != null, "Missing %s", PROP_SERVICE_NEGATE);
      checkArgument(_source != null, "Missing %s", PROP_SOURCE);
      checkArgument(_sourceNegate != null, "Missing %s", PROP_SOURCE_NEGATE);
      checkArgument(_vpn != null, "Missing %s", PROP_VPN);
      checkArgument(_uid != null, "Missing %s", PROP_UID);
      String name = firstNonNull(_name, "Rule " + _ruleNumber);
      return new AccessRule(
          _action,
          _comments,
          _content,
          _contentDirection,
          _contentNegate,
          _destination,
          _destinationNegate,
          _enabled,
          _installOn,
          name,
          _ruleNumber,
          _service,
          _serviceNegate,
          _source,
          _sourceNegate,
          _uid,
          _vpn);
    }

    private Builder() {}

    private @Nullable Uid _action;
    private @Nullable String _comments;
    private @Nullable List<Uid> _content;
    private @Nullable String _contentDirection;
    private @Nullable Boolean _contentNegate;
    private @Nullable List<Uid> _destination;
    private @Nullable Boolean _destinationNegate;
    private @Nullable Boolean _enabled;
    private @Nullable List<Uid> _installOn;
    private @Nullable String _name;
    private @Nullable Integer _ruleNumber;
    private @Nullable List<Uid> _service;
    private @Nullable Boolean _serviceNegate;
    private @Nullable List<Uid> _source;
    private @Nullable Boolean _sourceNegate;
    private @Nullable Uid _uid;
    private @Nullable List<Uid> _vpn;
  }

  @VisibleForTesting
  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @VisibleForTesting
  @Nonnull
  public static Builder testBuilder() {
    return builder()
        .setComments("")
        .setContentDirection("any")
        .setContentNegate(false)
        .setDestinationNegate(false)
        .setEnabled(true)
        .setRuleNumber(1)
        .setServiceNegate(false)
        .setSourceNegate(false);
  }

  /**
   * Returns a {@link Builder} with all applicable fields populated with the specified {@code
   * CpmiAny} object {@link Uid}.
   */
  @Nonnull
  public static Builder testBuilder(@Nonnull Uid cpmiAny) {
    List<Uid> listAny = ImmutableList.of(cpmiAny);
    return testBuilder()
        .setContent(listAny)
        .setService(listAny)
        .setDestination(listAny)
        .setSource(listAny)
        .setInstallOn(listAny)
        .setVpn(listAny);
  }

  public @Nonnull Uid getAction() {
    return _action;
  }

  public @Nonnull String getComments() {
    return _comments;
  }

  public @Nonnull List<Uid> getContent() {
    return _content;
  }

  public @Nonnull String getContentDirection() {
    return _contentDirection;
  }

  public boolean getContentNegate() {
    return _contentNegate;
  }

  public @Nonnull List<Uid> getDestination() {
    return _destination;
  }

  public boolean getDestinationNegate() {
    return _destinationNegate;
  }

  public boolean getEnabled() {
    return _enabled;
  }

  public @Nonnull List<Uid> getInstallOn() {
    return _installOn;
  }

  public int getRuleNumber() {
    return _ruleNumber;
  }

  public @Nonnull List<Uid> getService() {
    return _service;
  }

  public boolean getServiceNegate() {
    return _serviceNegate;
  }

  public @Nonnull List<Uid> getSource() {
    return _source;
  }

  public boolean getSourceNegate() {
    return _sourceNegate;
  }

  public @Nonnull List<Uid> getVpn() {
    return _vpn;
  }

  private AccessRule(
      Uid action,
      String comments,
      List<Uid> content,
      String contentDirection,
      Boolean contentNegate,
      List<Uid> destination,
      Boolean destinationNegate,
      boolean enabled,
      List<Uid> installOn,
      String name,
      int ruleNumber,
      List<Uid> service,
      Boolean serviceNegate,
      List<Uid> source,
      Boolean sourceNegate,
      Uid uid,
      List<Uid> vpn) {
    super(name, uid);
    _action = action;
    _comments = comments;
    _content = content;
    _contentDirection = contentDirection;
    _contentNegate = contentNegate;
    _destination = destination;
    _destinationNegate = destinationNegate;
    _enabled = enabled;
    _installOn = installOn;
    _ruleNumber = ruleNumber;
    _service = service;
    _serviceNegate = serviceNegate;
    _source = source;
    _sourceNegate = sourceNegate;
    _vpn = vpn;
  }

  @JsonCreator
  private static @Nonnull AccessRule create(
      @JsonProperty(PROP_ACTION) @Nullable Uid action,
      @JsonProperty(PROP_COMMENTS) @Nullable String comments,
      @JsonProperty(PROP_CONTENT) @Nullable List<Uid> content,
      @JsonProperty(PROP_CONTENT_DIRECTION) @Nullable String contentDirection,
      @JsonProperty(PROP_CONTENT_NEGATE) @Nullable Boolean contentNegate,
      @JsonProperty(PROP_DESTINATION) @Nullable List<Uid> destination,
      @JsonProperty(PROP_DESTINATION_NEGATE) @Nullable Boolean destinationNegate,
      @JsonProperty(PROP_ENABLED) @Nullable Boolean enabled,
      @JsonProperty(PROP_INSTALL_ON) @Nullable List<Uid> installOn,
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_RULE_NUMBER) @Nullable Integer ruleNumber,
      @JsonProperty(PROP_SERVICE) @Nullable List<Uid> service,
      @JsonProperty(PROP_SERVICE_NEGATE) @Nullable Boolean serviceNegate,
      @JsonProperty(PROP_SOURCE) @Nullable List<Uid> source,
      @JsonProperty(PROP_SOURCE_NEGATE) @Nullable Boolean sourceNegate,
      @JsonProperty(PROP_VPN) @Nullable List<Uid> vpn,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    return AccessRule.builder()
        .setAction(action)
        .setComments(comments)
        .setContent(content)
        .setContentDirection(contentDirection)
        .setContentNegate(contentNegate)
        .setDestination(destination)
        .setDestinationNegate(destinationNegate)
        .setEnabled(enabled)
        .setInstallOn(installOn)
        .setName(name)
        .setRuleNumber(ruleNumber)
        .setService(service)
        .setServiceNegate(serviceNegate)
        .setSource(source)
        .setSourceNegate(sourceNegate)
        .setVpn(vpn)
        .setUid(uid)
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    AccessRule accessRule = (AccessRule) o;
    return _enabled == accessRule._enabled
        && _ruleNumber == accessRule._ruleNumber
        && _contentNegate == accessRule._contentNegate
        && _destinationNegate == accessRule._destinationNegate
        && _serviceNegate == accessRule._serviceNegate
        && _sourceNegate == accessRule._sourceNegate
        && _action.equals(accessRule._action)
        && _comments.equals(accessRule._comments)
        && _content.equals(accessRule._content)
        && _contentDirection.equals(accessRule._contentDirection)
        && _destination.equals(accessRule._destination)
        && _installOn.equals(accessRule._installOn)
        && _service.equals(accessRule._service)
        && _source.equals(accessRule._source)
        && _vpn.equals(accessRule._vpn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        baseHashcode(),
        _action,
        _comments,
        _content,
        _contentDirection,
        _contentNegate,
        _destination,
        _destinationNegate,
        _enabled,
        _installOn,
        _ruleNumber,
        _service,
        _serviceNegate,
        _source,
        _sourceNegate,
        _vpn);
  }

  private final @Nonnull Uid _action;
  private final @Nonnull String _comments;
  private final @Nonnull List<Uid> _content;
  private final @Nonnull String _contentDirection;
  private final boolean _contentNegate;
  private final @Nonnull List<Uid> _destination;
  private final boolean _destinationNegate;
  private final boolean _enabled;
  private final @Nonnull List<Uid> _installOn;
  private final int _ruleNumber;
  private final @Nonnull List<Uid> _service;
  private final boolean _serviceNegate;
  private final @Nonnull List<Uid> _source;
  private final boolean _sourceNegate;
  private final @Nonnull List<Uid> _vpn;

  private static final String PROP_ACTION = "action";
  private static final String PROP_COMMENTS = "comments";
  private static final String PROP_CONTENT = "content";
  private static final String PROP_CONTENT_DIRECTION = "content-direction";
  private static final String PROP_CONTENT_NEGATE = "content-negate";
  private static final String PROP_DESTINATION = "destination";
  private static final String PROP_DESTINATION_NEGATE = "destination-negate";
  private static final String PROP_ENABLED = "enabled";
  private static final String PROP_INSTALL_ON = "install-on";
  private static final String PROP_RULE_NUMBER = "rule-number";
  private static final String PROP_SERVICE = "service";
  private static final String PROP_SERVICE_NEGATE = "service-negate";
  private static final String PROP_SOURCE = "source";
  private static final String PROP_SOURCE_NEGATE = "source-negate";
  private static final String PROP_VPN = "vpn";
}
