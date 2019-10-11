package org.batfish.representation.palo_alto;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;

/** Represents a Palo Alto address group */
public final class AddressGroup implements Serializable {

  /**
   * Type of this address-group. Dynamic groups rely on a filter, which combines tags to determine
   * address objects (or groups) that are part of this group. Static groups have explicitly defined
   * member address objects (or groups). Empty address-groups have neither.
   */
  public enum Type {
    DYNAMIC,
    STATIC
  }

  @Nullable private String _description;

  @Nullable private String _filter;

  private final Set<String> _members;

  private final String _name;

  @Nonnull private final Set<String> _tags;

  @Nullable private Type _type;

  public AddressGroup(String name) {
    _name = name;
    _members = new TreeSet<>();
    _tags = new HashSet<>();
  }

  /**
   * Returns all address objects that are directly or indirectly contained in this group. Accounts
   * for circular group references.
   */
  @VisibleForTesting
  Set<String> getDescendantObjects(
      Map<String, AddressObject> addressObjects,
      Map<String, AddressGroup> addressGroups,
      Set<String> alreadyTraversedGroups) {
    if (alreadyTraversedGroups.contains(_name)) {
      return ImmutableSet.of();
    }
    alreadyTraversedGroups.add(_name);
    if (_type == Type.STATIC) {
      return getStaticDescendantObjects(addressObjects, addressGroups, alreadyTraversedGroups);
    } else if (_type == Type.DYNAMIC) {
      return getDynamicDescendantObjects(addressObjects, addressGroups, alreadyTraversedGroups);
    }
    return ImmutableSet.of();
  }

  /** Remove enclosing single-quotes from the provided text, if applicable. */
  static String unquote(String text) {
    if (text.length() < 2) {
      return text;
    }
    char leading = text.charAt(0);
    char trailing = text.charAt(text.length() - 1);
    if (leading == '\'' && leading == trailing) {
      return text.substring(1, text.length() - 1);
    }
    return text;
  }

  /** Helper to convert simple filter containing tag conjuncts to an array of conjuncts. */
  @VisibleForTesting
  static Set<String> getFilterConjuncts(String filter) {
    return Arrays.stream(filter.split(" and "))
        .map(AddressGroup::unquote)
        .collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Helper to check if the specified object tags match the specified dynamic address-group's filter
   * conjuncts. Note: we only handle basic conjuncts for now, no support for parenthesis or
   * disjuncts.
   */
  private static boolean matchesFilter(Set<String> objectTags, Set<String> filterConjuncts) {
    return objectTags.containsAll(filterConjuncts);
  }

  /** Returns descendant objects for a dynamic address-group. */
  private Set<String> getDynamicDescendantObjects(
      Map<String, AddressObject> addressObjects,
      Map<String, AddressGroup> addressGroups,
      Set<String> alreadyTraversedGroups) {
    // Guaranteed by caller / address-group type is dynamic
    assert _filter != null;
    // Note: we only handle basic conjuncts for now, no support for parenthesis or disjuncts
    Set<String> conjuncts = getFilterConjuncts(_filter);

    Set<String> descendantObjects = new HashSet<>();
    // Check address objects
    for (Entry<String, AddressObject> entry : addressObjects.entrySet()) {
      AddressObject ao = entry.getValue();
      String name = entry.getKey();
      if (matchesFilter(ao.getTags(), conjuncts)) {
        descendantObjects.add(name);
      }
    }

    // Check address-group objects
    for (Entry<String, AddressGroup> entry : addressGroups.entrySet()) {
      AddressGroup ag = entry.getValue();
      if (matchesFilter(ag.getTags(), conjuncts)) {
        descendantObjects.addAll(
            ag.getDescendantObjects(addressObjects, addressGroups, alreadyTraversedGroups));
      }
    }
    return descendantObjects;
  }

  /** Returns descendant objects for a static address-group. */
  private Set<String> getStaticDescendantObjects(
      Map<String, AddressObject> addressObjects,
      Map<String, AddressGroup> addressGroups,
      Set<String> alreadyTraversedGroups) {
    Set<String> descendantObjects = new HashSet<>();
    for (String member : _members) {
      if (addressObjects.containsKey(member)) {
        descendantObjects.add(member);
      } else if (addressGroups.containsKey(member)) {
        descendantObjects.addAll(
            addressGroups
                .get(member)
                .getDescendantObjects(addressObjects, addressGroups, alreadyTraversedGroups));
      }
    }
    return descendantObjects;
  }

  /**
   * Returns the union of IpSpace of all members. Returns {@link EmptyIpSpace} if there are no
   * members
   */
  public IpSpace getIpSpace(
      Map<String, AddressObject> addressObjects, Map<String, AddressGroup> addressGroups) {
    Set<String> descendantObjects =
        getDescendantObjects(addressObjects, addressGroups, new HashSet<>());
    IpSpace space =
        AclIpSpace.union(
            descendantObjects.stream()
                .map(m -> addressObjects.get(m).getIpSpace())
                .collect(Collectors.toSet()));
    return space == null ? EmptyIpSpace.INSTANCE : space;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nullable String getFilter() {
    return _filter;
  }

  /** Add a member to the set of member address objects for this static address-group. */
  public void addMember(String member) {
    if (_type != Type.STATIC) {
      // Handle the case where a dynamic address-group is replaced by a static one
      _type = Type.STATIC;
      _filter = null;
    }
    _members.add(member);
  }

  public Set<String> getMembers() {
    return _members;
  }

  public String getName() {
    return _name;
  }

  @Nonnull
  public Set<String> getTags() {
    return _tags;
  }

  @Nullable
  public Type getType() {
    return _type;
  }

  public void setDescription(String description) {
    _description = description;
  }

  /** Set filter for this dynamic address-group. */
  public void setFilter(String filter) {
    if (_type != Type.DYNAMIC) {
      // Handle the case where a static address-group is replaced by a dynamic one
      _type = Type.DYNAMIC;
      _members.clear();
    }
    _filter = filter;
  }
}
