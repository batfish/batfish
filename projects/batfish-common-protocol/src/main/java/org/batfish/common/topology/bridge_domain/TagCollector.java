package org.batfish.common.topology.bridge_domain;

import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.edge.Edge;
import org.batfish.common.topology.bridge_domain.function.AssignVlanFromOuterTag;
import org.batfish.common.topology.bridge_domain.function.ClearVlanId;
import org.batfish.common.topology.bridge_domain.function.ComposeBaseImpl;
import org.batfish.common.topology.bridge_domain.function.FilterByOuterTag.FilterByOuterTagImpl;
import org.batfish.common.topology.bridge_domain.function.FilterByVlanId.FilterByVlanIdImpl;
import org.batfish.common.topology.bridge_domain.function.Identity;
import org.batfish.common.topology.bridge_domain.function.PopTag.PopTagImpl;
import org.batfish.common.topology.bridge_domain.function.PushTag;
import org.batfish.common.topology.bridge_domain.function.PushVlanId;
import org.batfish.common.topology.bridge_domain.function.SetVlanId;
import org.batfish.common.topology.bridge_domain.function.StateFunctionVisitor;
import org.batfish.common.topology.bridge_domain.function.TranslateVlan.TranslateVlanImpl;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.SubRange;

/**
 * A utility that iteratively collects tags allowed by {@link
 * org.batfish.common.topology.bridge_domain.function.StateFunction}s and reports when an overlap is
 * discovered.
 */
public final class TagCollector {

  public TagCollector() {
    _currentlyAllowed = IntegerSpace.EMPTY;
  }

  /**
   * Add the space of tags allowed on allowed by {@code edge}'s {@link
   * org.batfish.common.topology.bridge_domain.function.StateFunction} to the running space of
   * allowed tags, returning {@code true} iff they overlap.
   */
  public boolean resultsInOverlap(Edge edge) {
    IntegerSpace allowedByEdge = VISITOR.visit(edge.getStateFunction(), ALL_TAGS);
    if (!_currentlyAllowed.intersection(allowedByEdge).isEmpty()) {
      return true;
    }
    _currentlyAllowed = _currentlyAllowed.union(allowedByEdge);
    return false;
  }

  private @Nonnull IntegerSpace _currentlyAllowed;

  private static final StateFunctionVisitor<IntegerSpace, IntegerSpace> VISITOR =
      new StateFunctionVisitor<IntegerSpace, IntegerSpace>() {
        @Override
        public IntegerSpace visitAssignVlanFromOuterTag(
            AssignVlanFromOuterTag assignVlanFromOuterTag, IntegerSpace inputSpace) {
          throw new UnsupportedOperationException();
        }

        @Override
        public IntegerSpace visitClearVlanId(ClearVlanId clearVlanId, IntegerSpace inputSpace) {
          throw new UnsupportedOperationException();
        }

        @Override
        public IntegerSpace visitCompose(ComposeBaseImpl<?> compose, IntegerSpace inputSpace) {
          IntegerSpace result1 = visit(compose.getFunc1(), inputSpace);
          return visit(compose.getFunc2(), result1);
        }

        @Override
        public IntegerSpace visitFilterByOuterTag(
            FilterByOuterTagImpl filterByOuterTag, IntegerSpace inputSpace) {
          // 0 used for no tag
          IntegerSpace.Builder allowedHere =
              IntegerSpace.builder().including(filterByOuterTag.getAllowedOuterTags());
          if (filterByOuterTag.getAllowUntagged()) {
            allowedHere.including(0);
          }
          return inputSpace.intersection(allowedHere.build());
        }

        @Override
        public IntegerSpace visitFilterByVlanId(
            FilterByVlanIdImpl filterByVlanId, IntegerSpace inputSpace) {
          throw new UnsupportedOperationException();
        }

        @Override
        public IntegerSpace visitIdentity(Identity identity, IntegerSpace inputSpace) {
          return inputSpace;
        }

        @Override
        public IntegerSpace visitPopTag(PopTagImpl popTag, IntegerSpace inputSpace) {
          throw new UnsupportedOperationException();
        }

        @Override
        public IntegerSpace visitPushTag(PushTag pushTag, IntegerSpace inputSpace) {
          throw new UnsupportedOperationException();
        }

        @Override
        public IntegerSpace visitPushVlanId(PushVlanId pushVlanId, IntegerSpace inputSpace) {
          throw new UnsupportedOperationException();
        }

        @Override
        public IntegerSpace visitSetVlanId(SetVlanId setVlanId, IntegerSpace inputSpace) {
          throw new UnsupportedOperationException();
        }

        @Override
        public IntegerSpace visitTranslateVlan(
            TranslateVlanImpl translateVlan, IntegerSpace inputSpace) {
          throw new UnsupportedOperationException();
        }
      };

  // 0 means no tag
  private static final IntegerSpace ALL_TAGS = IntegerSpace.of(new SubRange(0, 4094));
}
