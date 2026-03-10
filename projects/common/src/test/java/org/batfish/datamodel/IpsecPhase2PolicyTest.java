package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Test;

/** Tests for {@link IpsecPhase2Policy} */
public class IpsecPhase2PolicyTest {

  @Test
  public void testDefaultConstructor() {
    IpsecPhase2Policy policy = new IpsecPhase2Policy();

    assertThat(policy.getProposals(), empty());
    assertThat(policy.getPfsKeyGroups(), empty());
  }

  @Test
  public void testSetPfsKeyGroups() {
    IpsecPhase2Policy policy = new IpsecPhase2Policy();
    Set<DiffieHellmanGroup> dhGroups =
        ImmutableSet.of(DiffieHellmanGroup.GROUP14, DiffieHellmanGroup.GROUP24);

    policy.setPfsKeyGroups(dhGroups);

    assertThat(
        policy.getPfsKeyGroups(),
        containsInAnyOrder(DiffieHellmanGroup.GROUP14, DiffieHellmanGroup.GROUP24));
  }

  @Test
  public void testSetPfsKeyGroup() {
    IpsecPhase2Policy policy = new IpsecPhase2Policy();

    policy.setPfsKeyGroup(DiffieHellmanGroup.GROUP2);

    // Should create a singleton set
    assertThat(policy.getPfsKeyGroups(), contains(DiffieHellmanGroup.GROUP2));
  }

  @Test
  public void testSetPfsKeyGroupNull() {
    IpsecPhase2Policy policy = new IpsecPhase2Policy();

    policy.setPfsKeyGroup(null);

    assertThat(policy.getPfsKeyGroups(), empty());
  }

  @Test
  public void testSetPfsKeyGroupsNull() {
    IpsecPhase2Policy policy = new IpsecPhase2Policy();

    policy.setPfsKeyGroups(null);

    assertThat(policy.getPfsKeyGroups(), empty());
  }

  @Test
  public void testSetProposals() {
    IpsecPhase2Policy policy = new IpsecPhase2Policy();

    policy.setProposals(ImmutableList.of("proposal1", "proposal2"));

    assertThat(policy.getProposals(), contains("proposal1", "proposal2"));
  }

  @Test
  public void testSetProposalsNull() {
    IpsecPhase2Policy policy = new IpsecPhase2Policy();

    policy.setProposals(null);

    assertThat(policy.getProposals(), empty());
  }

  @Test
  public void testEquals() {
    IpsecPhase2Policy policy1 = new IpsecPhase2Policy();
    policy1.setPfsKeyGroups(
        ImmutableSet.of(DiffieHellmanGroup.GROUP14, DiffieHellmanGroup.GROUP24));
    policy1.setProposals(ImmutableList.of("proposal1", "proposal2"));

    IpsecPhase2Policy policy2 = new IpsecPhase2Policy();
    policy2.setPfsKeyGroups(
        ImmutableSet.of(DiffieHellmanGroup.GROUP14, DiffieHellmanGroup.GROUP24));
    policy2.setProposals(ImmutableList.of("proposal1", "proposal2"));

    IpsecPhase2Policy policy3 = new IpsecPhase2Policy();
    policy3.setPfsKeyGroups(ImmutableSet.of(DiffieHellmanGroup.GROUP2));
    policy3.setProposals(ImmutableList.of("proposal1", "proposal2"));

    assertThat(policy1, equalTo(policy2));
    assertThat(policy1.hashCode(), equalTo(policy2.hashCode()));
    assertThat(policy1.equals(policy3), equalTo(false));
  }
}
