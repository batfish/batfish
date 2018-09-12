package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.bdd.BDDIpSpaceSpecializer;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;

/** An {@link InterfaceSpecifier} that resolves interfaces connected to a given {@link IpSpace}. */
public final class InterfaceWithConnectedIpsSpecifier implements InterfaceSpecifier {
  @Nonnull private final IpSpace _ipSpace;
  @Nonnull private final BDDIpSpaceSpecializer _specializer;
  public static final String NAME = InterfaceWithConnectedIpsSpecifier.class.getName();

  /**
   * Creates an {@link InterfaceWithConnectedIpsSpecifier} that resolves to interfaces connected to
   * networks overlapping the specified {@link IpSpace}.
   */
  public InterfaceWithConnectedIpsSpecifier(@Nonnull IpSpace ipSpace) {
    _ipSpace = ipSpace;
    _specializer = new BDDIpSpaceSpecializer(_ipSpace, Collections.emptyMap());
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof InterfaceWithConnectedIpsSpecifier)) {
      return false;
    }
    InterfaceWithConnectedIpsSpecifier other = (InterfaceWithConnectedIpsSpecifier) o;
    return Objects.equals(_ipSpace, other._ipSpace);
  }

  @Override
  public int hashCode() {
    return Objects.hash(InterfaceWithConnectedIpsSpecifier.class, _ipSpace);
  }

  private boolean interfaceAddressMatchesIpSpace(InterfaceAddress i) {
    return !_specializer.specialize(i.getPrefix().toIpSpace()).equals(EmptyIpSpace.INSTANCE);
  }

  @Override
  public Set<Interface> resolve(Set<String> nodes, SpecifierContext ctxt) {
    return ctxt.getConfigs()
        .values()
        .stream()
        .filter(c -> nodes.contains(c.getHostname()))
        .flatMap(c -> c.getAllInterfaces().values().stream().filter(Interface::getActive))
        .filter(i -> i.getAllAddresses().stream().anyMatch(this::interfaceAddressMatchesIpSpace))
        .collect(Collectors.toSet());
  }

  /** Factory for {@link InterfaceWithConnectedIpsSpecifier}. */
  @AutoService(InterfaceSpecifierFactory.class)
  public static class Factory implements InterfaceSpecifierFactory {

    @Override
    public String getName() {
      return NAME;
    }

    @Override
    public InterfaceSpecifier buildInterfaceSpecifier(Object input) {
      checkArgument(
          input instanceof String,
          "%s requires an IP address, prefix, or wildcard provided as a string input, not %s",
          NAME,
          input);
      return new InterfaceWithConnectedIpsSpecifier(new IpWildcard((String) input).toIpSpace());
    }
  }
}
