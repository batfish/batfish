package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.visitors.GenericIp6SpaceVisitor;

public class Ip6SpaceReference extends Ip6Space {
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_NAME = "name";

  private final @Nullable String _description;

  private final @Nonnull String _name;

  public Ip6SpaceReference(@Nonnull String name) {
    this(name, null);
  }

  /** A reference to a named {@link Ip6Space} */
  public Ip6SpaceReference(
      @JsonProperty(PROP_NAME) @Nonnull String name,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description) {
    _name = name;
    _description = description;
  }

  @Override
  public <R> R accept(GenericIp6SpaceVisitor<R> visitor) {
    return visitor.visitIp6SpaceReference(this);
  }

  @Override
  protected int compareSameClass(Ip6Space o) {
    return _name.compareTo(((Ip6SpaceReference) o)._name);
  }

  @Override
  protected boolean exprEquals(Object o) {
    Ip6SpaceReference rhs = (Ip6SpaceReference) o;
    return _name.equals(rhs._name) && Objects.equals(_description, rhs._description);
  }

  @JsonProperty(PROP_DESCRIPTION)
  public @Nullable String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _description);
  }

  @Nonnull
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_NAME, _name)
        .add(PROP_DESCRIPTION, _description)
        .toString();
  }
}
