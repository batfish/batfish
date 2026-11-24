package org.batfish.datamodel.acl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableList;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.junit.jupiter.api.Test;

/** Test of {@link AclReferencesVerifier}. */
final class AclReferencesVerifierTest {

  @Test
  void testUndefinedPermittedByAcl() {
    Configuration c = new Configuration("testNode", ConfigurationFormat.CISCO_IOS);

    IpAccessList.builder()
        .setName("acl1")
        .setOwner(c)
        .setLines(new ExprAclLine(LineAction.PERMIT, new PermittedByAcl("undefinedAcl"), "line1"))
        .build();

    Throwable exception =
        assertThrows(VendorConversionException.class, () -> AclReferencesVerifier.verify(c));
    assertThat(
        exception.getMessage(),
        containsString("Configuration testnode has undefined ACL references: [undefinedAcl]"));
  }

  @Test
  void testUndefinedDeniedByAcl() {
    Configuration c = new Configuration("testNode", ConfigurationFormat.CISCO_IOS);

    IpAccessList.builder()
        .setName("acl1")
        .setOwner(c)
        .setLines(new ExprAclLine(LineAction.DENY, new DeniedByAcl("undefinedAcl"), "line1"))
        .build();

    Throwable exception =
        assertThrows(VendorConversionException.class, () -> AclReferencesVerifier.verify(c));
    assertThat(
        exception.getMessage(),
        containsString("Configuration testnode has undefined ACL references: [undefinedAcl]"));
  }

  @Test
  void testUndefinedAclAclLine() {
    Configuration c = new Configuration("testNode", ConfigurationFormat.CISCO_IOS);

    IpAccessList.builder()
        .setName("acl1")
        .setOwner(c)
        .setLines(new org.batfish.datamodel.AclAclLine("line1", "undefinedAcl"))
        .build();

    Throwable exception =
        assertThrows(VendorConversionException.class, () -> AclReferencesVerifier.verify(c));
    assertThat(
        exception.getMessage(),
        containsString("Configuration testnode has undefined ACL references: [undefinedAcl]"));
  }

  @Test
  void testUndefinedPacketPolicyApplyFilter() {
    Configuration c = new Configuration("testNode", ConfigurationFormat.CISCO_IOS);

    c.getPacketPolicies()
        .put(
            "pp1",
            new PacketPolicy(
                "pp1",
                ImmutableList.of(
                    new org.batfish.datamodel.packet_policy.ApplyFilter("undefinedAcl")),
                new org.batfish.datamodel.packet_policy.Return(
                    org.batfish.datamodel.packet_policy.Drop.instance())));

    Throwable exception =
        assertThrows(VendorConversionException.class, () -> AclReferencesVerifier.verify(c));
    assertThat(
        exception.getMessage(),
        containsString("Configuration testnode has undefined ACL references: [undefinedAcl]"));
  }

  @Test
  void testMultipleUndefinedReferences() {
    Configuration c = new Configuration("testNode", ConfigurationFormat.CISCO_IOS);

    IpAccessList.builder()
        .setName("acl1")
        .setOwner(c)
        .setLines(
            new ExprAclLine(LineAction.PERMIT, new PermittedByAcl("undefinedAcl1"), "line1"),
            new ExprAclLine(LineAction.DENY, new DeniedByAcl("undefinedAcl2"), "line2"),
            new org.batfish.datamodel.AclAclLine("line3", "undefinedAcl3"))
        .build();

    Throwable exception =
        assertThrows(VendorConversionException.class, () -> AclReferencesVerifier.verify(c));
    assertThat(
        exception.getMessage(),
        containsString(
            "Configuration testnode has undefined ACL references: [undefinedAcl1, undefinedAcl2,"
                + " undefinedAcl3]"));
  }

  @Test
  void testDefinedReferencesNoError() {
    Configuration c = new Configuration("testNode", ConfigurationFormat.CISCO_IOS);

    // Create defined ACL
    IpAccessList.builder()
        .setName("definedAcl")
        .setOwner(c)
        .setLines(new ExprAclLine(LineAction.PERMIT, TrueExpr.INSTANCE, "definedLine"))
        .build();

    // Create ACL that references the defined ACL
    IpAccessList.builder()
        .setName("acl2")
        .setOwner(c)
        .setLines(new ExprAclLine(LineAction.PERMIT, new PermittedByAcl("definedAcl"), "line"))
        .build();

    // Should not throw
    AclReferencesVerifier.verify(c);
  }
}
