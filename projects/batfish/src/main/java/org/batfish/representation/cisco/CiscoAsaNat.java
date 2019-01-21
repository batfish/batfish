package org.batfish.representation.cisco;

import static org.batfish.representation.cisco.CiscoAsaNatUtil.dynamicTransformation;
import static org.batfish.representation.cisco.CiscoAsaNatUtil.secondTransformation;
import static org.batfish.representation.cisco.CiscoAsaNatUtil.staticTransformation;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Transformation;

@ParametersAreNonnullByDefault
public final class CiscoAsaNat implements Comparable<CiscoAsaNat>, Serializable {
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
   * NAT's specify 'any' address, a network object, or a network object-group as the real address of
   * sources for inside-to-outside flows.
   */
  private AccessListAddressSpecifier _realSource;
  /**
   * NAT's are sorted first by sections. Twice NATs are either in section 1 or 3 and Object NATs are
   * in section 2.
   */
  private Section _section;
  /**
   * NAT's optionally specify 'any' address, a network object, or a network object-group as the
   * translated mapping of destinations for inside-to-outside flows.
   */
  private AccessListAddressSpecifier _mappedDestination;
  /**
   * NAT's optionally specify 'any' address, a network object, or a network object-group as the
   * translated mapping of sources for inside-to-outside flows.
   */
  private AccessListAddressSpecifier _mappedSource;
  /**
   * NAT's specify 'any' address, a network object, or a network object-group as the real address of
   * sources for inside-to-outside flows.
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

  public void setRealSource(AccessListAddressSpecifier realSource) {
    _realSource = realSource;
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
    if (_section == Section.OBJECT && o._section == Section.OBJECT) {
      // Object NAT not implemented
      return 0;
    }
    return Comparator.comparing(CiscoAsaNat::getSection)
        .thenComparingInt(CiscoAsaNat::getLine)
        .compare(this, o);
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
        _section.ordinal(),
        _twice);
  }

  /*
   * Included for testing, but not integrated with data plane. ASA NATs aren't compatible yet with
   * the ACL/routing sequence.
   */
  public Optional<Transformation.Builder> toTransformationTest(
      boolean outgoing, Map<String, NetworkObject> networkObjects) {

    // Incoming flow for dynamic NAT is not supported
    if (!outgoing && _dynamic) {
      return Optional.empty();
    }

    AccessListAddressSpecifier matchSrc;
    AccessListAddressSpecifier translateSrc;
    AccessListAddressSpecifier matchDestination;
    AccessListAddressSpecifier shiftDestination;
    String insideInterface;
    IpField firstField;
    IpField secondField;
    if (outgoing) {
      // Outgoing transformations match the real source and mapped destination and transform into
      // the mapped source and real destination
      matchSrc = _realSource;
      translateSrc = _mappedSource;
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
      translateSrc = _realSource;
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
            ? dynamicTransformation(matchSrc, translateSrc, insideInterface, networkObjects)
            : staticTransformation(
                matchSrc, translateSrc, insideInterface, networkObjects, firstField);
    if (!_twice || firstTransformationBuilder == null) {
      return Optional.ofNullable(firstTransformationBuilder);
    }

    // NAT rules can optionally have an outbound destination transformation which is always static.
    return secondTransformation(
        shiftDestination,
        matchDestination,
        firstTransformationBuilder.build(),
        networkObjects,
        secondField);
  }

  public enum Section {
    BEFORE,
    OBJECT,
    AFTER
  }
}
