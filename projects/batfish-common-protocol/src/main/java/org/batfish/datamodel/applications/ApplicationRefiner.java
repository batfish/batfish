package org.batfish.datamodel.applications;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.SubRange;

/** Class for refining underspecified applications */
public final class ApplicationRefiner implements ApplicationVisitor<List<Application>> {
  public static List<Application> refine(
      List<Application> underspecifiedApplication, int maxSuggestionNumber) {
    return underspecifiedApplication.stream()
        .findFirst()
        .map(app -> app.accept(INSTANCE))
        .orElse(ImmutableList.of());
  }

  private static ApplicationRefiner INSTANCE = new ApplicationRefiner();

  private ApplicationRefiner() {}

  @Override
  public List<Application> visitTcpApplication(TcpApplication app) {
    Integer port = app.getPorts().stream().findFirst().map(SubRange::getStart).orElse(null);
    return port == null ? ImmutableList.of() : ImmutableList.of(new TcpApplication(port));
  }

  @Override
  public List<Application> visitUdpApplication(UdpApplication app) {
    Integer port = app.getPorts().stream().findFirst().map(SubRange::getStart).orElse(null);
    return port == null ? ImmutableList.of() : ImmutableList.of(new UdpApplication(port));
  }

  @Override
  public List<Application> visitIcmpTypesApplication(IcmpTypesApplication app) {
    Integer type = app.getTypes().stream().findFirst().map(SubRange::getStart).orElse(null);
    return type == null
        ? ImmutableList.of()
        : ImmutableList.of(new IcmpTypeCodesApplication(type, 0));
  }

  @Override
  public List<Application> visitIcmpTypeCodesApplication(IcmpTypeCodesApplication app) {
    Integer code = app.getCodes().stream().findFirst().map(SubRange::getStart).orElse(null);
    return code == null
        ? ImmutableList.of()
        : ImmutableList.of(new IcmpTypeCodesApplication(app.getType(), code));
  }
}
