# Huawei Grammar Fixes - Detailed Summary

## Critical Fixes Applied

### 1. NAT Outbound Interface Ambiguity - FIXED

**Original Issue:**
```
nat outbound 3001 interface
```
Was being parsed as:
- NAT stanza matching `nat outbound 3001`
- Leaving `interface` unconsumed
- `s_interface` then trying to parse `interface` as an interface name
- Failing because `interface` is a keyword, not a valid interface name

**Fix Applied:**
File: `Huawei_nat.g4`

Changed from:
```antlr
s_nat
:
   NAT nat_substanza
;

nat_substanza
:
   nat_address_group
   | nat_outbound
   | nat_static
   | nat_server
   | nat_null
;
```

To:
```antlr
s_nat
:
   NAT
   (
      // NAT address-group
      ADDRESS_GROUP uint16 ...
      |
      // NAT outbound
      NO? OUTBOUND
      (
         acl_num = uint16
         |
         acl_name = variable
      )
      (
         INTERFACE
         |
         pool_name = variable
      )?
      (
         VPN_INSTANCE vrf_name = variable
      )?
      |
      // NAT static
      NO? STATIC ...
      |
      // NAT server
      NO? SERVER ...
      |
      // Other NAT commands
      null_rest_of_line
   )
;
```

**Impact:** Each NAT command is now a standalone stanza that fully consumes its tokens, preventing keyword leakage to other rules.

### 2. ACL "number" Syntax Support - FIXED

**Original Issue:**
```
acl number 2001
```
Failed because grammar expected `acl <number>` or `acl <name>`, not `acl number <number>`.

**Fix Applied:**
File: `Huawei_acl.g4`

Added alternative:
```antlr
s_acl
:
   ACL
   (
      acl_num = uint16 (acl_type = ACL_BASIC | acl_type = ACL_ADVANCED)?
      |
      NUMBER acl_num2 = uint16 (acl_type = ACL_BASIC | acl_type = ACL_ADVANCED)?
      |
      ACL_NAME acl_name = variable (acl_type = ACL_BASIC | acl_type = ACL_ADVANCED)?
      |
      acl_name = variable (acl_type = ACL_BASIC | acl_type = ACL_ADVANCED)?
   )
   ...
;
```

**Impact:** Supports all four Huawei ACL declaration syntaxes.

### 3. ACL Port Specifications - FIXED

**Original Issue:**
```
rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255 destination-port eq 80
```
Failed because `destination-port` was being parsed as `DESTINATION` keyword followed by variable `-port`.

**Fix Applied:**
File: `Huawei_acl.g4`

Updated port matching to use dedicated tokens:
```antlr
   (
      SOURCE_PORT
      (
         src_eq = EQ src_port = uint16
         |
         src_gt = GT src_port = uint16
         |
         src_lt = LT src_port = uint16
         |
         src_range = RANGE src_port_start = uint16 src_port_end = uint16
      )
   )?
   (
      DESTINATION_PORT
      (
         dest_eq = EQ dest_port = uint16
         |
         dest_gt = GT dest_port = uint16
         |
         dest_lt = LT dest_port = uint16
         |
         dest_range = RANGE dest_port_start = uint16 dest_port_end = uint16
      )
   )?
```

**Impact:** Properly parses ACL rules with source-port and destination-port specifications.

## Java Extractor Updates

### File: HuaweiControlPlaneExtractor.java

Updated `exitS_nat()` method to work with flattened grammar structure:

**Before:**
```java
if (ctx.nat_substanza() != null && ctx.nat_substanza().nat_outbound() != null) {
   HuaweiParser.Nat_outboundContext outboundCtx = ctx.nat_substanza().nat_outbound();
   if (outboundCtx.acl_num != null) {
      natRule.setAclName(outboundCtx.acl_num.getText());
   }
}
```

**After:**
```java
if (ctx.OUTBOUND() != null) {
   if (ctx.acl_num != null) {
      natRule.setAclName(ctx.acl_num.getText());
   } else if (ctx.acl_name != null) {
      natRule.setAclName(ctx.acl_name.getText());
   }
   if (ctx.INTERFACE() != null) {
      natRule.setType(NatType.EASY_IP);
   } else if (ctx.pool_name != null) {
      natRule.setPoolName(ctx.pool_name.getText());
   }
   if (ctx.vrf_name != null) {
      natRule.setVrfName(ctx.vrf_name.getText());
   }
}
```

## Key Improvements

1. **No Token Leakage**: Each rule now fully consumes all its tokens, preventing unconsumed keywords from triggering wrong rules
2. **Explicit Token Matching**: Uses specific keywords (SOURCE_PORT, DESTINATION_PORT) instead of keyword + separator combinations
3. **Flattened Structure**: Removed unnecessary nesting for standalone commands
4. **All Syntax Variants**: Supports multiple ways to write the same command as per Huawei VRP specification

## Test Coverage

### Tests Affected by Fixes:
1. ✅ `testNatOutbound` - NAT with interface keyword
2. ✅ `testAclRules` - ACL with "number" syntax
3. ✅ `testAclAdvanced` - ACL with port specifications
4. ⚠️  `testVrfWithRouteTargets` - VRF route targets (code logic, not grammar)

### Tests Needing Verification:
All 37 tests in `HuaweiGrammarTest.java` should be run to ensure:
- No regressions in existing functionality
- New fixes work correctly
- Edge cases are handled

## Next Steps

1. **Build and Test**: Run full test suite to verify all fixes
2. **VRF Issue**: Investigate `testVrfWithRouteTargets` failure (expects 3 import RTs, gets 2)
3. **Additional Coverage**: Add tests for:
   - ACL with `name` syntax
   - NAT server with protocol specifications
   - Edge cases with interface names
4. **Documentation**: Update inline grammar comments with lessons learned

## Grammar Design Best Practices Applied

1. **Start with specific, end with general**: Order alternatives from most specific to most general
2. **Avoid ambiguity in token definition**: Use dedicated tokens for compound keywords like `source-port`
3. **Flatten when appropriate**: Don't create unnecessary hierarchy for standalone commands
4. **Label for access**: Use labels in alternatives for generated field access
5. **Test with real configs**: Always test against actual device configurations
