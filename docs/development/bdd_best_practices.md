# BDD Best Practices

This guide covers essential practices for working with Binary Decision Diagrams (BDDs) in Batfish. BDDs are a core component of Batfish's symbolic analysis engine, and proper usage is critical for performance and correctness.

## Memory Management

### Overview

The JavaBDD library does not use Java's garbage collection. Instead, it requires explicit reference counting at the `BDDFactory` level for all BDD operations. Proper memory management is critical for BDD-heavy code—reference leaks prevent BDDs from being freed, consuming heap and cache space, and severely degrading performance.

Because BDDs are recursive structures, leaked references can create effectively unbounded memory growth. Real-world cases exist where adding a single missing `free()` call reduced memory usage from 100+ GiB (with heap overflow) and hours of runtime to under 100 MiB and milliseconds of execution time.

### Reference Counting Rules

#### Base Operations (Create New Reference)

Base BDD operations create **new** references and destroy **none**. Both the input and output BDD objects must be freed separately.

Examples:
- `bdd.or(other)` - returns new BDD, both `bdd` and `other` still need freeing
- `bdd.and(other)` - returns new BDD, both `bdd` and `other` still need freeing
- `bdd.xor(other)` - returns new BDD, both `bdd` and `other` still need freeing
- `bdd.not()` - returns new BDD, `bdd` still needs freeing
- `bdd.fullSatOne()` - returns new BDD, `bdd` still needs freeing

#### In-Place Operations (No New Reference)

In-place operations modify the target BDD and create **no** new references. The suffix `Eq` indicates "equals" semantics.

Examples:
- `bdd.orEq(other)` - changes `bdd` to result of `bdd | other`, returns void
- `bdd.andEq(other)` - changes `bdd` to result of `bdd & other`, returns void
- `bdd.xorEq(other)` - changes `bdd` to result of `bdd ^ other`, returns void
- `bdd.notEq()` - changes `bdd` to result of `!bdd`, returns void

**CRITICAL**: These operations **modify the BDD in-place**. Do not use with the same object as both operands (e.g., `a.notEq(a)`), as this will produce incorrect results.

#### Combined Operations (Consume One Reference)

Combined operations create no new references and automatically free one input. The suffix `With` indicates "consume with" semantics.

Examples:
- `bdd.orWith(other)` - equivalent to `bdd.orEq(other); other.free();`
- `bdd.andWith(other)` - equivalent to `bdd.andEq(other); other.free();`
- `bdd.xorWith(other)` - equivalent to `bdd.xorEq(other); other.free();`

The `With` operations are useful when the second operand is a temporary value that won't be used again.

#### Copying References

Use `bdd.id()` to create a copy of a BDD reference. This is useful when you need an intermediate value that can be freed independently (e.g., as an accumulator in a loop).

```java
BDD accumulator = factory.zero().id();  // Create a copy we can mutate
for (...) {
  BDD term = ...;
  accumulator.orWith(term);  // Updates accumulator, frees term
}
// Use accumulator, then free it when done
accumulator.free();
```

### Ownership and Documentation

BDD-heavy code frequently passes and creates BDD objects throughout the codebase. Clear ownership documentation is essential:

1. **Document ownership**: Specify in Javadoc which methods take ownership of BDD parameters and which methods return BDDs that callers must free
2. **Consistent patterns**: Use consistent ownership patterns throughout the codebase
3. **Avoid garbage in hot paths**: Core computation loops must not create BDD garbage—the result is returned, intermediate values are freed

Example Javadoc patterns:
```java
/**
 * Computes the union of two BDD sets.
 * @param first - caller retains ownership, must free separately
 * @param second - caller retains ownership, must free separately
 * @return new BDD representing the union - caller must free
 */
```

### Performance Impact

Proper BDD memory management is not optional—it directly determines whether computations are feasible:

- **Memory overhead**: Leaked BDDs accumulate in heap space, causing OOM errors
- **Cache pressure**: Leaked BDD nodes pollute the BDD node cache, reducing hit rates
- **Recursive growth**: BDD leaks compound recursively, creating unbounded growth
- **Dramatic performance differences**: See examples in the next section

#### Real-World Examples

These pull requests demonstrate the dramatic impact of proper BDD memory management:

1. [PR #7852](https://github.com/batfish/batfish/pull/7852) - Fixed missing `free()` calls in `Composite` transition
2. [PR #7820](https://github.com/batfish/batfish/pull/7820) - Fixed BDD garbage in reachability fixpoint computation
3. [PR #7847](https://github.com/batfish/batfish/pull/7847) - Fixed BDD factory to avoid internal garbage generation

Each of these PRs made small code changes (adding missing `free()`, using `id()` correctly, switching to in-place operations) that resulted in order-of-magnitude performance improvements.

### Guidelines

#### For Long-Lived BDDFactories

Code using long-lived `BDDFactory` instances (e.g., shared across an entire analysis) **must** eliminate all BDD garbage:

1. Use `With` operations when consuming temporary values
2. Use `Eq` operations when updating accumulators in-place
3. Explicitly `free()` all intermediate BDDs before returning
4. Use `id()` when you need independent copies for mutation
5. Never leak BDD references from loops or recursive calls

#### For Short-Lived BDDFactories

Code using short-lived `BDDFactory` instances (e.g., created and destroyed for a single small computation) can be more relaxed about memory management. However:

1. This relaxation is primarily due to legacy code, not best practice
2. We should improve this code over time
3. When touching such code, consider improving memory management
4. It's not urgent, but it's still valuable

### Common Pitfalls

1. **Same-object operations**: Never use `Eq` operations with the same object (e.g., `a.orEq(a)`)—this modifies the RHS while computing
2. **Loop accumulators**: Forgetting to use `With` or `Eq` in loops creates garbage on every iteration
3. **Returned references**: Forgetting that base operations return new references that must be freed
4. **Recursive calls**: Not freeing intermediate results before returning from recursive functions

### Verification

When modifying BDD-heavy code:

1. Review all base operations—ensure results are eventually freed
2. Look for loops—ensure no garbage is created per iteration
3. Check recursive calls—ensure all paths free intermediate values
4. Test with large inputs—memory leaks become obvious at scale
5. Profile heap usage—unexpected growth indicates leaks
