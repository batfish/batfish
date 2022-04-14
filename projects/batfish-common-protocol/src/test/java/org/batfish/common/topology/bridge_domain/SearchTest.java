package org.batfish.common.topology.bridge_domain;

import static org.batfish.common.topology.bridge_domain.Search.STATE_FUNCTION_EVALUATOR;
import static org.batfish.common.topology.bridge_domain.edge.VlanAwareBridgeDomainToL2.compose;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.assignVlanFromOuterTag;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.clearVlanId;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.filterByOuterTag;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.filterByVlanId;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.popTag;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.pushTag;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.pushVlanId;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.setVlanId;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.translateVlan;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.datamodel.IntegerSpace;
import org.junit.Test;

/** Test of {@link Search}. */
public final class SearchTest {

  @Test
  public void testVisitAssignVlanFromOuterTag() {
    assertThat(
        visit(assignVlanFromOuterTag(null), State.of(1, null)),
        equalTo(Optional.of(State.of(null, 1))));
    assertThat(
        visit(assignVlanFromOuterTag(1), State.of(null, null)),
        equalTo(Optional.of(State.of(null, 1))));
    assertThat(
        visit(assignVlanFromOuterTag(2), State.of(1, null)),
        equalTo(Optional.of(State.of(null, 1))));
  }

  @Test(expected = AssertionError.class)
  public void testVisitAssignVlanFromOuterTag_invalid() {
    visit(assignVlanFromOuterTag(null), State.empty());
  }

  @Test
  public void testVisitClearVlanId() {
    assertThat(visit(clearVlanId(), State.of(1, 1)), equalTo(Optional.of(State.of(1, null))));
    assertThat(visit(clearVlanId(), State.of(null, 1)), equalTo(Optional.of(State.of(null, null))));
  }

  @Test(expected = AssertionError.class)
  public void testVisitClearVlanId_invalid() {
    visit(clearVlanId(), State.empty());
  }

  @Test
  public void testVisitCompose() {
    assertThat(
        visit(
            compose(translateVlan(ImmutableMap.of(1, 2)), filterByVlanId(IntegerSpace.of(2))),
            State.of(1, 1)),
        equalTo(Optional.of(State.of(1, 2))));
    assertThat(
        visit(
            compose(filterByVlanId(IntegerSpace.of(2)), translateVlan(ImmutableMap.of(1, 2))),
            State.of(1, 1)),
        equalTo(Optional.empty()));
  }

  @Test
  public void testVisitFilterByOuterTag() {
    assertThat(
        visit(filterByOuterTag(IntegerSpace.of(1), true), State.of(null, 100)),
        equalTo(Optional.of(State.of(null, 100))));
    assertThat(
        visit(filterByOuterTag(IntegerSpace.of(1), false), State.of(null, 100)),
        equalTo(Optional.empty()));
    assertThat(
        visit(filterByOuterTag(IntegerSpace.of(1), false), State.of(1, 100)),
        equalTo(Optional.of(State.of(1, 100))));
  }

  @Test
  public void testVisitFilterByVlanId() {
    assertThat(
        visit(filterByVlanId(IntegerSpace.of(1)), State.of(2, 1)),
        equalTo(Optional.of(State.of(2, 1))));
    assertThat(
        visit(filterByVlanId(IntegerSpace.of(2)), State.of(2, 1)), equalTo(Optional.empty()));
  }

  @Test(expected = AssertionError.class)
  public void testVisitFilterByVlanId_invalid() {
    visit(filterByVlanId(IntegerSpace.of(1)), State.of(2, null));
  }

  @Test
  public void testVisitIdentity() {
    assertThat(visit(identity(), State.empty()), equalTo(Optional.of(State.empty())));
    assertThat(visit(identity(), State.of(null, 1)), equalTo(Optional.of(State.of(null, 1))));
    assertThat(visit(identity(), State.of(1, null)), equalTo(Optional.of(State.of(1, null))));
    assertThat(visit(identity(), State.of(1, 1)), equalTo(Optional.of(State.of(1, 1))));
  }

  @Test
  public void testVisitPopTag() {
    assertThat(visit(popTag(0), State.empty()), equalTo(Optional.of(State.empty())));
    assertThat(visit(popTag(0), State.of(1, 2)), equalTo(Optional.of(State.of(1, 2))));
    assertThat(visit(popTag(1), State.of(1, 2)), equalTo(Optional.of(State.of(null, 2))));
  }

  @Test(expected = AssertionError.class)
  public void testVisitPopTag_invalid() {
    visit(popTag(1), State.of(null, 2));
  }

  @Test
  public void testVisitPushTag() {
    assertThat(visit(pushTag(1), State.of(null, 2)), equalTo(Optional.of(State.of(1, 2))));
  }

  @Test(expected = AssertionError.class)
  public void testVisitPushTag_invalid() {
    // TODO: remove/update when tag stacks are supported
    visit(pushTag(1), State.of(1, 1));
  }

  @Test
  public void testVisitPushVlanId() {
    assertThat(visit(pushVlanId(null), State.of(null, 2)), equalTo(Optional.of(State.of(2, 2))));
    assertThat(visit(pushVlanId(2), State.of(null, 2)), equalTo(Optional.of(State.of(null, 2))));
  }

  @Test(expected = AssertionError.class)
  public void testVisitPushVlanId_invalidNoVlanId() {
    visit(pushVlanId(null), State.of(null, null));
  }

  @Test(expected = AssertionError.class)
  public void testVisitPushVlanId_invalidTagPresent() {
    // TODO: remove/update when tag stacks are supported
    visit(pushVlanId(null), State.of(1, 2));
  }

  @Test
  public void testVisitSetVlanId() {
    assertThat(visit(setVlanId(3), State.of(1, null)), equalTo(Optional.of(State.of(1, 3))));
  }

  @Test(expected = AssertionError.class)
  public void testVisitSetVlanId_invalid() {
    visit(setVlanId(3), State.of(1, 2));
  }

  @Test
  public void testVisitTranslateVlan() {
    assertThat(
        visit(translateVlan(ImmutableMap.of(1, 2)), State.of(null, 1)),
        equalTo(Optional.of(State.of(null, 2))));
    assertThat(
        visit(translateVlan(ImmutableMap.of(1, 2)), State.of(5, 2)),
        equalTo(Optional.of(State.of(5, 2))));
    assertThat(
        visit(translateVlan(ImmutableMap.of(1, 2)), State.of(5, 3)),
        equalTo(Optional.of(State.of(5, 3))));
  }

  @Test(expected = AssertionError.class)
  public void testVisitTranslateVlan_invalid() {
    visit(translateVlan(ImmutableMap.of(1, 2)), State.of(5, null));
  }

  private static @Nonnull Optional<State> visit(StateFunction stateFunction, State arg) {
    return STATE_FUNCTION_EVALUATOR.visit(stateFunction, arg);
  }
}
