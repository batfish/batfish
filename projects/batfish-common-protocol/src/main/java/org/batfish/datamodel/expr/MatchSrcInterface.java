package org.batfish.datamodel.expr;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;

public class MatchSrcInterface extends BooleanExpr {
  private Set<String> _interfaces;

  public MatchSrcInterface(Set<String> interfaces) {
    _interfaces = ImmutableSet.copyOf(interfaces);
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Set<IpAccessList> availableAcls) {
    return _interfaces.contains(srcInterface);
  }

  @Override
  public int hashCode() {
    return _interfaces.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("MatchSrcInterface (");
    String separator = ",";
    _interfaces.forEach(i -> {
      sb.append(i);
      sb.append(separator);
    });
    sb.setLength(sb.length() - separator.length());
    sb.append(")");
    return sb.toString();
  }
}
