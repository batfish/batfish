package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Map.Entry;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;

/**
 * A {@link LocationSpecifier} specifying all {@link Location Locations} marked as a {@link
 * LocationInfo#isSource() source}.
 */
public final class AllActiveSources implements LocationSpecifier {
  public static final AllActiveSources ALL_ACTIVE_SOURCES = new AllActiveSources();

  private AllActiveSources() {}

  private static final class IsActiveLocationVisitor implements LocationVisitor<Boolean> {

    private final SpecifierContext _specifierContext;

    public IsActiveLocationVisitor(SpecifierContext ctxt) {
      _specifierContext = ctxt;
    }

    private boolean isActiveInterface(String hostname, String ifaceName) {
      Configuration config = _specifierContext.getConfigs().get(hostname);
      if (config == null) {
        return false;
      }
      Interface iface = config.getAllInterfaces().get(ifaceName);
      return iface != null && iface.getActive();
    }

    @Override
    public Boolean visitInterfaceLinkLocation(InterfaceLinkLocation loc) {
      return isActiveInterface(loc.getNodeName(), loc.getInterfaceName());
    }

    @Override
    public Boolean visitInterfaceLocation(InterfaceLocation loc) {
      return isActiveInterface(loc.getNodeName(), loc.getInterfaceName());
    }
  }

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    IsActiveLocationVisitor isActiveLocationVisitor = new IsActiveLocationVisitor(ctxt);
    return ctxt.getLocationInfo().entrySet().stream()
        .filter(entry -> entry.getValue().isSource())
        .filter(entry -> isActiveLocationVisitor.visit(entry.getKey()))
        .map(Entry::getKey)
        .collect(ImmutableSet.toImmutableSet());
  }
}
