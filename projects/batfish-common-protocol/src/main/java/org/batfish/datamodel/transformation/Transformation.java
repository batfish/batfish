package org.batfish.datamodel.transformation;

import static java.util.Comparator.nullsFirst;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IpAccessList;

// TODO javadoc
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class Transformation implements Serializable, Comparable<Transformation> {

  static final String PROP_ACL = "acl";
  static final String PROP_ACTION = "action";
  static final String PROP_DESCRIPTION = "description";
  private static final long serialVersionUID = 1L;
  final @Nullable IpAccessList _acl;
  final RuleAction _action;
  final @Nullable String _description;

  Transformation(
      @Nullable IpAccessList acl,
      @Nonnull RuleAction action,
      @Nullable String description) {
    _acl = acl;
    _action = action;
    _description = description;
  }

  @Nullable
  public abstract <R> R accept(GenericTransformationRuleVisitor<R> visitor);

  @Override
  public final int compareTo(@Nonnull Transformation o) {
    if (this == o) {
      return 0;
    }
    //TODO check this
    return Comparator.comparing((Transformation t) -> t.getClass().getSimpleName())
        .thenComparing(Transformation::getAcl, nullsFirst(Comparator.naturalOrder()))
        .thenComparing(Transformation::getAction)
        .thenComparing(Transformation::getDescription, nullsFirst(Comparator.naturalOrder()))
        .thenComparing(this::compareSameClass)
        .compare(this, o);
  }

  protected abstract int compareSameClass(Transformation o);

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(getClass() == o.getClass())) {
      return false;
    }
    Transformation rhs = (Transformation) o;
    return Objects.equals(_acl, rhs._acl)
        && Objects.equals(_action, rhs._action)
        && Objects.equals(_description, rhs._description)
        && transformEquals(rhs);
  }

  @JsonProperty(PROP_ACL)
  @Nullable
  public IpAccessList getAcl() {
    return _acl;
  }

  @JsonProperty(PROP_ACTION)
  @Nonnull
  public RuleAction getAction() {
    return _action;
  }

  @JsonProperty(PROP_DESCRIPTION)
  @Nullable
  public String getDescription() {
    return _description;
  }

  @Override
  public abstract int hashCode();

  @Override
  public abstract String toString();

  abstract boolean transformEquals(Transformation o);

  public enum Direction {
    /** inside -> outside */
    EGRESS,
    /** outside-> inside */
    INGRESS
  }

  public enum RuleAction {
    SOURCE_INSIDE,
    SOURCE_OUTSIDE,
    DESTINATION_INSIDE
  }
}
