package org.batfish.datamodel.acl;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.LineAction;

public final class DefaultDeniedByIpAccessList implements TerminalTraceEvent {

  private static final String PROP_NAME = "name";

  private static final long serialVersionUID = 1L;

  private static String computeDescription(
      @Nonnull String name, @Nullable String sourceName, @Nullable String sourceType) {
    if (sourceName != null) {
      return String.format("Flow did not match '%s' named '%s'", sourceType, sourceName);
    }
    return String.format("Flow did not match ACL named '%s'", name);
  }

  @JsonCreator
  private static DefaultDeniedByIpAccessList create(
      @JsonProperty(PROP_DESCRIPTION) String description, @JsonProperty(PROP_NAME) String name) {
    return new DefaultDeniedByIpAccessList(requireNonNull(description), requireNonNull(name));
  }

  private final String _description;

  private final String _name;

  public DefaultDeniedByIpAccessList(
      @Nonnull String name, @Nullable String sourceName, @Nullable String sourceType) {
    this(computeDescription(name, sourceName, sourceType), name);
  }

  private DefaultDeniedByIpAccessList(@Nonnull String description, @Nonnull String name) {
    _description = description;
    _name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DefaultDeniedByIpAccessList)) {
      return false;
    }
    DefaultDeniedByIpAccessList rhs = (DefaultDeniedByIpAccessList) obj;
    return _description.equals(rhs._description) && _name.equals(rhs._name);
  }

  @Override
  public String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_description, _name);
  }

  @Override
  public FilterResult toFilterResult() {
    return new FilterResult(null, LineAction.DENY);
  }

  @Override
  public String toString() {
    return _description;
  }
}
