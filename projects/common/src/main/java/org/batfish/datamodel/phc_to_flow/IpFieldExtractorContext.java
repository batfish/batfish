package org.batfish.datamodel.phc_to_flow;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.visitors.IpSpaceRepresentative;
import org.batfish.specifier.InferFromLocationIpSpaceAssignmentSpecifier;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.IpSpaceAssignmentSpecifier;
import org.batfish.specifier.Location;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;

/**
 * Context and utility functions used to infer src / dst Ips from {@link
 * org.batfish.datamodel.PacketHeaderConstraints}.
 */
@ParametersAreNonnullByDefault
public class IpFieldExtractorContext {

  private final IpSpaceRepresentative _ipSpaceRepresentative;
  private final IpSpaceAssignment _sourceIpAssignment;
  private final SpecifierContext _specifierContext;

  private static final String SOURCE = "source";
  private static final String DESTINATION = "destination";

  public IpFieldExtractorContext(
      IpSpaceAssignment sourceIpAssignment, SpecifierContext specifierContext) {
    _ipSpaceRepresentative = new IpSpaceRepresentative();
    _specifierContext = specifierContext;
    _sourceIpAssignment = sourceIpAssignment;
  }

  /** infer source IP from packet header contraints */
  public Ip inferSrcIpFromHeaderSrcIp(String headerSrcIp) {
    return inferIpFromHeaderIpString(headerSrcIp, SOURCE);
  }

  /** infer destination IP from packet header contraints */
  public Ip inferDstIpFromHeaderDstIp(String headerDstIp) {
    return inferIpFromHeaderIpString(headerDstIp, DESTINATION);
  }

  /** infer source IP from source location */
  public Ip inferSrcIpFromSourceLocation(Location srcLocation) {
    Optional<Entry> entry =
        _sourceIpAssignment.getEntries().stream()
            .filter(e -> e.getLocations().contains(srcLocation))
            .findFirst();
    checkArgument(
        entry.isPresent(), "Cannot resolve a source IP address from location %s", srcLocation);
    Optional<Ip> srcIp = _ipSpaceRepresentative.getRepresentative(entry.get().getIpSpace());
    checkArgument(
        srcIp.isPresent(),
        "At least one source IP is required, location %s produced none",
        srcLocation);
    return srcIp.get();
  }

  /** resolve IP by the specifier context */
  private IpSpaceAssignment resolverHeaderIp(String headerIp) {
    // interpret given IP "flexibly"
    IpSpaceAssignmentSpecifier ipSpecifier =
        SpecifierFactories.getIpSpaceAssignmentSpecifierOrDefault(
            headerIp, InferFromLocationIpSpaceAssignmentSpecifier.INSTANCE);

    // Resolve to set of locations/IPs
    return ipSpecifier.resolve(ImmutableSet.of(), _specifierContext);
  }

  /** pick a representative IP from an IP space assignment entry */
  private Optional<Ip> pickRepresentativeFromIpSpaceAssignment(
      IpSpaceAssignment.Entry ipSpaceAssignmentEntry) {
    return _ipSpaceRepresentative.getRepresentative(ipSpaceAssignmentEntry.getIpSpace());
  }

  private Ip inferIpFromHeaderIpString(String headerIp, String field) {
    IpSpaceAssignment ips = resolverHeaderIp(headerIp);
    // Filter out empty IP assignments
    List<Entry> nonEmptyIpSpaces =
        ips.getEntries().stream()
            .filter(e -> !e.getIpSpace().equals(EmptyIpSpace.INSTANCE))
            .collect(ImmutableList.toImmutableList());
    checkArgument(
        !nonEmptyIpSpaces.isEmpty(),
        "Specified '%s' '%s' could not be resolved to any IP.",
        field,
        headerIp);
    checkArgument(
        nonEmptyIpSpaces.size() == 1,
        "Specified '%s' '%s' resolves to more than one location/IP: %s",
        field,
        headerIp,
        nonEmptyIpSpaces);
    Optional<Ip> ip = pickRepresentativeFromIpSpaceAssignment(nonEmptyIpSpaces.iterator().next());
    // Extra check to ensure that we actually got an IP
    checkArgument(ip.isPresent(), "Specified '%s' '%s' has no IPs", field, headerIp);
    return ip.get();
  }
}
