package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import org.batfish.common.util.CommonUtil;

public class MatchSrcInterface extends AclLineMatchExpr {
  private static final String PROP_SRC_INTERFACES = "srcInterfaces";
  private static final long serialVersionUID = 1L;

  private final Set<String> _srcInterfaces;

  public MatchSrcInterface(@JsonProperty(PROP_SRC_INTERFACES) Iterable<String> interfaces) {
    _srcInterfaces = ImmutableSet.copyOf(interfaces);
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitMatchSrcInterface(this);
  }

  @Override
  protected int compareSameClass(AclLineMatchExpr o) {
    return CommonUtil.compareCollection(_srcInterfaces, ((MatchSrcInterface) o)._srcInterfaces);
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
