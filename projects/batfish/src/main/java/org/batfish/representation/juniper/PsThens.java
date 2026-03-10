package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents all {@link PsThen} statements in a single {@link PsTerm} */
public final class PsThens implements Serializable {

  /**
   * Incorporates the given {@code then foo} statement in this {@link PsThens}. Returns the list of
   * prior {@code then foo} that may have been cleared or overwritten by this operation.
   */
  public @Nonnull List<String> addPsThen(PsThen then) {
    if (then instanceof PsThenAsPathExpand) {
      return setAsPathExpand((PsThenAsPathExpand) then);
    } else if (then instanceof PsThenAsPathPrepend) {
      return setAsPathPrepend((PsThenAsPathPrepend) then);
    } else if (then instanceof PsThenCommunitySet) {
      return setCommunitySet((PsThenCommunitySet) then);
    } else if (then instanceof PsThenCommunityAdd) {
      return addCommunityAdd((PsThenCommunityAdd) then);
    }
    _others.add(then);
    return ImmutableList.of();
  }

  public @Nonnull Set<PsThen> getAllThens() {
    ImmutableSet.Builder<PsThen> allThens = ImmutableSet.builder();
    if (_asPathPrepend != null) {
      allThens.add(_asPathPrepend);
    }
    if (_asPathExpand != null) {
      allThens.add(_asPathExpand);
    }
    if (_communitySet != null) {
      allThens.add(_communitySet);
    }
    if (!_communityAdds.isEmpty()) {
      allThens.addAll(_communityAdds);
    }
    allThens.addAll(_others);
    return allThens.build();
  }

  public boolean isEmpty() {
    return getAllThens().isEmpty();
  }

  private @Nullable PsThenAsPathExpand _asPathExpand;
  private @Nullable PsThenAsPathPrepend _asPathPrepend;

  private @Nullable PsThenCommunitySet _communitySet;
  private final @Nonnull List<PsThenCommunityAdd> _communityAdds;

  private final @Nonnull List<PsThen> _others;

  PsThens() {
    _communityAdds = new ArrayList<>(0);
    _others = new ArrayList<>(0);
  }

  private @Nonnull List<String> setAsPathExpand(@Nullable PsThenAsPathExpand asPathExpand) {
    List<String> cleared = new ArrayList<>();
    if (_asPathExpand != null) {
      cleared.add("as-path-expand");
    }
    _asPathExpand = asPathExpand;
    return cleared;
  }

  private List<String> setAsPathPrepend(@Nullable PsThenAsPathPrepend asPathPrepend) {
    List<String> cleared = new ArrayList<>();
    if (_asPathPrepend != null) {
      cleared.add("as-path-prepend");
    }
    _asPathPrepend = asPathPrepend;
    return cleared;
  }

  private List<String> setCommunitySet(PsThenCommunitySet set) {
    List<String> cleared = new ArrayList<>();
    if (_communitySet != null) {
      cleared.add("community set");
    }
    if (!_communityAdds.isEmpty()) {
      cleared.add("community add");
    }
    _communitySet = set;
    _communityAdds.clear();
    // TODO: do we also clear deletes?
    return cleared;
  }

  private List<String> addCommunityAdd(PsThenCommunityAdd add) {
    _communityAdds.add(add);
    return ImmutableList.of();
  }

  private @Nonnull List<PsThen> getOthers() {
    return _others;
  }
}
