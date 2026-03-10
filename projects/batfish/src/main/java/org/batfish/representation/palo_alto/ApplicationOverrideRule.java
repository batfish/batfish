package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNullableByDefault;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;

/** PAN datamodel component containing application-override rule configuration */
@ParametersAreNullableByDefault
public final class ApplicationOverrideRule implements Serializable {

  // Possible protocols for traffic to match an application-override rule
  public enum Protocol {
    TCP,
    UDP,
    UNSPECIFIED
  }

  // Name of the rule
  private final @Nonnull String _name;

  // Description of the rule
  private @Nullable String _description;

  // Application used for traffic matching this rule
  private @Nullable ApplicationOrApplicationGroupReference _application;

  private boolean _disabled;

  // Zones to match
  private final @Nonnull SortedSet<String> _from;
  private final @Nonnull SortedSet<String> _to;

  // IPs to match
  private final @Nonnull List<RuleEndpoint> _source;
  private final @Nonnull List<RuleEndpoint> _destination;
  private boolean _negateSource;
  private boolean _negateDestination;

  // Traffic characteristics to match
  private @Nonnull Protocol _protocol;
  private @Nonnull IntegerSpace _port;

  private final @Nonnull Set<String> _tags;

  public ApplicationOverrideRule(@Nonnull String name) {
    _destination = new LinkedList<>();
    _negateDestination = false;
    _source = new LinkedList<>();
    _negateSource = false;
    _disabled = false;
    _from = new TreeSet<>();
    _to = new TreeSet<>();
    _tags = new HashSet<>(1);
    _name = name;
    _port = IntegerSpace.EMPTY;
    _protocol = Protocol.UNSPECIFIED;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nullable ApplicationOrApplicationGroupReference getApplication() {
    return _application;
  }

  public boolean getDisabled() {
    return _disabled;
  }

  public @Nonnull SortedSet<String> getFrom() {
    return _from;
  }

  public @Nonnull SortedSet<String> getTo() {
    return _to;
  }

  public @Nonnull List<RuleEndpoint> getSource() {
    return _source;
  }

  public @Nonnull List<RuleEndpoint> getDestination() {
    return _destination;
  }

  public boolean getNegateSource() {
    return _negateSource;
  }

  public boolean getNegateDestination() {
    return _negateDestination;
  }

  public @Nonnull Protocol getProtocol() {
    return _protocol;
  }

  public @Nullable IpProtocol getIpProtocol() {
    return switch (_protocol) {
      case TCP -> IpProtocol.TCP;
      case UDP -> IpProtocol.UDP;
      case UNSPECIFIED -> null;
    };
  }

  public @Nonnull IntegerSpace getPort() {
    return _port;
  }

  public @Nonnull Set<String> getTags() {
    return _tags;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setApplication(@Nonnull String application) {
    _application = new ApplicationOrApplicationGroupReference(application);
  }

  public void setDisabled(boolean disabled) {
    _disabled = disabled;
  }

  public void setNegateSource(boolean negateSource) {
    _negateSource = negateSource;
  }

  public void setNegateDestination(boolean negateDestination) {
    _negateDestination = negateDestination;
  }

  public void setProtocol(@Nonnull Protocol protocol) {
    _protocol = protocol;
  }

  public void addPort(int port) {
    _port = IntegerSpace.builder().including(_port).including(port).build();
  }

  public void addPorts(@Nonnull SubRange ports) {
    _port = IntegerSpace.builder().including(_port).including(ports).build();
  }
}
