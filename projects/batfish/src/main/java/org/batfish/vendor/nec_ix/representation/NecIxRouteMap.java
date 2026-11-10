package org.batfish.vendor.nec_ix.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.bgp.community.StandardCommunity;

/** A {@code route-map}. Entries are evaluated in sequence-number order. */
public final class NecIxRouteMap implements Serializable {

  /** One {@code route-map <name> {permit|deny} <seq>} entry. */
  public static final class Entry implements Serializable {
    private final int _sequence;
    private final @Nonnull LineAction _action;
    private final @Nonnull List<String> _matchPrefixLists;
    private final @Nonnull List<Long> _asPathPrepend;
    private @Nullable List<StandardCommunity> _setCommunities;
    private boolean _setCommunitiesAdditive;
    private @Nullable Long _setLocalPreference;
    private @Nullable Long _setMetric;

    public Entry(int sequence, LineAction action) {
      _sequence = sequence;
      _action = action;
      _matchPrefixLists = new ArrayList<>();
      _asPathPrepend = new ArrayList<>();
    }

    public int getSequence() {
      return _sequence;
    }

    public @Nonnull LineAction getAction() {
      return _action;
    }

    /** Prefix-lists from {@code match ip address prefix-list}; a route matching any of them. */
    public @Nonnull List<String> getMatchPrefixLists() {
      return _matchPrefixLists;
    }

    /** AS numbers from {@code set as-path prepend}, in the order written. */
    public @Nonnull List<Long> getAsPathPrepend() {
      return _asPathPrepend;
    }

    public @Nullable List<StandardCommunity> getSetCommunities() {
      return _setCommunities;
    }

    public boolean getSetCommunitiesAdditive() {
      return _setCommunitiesAdditive;
    }

    public void setSetCommunities(List<StandardCommunity> communities, boolean additive) {
      _setCommunities = communities;
      _setCommunitiesAdditive = additive;
    }

    public @Nullable Long getSetLocalPreference() {
      return _setLocalPreference;
    }

    public void setSetLocalPreference(@Nullable Long setLocalPreference) {
      _setLocalPreference = setLocalPreference;
    }

    public @Nullable Long getSetMetric() {
      return _setMetric;
    }

    public void setSetMetric(@Nullable Long setMetric) {
      _setMetric = setMetric;
    }
  }

  private final @Nonnull String _name;
  private final @Nonnull SortedMap<Integer, Entry> _entries;

  public NecIxRouteMap(String name) {
    _name = name;
    _entries = new TreeMap<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  /** Entries keyed by sequence number. */
  public @Nonnull SortedMap<Integer, Entry> getEntries() {
    return _entries;
  }
}
