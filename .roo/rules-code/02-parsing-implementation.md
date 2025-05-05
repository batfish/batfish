# Parsing Implementation Guidelines

## Command Implementation Decision Process

When implementing a new command or syntax in Batfish, you MUST follow this decision process:

1. **Identify the command's purpose**: Before implementing, understand what the command does and how it affects network behavior.

2. **Determine implementation level**: Ask the user which implementation state is appropriate for the command:

   - State 1: Not parsed at all (unrecognized)
   - State 2: In grammar but never extracted (use `_null` suffix)
   - State 3: In grammar but not implemented yet (use `todo()` or `warn()`)
   - State 4: Extracted but conditionally supported (add warnings)
   - State 5: Fully implemented (no warnings)

3. **Confirm testing approach**: Verify with the user what level of testing is required:
   - Parse-only test (for `_null` suffix rules)
   - Extraction test (for extracted properties)
   - Conversion test (for fully implemented features)

## Implementation Checklist

For any new command implementation, follow this checklist:

- [ ] Ask the user which implementation state is appropriate
- [ ] Add appropriate tokens to the lexer
- [ ] Add grammar rules with proper naming (use `_null` suffix if appropriate)
- [ ] Add extraction code if needed (states 3-5)
- [ ] Add conversion code if needed (states 4-5)
- [ ] Write appropriate tests based on implementation level
- [ ] Document any limitations or edge cases

## Required Questions

When implementing a new command, you MUST ask the user the following questions:

1. "Which implementation state should be used for this command?"

   - Provide a brief explanation of each state
   - Recommend a state based on the command's purpose and impact

2. "Should this command be fully extracted and converted, or just parsed with a `_null` suffix?"

   - Explain the trade-offs of each approach
   - Recommend based on whether the command affects network behavior

3. "What level of testing should be implemented for this command?"
   - Recommend appropriate testing based on implementation state

## Vendor-Specific Considerations

For vendor-specific implementations, consult the appropriate documentation:

- [Juniper-specific parsing guide](../docs/parsing/vendors/juniper.md)
- [Implementation guide](../docs/parsing/implementation_guide.md)

## Examples

### Example Dialog

```
User: I need to implement support for the Juniper 'recovery-timeout' command.

Roo: Before implementing the Juniper 'recovery-timeout' command, I need to determine the appropriate implementation level.

Which implementation state should be used for this command?

1. State 1: Not parsed at all (unrecognized)
2. State 2: In grammar but never extracted (use `_null` suffix)
3. State 3: In grammar but not implemented yet (use `todo()` or `warn()`)
4. State 4: Extracted but conditionally supported (add warnings)
5. State 5: Fully implemented (no warnings)

Based on the documentation, this command affects interface behavior when MAC limiting or storm control is in effect. However, if Batfish doesn't model these specific behaviors, option 2 (using `_null` suffix) might be appropriate.
```
