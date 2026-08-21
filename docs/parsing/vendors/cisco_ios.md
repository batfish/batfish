# Cisco IOS-Specific Parsing and Extraction

## Cisco IOS fidelity takes precedence in the `cisco` grammar

The `Cisco_*.g4` grammars, `CiscoControlPlaneExtractor`, and the `representation/cisco` model serve
five configuration formats, not just `CISCO_IOS`: `ARUBAOS`, `CADANT`, `FORCE10`, and `FOUNDRY`
share them. See the shared `case` block in
[`ParseVendorConfigurationJob.java`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/job/ParseVendorConfigurationJob.java).
A few rules exist only for one of those four: the `FACILITY` clause of `logging_host` was added for
Cadant and is not documented for IOS.

**When IOS syntax or semantics conflict with one of the other four formats, model IOS.** Narrowing
a parser rule or a value range to match the IOS command reference is acceptable even when it drops
input that previously parsed for ArubaOS, Cadant, Force10, or Foundry. Those four are legacy: no
vendor-specific representation, no conversion, no lab validation, and no test configurations in the
repository. Name the format that loses coverage in the commit message, but do not consult its
manual to justify the change.

Only lost coverage is licensed. Silently extracting a wrong value is still a defect, whatever the
format.
