package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.junit.Test;

/** Tests of {@link IpAccessList}. */
public class IpAccessListTest {

  IpAccessList.Builder _aclb = IpAccessList.builder();

  @Test
  public void testCreateVersionWithCycleBroken() {

    // Create an ACL that permits packets permitted by itself. Sad
    IpAccessList originalAcl =
        _aclb
            .setName("ACL")
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(new PermittedByAcl("ACL"))
                        .setName("reference myself")
                        .build()))
            .build();

    // Create a version of it where that line is marked as in a cycle and set as unmatchable
    IpAccessList brokenCycleCopy = originalAcl.createVersionWithUnmatchableLine(0, true, false);

    // Ensure the copy is as expected and that the original is unchanged
    IpAccessList expectedOriginal =
        _aclb
            .setName("ACL")
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(new PermittedByAcl("ACL"))
                        .setName("reference myself")
                        .build()))
            .build();
    IpAccessList expectedNewVersion =
        _aclb
            .setName("ACL")
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(FalseExpr.INSTANCE)
                        .setName("reference myself")
                        .setInCycle(true)
                        .build()))
            .build();

    assertThat(originalAcl.getName(), equalTo("ACL"));
    assertThat(originalAcl, equalTo(expectedOriginal));

    assertThat(brokenCycleCopy.getName(), equalTo("ACL"));
    assertThat(brokenCycleCopy, equalTo(expectedNewVersion));
  }

  @Test
  public void testCreateVersionWithUndefinedRefRemoved() {

    // Create an ACL that references an undefined ACL
    IpAccessList originalAcl =
        _aclb
            .setName("ACL")
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(new PermittedByAcl("Undefined ACL"))
                        .setName("reference nobody")
                        .build()))
            .build();

    // Create a version of it where that line is marked as an undefined ref and set as unmatchable
    IpAccessList brokenCycleCopy = originalAcl.createVersionWithUnmatchableLine(0, false, true);

    // Ensure the copy is as expected and that the original is unchanged
    IpAccessList expectedOriginal =
        _aclb
            .setName("ACL")
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(new PermittedByAcl("Undefined ACL"))
                        .setName("reference nobody")
                        .build()))
            .build();
    IpAccessList expectedNewVersion =
        _aclb
            .setName("ACL")
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(FalseExpr.INSTANCE)
                        .setName("reference nobody")
                        .setUndefinedReference(true)
                        .build()))
            .build();

    assertThat(originalAcl.getName(), equalTo("ACL"));
    assertThat(originalAcl, equalTo(expectedOriginal));

    assertThat(brokenCycleCopy.getName(), equalTo("ACL"));
    assertThat(brokenCycleCopy, equalTo(expectedNewVersion));
  }
}
