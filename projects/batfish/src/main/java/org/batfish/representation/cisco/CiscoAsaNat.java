package org.batfish.representation.cisco;

import static org.batfish.representation.cisco.CiscoAsaNatUtil.dynamicTransformation;
import static org.batfish.representation.cisco.CiscoAsaNatUtil.secondTransformation;
import static org.batfish.representation.cisco.CiscoAsaNatUtil.staticTransformation;

import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Transformation;

@ParametersAreNonnullByDefault
public final class CiscoAsaNat implements Comparable<CiscoAsaNat>, Serializable {
  public static final String ANY_INTERFACE = "any";
  private static final long serialVersionUID = 1L;
  /** If true, this NAT rule is inactive and will not be used. */
  private boolean _inactive;
  /**
   * NATs optionally specify an inside, or 'real', interface. Interface is null if unspecified or
   * specified as 'any'.
   */
  private String _insideInterface;
  /** Indicates the order in which this NAT was specified in the configuration */
  private int _line;
  /**
   * NATs optionally specify an outside, or 'mapped', interface. Interface is null if unspecified or
   * specified as 'any'.
   */
  private String _outsideInterface;
  /**
   * NATs specify 'any' address, a network object, or a network object-group as the real address of
   * sources for inside-to-outside flows.
   */
  private AccessListAddressSpecifier _realSource;
  /**
   * Object NATs depend on the actual network object that they were declared in for sorting. This
   * network object must have a valid start (and end) IP.
   */
  private NetworkObject _realSourceObject;
  /**
   * NATs are sorted into sections. There are three sections (in decreasing order of precedence):
   * BEFORE, OBJECT, and AFTER. Twice NATs are in BEFORE by default and can be configured to be in
   * AFTER. Object NATs are always in the OBJECT section.
   */
  private Section _section;
  /**
   * If this NAT has an optional destination transformation, it is configured with 'any' address, a
   * network object, a network object-group, or 'interface' as the translated mapping of
   * destinations for inside-to-outside flows. Interface NAT is not supported. Value is null if this
   * NAT does not have a destination transformation or if interface NAT was configured.
   */
  private AccessListAddressSpecifier _mappedDestination;
  /**
   * NATs specify 'any' address, a network object, a network object-group, 'interface', a PAT pool,
   * or an inline IP as the translated mapping of sources for inside-to-outside flows. PAT and
   * interface NAT are not supported. Value is null if interface NAT or a PAT pool was configured.
   */
  private AccessListAddressSpecifier _mappedSource;
  /**
   * If this NAT has an optional destination transformation, it is configured with 'any' address, a
   * network object, or a network object-group as the real address of destinations for
   * inside-to-outside flows. Value is null if this NAT does not have a destination transformation.
   */
  private AccessListAddressSpecifier _realDestination;
  /** Whether this NAT includes a destination NAT */
  private boolean _twice;
  /** Whether this NAT is dynamic or static */
  private boolean _dynamic;

  public boolean getDynamic() {
    return _dynamic;
  }

  public void setDynamic(boolean dynamic) {
    _dynamic = dynamic;
  }

  public boolean getInactive() {
    return _inactive;
  }

  public void setInactive(boolean inactive) {
    _inactive = inactive;
  }

  public String getInsideInterface() {
    return _insideInterface;
  }

  public void setInsideInterface(String insideInterface) {
    _insideInterface = insideInterface;
  }

  public int getLine() {
    return _line;
  }

  public void setLine(int line) {
    _line = line;
  }

  public AccessListAddressSpecifier getMappedDestination() {
    return _mappedDestination;
  }

  public void setMappedDestination(AccessListAddressSpecifier mappedDestination) {
    _mappedDestination = mappedDestination;
  }

  public AccessListAddressSpecifier getMappedSource() {
    return _mappedSource;
  }

  public void setMappedSource(AccessListAddressSpecifier mappedSource) {
    _mappedSource = mappedSource;
  }

  public String getOutsideInterface() {
    return _outsideInterface;
  }

  public void setOutsideInterface(String outsideInterface) {
    _outsideInterface = outsideInterface;
  }

  public AccessListAddressSpecifier getRealDestination() {
    return _realDestination;
  }

  public void setRealDestination(AccessListAddressSpecifier realDestination) {
    _realDestination = realDestination;
  }

  public AccessListAddressSpecifier getRealSource() {
    return _realSource;
  }

  public NetworkObject getRealSourceObject() {
    return _realSourceObject;
  }

  public void setRealSource(AccessListAddressSpecifier realSource) {
    _realSource = realSource;
  }

  void setRealSourceObject(NetworkObject networkObject) {
    _realSourceObject = networkObject;
  }

  public Section getSection() {
    return _section;
  }

  public void setSection(Section section) {
    _section = section;
  }

  public boolean getTwice() {
    return _twice;
  }

  public void setTwice(boolean twice) {
    _twice = twice;
  }

  @Override
  public int compareTo(CiscoAsaNat o) {
    // Different sections have different sorting rules
    if (_section != o._section) {
      return Comparator.comparing(CiscoAsaNat::getSection).compare(this, o);
    }
    /*
     * Twice NATs are in the BEFORE or AFTER section. Within those sections, they are sorted
     * by line, where line is the order they were specified in the configuration.
     */
    if (_section == Section.BEFORE || _section == Section.AFTER) {
      return Integer.compare(_line, o._line);
    }
    if (_section == Section.OBJECT) {
      // Within the object NAT section, static is sorted before dynamic.
      if (_dynamic != o._dynamic) {
        return _dynamic ? 1 : -1;
      }

      /*
       * Object NATs of the same type are finally sorted by properties of their object:
       * 1) # of addresses (ascending)
       * 2) Start address (ascending)
       * 3) Object name (alphabetical)
       */
      return Comparator.<NetworkObject>comparingLong(
              obj -> obj.getEnd().asLong() - obj.getStart().asLong())
          .thenComparing(NetworkObject::getStart)
          .thenComparing(NetworkObject::getName)
          .compare(_realSourceObject, o._realSourceObject);
    }
    throw new BatfishException("Unexpected Section");
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CiscoAsaNat)) {
      return false;
    }
    CiscoAsaNat other = (CiscoAsaNat) o;
    return _dynamic == other._dynamic
        && _inactive == other._inactive
        && Objects.equals(_insideInterface, other._insideInterface)
        && _line == other._line
        && Objects.equals(_mappedDestination, other._mappedDestination)
        && Objects.equals(_mappedSource, other._mappedSource)
        && Objects.equals(_outsideInterface, other._outsideInterface)
        && Objects.equals(_realDestination, other._realDestination)
        && Objects.equals(_realSource, other._realSource)
        && Objects.equals(_realSourceObject, other._realSourceObject)
        && Objects.equals(_section, other._section)
        && _twice == other._twice;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _dynamic,
        _inactive,
        _insideInterface,
        _line,
        _mappedDestination,
        _mappedSource,
        _outsideInterface,
        _realDestination,
        _realSource,
        _realSourceObject,
        _section.ordinal(),
        _twice);
  }

  @VisibleForTesting
  public Optional<Transformation.Builder> toIncomingTransformation(
      Map<String, NetworkObject> networkObjects, Warnings w) {
    return toTransformation(false, networkObjects, w);
  }

  /*
   * Included for testing, but not integrated with data plane. ASA NATs aren't compatible yet with
   * the ACL/routing sequence.
   *
   * First, some background. ASA NATs optionally specify a real and a mapped interface, which
   * correspond to "inside" and "outside" interfaces. NAT rules can match both source and
   * destination and transform both source and destination. Only one NAT rule applies for any given
   * packet.
   *
   * 1) ASA NATs can optionally circumvent routing for packets that match transformation. For
   *    example, if a packet would otherwise be routed to 'DMZ' but matches a rule which specifies
   *    'outside' as the mapped interface, the packet would be diverted to 'outside'. "Identity NAT"
   *    does not transform packets but can be used to impose policy routing. Identity NAT rules
   *    which specify both real and mapped interfaces can optionally require route lookups anyway,
   *    which can be useful for management traffic.
   * 2) Both inbound and outbound ACLs match against packets using the original source address and
   *    the post-NAT destination address.
   * 3) When the NAT rule des not specify a mapped interface, the routing table is used. The routing
   *    lookup uses the post-NAT destination address.
   * 4) The NAT rules match based on the original source and destination.
   * 5) When the NAT rule specifies an interface, the packet will be dropped unless there is a route
   *    to the post-NAT destination out of that interface. This implies a RIB check rather than a
   *    FIB check.
   *    "Only routes pointing out the egress interface are eligible"
   *    https://clnv.s3.amazonaws.com/2018/usa/pdf/BRKSEC-3020.pdf
   *
   * Not necessarily in chronological order, the ASA packet flow when considering twice NATs:
   * - Receive packet with original source and original destination
   * - Choose a single source and destination transformation based on original source and original
   *   destination
   * - Check inbound ACL using original source and transformed destination
   * - Route based on transformed destination or chosen transformation
   * - Drop packets which are sent by a NAT rule to an interface which does not have a route for the
   *   transformed destination.
   * - Check outbound ACL using original source and transformed destination
   * - Transmit packet with transformed source and destination
   *
   * See https://github.com/batfish/batfish/issues/3005
   *
   * P.S. Besides twice NATs, there are Object NATs. Object NATs are source-only (or
   * destination-only in reverse). If a twice NAT is chosen, Object NATS are ignored. If an object
   * NAT is chosen, it is possible to match a source NAT and a destination NAT for the same packet.
   * Object NATs do not divert packets in reverse, only forward.
   */
  private Optional<Transformation.Builder> toTransformation(
      boolean outgoing, Map<String, NetworkObject> networkObjects, Warnings w) {

    if (!outgoing && _dynamic) {
      return Optional.empty();
    }

    AccessListAddressSpecifier matchSrc;
    AccessListAddressSpecifier assignOrShiftSrc;
    AccessListAddressSpecifier matchDestination;
    AccessListAddressSpecifier shiftDestination;
    String insideInterface;
    IpField firstField;
    IpField secondField;
    if (outgoing) {
      // Outgoing transformations match the real source and mapped destination and transform into
      // the mapped source and real destination
      matchSrc = _realSource;
      assignOrShiftSrc = _mappedSource;
      matchDestination = _mappedDestination;
      shiftDestination = _realDestination;
      insideInterface = _insideInterface;
      firstField = IpField.SOURCE;
      secondField = IpField.DESTINATION;
    } else {
      // Incoming transformations match the mapped source and real destination and transform into
      // the real source and mapped destination
      matchSrc = _mappedSource;
      matchDestination = _realDestination;
      shiftDestination = _mappedDestination;
      assignOrShiftSrc = _realSource;
      insideInterface = null;
      firstField = IpField.DESTINATION;
      secondField = IpField.SOURCE;
    }

    /*
     * All NAT rules start with an outbound source transformation which matches realSource and
     * translates to mappedSource. The reverse of this transformation is a destination
     * transformation matches mappedSource and translated into realSource.
     */
    Transformation.Builder firstTransformationBuilder =
        _dynamic
            ? dynamicTransformation(matchSrc, assignOrShiftSrc, insideInterface, networkObjects, w)
            : staticTransformation(
                matchSrc, assignOrShiftSrc, insideInterface, networkObjects, firstField, w);
    if (!_twice || firstTransformationBuilder == null) {
      return Optional.ofNullable(firstTransformationBuilder);
    }

    // NAT rules can optionally have an outbound destination transformation which is always static.
    return secondTransformation(
        shiftDestination,
        matchDestination,
        firstTransformationBuilder.build(),
        networkObjects,
        secondField,
        w);
  }

  @VisibleForTesting
  public Optional<Transformation.Builder> toOutgoingTransformation(
      Map<String, NetworkObject> networkObjects, Warnings w) {
    return toTransformation(true, networkObjects, w);
  }

  public enum Section {
    BEFORE,
    OBJECT,
    AFTER
  }
}
