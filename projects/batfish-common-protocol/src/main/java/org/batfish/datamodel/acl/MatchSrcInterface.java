package org.batfish.datamodel.acl;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

public class MatchSrcInterface extends AclLineMatchExpr {
  private static final String PROP_SRC_INTERFACES = "srcInterfaces";
  private static final long serialVersionUID = 1L;

  private final Set<String> _srcInterfaces;

  public MatchSrcInterface(Iterable<String> interfaces) {
    this(interfaces, null);
  }

  @JsonCreator
  public MatchSrcInterface(
      @JsonProperty(PROP_SRC_INTERFACES) @Nullable Iterable<String> interfaces,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description) {
    super(description);
    _srcInterfaces = ImmutableSet.copyOf(firstNonNull(interfaces, ImmutableSet.of()));
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitMatchSrcInterface(this);
  }

  @Override
  protected int compareSameClass(AclLineMatchExpr o) {
    return Comparators.lexicographical(Ordering.<String>natural())
        .compare(_srcInterfaces, ((MatchSrcInterface) o)._srcInterfaces);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return Objects.equals(_srcInterfaces, ((MatchSrcInterface) o)._srcInterfaces);
  }

  @JsonProperty(PROP_SRC_INTERFACES)
  public Set<String> getSrcInterfaces() {
    return _srcInterfaces;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_srcInterfaces);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add(PROP_SRC_INTERFACES, _srcInterfaces)
        .toString();
  }
}
