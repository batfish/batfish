package org.batfish.representation.palo_alto;

import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchBuiltInServiceTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchServiceAnyTraceElement;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.function.Supplier;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.TrueExpr;

public enum ServiceBuiltIn {
  ANY,
  APPLICATION_DEFAULT,
  SERVICE_HTTP,
  SERVICE_HTTPS;

  private final Supplier<HeaderSpace> _serviceHeaderSpace;

  ServiceBuiltIn() {
    _serviceHeaderSpace = Suppliers.memoize(this::init);
  }

  private HeaderSpace init() {
    return switch (this) {
      case SERVICE_HTTP ->
          HeaderSpace.builder()
              .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
              .setDstPorts(ImmutableSortedSet.of(SubRange.singleton(80), new SubRange(8080, 8080)))
              .build();
      case SERVICE_HTTPS ->
          HeaderSpace.builder()
              .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
              .setDstPorts(ImmutableSortedSet.of(SubRange.singleton(443)))
              .build();
      // any and application-default don't match a specific port
      case ANY, APPLICATION_DEFAULT -> null;
    };
  }

  public HeaderSpace getHeaderSpace() {
    return _serviceHeaderSpace.get();
  }

  public AclLineMatchExpr toAclLineMatchExpr() {
    return switch (this) {
      case ANY -> new TrueExpr(matchServiceAnyTraceElement());
      case SERVICE_HTTP, SERVICE_HTTPS ->
          new MatchHeaderSpace(_serviceHeaderSpace.get(), matchBuiltInServiceTraceElement());
      case APPLICATION_DEFAULT ->
          // application-default doesn't provide useful headerspace info
          FalseExpr.INSTANCE;
    };
  }

  public String getName() {
    return switch (this) {
      case ANY -> "any";
      case APPLICATION_DEFAULT -> "application-default";
      case SERVICE_HTTP -> "service-http";
      case SERVICE_HTTPS -> "service-https";
    };
  }
}
