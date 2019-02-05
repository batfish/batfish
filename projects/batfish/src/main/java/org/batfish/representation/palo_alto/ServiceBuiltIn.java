package org.batfish.representation.palo_alto;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.function.Supplier;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;

public enum ServiceBuiltIn {
  SERVICE_HTTP,
  SERVICE_HTTPS;

  private final Supplier<HeaderSpace> _serviceHeaderSpace;
  private final Supplier<String> _serviceName;

  ServiceBuiltIn() {
    _serviceHeaderSpace = Suppliers.memoize(this::init);
    _serviceName = Suppliers.memoize(this::initName);
  }

  private HeaderSpace init() {
    switch (this) {
      case SERVICE_HTTP:
        return HeaderSpace.builder()
            .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
            .setDstPorts(ImmutableSortedSet.of(new SubRange(80, 80), new SubRange(8080, 8080)))
            .build();
      case SERVICE_HTTPS:
        return HeaderSpace.builder()
            .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
            .setDstPorts(ImmutableSortedSet.of(new SubRange(443, 443)))
            .build();
      default:
        return null;
    }
  }

  private String initName() {
    switch (this) {
      case SERVICE_HTTP:
        return "service-http";
      case SERVICE_HTTPS:
        return "service-https";
      default:
        return null;
    }
  }

  public HeaderSpace getHeaderSpace() {
    return _serviceHeaderSpace.get();
  }

  public String getName() {
    return _serviceName.get();
  }
}
