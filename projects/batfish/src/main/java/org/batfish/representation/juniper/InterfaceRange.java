package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a Juniper interface range. We inherit from {@code Interface} even though these things
 * are conceptually different so we can share all the fields that the two share.
 */
@ParametersAreNonnullByDefault
public class InterfaceRange extends Interface implements Serializable {

  private final List<InterfaceRangeMemberRange> _memberRanges;

  private final List<InterfaceRangeMember> _members;

  private final String _rangeName;

  public InterfaceRange(String rangeName) {
    super(rangeName);
    _rangeName = rangeName;
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

  public List<InterfaceRangeMemberRange> getMemberRanges() {
    return _memberRanges;
  }

  public List<InterfaceRangeMember> getMembers() {
    return _members;
  }

  public String getRangeName() {
    return _rangeName;
  }
}
