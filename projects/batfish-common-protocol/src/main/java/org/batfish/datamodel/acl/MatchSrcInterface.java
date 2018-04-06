package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;

public class MatchSrcInterface extends AclLineMatchExpr {
  private final Set<String> _srcInterfaces;

  public MatchSrcInterface(Set<String> interfaces) {
    _srcInterfaces = ImmutableSet.copyOf(interfaces);
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitMatchSrcInterface(this);
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Map<String, IpAccessList> availableAcls) {
    return _srcInterfaces.contains(srcInterface);
  }

  @Override
  public boolean exprEquals(Object o) {
    return Objects.equals(_srcInterfaces, ((MatchSrcInterface) o).getSrcInterfaces());
  }

  @Override
  public int hashCode() {
    return Objects.hash(_srcInterfaces);
  }

  @Override
  public String toString() {
    ToStringHelper helper = MoreObjects.toStringHelper(getClass());
    helper.add("srcInterfaces", _srcInterfaces);
    return helper.toString();
  }

  public Set<String> getSrcInterfaces() {
    return _srcInterfaces;
  }
}
