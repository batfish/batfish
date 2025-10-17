# BDD Best Practices

This guide covers essential practices for working with Binary Decision Diagrams (BDDs) in Batfish. BDDs are a core component of Batfish's symbolic analysis engine, and proper usage is critical for performance and correctness.

## Memory Management

### Overview

The JavaBDD library does not use Java's garbage collection. Instead, it requires explicit reference counting at the `BDDFactory` level for all BDD operations. Proper memory management is critical for BDD-heavy code—reference leaks prevent BDDs from being freed, consuming heap and cache space, and severely degrading performance.

Because BDDs are recursive structures, leaked references can create effectively unbounded memory growth. Real-world cases exist where adding a single missing `free()` call reduced memory usage from 100+ GiB (with heap overflow) and hours of runtime to under 100 MiB and milliseconds of execution time.

### Reference Counting Rules

#### Base Operations (Create New Reference)

Base BDD operations create **new** references and destroy **none**. Both the input and output BDD objects must be freed separately.

Examples: `bdd.or(other)`, `bdd.and(other)`, `bdd.not()`, `bdd.fullSatOne()`

#### In-Place Operations (No New Reference)

In-place operations modify the target BDD and create **no** new references. The suffix `Eq` indicates "equals" semantics.

Examples: `bdd.orEq(other)`, `bdd.andEq(other)`, `bdd.notEq()`

**CRITICAL**: Do not use with the same object as both operands (e.g., `a.andEq(a)`). Currently produces incorrect results; ideally the code should reject this.

#### Combined Operations (Consume One Reference)

Combined operations create no new references and automatically free one input. The suffix `With` indicates "consume with" semantics.

Example: `bdd.andWith(other)` is equivalent to `bdd.andEq(other); other.free();`

Note: `bdd.andWith(bdd)` is safe (unlike `bdd.andEq(bdd); bdd.free()` which frees `bdd` twice).

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

Default assumption: callees never free BDD parameters or return values unless documented. For complex call patterns, document ownership explicitly—particularly whether the caller must free the return value.

Example:
```java
/**
 * @return new BDD - caller must free
 */
```

Core computation loops must not create BDD garbage—the result is returned, intermediate values are freed.

### Real-World Examples

These pull requests demonstrate the impact of proper BDD memory management:

1. [PR #7852](https://github.com/batfish/batfish/pull/7852) - Fixed missing `free()` calls in `Composite` transition
2. [PR #7820](https://github.com/batfish/batfish/pull/7820) - Fixed BDD garbage in reachability fixpoint computation
3. [PR #7847](https://github.com/batfish/batfish/pull/7847) - Fixed BDD factory to avoid internal garbage generation

### Guidelines

Long-lived `BDDFactory` instances (shared across an entire analysis) **must** eliminate all BDD garbage:

1. Use `With` operations when consuming temporary values
2. Use `Eq` operations when updating accumulators in-place
3. Explicitly `free()` all intermediate BDDs before returning
4. Use `id()` when you need independent copies for mutation
5. Never leak BDD references from loops or recursive calls

Short-lived `BDDFactory` instances can be more relaxed, but we should improve this code over time.

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
