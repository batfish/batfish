package org.batfish.datamodel.routing_policy;

import static org.batfish.datamodel.routing_policy.RoutingPolicyReferencesVerifier.BOOLEAN_EXPR_VERIFIER;
import static org.batfish.datamodel.routing_policy.RoutingPolicyReferencesVerifier.STATEMENT_VERIFIER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.RoutingPolicyReferencesVerifier.RoutingPolicyReferencesVerifierContext;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.junit.jupiter.api.Test;

/** Test of {@link RoutingPolicyReferencesVerifier}. */
final class RoutingPolicyReferencesVerifierTest {

  @Test
  void testVerifyCallExprDefined() {
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c")
            .build();
    RoutingPolicy.builder().setName("existingPolicy").setOwner(c).build();
    RoutingPolicy caller = RoutingPolicy.builder().setName("caller").setOwner(c).build();
    caller.setStatements(
        ImmutableList.of(
            new If(
                new CallExpr("existingPolicy"),
                ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                ImmutableList.of())));

    // Should not throw
    RoutingPolicyReferencesVerifier.verify(c);
  }

  @Test
  void testVerifyCallExprUndefined() {
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c")
            .build();
    RoutingPolicy policy = RoutingPolicy.builder().setName("policy").setOwner(c).build();
    policy.setStatements(
        ImmutableList.of(
            new If(
                new CallExpr("nonexistentPolicy"),
                ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                ImmutableList.of())));
    c.setRoutingPolicies(ImmutableMap.of("policy", policy));

    Throwable exception =
        assertThrows(
            VendorConversionException.class, () -> RoutingPolicyReferencesVerifier.verify(c));
    assertThat(
        exception.getMessage(),
        containsString("Undefined reference to routing policy in CallExpr"));
  }

  @Test
  void testVerifyCallStatementDefined() {
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c")
            .build();
    RoutingPolicy.builder().setName("existingPolicy").setOwner(c).build();
    RoutingPolicy caller = RoutingPolicy.builder().setName("caller").setOwner(c).build();
    caller.setStatements(ImmutableList.of(new CallStatement("existingPolicy")));

    // Should not throw
    RoutingPolicyReferencesVerifier.verify(c);
  }

  @Test
  void testVerifyCallStatementUndefined() {
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c")
            .build();
    RoutingPolicy policy = RoutingPolicy.builder().setName("policy").setOwner(c).build();
    policy.setStatements(ImmutableList.of(new CallStatement("nonexistentPolicy")));
    c.setRoutingPolicies(ImmutableMap.of("policy", policy));

    Throwable exception =
        assertThrows(
            VendorConversionException.class, () -> RoutingPolicyReferencesVerifier.verify(c));
    assertThat(
        exception.getMessage(),
        containsString("Undefined reference to routing policy in CallStatement"));
  }

  @Test
  void testVisitCallExprDefined() {
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c")
            .build();
    RoutingPolicy policy = RoutingPolicy.builder().setName("existingPolicy").setOwner(c).build();
    c.setRoutingPolicies(ImmutableMap.of("existingPolicy", policy));
    RoutingPolicyReferencesVerifierContext ctx =
        RoutingPolicyReferencesVerifierContext.fromConfiguration(c);

    // Should not throw
    new CallExpr("existingPolicy").accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  void testVisitCallExprUndefined() {
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c")
            .build();
    c.setRoutingPolicies(ImmutableMap.of());
    RoutingPolicyReferencesVerifierContext ctx =
        RoutingPolicyReferencesVerifierContext.fromConfiguration(c);

    Throwable exception =
        assertThrows(
            VendorConversionException.class,
            () -> new CallExpr("nonexistentPolicy").accept(BOOLEAN_EXPR_VERIFIER, ctx));
    assertThat(
        exception.getMessage(),
        containsString("Undefined reference to routing policy in CallExpr"));
  }

  @Test
  void testVisitCallStatementDefined() {
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c")
            .build();
    RoutingPolicy policy = RoutingPolicy.builder().setName("existingPolicy").setOwner(c).build();
    c.setRoutingPolicies(ImmutableMap.of("existingPolicy", policy));
    RoutingPolicyReferencesVerifierContext ctx =
        RoutingPolicyReferencesVerifierContext.fromConfiguration(c);

    // Should not throw
    new CallStatement("existingPolicy").accept(STATEMENT_VERIFIER, ctx);
  }

  @Test
  void testVisitCallStatementUndefined() {
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c")
            .build();
    c.setRoutingPolicies(ImmutableMap.of());
    RoutingPolicyReferencesVerifierContext ctx =
        RoutingPolicyReferencesVerifierContext.fromConfiguration(c);

    Throwable exception =
        assertThrows(
            VendorConversionException.class,
            () -> new CallStatement("nonexistentPolicy").accept(STATEMENT_VERIFIER, ctx));
    assertThat(
        exception.getMessage(),
        containsString("Undefined reference to routing policy in CallStatement"));
  }

  @Test
  void testVerifyNestedCallExprUndefined() {
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c")
            .build();
    RoutingPolicy policy = RoutingPolicy.builder().setName("policy").setOwner(c).build();
    policy.setStatements(
        ImmutableList.of(
            new If(
                BooleanExprs.TRUE,
                ImmutableList.of(
                    new If(
                        new CallExpr("nonexistentPolicy"),
                        ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                        ImmutableList.of())),
                ImmutableList.of())));
    c.setRoutingPolicies(ImmutableMap.of("policy", policy));

    Throwable exception =
        assertThrows(
            VendorConversionException.class, () -> RoutingPolicyReferencesVerifier.verify(c));
    assertThat(
        exception.getMessage(),
        containsString("Undefined reference to routing policy in CallExpr"));
  }
}
