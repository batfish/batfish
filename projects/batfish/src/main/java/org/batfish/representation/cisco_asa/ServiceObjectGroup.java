package org.batfish.representation.cisco_asa;

import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import com.google.common.collect.ImmutableSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.acl.AclLineMatchExpr;

@ParametersAreNonnullByDefault
public class ServiceObjectGroup extends ObjectGroup {
  public enum ServiceProtocol {
    TCP,
    UDP,
    TCP_UDP
  }

  private final List<ServiceObjectGroupLine> _lines;
  private @Nullable ServiceProtocol _protocol;

  public ServiceObjectGroup(String name, @Nullable ServiceProtocol protocol) {
    super(name);
    _protocol = protocol;
    _lines = new LinkedList<>();
  }

  public List<ServiceObjectGroupLine> getLines() {
    return _lines;
  }

  public @Nullable ServiceProtocol getProtocol() {
    return _protocol;
  }

  public @Nonnull AclLineMatchExpr toAclLineMatchExpr(
      Map<String, ServiceObject> serviceObjects,
      Map<String, ServiceObjectGroup> serviceObjectGroups) {
    return or(
        _lines.stream()
            .map(line -> line.toAclLineMatchExpr(serviceObjects, serviceObjectGroups))
            .collect(ImmutableSet.toImmutableSet()));
  }
}
