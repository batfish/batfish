package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class AsPathAccessListTest {

  @Test
  public void testPermits() {
    // Single permit line
    AsPathAccessList permitAcl =
        new AsPathAccessList(
            "test", ImmutableList.of(new AsPathAccessListLine(LineAction.PERMIT, ".*65001.*")));
    assertThat(
        "permit matching path", permitAcl.permits(AsPath.ofSingletonAsSets(65001L)), equalTo(true));
    assertThat(
        "deny non-matching path",
        permitAcl.permits(AsPath.ofSingletonAsSets(65002L)),
        equalTo(false));

    // Single deny line
    AsPathAccessList denyAcl =
        new AsPathAccessList(
            "test", ImmutableList.of(new AsPathAccessListLine(LineAction.DENY, ".*65001.*")));
    assertThat(
        "deny matching path", denyAcl.permits(AsPath.ofSingletonAsSets(65001L)), equalTo(false));
    assertThat(
        "deny non-matching path",
        denyAcl.permits(AsPath.ofSingletonAsSets(65002L)),
        equalTo(false));

    // Multiple lines - tests multi-line and first match wins
    AsPathAccessList multiAcl =
        new AsPathAccessList(
            "test",
            ImmutableList.of(
                new AsPathAccessListLine(LineAction.PERMIT, ".*65001.*"),
                new AsPathAccessListLine(LineAction.DENY, ".*65001.*"),
                new AsPathAccessListLine(LineAction.PERMIT, ".*6500.*")));
    assertThat(
        "first match wins", multiAcl.permits(AsPath.ofSingletonAsSets(65001L)), equalTo(true));
    assertThat(
        "third line permits", multiAcl.permits(AsPath.ofSingletonAsSets(65002L)), equalTo(true));
    assertThat("no match denies", multiAcl.permits(AsPath.ofSingletonAsSets(1L)), equalTo(false));

    // Empty lines
    AsPathAccessList emptyAcl = new AsPathAccessList("test", ImmutableList.of());
    assertThat(
        "empty lines deny", emptyAcl.permits(AsPath.ofSingletonAsSets(65001L)), equalTo(false));

    // Complex regex and AS-SET
    AsPathAccessList regexAcl =
        new AsPathAccessList(
            "test",
            ImmutableList.of(
                new AsPathAccessListLine(LineAction.PERMIT, "^65001 65002$"),
                new AsPathAccessListLine(LineAction.PERMIT, ".*\\{.*65001.*\\}.*")));
    assertThat(
        "exact regex match",
        regexAcl.permits(AsPath.ofSingletonAsSets(65001L, 65002L)),
        equalTo(true));
    assertThat(
        "no regex match",
        regexAcl.permits(AsPath.ofSingletonAsSets(65001L, 65003L)),
        equalTo(false));
    assertThat(
        "AS-SET match", regexAcl.permits(AsPath.of(AsSet.of(65001L, 65002L))), equalTo(true));
    assertThat(
        "singleton AS no match",
        regexAcl.permits(AsPath.ofSingletonAsSets(65001L)),
        equalTo(false));
  }

  @Test
  public void testSerialization() {
    AsPathAccessList acl =
        new AsPathAccessList(
            "test", ImmutableList.of(new AsPathAccessListLine(LineAction.PERMIT, ".*65001.*")));
    assertThat(SerializationUtils.clone(acl), equalTo(acl));
    assertThat(BatfishObjectMapper.clone(acl, AsPathAccessList.class), equalTo(acl));
    // Verify cache works after deserialization
    assertThat(
        SerializationUtils.clone(acl).permits(AsPath.ofSingletonAsSets(65001L)), equalTo(true));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new AsPathAccessList(
                "test", ImmutableList.of(new AsPathAccessListLine(LineAction.PERMIT, ".*65001.*"))),
            new AsPathAccessList(
                "test", ImmutableList.of(new AsPathAccessListLine(LineAction.PERMIT, ".*65001.*"))))
        .addEqualityGroup(
            new AsPathAccessList(
                "different",
                ImmutableList.of(new AsPathAccessListLine(LineAction.PERMIT, ".*65001.*"))))
        .addEqualityGroup(
            new AsPathAccessList(
                "test", ImmutableList.of(new AsPathAccessListLine(LineAction.DENY, ".*65001.*"))))
        .testEquals();
  }
}
