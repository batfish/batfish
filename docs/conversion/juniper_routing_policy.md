# Juniper Routing Policy Conversion

## Default Fall-Through Actions

When wiring a Juniper `policy-statement` to a VI context (BGP import,
BGP export, FIB export, etc.), always consider the **default
fall-through action** for that context.

### Background

On Junos, each policy evaluation context has a default action that
applies when a route falls through the entire policy without hitting a
terminal action (`accept` or `reject`). The Junos routing policy manual
documents these in Table 4 ("Default Import and Export Policies for
Protocols"). For example:

| Context | Default action |
|---------|---------------|
| BGP import | accept |
| BGP export | accept (with restrictions) |
| OSPF/IS-IS export | reject |
| Forwarding-table export | accept |

### The Pitfall

In the VI model, `RoutingPolicy.call()` returns
`Environment.getDefaultAction()` when a policy falls through without a
terminal action. The `Environment` defaults `defaultAction` to `false`
(reject). This means a Juniper policy-statement converted directly to a
VI `RoutingPolicy` will **reject** on fall-through, regardless of what
the Junos context would do.

This is correct for contexts where the Junos default is reject (e.g.,
OSPF export), but wrong for contexts where the Junos default is accept
(e.g., BGP import, forwarding-table export).

### The Fix

For contexts where the Junos default is accept, wrap the user's policy
in a **generated wrapper policy** that sets `SetDefaultActionAccept`
before calling the user's policy:

```java
String generatedName = generatedFibExportPolicyName(ri.getName());
RoutingPolicy wrapper = new RoutingPolicy(generatedName, _c);
wrapper.getStatements().add(Statements.SetDefaultActionAccept.toStaticStatement());
If callUserPolicy = new If();
callUserPolicy.setGuard(new CallExpr(policyName));
callUserPolicy.setTrueStatements(
    ImmutableList.of(Statements.ExitAccept.toStaticStatement()));
callUserPolicy.setFalseStatements(
    ImmutableList.of(Statements.ExitReject.toStaticStatement()));
wrapper.getStatements().add(callUserPolicy);
_c.getRoutingPolicies().put(generatedName, wrapper);
```

### Existing Examples

| Context | Generated policy name | Sets default accept? |
|---------|----------------------|---------------------|
| BGP import | `generatedBgpPeerImportPolicyName` | Yes |
| BGP export | `generatedBgpPeerExportPolicyName` | No (uses reject chain) |
| FIB export | `generatedFibExportPolicyName` | Yes |

### Why Not Just Change the Environment Default?

The `Environment.defaultAction` is global to the policy evaluation, not
per-context. Changing its default would affect all policy evaluations.
The wrapper policy approach keeps the fix localized to the specific
context.

### Related

- Junos routing policy actions: `load-balance per-packet` is an
  attribute manipulation action, not a flow control action. It does not
  terminate policy evaluation. See `PsThenLoadBalance`.
- Flow control actions (accept, reject, next term, next policy) are
  documented in `JuniperConfiguration.isFinalThen()`.
