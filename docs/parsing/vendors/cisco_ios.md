# Cisco IOS-Specific Parsing and Extraction

## The `cisco` grammar is for Cisco IOS, not for vendors that copied its syntax

Batfish once maintained the `cisco` grammar as a single "Frankenparser" spanning every vendor whose
CLI resembles IOS. That proved a maintenance and correctness burden, and we are unwinding it by
making this vendor specific to Cisco IOS(-XE). `ParseVendorConfigurationJob` still routes ARUBAOS,
CADANT, FORCE10, and FOUNDRY configs through this parser, but where they work it is largely by
accident, and that is expected to get worse over time. A few rules exist only to serve them: the
`FACILITY` clause of `logging_host` was added for Cadant and is not documented for IOS.

**So when IOS syntax or semantics conflict with one of those four formats, model IOS.** Narrowing a
parser rule or a value range to match the IOS command reference is acceptable even when it drops
input that previously parsed for ArubaOS, Cadant, Force10, or Foundry. None of the four has a
vendor-specific representation, conversion, lab validation, or test configuration in this
repository. Name the format that loses coverage in the commit message, but do not consult its
manual to justify the change.
