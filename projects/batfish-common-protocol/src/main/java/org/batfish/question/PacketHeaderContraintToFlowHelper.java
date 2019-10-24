package org.batfish.question;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.visitors.IpSpaceRepresentative;
import org.batfish.specifier.AllInterfacesLocationSpecifier;
import org.batfish.specifier.InferFromLocationIpSpaceSpecifier;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;

/**
 * Wrapper for utility functions used to infer src / dst Ips in a flow used by test filter and
 * traceroute
 */
public class PacketHeaderContraintToFlowHelper {

  private final IpSpaceRepresentative _ipSpaceRepresentative;
  private final String _sourceLocationStr;
  private final IpSpaceAssignment _sourceIpAssignment;
  private final SpecifierContext _specifierContext;

  public PacketHeaderContraintToFlowHelper(
      String sourceLocationStr,
      IpSpaceAssignment sourceIpAssignment,
      SpecifierContext specifierContext) {
    _ipSpaceRepresentative = new IpSpaceRepresentative();
    _sourceLocationStr = sourceLocationStr;
    _specifierContext = specifierContext;
    _sourceIpAssignment = sourceIpAssignment;
  }

  public Ip inferSrcIpFromHeaderSrcIp(String headerSrcIp) {
    IpSpaceAssignment srcIps = resolverHeaderIp(headerSrcIp);
    // Filter out empty IP assignments
    List<Entry> nonEmptyIpSpaces =
        srcIps.getEntries().stream()
            .filter(e -> !e.getIpSpace().equals(EmptyIpSpace.INSTANCE))
            .collect(ImmutableList.toImmutableList());
    checkArgument(
        !nonEmptyIpSpaces.isEmpty(),
        "Specified source '%s' could not be resolved to any IP.",
        headerSrcIp);
    checkArgument(
        nonEmptyIpSpaces.size() == 1,
        "Specified source '%s' resolves to more than one location/IP: %s",
        headerSrcIp,
        nonEmptyIpSpaces);
    Optional<Ip> srcIp = pickRepresentativeFromIpSpaceAssignment(srcIps);
    // Extra check to ensure that we actually got an IP
    checkArgument(srcIp.isPresent(), "Specified source '%s' has no IPs", headerSrcIp);
    return srcIp.get();
  }

  public Ip inferSrcIpFromSourceLocation(Location srcLocation) {
    Optional<Entry> entry =
        _sourceIpAssignment.getEntries().stream()
            .filter(e -> e.getLocations().contains(srcLocation))
            .findFirst();
    checkArgument(
        entry.isPresent(),
        "Cannot resolve a source IP address from location %s",
        _sourceLocationStr);
    Optional<Ip> srcIp = _ipSpaceRepresentative.getRepresentative(entry.get().getIpSpace());
    checkArgument(
        srcIp.isPresent(),
        "At least one source IP is required, location %s produced none",
        srcLocation);
    return srcIp.get();
  }

  public IpSpaceAssignment resolverHeaderIp(String headerIp) {
    // interpret given IP "flexibly"
    IpSpaceSpecifier ipSpecifier =
        SpecifierFactories.getIpSpaceSpecifierOrDefault(
            headerIp, InferFromLocationIpSpaceSpecifier.INSTANCE);

    // Resolve to set of locations/IPs
    return ipSpecifier.resolve(ImmutableSet.of(), _specifierContext);
  }

  public Optional<Ip> pickRepresentativeFromIpSpaceAssignment(IpSpaceAssignment ipSpaceAssignment) {
    return _ipSpaceRepresentative.getRepresentative(
        ipSpaceAssignment.getEntries().iterator().next().getIpSpace());
  }
}
