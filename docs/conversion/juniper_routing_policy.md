# Juniper Routing Policy Conversion

## Default Fall-Through Actions

When wiring a Juniper `policy-statement` to a VI context, always
consider the **default fall-through action** for that context.

In the VI model, `RoutingPolicy.call()` returns
`Environment.getDefaultAction()` when a policy falls through without a
terminal action. The `Environment` defaults to `false` (reject). This is
correct for contexts where Junos defaults to reject (e.g., OSPF export),
but wrong for contexts where Junos defaults to accept.

For accept-default contexts, wrap the user's policy in a generated
policy that sets `SetDefaultActionAccept`. See
`generatedBgpPeerImportPolicyName` (BGP import) and
`generatedFibExportPolicyName` (forwarding-table export) for examples.

Junos default actions per context are documented in the Junos routing
policy manual, Table 4 ("Default Import and Export Policies for
Protocols").
