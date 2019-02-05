package org.batfish.representation.palo_alto;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSortedSet;
import java.util.function.Supplier;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;

public enum ServiceBuiltIn implements ServiceGroupMember {
  SERVICE_HTTP,
  SERVICE_HTTPS;

  private final Supplier<ServiceGroupMember> _service;

  ServiceBuiltIn() {
    _service = Suppliers.memoize(this::init);
  }

  private ServiceGroupMember init() {
    switch (this) {
      case SERVICE_HTTP:
        return new Service("service-http", ImmutableSortedSet.of(80, 8080), IpProtocol.TCP);
      case SERVICE_HTTPS:
        return new Service("service-https", ImmutableSortedSet.of(443), IpProtocol.TCP);
      default:
        return null;
    }
  }

  @Override
  public IpAccessList toIpAccessList(LineAction action, PaloAltoConfiguration pc, Vsys vsys) {
    return _service.get().toIpAccessList(action, pc, vsys);
  }

  @Override
  public String getName() {
    return _service.get().getName();
  }
}
