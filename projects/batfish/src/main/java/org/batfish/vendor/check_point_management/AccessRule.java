package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A single access-rule in an {@link AccessLayer}. */
public final class AccessRule extends NamedManagementObject implements AccessRuleOrSection {

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

  @VisibleForTesting
  AccessRule(
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
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(comments != null, "Missing %s", PROP_COMMENTS);
    checkArgument(content != null, "Missing %s", PROP_CONTENT);
    checkArgument(contentDirection != null, "Missing %s", PROP_CONTENT_DIRECTION);
    checkArgument(contentNegate != null, "Missing %s", PROP_CONTENT_NEGATE);
    checkArgument(destination != null, "Missing %s", PROP_DESTINATION);
    checkArgument(destinationNegate != null, "Missing %s", PROP_DESTINATION_NEGATE);
    checkArgument(enabled != null, "Missing %s", PROP_ENABLED);
    checkArgument(installOn != null, "Missing %s", PROP_INSTALL_ON);
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(ruleNumber != null, "Missing %s", PROP_RULE_NUMBER);
    checkArgument(service != null, "Missing %s", PROP_SERVICE);
    checkArgument(serviceNegate != null, "Missing %s", PROP_SERVICE_NEGATE);
    checkArgument(source != null, "Missing %s", PROP_SOURCE);
    checkArgument(sourceNegate != null, "Missing %s", PROP_SOURCE_NEGATE);
    checkArgument(vpn != null, "Missing %s", PROP_VPN);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new AccessRule(
        action,
        comments,
        content,
        contentDirection,
        contentNegate,
        destination,
        destinationNegate,
        enabled,
        installOn,
        name,
        ruleNumber,
        service,
        serviceNegate,
        source,
        sourceNegate,
        uid,
        vpn);
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
