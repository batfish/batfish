package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents all {@link PsThen} statements in a single {@link PsTerm}.
 *
 * <p>Models Junos runtime behavior for actions within a single {@code then} block:
 *
 * <ul>
 *   <li>Scalar/numeric families (origin, metric, local-preference, next-hop, tunnel-attribute,
 *       etc.) use last-wins semantics: only the last action is retained.
 *   <li>Community actions (set/add/delete) live in a single ordered list, executed in declared
 *       order. A new {@code community set} wipes all prior community actions (since {@code set}
 *       replaces the route's communities, prior add/delete are no-ops). Within add/delete, a new
 *       action that conflicts with a prior one on the same community name (e.g., {@code add X}
 *       followed by {@code delete X}) is retained — order matters at runtime — but a warning is
 *       emitted.
 *   <li>{@code next term} and {@code next policy} share one last-wins slot: a {@code then} block
 *       holds at most one such action. Bare {@code accept}/{@code reject} live in a separate slot
 *       and also use last-wins. When both a {@code next term}/{@code next policy} and a bare
 *       terminator appear in the same term, both lines stay in retained config but the {@code next
 *       term}/{@code next policy} wins at runtime: the bare terminator does not fire.
 * </ul>
 *
 * <p>All cleared/eliminated actions produce a RISKY warning so the user can audit the config.
 */
public final class PsThens implements Serializable {

  /**
   * Incorporates the given {@code then foo} statement in this {@link PsThens}. Returns the list of
   * prior {@code then foo} that may have been cleared or overwritten by this operation, or a
   * one-element list with a {@code "(dedup)"} suffix if the new statement was an exact duplicate.
   */
  public @Nonnull List<String> addPsThen(PsThen then) {
    if (isCommunityAction(then)) {
      return addCommunityAction(then);
    }
    if (isNextTermOrPolicy(then)) {
      return setNextTermOrPolicy(then);
    }
    if (then instanceof PsThenAccept || then instanceof PsThenReject) {
      return setBareTerminator(then);
    }
    String family = getFamily(then);
    if (family == null) {
      // Unclassified action (flow-control, unmodeled, etc.) — always retain
      _others.add(then);
      return ImmutableList.of();
    }
    return setLastWins(then, family);
  }

  /**
   * Returns all retained actions in a canonical order that matches Junos commit-time normalization.
   * AS-path-prepend always comes before as-path-expand (Junos applies prepend first). Community
   * actions are emitted in their declared order (set/add/delete are interleaved as typed).
   *
   * <p>The result is a list rather than a set because community actions can repeat (e.g., {@code
   * add X; delete X; add X}) and order matters at runtime. All other families dedup naturally via
   * last-wins semantics, so they contribute at most one entry each.
   */
  public @Nonnull List<PsThen> getAllThens() {
    ImmutableList.Builder<PsThen> allThens = ImmutableList.builder();
    for (String family : SCALAR_CANONICAL_ORDER) {
      List<PsThen> actions = _familyActions.get(family);
      if (actions != null) {
        allThens.addAll(actions);
      }
    }
    allThens.addAll(_communityActions);
    if (_nextTermOrPolicy != null) {
      allThens.add(_nextTermOrPolicy);
    }
    if (_bareTerminator != null) {
      allThens.add(_bareTerminator);
    }
    allThens.addAll(_others);
    return allThens.build();
  }

  public boolean isEmpty() {
    return getAllThens().isEmpty();
  }

  PsThens() {
    _familyActions = new HashMap<>();
    _communityActions = new ArrayList<>(0);
    _others = new ArrayList<>(0);
  }

  // Canonical output order for scalar last-wins families. Matches Junos commit-time
  // normalization (e.g., as-path-prepend is applied before as-path-expand). After scalars,
  // getAllThens emits communities, then the `next term`/`next policy` slot,
  // then the bare-terminator slot (accept or reject), then _others.
  private static final ImmutableList<String> SCALAR_CANONICAL_ORDER =
      ImmutableList.of(
          "as-path-prepend",
          "as-path-expand",
          "origin",
          "preference",
          "tag",
          "next-hop",
          "external",
          "local-preference",
          "metric",
          "metric2",
          "load-balance",
          "source-class",
          "tunnel-attribute set",
          "tunnel-attribute remove");

  // --- Family classification (scalar last-wins families only) ---

  /**
   * Returns the family name for a scalar last-wins {@link PsThen} action, or null if unclassified.
   * Community actions, {@code next term}/{@code next policy}, and bare {@code accept}/{@code
   * reject} are handled separately and are not classified by this method.
   */
  static @Nullable String getFamily(PsThen then) {
    if (then instanceof PsThenOrigin) {
      return "origin";
    } else if (then instanceof PsThenPreference) {
      return "preference";
    } else if (then instanceof PsThenTag) {
      return "tag";
    } else if (then instanceof PsThenNextHopIp
        || then instanceof PsThenNextHopSelf
        || then instanceof PsThenNextHopPeerAddress
        || then instanceof PsThenNextHopDiscard
        || then instanceof PsThenNextHopReject) {
      return "next-hop";
    } else if (then instanceof PsThenExternal) {
      return "external";
    } else if (then instanceof PsThenLocalPreference) {
      return "local-preference";
    } else if (then instanceof PsThenMetric) {
      return "metric";
    } else if (then instanceof PsThenMetric2) {
      return "metric2";
    } else if (then instanceof PsThenLoadBalance) {
      return "load-balance";
    } else if (then instanceof PsThenAsPathPrepend) {
      return "as-path-prepend";
    } else if (then instanceof PsThenAsPathExpand) {
      return "as-path-expand";
    } else if (then instanceof PsThenTunnelAttributeSet) {
      return "tunnel-attribute set";
    } else if (then instanceof PsThenTunnelAttributeRemove) {
      return "tunnel-attribute remove";
    } else if (then instanceof PsThenSourceClass) {
      return "source-class";
    }
    return null;
  }

  private static boolean isCommunityAction(PsThen then) {
    return then instanceof PsThenCommunitySet
        || then instanceof PsThenCommunityAdd
        || then instanceof PsThenCommunityDelete;
  }

  private static @Nonnull String communityLabel(PsThen then) {
    if (then instanceof PsThenCommunitySet) {
      return "community set " + ((PsThenCommunitySet) then).getName();
    } else if (then instanceof PsThenCommunityAdd) {
      return "community add " + ((PsThenCommunityAdd) then).getName();
    } else {
      return "community delete " + ((PsThenCommunityDelete) then).getName();
    }
  }

  private static @Nullable String communityActionName(PsThen then) {
    if (then instanceof PsThenCommunitySet) {
      return ((PsThenCommunitySet) then).getName();
    } else if (then instanceof PsThenCommunityAdd) {
      return ((PsThenCommunityAdd) then).getName();
    } else if (then instanceof PsThenCommunityDelete) {
      return ((PsThenCommunityDelete) then).getName();
    }
    return null;
  }

  // --- Last-wins logic for scalar families ---

  private @Nonnull List<String> setLastWins(PsThen then, String family) {
    List<PsThen> existing = _familyActions.get(family);
    if (existing == null) {
      List<PsThen> list = new ArrayList<>(1);
      list.add(then);
      _familyActions.put(family, list);
      return ImmutableList.of();
    }
    // Overwrite: clear all prior and replace with this one
    PsThen prior = existing.get(0);
    existing.clear();
    existing.add(then);
    if (then.equals(prior)) {
      return ImmutableList.of(family + " (dedup)");
    }
    return ImmutableList.of(family);
  }

  // --- Community actions: ordered list with set-as-barrier and add/delete conflict detection ---

  private @Nonnull List<String> addCommunityAction(PsThen then) {
    // Dedup only against the immediately preceding community action: e.g., `add X; add X`
    // collapses, but `add X; delete X; add X` must keep the second add (the intervening delete
    // changes the runtime effect). Set-as-barrier (below) handles set-after-anything.
    if (!_communityActions.isEmpty()
        && _communityActions.get(_communityActions.size() - 1).equals(then)) {
      return ImmutableList.of(communityLabel(then) + " (dedup)");
    }

    // A new `community set` wipes ALL prior community actions (set replaces the route's
    // communities, so prior set/add/delete are no-ops). Warn for each cleared action.
    if (then instanceof PsThenCommunitySet) {
      List<String> cleared = new ArrayList<>();
      for (PsThen prior : _communityActions) {
        cleared.add(communityLabel(prior));
      }
      _communityActions.clear();
      _communityActions.add(then);
      return cleared;
    }

    // For add/delete: warn if a prior add/delete on the same name is the opposite kind. Both
    // are retained because order matters at runtime — `add X; delete X` zeroes X out, while
    // `delete X; add X` results in X being present.
    List<String> conflicts = new ArrayList<>();
    String name = communityActionName(then);
    boolean isAdd = then instanceof PsThenCommunityAdd;
    for (PsThen prior : _communityActions) {
      String priorName = communityActionName(prior);
      if (!java.util.Objects.equals(priorName, name)) {
        continue;
      }
      boolean priorIsAdd = prior instanceof PsThenCommunityAdd;
      boolean priorIsDelete = prior instanceof PsThenCommunityDelete;
      if ((isAdd && priorIsDelete) || (!isAdd && priorIsAdd)) {
        conflicts.add(communityLabel(prior) + " (conflict)");
      }
    }
    _communityActions.add(then);
    return conflicts;
  }

  // --- `next term` and `next policy` share one last-wins slot ---

  private static boolean isNextTermOrPolicy(PsThen then) {
    return then instanceof PsThenNextTerm || then instanceof PsThenNextPolicy;
  }

  private @Nonnull List<String> setNextTermOrPolicy(PsThen then) {
    ImmutableList.Builder<String> warnings = ImmutableList.builder();
    if (_nextTermOrPolicy != null) {
      if (_nextTermOrPolicy.equals(then)) {
        warnings.add("next-term-or-policy (dedup)");
      } else {
        warnings.add("next-term-or-policy");
      }
    }
    _nextTermOrPolicy = then;
    if (_bareTerminator != null) {
      // `then next term` / `then next policy` suppresses any bare `accept`/`reject` in the same
      // term, regardless of source order. Both lines are retained at commit, but the bare
      // terminator does not fire.
      warnings.add("bare-terminator (suppressed-by-next-term-or-policy)");
    }
    return warnings.build();
  }

  // --- bare accept/reject: dedicated slot, last-wins, suppressed by next term/next policy ---

  private @Nonnull List<String> setBareTerminator(PsThen then) {
    ImmutableList.Builder<String> warnings = ImmutableList.builder();
    if (_bareTerminator != null) {
      if (_bareTerminator.equals(then)) {
        warnings.add("bare-terminator (dedup)");
      } else {
        warnings.add("bare-terminator");
      }
    }
    _bareTerminator = then;
    if (_nextTermOrPolicy != null) {
      // Adding a bare terminator when `next term`/`next policy` is already present: the new
      // line is dead — `next term`/`next policy` wins at runtime.
      warnings.add("bare-terminator (dead-after-next-term-or-policy)");
    }
    return warnings.build();
  }

  private final @Nonnull Map<String, List<PsThen>> _familyActions;
  private final @Nonnull List<PsThen> _communityActions;
  private @Nullable PsThen _nextTermOrPolicy;
  private @Nullable PsThen _bareTerminator;
  private final @Nonnull List<PsThen> _others;
}
