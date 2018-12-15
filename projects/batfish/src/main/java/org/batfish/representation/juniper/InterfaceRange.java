package org.batfish.representation.juniper;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a Juniper interface range */
@ParametersAreNonnullByDefault
public class InterfaceRange implements Serializable {

  private static final long serialVersionUID = 1L;

  private final List<InterfaceRangeMemberRange> _memberRanges;

  private final List<InterfaceRangeMember> _members;

  private final String _name;

  /**
   * If more properties are added below, make sure that they are inherited in {@link
   * LogicalSystem#expandInterfaceRangeInterface(InterfaceRange, Interface)}
   */

  // Dumb name to appease checkstyle
  private String _agg8023adInterface;

  private String _description;

  private Integer _mtu;

  @Nullable private String _redundantParentInterface;

  public InterfaceRange(String name) {
    _name = name;
    _members = new LinkedList<>();
    _memberRanges = new LinkedList<>();
  }

  public Set<String> getAllMembers() {
    Set<String> retSet = new TreeSet<>();
    _memberRanges.forEach(r -> retSet.addAll(r.getAllMembers()));
    _members.forEach(m -> retSet.addAll(m.getAllMembers()));
    return retSet;
  }

  static String toInterfaceId(String type, int fpc, int pic, int port) {
    return String.format("%s-%d/%d/%d", type, fpc, pic, port);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof InterfaceRange)) {
      return false;
    }
    InterfaceRange rhs = (InterfaceRange) o;
    return Objects.equals(_name, rhs._name)
        && Objects.equals(_memberRanges, rhs._memberRanges)
        && Objects.equals(_members, rhs._members)
        && Objects.equals(_agg8023adInterface, rhs._agg8023adInterface)
        && Objects.equals(_description, rhs._description)
        && Objects.equals(_mtu, rhs._mtu)
        && Objects.equals(_redundantParentInterface, rhs._redundantParentInterface);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _name,
        _memberRanges,
        _members,
        _agg8023adInterface,
        _description,
        _mtu,
        _redundantParentInterface);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add("name", _name)
        .add("memberRanges", _memberRanges)
        .add("members", _members)
        .add("8023ad", _agg8023adInterface)
        .add("desc", _description)
        .add("mtu", _mtu)
        .add("redundantParent", _redundantParentInterface)
        .toString();
  }

  /** getters and setters are below */
  public String get8023adInterface() {
    return _agg8023adInterface;
  }

  public String getDescription() {
    return _description;
  }

  public List<InterfaceRangeMemberRange> getMemberRanges() {
    return _memberRanges;
  }

  public List<InterfaceRangeMember> getMembers() {
    return _members;
  }

  public Integer getMtu() {
    return _mtu;
  }

  public String getName() {
    return _name;
  }

  @Nullable
  public String getRedundantParentInterface() {
    return _redundantParentInterface;
  }

  public void set8023adInterface(@Nullable String interfaceName) {
    _agg8023adInterface = interfaceName;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setMtu(Integer mtu) {
    _mtu = mtu;
  }

  public void setRedundantParentInterface(@Nullable String redundantParentInterface) {
    _redundantParentInterface = redundantParentInterface;
  }
}
