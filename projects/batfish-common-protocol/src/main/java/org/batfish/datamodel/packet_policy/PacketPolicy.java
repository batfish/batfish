package org.batfish.datamodel.packet_policy;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A policy used to perform policy-based routing */
public final class PacketPolicy implements Serializable {
  private static final String PROP_DEFAULT_ACTION = "defaultAction";
  private static final String PROP_NAME = "name";
  private static final String PROP_STATEMENTS = "statements";

  @Nonnull private final String _name;
  @Nonnull private final List<Statement> _statements;
  @Nonnull private final Return _defaultAction;

  public PacketPolicy(String name, List<Statement> statements, Return defaultAction) {
    _name = name;
    _statements = ImmutableList.copyOf(statements);
    _defaultAction = defaultAction;
  }

  @JsonCreator
  private static PacketPolicy jsonCreator(
      @Nullable @JsonProperty(PROP_DEFAULT_ACTION) Return defaultAction,
      @Nullable @JsonProperty(PROP_NAME) String name,
      @Nullable @JsonProperty(PROP_STATEMENTS) List<Statement> statements) {
    checkArgument(name != null, "Missing %s", PROP_DEFAULT_ACTION);
    checkArgument(defaultAction != null, "Missing %s", PROP_NAME);
    return new PacketPolicy(name, firstNonNull(statements, ImmutableList.of()), defaultAction);
  }

  @Nonnull
  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @Nonnull
  @JsonProperty(PROP_STATEMENTS)
  public List<Statement> getStatements() {
    return _statements;
  }

  /** Return the default action for this policy */
  @Nonnull
  @JsonProperty(PROP_DEFAULT_ACTION)
  public Return getDefaultAction() {
    return _defaultAction;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PacketPolicy that = (PacketPolicy) o;
    return Objects.equals(getStatements(), that.getStatements())
        && Objects.equals(getName(), that.getName())
        && Objects.equals(getDefaultAction(), that.getDefaultAction());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getStatements(), getName(), getDefaultAction());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("name", _name).toString();
  }
}
