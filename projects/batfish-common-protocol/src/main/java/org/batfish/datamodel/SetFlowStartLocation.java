package org.batfish.datamodel;

import java.util.Map;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationVisitor;

/**
 * Sets the start location fields {@link Flow.Builder#setIngressNode(String)}, {@link
 * Flow.Builder#setIngressVrf(String)}, {@link Flow.Builder#setIngressInterface(String)} of an input
 * {@link Flow.Builder} according to an input {@link Location}.
 */
public final class SetFlowStartLocation {
  private SetFlowStartLocation() {}

  private static final class Visitor implements LocationVisitor<Void> {
    final Map<String, Configuration> _configs;
    final Flow.Builder _builder;

    private Visitor(Map<String, Configuration> configs, Builder builder) {
      _configs = configs;
      _builder = builder;
    }

    @Override
    public Void visitInterfaceLinkLocation(InterfaceLinkLocation loc) {
      _builder
          .setIngressNode(loc.getNodeName())
          .setIngressInterface(loc.getInterfaceName())
          .setIngressVrf(null);
      return null;
    }

    @Override
    public Void visitInterfaceLocation(InterfaceLocation loc) {
      _builder
          .setIngressNode(loc.getNodeName())
          .setIngressInterface(null)
          .setIngressVrf(
              _configs
                  .get(loc.getNodeName())
                  .getAllInterfaces()
                  .get(loc.getInterfaceName())
                  .getVrfName());
      return null;
    }
  }

  public static void setStartLocation(
      Map<String, Configuration> configs, Flow.Builder builder, Location startLocation) {
    new Visitor(configs, builder).visit(startLocation);
  }
}
