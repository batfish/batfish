package org.batfish.datamodel.acl;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.datamodel.TraceElement;

public class MatchSrcInterface extends AclLineMatchExpr {
  private static final String PROP_SRC_INTERFACES = "srcInterfaces";

  private final Set<String> _srcInterfaces;

  public MatchSrcInterface(Iterable<String> interfaces) {
    this(interfaces, (TraceElement) null);
  }

  @JsonCreator
  public MatchSrcInterface(
      @JsonProperty(PROP_SRC_INTERFACES) @Nullable Iterable<String> interfaces,
      @JsonProperty(PROP_TRACE_ELEMENT) @Nullable TraceElement traceElement) {
    super(traceElement);
    _srcInterfaces = ImmutableSet.copyOf(firstNonNull(interfaces, ImmutableSet.of()));
  }

  public MatchSrcInterface(@Nullable Iterable<String> interfaces, @Nullable String traceElement) {
    this(interfaces, traceElement == null ? null : TraceElement.of(traceElement));
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitMatchSrcInterface(this);
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
    return Objects.hashCode(_srcInterfaces);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add(PROP_SRC_INTERFACES, _srcInterfaces)
        .toString();
  }
}
