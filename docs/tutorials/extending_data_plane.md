# Extending the Data Plane

**Time**: 2-3 hours | **Difficulty**: Advanced

This tutorial teaches you how to extend Batfish's data plane computation. You'll learn how to add support for a new routing protocol or custom forwarding logic.

## What You'll Learn

By the end of this tutorial, you'll understand:
- How the IBDP (Incremental Batfish Data Plane) algorithm works
- How routing protocols are processed
- How to add custom forwarding logic
- How to test data plane changes

**Note**: This is advanced content. You should be comfortable with:
- Java programming
- Network protocols (BGP, OSPF, etc.)
- Batfish architecture

---

## Prerequisites

Before starting, ensure you have:

1. **Strong Java skills**: Comfortable with complex Java code
2. **Protocol knowledge**: Understand routing protocols (BGP, OSPF, etc.)
3. **Read data plane docs**: [Data Plane README](../data_plane/README.md)
4. **Read symbolic engine docs**: [Symbolic Engine README](../symbolic_engine/README.md)

**Not ready yet?** Try these first:
- [Writing a Custom Question](writing_custom_questions.md)
- [Debugging Parser Issues](debugging_parser_issues.md)

---

## Overview: The Data Plane

The data plane answers: "Given this network state, how will packets flow?"

### Key Components

```
┌─────────────────────────────────────────┐
│      Configuration (Control Plane)      │
│  - BGP sessions                         │
│  - OSPF areas                           │
│  - Static routes                        │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│     Data Plane Computation (IBDP)       │
│  - Compute RIBs                         │
│  - Resolve next-hops                    │
│  - Compute FIBs                         │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│     Forwarding Analysis                 │
│  - Trace packet paths                   │
│  - Check reachability                   │
└─────────────────────────────────────────┘
```

### IBDP Algorithm

1. **Initialize**: Start with configuration
2. **Iterate**: Compute routes until convergence
3. **Detect oscillation**: Identify non-converging scenarios
4. **Output**: Final forwarding tables

---

## Step 1: Understand the Codebase

### Key Packages

```
projects/batfish/src/main/java/org/batfish/dataplane/
├── ibdp/                          # IBDP algorithm
│   ├── IncrementalDataPlane.java  # Main entry point
│   └── ...
├── router/                        # Router abstraction
│   └── AbstractRouter.java
└── protocol/                      # Protocol processing
    ├── BgpRouter.java
    ├── OspfRouter.java
    └── ...
```

### Key Classes

- **IncrementalDataPlane**: Main data plane computation engine
- **AbstractRouter**: Per-router route computation
- **Rib**: Routing Information Base
- **RibRoute**: Individual route entry

---

## Step 2: Example: Adding Custom Route Selection

Let's add support for a custom route selection policy: "Prefer routes with a specific community tag."

### Step 2.1: Define Community Preference

Create: `projects/batfish/src/main/java/org/batfish/dataplane/CommunityPreference.java`

```java
package org.batfish.dataplane;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Preference for routes based on BGP community.
 */
public class CommunityPreference implements Comparable<CommunityPreference> {

    private final long _community;
    private final int _preference;

    public CommunityPreference(long community, int preference) {
        _community = community;
        _preference = preference;
    }

    @Nonnull
    public Long getCommunity() {
        return _community;
    }

    public int getPreference() {
        return _preference;
    }

    @Override
    public int compareTo(CommunityPreference o) {
        // Higher preference is better
        return Integer.compare(o._preference, _preference);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommunityPreference)) {
            return false;
        }
        CommunityPreference that = (CommunityPreference) o;
        return _community == that._community;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_community);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("community", _community)
            .add("preference", _preference)
            .toString();
    }
}
```

---

### Step 2.2: Modify Route Comparison

Routes are compared in `Rib.java`. We'll modify the comparison logic.

Find the route comparison logic in `Rib.java`:

```java
// Existing comparison logic (simplified)
private int compareRoutes(RibRoute lhs, RibRoute.Builder rhs) {
    // 1. Compare admin distance
    int cmp = Integer.compare(lhs.getAdmin(), rhs.getAdmin());
    if (cmp != 0) {
        return cmp;
    }

    // 2. Compare metric
    cmp = Long.compare(lhs.getMetric(), rhs.getMetric());
    if (cmp != 0) {
        return cmp;
    }

    // 3. Compare router ID
    // ... existing logic ...
}
```

Add community preference:

```java
private int compareRoutes(
    RibRoute lhs,
    RibRoute.Builder rhs,
    @Nullable Map<Long, Integer> communityPreferences
) {
    // 1. Compare admin distance
    int cmp = Integer.compare(lhs.getAdmin(), rhs.getAdmin());
    if (cmp != 0) {
        return cmp;
    }

    // NEW: Compare community preferences
    if (communityPreferences != null && !communityPreferences.isEmpty()) {
        cmp = compareCommunityPreferences(lhs, rhs, communityPreferences);
        if (cmp != 0) {
            return cmp;
        }
    }

    // 2. Compare metric
    cmp = Long.compare(lhs.getMetric(), rhs.getMetric());
    if (cmp != 0) {
        return cmp;
    }

    // ... rest of comparison ...
}

private int compareCommunityPreferences(
    RibRoute lhs,
    RibRoute.Builder rhs,
    Map<Long, Integer> communityPreferences
) {
    Integer lhsPref = getCommunityPreference(lhs, communityPreferences);
    Integer rhsPref = getCommunityPreference(rhs, communityPreferences);

    if (lhsPref == null && rhsPref == null) {
        return 0;
    }
    if (lhsPref == null) {
        return 1; // Prefer route with community
    }
    if (rhsPref == null) {
        return -1;
    }
    return Integer.compare(rhsPref, lhsPref); // Higher preference wins
}

@Nullable
private Integer getCommunityPreference(
    RibRoute route,
    Map<Long, Integer> communityPreferences
) {
    for (long community : route.getCommunities()) {
        if (communityPreferences.containsKey(community)) {
            return communityPreferences.get(community);
        }
    }
    return null;
}
```

---

### Step 2.3: Pass Community Preferences

Modify `IncrementalDataPlane.java` to accept and pass community preferences:

```java
public class IncrementalDataPlane {

    private final Map<String, Map<Long, Integer>> _communityPreferences;

    public IncrementalDataPlane(
        // ... existing parameters ...
        Map<String, Map<Long, Integer>> communityPreferences
    ) {
        // ... existing initialization ...
        _communityPreferences = communityPreferences;
    }

    private void computeRibs() {
        for (Entry<String, Node> e : _nodes.entrySet()) {
            String hostname = e.getKey();
            Node node = e.getValue();

            // Get community preferences for this router
            Map<Long, Integer> prefs = _communityPreferences.getOrDefault(
                hostname,
                Collections.emptyMap()
            );

            // Pass to router
            AbstractRouter router = node.getRouter();
            router.computeRib(_fib, prefs);
        }
    }
}
```

---

## Step 3: Adding a New Protocol

Let's add support for a hypothetical "CUSTOM-PROTOCOL" routing protocol.

### Step 3.1: Define Protocol Constants

Create: `projects/batfish/src/main/java/org/batfish/datamodel/CustomProtocol.java`

```java
package org.batfish.datamodel;

import javax.annotation.Nonnull;

/**
 * Custom routing protocol.
 */
public enum CustomProtocol implements RoutingProtocol {
    INSTANCE;

    @Override
    @Nonnull
    public String protocolName() {
        return "custom";
    }

    @Override
    public int defaultAdminDistance() {
        return 150; // Between OSPF (110) and BGP (200)
    }

    @Override
    public boolean isDynamic() {
        return true; // It's a dynamic protocol
    }
}
```

---

### Step 3.2: Add Router Logic

Create: `projects/batfish/src/main/java/org/batfish/dataplane/protocol/CustomProtocolRouter.java`

```java
package org.batfish.dataplane.protocol;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.dataplane.Rib;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.CustomProtocol;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.RoutingProtocol;

/**
 * Router for CUSTOM-PROTOCOL.
 */
public class CustomProtocolRouter {

    private final Configuration _config;
    private final Rib _mainRib;

    public CustomProtocolRouter(Configuration config, Rib mainRib) {
        _config = config;
        _mainRib = mainRib;
    }

    public void computeRib() {
        // 1. Get CUSTOM-PROTOCOL routes from configuration
        Set<CustomRoute> customRoutes = getCustomRoutes();

        // 2. Convert to RIB routes
        for (CustomRoute route : customRoutes) {
            RibRoute.Builder ribRoute = RibRoute.builder()
                .setNetwork(route.getNetwork())
                .setNextHopIp(route.getNextHop())
                .setAdmin(CustomProtocol.INSTANCE.defaultAdminDistance())
                .setProtocol(CustomProtocol.INSTANCE)
                .setMetric(route.getMetric());

            _mainRib.mergeRoute(ribRoute);
        }
    }

    private Set<CustomRoute> getCustomRoutes() {
        // Extract custom routes from configuration
        // This depends on how CUSTOM-PROTOCOL is configured
        return ImmutableSet.of();
    }

    /**
     * Internal representation of a CUSTOM-PROTOCOL route.
     */
    private static class CustomRoute {
        private final Ip _network;
        private final Ip _nextHop;
        private final long _metric;

        // Constructor, getters, etc.
    }
}
```

---

### Step 3.3: Integrate with Main Router

Modify `AbstractRouter.java` to call your custom protocol router:

```java
public abstract class AbstractRouter {

    protected void computeRib(Rib mainRib, Map<Long, Integer> communityPrefs) {
        // ... existing protocol computations ...

        // NEW: Compute CUSTOM-PROTOCOL routes
        if (_config.getCustomProtocolSettings() != null) {
            CustomProtocolRouter customRouter = new CustomProtocolRouter(_config, mainRib);
            customRouter.computeRib();
        }
    }
}
```

---

## Step 4: Testing Your Changes

### Unit Tests

Create: `projects/batfish/src/test/java/org/batfish/dataplane/CommunityPreferenceTest.java`

```java
package org.batfish.dataplane;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public class CommunityPreferenceTest {

    @Test
    public void testCommunityPreference() {
        CommunityPreference pref = new CommunityPreference(65001L, 100);

        assertEquals(Long.valueOf(65001L), pref.getCommunity());
        assertEquals(100, pref.getPreference());
    }

    @Test
    public void testCompareTo() {
        CommunityPreference p1 = new CommunityPreference(65001L, 100);
        CommunityPreference p2 = new CommunityPreference(65002L, 200);
        CommunityPreference p3 = new CommunityPreference(65001L, 50);

        // Higher preference wins
        assertTrue(p2.compareTo(p1) > 0);
        assertTrue(p1.compareTo(p3) > 0);
        assertEquals(0, p1.compareTo(p1));
    }
}
```

---

### Integration Tests

Create a test with a network configuration:

```java
@Test
public void testDataPlaneWithCommunityPreference() {
    // 1. Create test network
    Configuration c1 = new Configuration();
    c1.setHostname("router1");

    // 2. Configure BGP with communities
    BgpProcess bgp = new BgpProcess();
    bgp.setAs(65001L);
    BgpNeighbor neighbor = new BgpNeighbor();
    neighbor.setIp(Ip.parse("10.0.0.2"));
    neighbor.setAs(65002L);
    bgp.setNeighbors(ImmutableMap.of("10.0.0.2", neighbor));
    c1.setBgpProcess(bgp);

    // 3. Set up community preferences
    Map<String, Map<Long, Integer>> communityPrefs = ImmutableMap.of(
        "router1",
        ImmutableMap.of(65001L, 100) // Prefer routes with community 65001:100
    );

    // 4. Compute data plane
    IncrementalDataPlane dp = new IncrementalDataPlane(
        ImmutableMap.of("router1", c1),
        communityPrefs
    );
    dp.computeDataPlane();

    // 5. Verify route selection
    Rib rib = dp.getRib("router1");
    // Assert that preferred route is selected
}
```

---

## Step 5: Debugging Data Plane Issues

### Enable Detailed Logging

```bash
bazel run --jvmopt="-Dloglevel=TRACE -Dlogger.org.batfish.dataplane=TRACE" \
  //projects/allinone:allinone_main
```

### Check Route Computation

```java
// In your code, add debug logging
private void computeRib() {
    _logger.debugf("Computing RIB for %s", _config.getHostname());

    int before = _mainRib.getRoutes().size();
    // ... compute ...
    int after = _mainRib.getRoutes().size();

    _logger.debugf("RIB size: %d -> %d", before, after);
}
```

### Verify Convergence

```java
@Test
public void testOscillationDetection() {
    IncrementalDataPlane dp = new IncrementalDataPlane(configs, prefs);

    // Should converge without oscillation
    DataPlane result = dp.computeDataPlane();

    assertNotNull(result);
    assertFalse(dp.hasOscillation());
}
```

---

## Common Issues and Solutions

### Issue: Routes Not Appearing in RIB

**Symptoms**: Expected routes missing from forwarding table.

**Debug**:
1. Check if routes are being generated:
   ```java
   _logger.debug("Generated {} custom routes", customRoutes.size());
   ```

2. Check if routes are being merged:
   ```java
   boolean merged = _mainRib.mergeRoute(ribRoute);
   _logger.debug("Route merged: {}", merged);
   ```

3. Check if routes are being filtered by existing routes.

---

### Issue: Oscillation Detection Triggers

**Symptoms**: `OscillationException` thrown.

**Causes**:
1. Route maps creating feedback loops
2. Asymmetric policies
3. Bugs in route comparison logic

**Fixes**:
1. Simplify route maps
2. Add explicit admin distance tie-breakers
3. Review comparison logic for bugs

---

### Issue: Performance Degradation

**Symptoms**: Data plane computation very slow.

**Solutions**:
1. Reduce route computation iterations
2. Optimize route comparison logic
3. Use incremental updates instead of recomputation

---

## Quick Reference

### Key Files

```
Data plane: projects/batfish/src/main/java/org/batfish/dataplane/
├── ibdp/IncrementalDataPlane.java  # Main entry point
├── router/AbstractRouter.java       # Router abstraction
├── Rib.java                         # Route computation
└── protocol/                        # Protocol implementations
    ├── BgpRouter.java
    ├── OspfRouter.java
    └── ...
```

### Testing Commands

```bash
# Unit tests
bazel test //projects/batfish/src/test/java/org/batfish/dataplane/...

# Integration tests
bazel test //projects/batfish/src/test/java/org/batfish/dataplane/ibdp/...

# With logging
bazel test --test_output=streamed \
  --jvmopt="-Dloglevel=DEBUG -Dlogger.org.batfish.dataplane=DEBUG" \
  //projects/batfish/src/test/...
```

---

## Advanced Topics

### Incremental Data Plane Updates

For large networks, recomputing the entire data plane is expensive. Batfish supports incremental updates:

```java
// Compute initial data plane
DataPlane dp = incrementalDataPlane.computeDataPlane();

// Make configuration change
c1.getInterfaces().get("eth0").setAdminUp(false);

// Compute incrementally
DataPlane newDp = incrementalDataPlane.computeDataPlaneIncremental(dp);
```

### Parallel Data Plane Computation

For very large networks, you can parallelize computation:

```bash
# Increase parallelism
bazel run --jvmopt=-Dbatfish.dp.workers=16 //projects/allinone:allinone_main
```

### Custom Forwarding Logic

You can add custom forwarding behavior by modifying the flow traversal logic in `FlowTracer.java`.

---

## Next Steps

Now that you understand data plane extension:

1. **Contribute protocol support**: Add missing protocols (IS-IS, RIP, etc.)
2. **Optimize performance**: Improve IBDP algorithm
3. **Add verification**: Check for common misconfigurations
4. **Document findings**: Share knowledge with community

---

## Related Documentation

- [Data Plane README](../data_plane/README.md)
- [Symbolic Engine README](../symbolic_engine/README.md)
- [Performance Tuning](../performance.md)
- [Troubleshooting](../troubleshooting.md)
- [Development Guide](../development/README.md)
