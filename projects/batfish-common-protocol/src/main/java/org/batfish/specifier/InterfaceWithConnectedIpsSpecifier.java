package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.BDDUtils;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

/** An {@link InterfaceSpecifier} that resolves interfaces connected to a given {@link IpSpace}. */
public final class InterfaceWithConnectedIpsSpecifier implements InterfaceSpecifier {
  @Nonnull private final IpSpace _ipSpace;
  @Nonnull private final IpSpaceToBDD _ipSpaceToBdd;
  @Nonnull private final BDD _ipSpaceBdd;
  public static final String NAME = InterfaceWithConnectedIpsSpecifier.class.getName();

  /**
   * Creates an {@link InterfaceWithConnectedIpsSpecifier} that resolves to interfaces connected to
   * networks overlapping the specified {@link IpSpace}.
   */
  public InterfaceWithConnectedIpsSpecifier(@Nonnull IpSpace ipSpace) {
    _ipSpace = ipSpace;
    BDDFactory factory = BDDUtils.bddFactory(Prefix.MAX_PREFIX_LENGTH);
    BDDInteger integer = BDDInteger.makeFromIndex(factory, Prefix.MAX_PREFIX_LENGTH, 0, true);
    _ipSpaceToBdd = new IpSpaceToBDD(factory, integer);
    _ipSpaceBdd = _ipSpaceToBdd.visit(ipSpace);
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
    return !_ipSpaceBdd.and(_ipSpaceToBdd.toBDD(i.getPrefix())).isZero();
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
