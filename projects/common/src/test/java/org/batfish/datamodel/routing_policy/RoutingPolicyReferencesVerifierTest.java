package org.batfish.datamodel.routing_policy;

import static org.batfish.datamodel.routing_policy.RoutingPolicyReferencesVerifier.BOOLEAN_EXPR_VERIFIER;
import static org.batfish.datamodel.routing_policy.RoutingPolicyReferencesVerifier.STATEMENT_VERIFIER;
import static org.hamcrest.Matchers.containsString;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Test of {@link RoutingPolicyReferencesVerifier}. */
public final class RoutingPolicyReferencesVerifierTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testVerifyCallExprDefined() {
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
  public void testVerifyCallExprUndefined() {
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

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference to routing policy in CallExpr"));
    RoutingPolicyReferencesVerifier.verify(c);
  }

  @Test
  public void testVerifyCallStatementDefined() {
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
  public void testVerifyCallStatementUndefined() {
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c")
            .build();
    RoutingPolicy policy = RoutingPolicy.builder().setName("policy").setOwner(c).build();
    policy.setStatements(ImmutableList.of(new CallStatement("nonexistentPolicy")));
    c.setRoutingPolicies(ImmutableMap.of("policy", policy));

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference to routing policy in CallStatement"));
    RoutingPolicyReferencesVerifier.verify(c);
  }

  @Test
  public void testVisitCallExprDefined() {
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
  public void testVisitCallExprUndefined() {
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c")
            .build();
    c.setRoutingPolicies(ImmutableMap.of());
    RoutingPolicyReferencesVerifierContext ctx =
        RoutingPolicyReferencesVerifierContext.fromConfiguration(c);

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference to routing policy in CallExpr"));
    new CallExpr("nonexistentPolicy").accept(BOOLEAN_EXPR_VERIFIER, ctx);
  }

  @Test
  public void testVisitCallStatementDefined() {
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
  public void testVisitCallStatementUndefined() {
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c")
            .build();
    c.setRoutingPolicies(ImmutableMap.of());
    RoutingPolicyReferencesVerifierContext ctx =
        RoutingPolicyReferencesVerifierContext.fromConfiguration(c);

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference to routing policy in CallStatement"));
    new CallStatement("nonexistentPolicy").accept(STATEMENT_VERIFIER, ctx);
  }

  @Test
  public void testVerifyNestedCallExprUndefined() {
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

    _thrown.expect(VendorConversionException.class);
    _thrown.expectMessage(containsString("Undefined reference to routing policy in CallExpr"));
    RoutingPolicyReferencesVerifier.verify(c);
  }
}
