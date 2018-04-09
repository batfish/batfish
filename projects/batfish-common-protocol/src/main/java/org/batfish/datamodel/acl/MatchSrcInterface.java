package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;

public class MatchSrcInterface extends AclLineMatchExpr {
  private static final long serialVersionUID = 1L;
  private final Set<String> _srcInterfaces;

  public MatchSrcInterface(Iterable<String> interfaces) {
    _srcInterfaces = ImmutableSet.copyOf(interfaces);
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitMatchSrcInterface(this);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return Objects.equals(_srcInterfaces, ((MatchSrcInterface) o)._srcInterfaces);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_srcInterfaces);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("srcInterfaces", _srcInterfaces).toString();
  }

  public Set<String> getSrcInterfaces() {
    return _srcInterfaces;
  }
}
