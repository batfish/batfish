# BDD Best Practices

This guide covers essential practices for working with Binary Decision Diagrams (BDDs) in Batfish. BDDs are a core component of Batfish's symbolic analysis engine, and proper usage is critical for performance and correctness.

## Table of Contents

- [Memory Management](#memory-management)
- [Common BDD Operations](#common-bdd-operations)
- [BDDInteger Usage](#bddinteger-usage)
- [Performance Considerations](#performance-considerations)
- [Testing BDD Code](#testing-bdd-code)
- [Common Pitfalls](#common-pitfalls)
- [Real-World Examples](#real-world-examples)

---

## Memory Management

### Overview

The JavaBDD library does not use Java's garbage collection. Instead, it requires explicit reference counting at the `BDDFactory` level for all BDD operations. Proper memory management is critical for BDD-heavy code—reference leaks prevent BDDs from being freed, consuming heap and cache space, and severely degrading performance.

Because BDDs are recursive structures, leaked references can create effectively unbounded memory growth. Real-world cases exist where adding a single missing `free()` call reduced memory usage from 100+ GiB (with heap overflow) and hours of runtime to under 100 MiB and milliseconds of execution time.

### Reference Counting Rules

#### Base Operations (Create New Reference)

Base BDD operations create **new** references and destroy **none**. Both the input and output BDD objects must be freed separately.

**Examples:** `bdd.or(other)`, `bdd.and(other)`, `bdd.not()`, `bdd.fullSatOne()`

```java
BDD a = factory.ithVar(0);
BDD b = factory.ithVar(1);
BDD result = a.and(b);  // Creates new reference

// All three BDDs must be freed separately
a.free();
b.free();
result.free();
```

#### In-Place Operations (No New Reference)

In-place operations modify the target BDD and create **no** new references. The suffix `Eq` indicates "equals" semantics.

**Examples:** `bdd.orEq(other)`, `bdd.andEq(other)`, `bdd.notEq()`

```java
BDD a = factory.ithVar(0);
BDD b = factory.ithVar(1);

a.andEq(b);  // Modifies 'a', consumes no new references
// Only 'a' and 'b' need to be freed

a.free();
b.free();
```

**CRITICAL**: Do not use with the same object as both operands (e.g., `a.andEq(a)`). This produces incorrect results because the RHS is modified during computation.

#### Combined Operations (Consume One Reference)

Combined operations create no new references and automatically free one input. The suffix `With` indicates "consume with" semantics.

**Example:** `bdd.andWith(other)` is equivalent to `bdd.andEq(other); other.free();`

```java
BDD a = factory.ithVar(0);
BDD b = factory.ithVar(1);

a.andWith(b);  // Modifies 'a' and frees 'b'
// Only 'a' needs to be freed (b is already freed)

a.free();
```

**Note:** `a.andWith(a)` is safe (unlike `a.andEq(a)` which frees `a` twice).

#### Copying References

Use `bdd.id()` to create a copy of a BDD reference. This is useful when you need an intermediate value that can be freed independently (e.g., as an accumulator in a loop).

```java
BDD accumulator = factory.zero().id();  // Create a copy we can mutate
for (int i = 0; i < n; i++) {
  BDD term = computeTerm(i);
  accumulator.orWith(term);  // Updates accumulator, frees term
}
// Use accumulator, then free it when done
accumulator.free();
```

### Ownership and Documentation

**Default assumption**: Callees never free BDD parameters or return values unless documented. For complex call patterns, document ownership explicitly—particularly whether the caller must free the return value.

**Example documentation:**
```java
/**
 * Computes the set of packets matching a condition.
 *
 * @param factory the BDDFactory to use
 * @return new BDD - caller must free
 */
public static BDD computeMatchingPackets(BDDFactory factory) {
    BDD result = ...;
    return result;  // Caller is responsible for freeing
}
```

**Core computation loops** must not create BDD garbage—the result is returned, intermediate values are freed.

### Memory Management Guidelines

**Long-lived `BDDFactory` instances** (shared across an entire analysis) **must** eliminate all BDD garbage:

1. Use `With` operations when consuming temporary values
2. Use `Eq` operations when updating accumulators in-place
3. Explicitly `free()` all intermediate BDDs before returning
4. Use `id()` when you need independent copies for mutation
5. Never leak BDD references from loops or recursive calls

**Short-lived `BDDFactory` instances** can be more relaxed, but should be improved over time.

### Verification

When modifying BDD-heavy code:

1. Review all base operations—ensure results are eventually freed
2. Look for loops—ensure no garbage is created per iteration
3. Check recursive calls—ensure all paths free intermediate values
4. Use `factory.numOutstandingBDDs()` to detect leaks

```java
long before = factory.numOutstandingBDDs();
BDD result = operation();
long after = factory.numOutstandingBDDs();
assertThat(after - before, equalTo(1));  // Should have exactly one new BDD
result.free();
```

---

## Common BDD Operations

### Logical Operations

```java
// Basic operations (create new reference)
BDD and = a.and(b);
BDD or = a.or(b);
BDD not = a.not();
BDD xor = a.xor(b);
BDD imp = a.imp(b);

// In-place operations (modify existing, no new reference)
a.andEq(b);
a.orEq(b);
a.notEq();

// Combined operations (consume one reference)
a.andWith(b);   // Equivalent to: a.andEq(b); b.free();
a.orWith(b);    // Equivalent to: a.orEq(b); b.free();
```

### Quantification

```java
// Existential quantification (∃)
BDD existsX = bdd.exist(x);

// Universal quantification (∀)
BDD forallX = bdd.forAll(x);

// Unique quantification (unique assignment)
BDD uniqueX = bdd.unique(x);
```

### Restriction and Substitution

```java
// Restrict variable to specific value
BDD restricted = bdd.restrict(var, value);

// Replace variables according to pairing
BDDPairing pairing = BDDUtils.swapPairing(srcVar, dstVar);
BDD replaced = bdd.replace(pairing);
```

### Satisfaction

```java
// Check if BDD is satisfiable
boolean isSat = bdd.isSat();

// Get one satisfying assignment
BDD satOne = bdd.fullSatOne();
satOne.free();

// Get all satisfying assignments (use with caution!)
Iterator<BDD> allSat = bdd.allsat();
```

---

## BDDInteger Usage

`BDDInteger` represents symbolic integers using BDD bitvectors.

### Creating BDDIntegers

```java
// Create a 32-bit integer
int numBits = 32;
int varStart = FIRST_PACKET_VAR;
BDDInteger ip = new BDDInteger(factory, numBits, varStart, false);
```

### Common Operations

```java
// Equality
BDD equals = ip.value(10);  // ip == 10

// Less than or equal
BDD leq = ip.leq(10);       // ip <= 10

// Greater than
BDD gt = ip.leq(10).not();  // ip > 10

// Range checks
BDD inRange = ip.leq(100).and(ip.geq(0));  // 0 <= ip <= 100

// Addition (creates new BDDInteger)
BDDInteger sum = ip.add(other);
```

### Primed BDDInteger

`PrimedBDDInteger` represents state transitions (e.g., NAT transformations):

```java
PrimedBDDInteger srcIp = new PrimedBDDInteger(factory, 32, startVar);

// Get unprimed version
BDDInteger unprimed = srcIp;

// Get primed version (state after transformation)
BDDInteger primed = srcIp.getPrimed();

// Set primed value based on unprimed
primed.setValue( factory.ithVar(100) );  // Some transformation
```

---

## Performance Considerations

### Factory Initialization

Proper factory initialization is critical for performance:

```java
// Recommended settings for BDDPacket
private static final int INITIAL_NODE_TABLE_SIZE = 1_000_000;
private static final int CACHE_RATIO = 8;
private static final int INITIAL_CACHE_SIZE =
    (INITIAL_NODE_TABLE_SIZE + CACHE_RATIO - 1) / CACHE_RATIO;

BDDFactory factory = JFactory.init(INITIAL_NODE_TABLE_SIZE, INITIAL_CACHE_SIZE);
factory.setCacheRatio(CACHE_RATIO);
```

**Cache Ratio Guidelines:**
- **Small problems (100-1000 variables)**: Use ratio 64
- **Medium problems (1000-10000 variables)**: Use ratio 8-16
- **Large problems (10000+ variables)**: Use ratio 1-4

### Variable Ordering

Variable ordering significantly affects BDD size. Poor ordering can cause exponential blowup.

**Good ordering:** Group related variables together
```java
// Packet field ordering (from BDDPacket)
FIRST_PACKET_VAR = 100
DSCP_BITS = 6 bits (vars 100-105)
IP_PROTOCOL = 8 bits (vars 106-113)
DST_IP = 32 bits (vars 114-145)
SRC_IP = 32 bits (vars 146-177)
// ... etc
```

**Best practices:**
- Place frequently-tested variables near the root
- Group related fields together
- Keep independent variables far apart
- Consider the decision tree structure

### Reuse and Caching

**BDD reuse:**
```java
// Enable BDD reuse
factory.enableReorder();
```

**Cache intermediate results:**
```java
// Cache complex computations in maps
Map<Key, BDD> cache = new HashMap<>();
BDD result = cache.computeIfAbsent(key, k -> expensiveComputation());
```

### Performance Monitoring

```java
// Monitor BDD count
long count = factory.numberOfNodes();

// Monitor outstanding references
long outstanding = factory.numOutstandingBDDs();

// Monitor cache statistics
int cacheSize = factory.getCacheSize();
int usedCache = factory.getUsedCache();
```

---

## Testing BDD Code

### Memory Leak Detection

Always verify that BDD references are properly managed:

```java
@Test
public void testNoMemoryLeaks() {
    BDDFactory factory = JFactory.init(1000, 100);
    long before = factory.numOutstandingBDDs();

    // Run operation
    BDD result = operationUnderTest(factory);

    long after = factory.numOutstandingBDDs();

    // Should have exactly one new BDD (the result)
    assertEquals(before + 1, after);

    // Clean up
    result.free();

    // All BDDs should be freed
    assertEquals(before, factory.numOutstandingBDDs());
}
```

### Logical Correctness

Test fundamental BDD properties:

```java
@Test
public void testDeMorgansLaw() {
    BDD x = factory.ithVar(0);
    BDD y = factory.ithVar(1);

    // ¬(x ∨ y) = ¬x ∧ ¬y
    BDD lhs = x.or(y).not();
    BDD rhs = x.not().and(y.not());

    assertEquals(factory.zero(), lhs.xor(rhs));

    x.free();
    y.free();
    lhs.free();
    rhs.free();
}

@Test
public void testDistributiveLaw() {
    BDD x = factory.ithVar(0);
    BDD y = factory.ithVar(1);
    BDD z = factory.ithVar(2);

    // x ∧ (y ∨ z) = (x ∧ y) ∨ (x ∧ z)
    BDD lhs = x.and(y.or(z));
    BDD rhs = x.and(y).or(x.and(z));

    assertEquals(factory.zero(), lhs.xor(rhs));

    x.free();
    y.free();
    z.free();
    lhs.free();
    rhs.free();
}
```

### Edge Cases

Test boundary conditions:

```java
@Test
public void testEmptySet() {
    BDD zero = factory.zero();
    assertFalse(zero.isSat());
    zero.free();
}

@Test
public void testUniversalSet() {
    BDD one = factory.one();
    assertTrue(one.isSat());
    one.free();
}

@Test
public void testSingleVariable() {
    BDD var = factory.ithVar(0);
    assertTrue(var.isSat());

    BDD notVar = var.not();
    assertTrue(notVar.isSat());

    BDD contradiction = var.and(notVar);
    assertFalse(contradiction.isSat());

    var.free();
    notVar.free();
    contradiction.free();
}
```

---

## Common Pitfalls

### 1. Same-Object Operations with Eq

**Wrong:**
```java
a.andEq(a);  // Modifies 'a' during computation
```

**Correct:**
```java
BDD result = a.and(a.id());  // Create copy first
// Or simply: result = a.id();
a.free();
// Use result
```

### 2. Loop Accumulator Without With

**Wrong (creates garbage):**
```java
BDD accumulator = factory.zero();
for (BDD term : terms) {
    accumulator = accumulator.or(term);  // New reference each iteration!
    // Previous accumulator leaked
}
```

**Correct:**
```java
BDD accumulator = factory.zero().id();  // Create mutable copy
for (BDD term : terms) {
    accumulator.orWith(term);  // In-place, frees term
}
// Use accumulator, then free
accumulator.free();
```

### 3. Forgetting to Free Return Values

**Wrong:**
```java
BDD result = computeSomething();
// Use result
// Forgot to free!
```

**Correct:**
```java
BDD result = computeSomething();
try {
    // Use result
} finally {
    result.free();
}
```

### 4. Double Free

**Wrong:**
```java
a.andWith(b);  // Frees 'b'
b.free();      // Double free!
```

**Correct:**
```java
a.andWith(b);  // Frees 'b' automatically
// Don't free 'b' again
```

### 5. Using Freed BDDs

**Wrong:**
```java
BDD result = a.and(b);
a.free();
b.free();
result.orWith(a);  // 'a' is already freed!
```

**Correct:**
```java
BDD result = a.and(b);
BDD copy = result.id();
a.free();
b.free();
result.orWith(copy);  // Use the copy
result.free();
copy.free();
```

---

## Real-World Examples

### Pattern 1: Accumulating Transitions

From `CompositeTransition.java`:

```java
public BDD transitForward(BDD bdd) {
    BDD result = bdd.id();  // Create copy for mutation
    for (Transition transition : _transitions) {
        BDD nextResult = transition.transitForward(result);
        result.free();         // Free previous result
        result = nextResult;   // Use new result
    }
    return result;  // Caller must free
}
```

### Pattern 2: Conditional Construction

```java
public BDD buildConstraint(Condition cond) {
    BDD constraint = factory.one().id();  // Start with true

    for (Clause clause : cond.getClauses()) {
        BDD clauseBdd = evaluateClause(clause);
        constraint.andWith(clauseBdd);  // Consumes clauseBdd
    }

    return constraint;  // Caller must free
}
```

### Pattern 3: Fixpoint Computation

From `BDDReachabilityUtils.java`:

```java
public static Map<StateExpr, BDD> fixpoint(
    Table<StateExpr, StateExpr, Transition> edges,
    Set<StateExpr> initialStates,
    BDDPacket packet) {

    Map<StateExpr, BDD> reachable = new HashMap<>();
    Queue<StateExpr> worklist = new LinkedList<>();

    // Initialize
    for (StateExpr state : initialStates) {
        BDD bdd = packet.getHeaderSpaceToBDD();
        reachable.put(state, bdd);
        worklist.add(state);
    }

    // Fixpoint iteration (no garbage created)
    while (!worklist.isEmpty()) {
        StateExpr current = worklist.remove();
        BDD currentBdd = reachable.get(current);

        for (Map.Entry<StateExpr, Transition> entry : edges.row(current).entrySet()) {
            StateExpr nextState = entry.getKey();
            Transition transition = entry.getValue();

            BDD newBdd = transition.transitForward(currentBdd);

            BDD existingBdd = reachable.get(nextState);
            if (existingBdd == null) {
                reachable.put(nextState, newBdd);
                worklist.add(nextState);
            } else {
                // Check if new state adds anything
                BDD combined = existingBdd.or(newBdd);
                newBdd.free();

                if (!combined.equals(existingBdd)) {
                    existingBdd.free();
                    reachable.put(nextState, combined);
                    worklist.add(nextState);
                } else {
                    combined.free();
                }
            }
        }
    }

    return reachable;  // Caller must free all BDDs
}
```

### Memory Leak Fixes

These pull requests demonstrate the impact of proper BDD memory management:

1. **[PR #7852](https://github.com/batfish/batfish/pull/7852)** - Fixed missing `free()` calls in `Composite` transition
   - Reduced memory from 100+ GiB to under 100 MiB
   - Reduced runtime from hours to milliseconds

2. **[PR #7820](https://github.com/batfish/batfish/pull/7820)** - Fixed BDD garbage in reachability fixpoint computation
   - Eliminated per-iteration garbage in core loop
   - Improved scalability for large networks

3. **[PR #7847](https://github.com/batfish/batfish/pull/7847)** - Fixed BDD factory to avoid internal garbage generation
   - Reduced internal BDD node creation
   - Improved cache efficiency

---

## Related Documentation

- [Symbolic Engine](../symbolic_engine/README.md): Overview of symbolic analysis in Batfish
- [Flow Dispositions](../flow_dispositions/README.md): How BDDs determine flow outcomes
- [Forwarding Analysis](../forwarding_analysis/README.md): BDD usage in forwarding analysis

---

## Summary

**Critical rules for BDD code:**

1. **Every `BDD` object must be freed exactly once**
2. **Use `With` operations to consume temporary values**
3. **Use `Eq` operations for in-place updates (never with same object)**
4. **Base operations create new references that must be freed**
5. **Use `id()` to create independent copies**
6. **Test with `numOutstandingBDDs()` to catch leaks**
7. **Document ownership in Javadoc for complex methods**
8. **Prefer immutable patterns when possible**
