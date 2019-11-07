package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableSet;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;

public class ServiceObjectGroup extends ObjectGroup {
  public enum ServiceProtocol {
    TCP,
    UDP,
    TCP_UDP
  }

  private final List<ServiceObjectGroupLine> _lines;
  @Nullable private ServiceProtocol _protocol;

  public ServiceObjectGroup(String name, @Nullable ServiceProtocol protocol) {
    super(name);
    _protocol = protocol;
    _lines = new LinkedList<>();
  }

  public List<ServiceObjectGroupLine> getLines() {
    return _lines;
  }

  @Nullable
  public ServiceProtocol getProtocol() {
    return _protocol;
  }

  public AclLineMatchExpr toAclLineMatchExpr() {
    return new OrMatchExpr(
        _lines.stream()
            .map(ServiceObjectGroupLine::toAclLineMatchExpr)
            .collect(ImmutableSet.toImmutableSet()));
  }
}
