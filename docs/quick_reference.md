# Batfish Quick Reference

This guide provides quick reference for common Batfish commands, patterns, and workflows for both users and developers.

## Table of Contents

- [Pybatfish Reference](#pybatfish-reference)
  - [Snapshot Management](#snapshot-management)
  - [Reachability Analysis](#reachability-analysis)
  - [Filter Analysis](#filter-analysis)
  - [Routing Analysis](#routing-analysis)
  - [Configuration Questions](#configuration-questions)
  - [Differential Analysis](#differential-analysis)
- [Development Reference](#development-reference)
  - [Bazel Commands](#bazel-commands)
  - [Testing Commands](#testing-commands)
  - [Code Formatting](#code-formatting)
  - [Git Workflow](#git-workflow)
- [Common Patterns](#common-patterns)
- [Troubleshooting](#troubleshooting)
- [Keyboard Shortcuts](#keyboard-shortcuts)

---

## Pybatfish Reference

### Snapshot Management

#### Initialize Snapshot

```python
# From network directory
bf.init_snapshot('network_configs/', name='baseline')

# From compressed file
bf.init_snapshot('configs.tar.gz', name='snapshot1')

# From specific files
bf.init_snapshot('configs/router1.cfg', name='single')

# With additional files
bf.init_snapshot('configs/', name='snap',
                additional_files=['external_bgp_announcements.json'])
```

#### List and Check Snapshots

```python
# List all snapshots
snapshots = bf.list_snapshots()
print(snapshots)

# Check if snapshot exists
if bf.snapshot_exists('baseline'):
    print("Baseline snapshot exists")

# Get snapshot info
info = bf.get_snapshot_info('baseline')
print(f"Nodes: {info.num_nodes}")
print(f"Created: {info.creation_time}")
```

#### Delete Snapshot

```python
# Delete specific snapshot
bf.delete_snapshot('old_snapshot')

# Delete all snapshots
for snap in bf.list_snapshots():
    bf.delete_snapshot(snap)
```

### Reachability Analysis

#### Basic Reachability

```python
# Can host A reach host B?
result = bf.reachability(
    headerConstraints=HeaderConstraints(
        src_ips=ip_to_header_space("10.0.0.5"),
        dst_ips=ip_to_header_space("10.0.1.10"),
        dst_ports=[80],
        protocols=["TCP"]
    )
)

if result.hasFlows():
    print("Reachable!")
    print(f"Example flow: {result.flows()[0]}")
else:
    print("Not reachable")
```

#### Service Reachability from Multiple Sources

```python
# Can internet reach web server?
result = bf.reachability(
    headerConstraints=HeaderConstraints(
        dst_ips=ip_to_header_space("10.0.1.10"),
        dst_ports=[443],
        protocols=["TCP"]
    ),
    startLocation="internet",
    actions=["success"]  # Only successful flows
)

for flow in result.flows():
    print(f"Flow: {flow}")
    print(f"Path: {flow.traces()[0].get_hops()}")
```

#### Bidirectional Reachability

```python
# Check round-trip connectivity
result = bf.bidirectionalReachability(
    headerConstraints=HeaderConstraints(
        dst_ips=ip_to_header_space("10.0.1.10"),
        dst_ports=[22],
        protocols=["TCP"]
    ),
    startLocation="datacenter1"
)

# Returns flows that succeed both forward and return
for flow in result.flows():
    print(f"Full path: {flow}")
```

#### Verify Service Isolation

```python
# Verify service is NOT accessible from internet
result = bf.reachability(
    headerConstraints=HeaderConstraints(
        dst_ips=ip_to_header_space("10.0.10.0/24")  # Database
    ),
    startLocation="internet",
    returnPermissionsResult=True  # Return FAILING flows
)

if result.hasFlows():
    print("VIOLATION: Service accessible from internet")
    for flow in result.flows():
        print(f"  {flow}")
else:
    print("VERIFIED: Service is isolated")
```

### Filter Analysis

#### Search Filter Actions

```python
# Find what firewall denies
result = bf.searchFilters(
    headers=HeaderConstraints(
        dst_ports=[22],
        protocols=["TCP"]
    ),
    action="deny",
    filters="firewall_in"
)

print(f"Firewall denies {len(result.flows())} SSH flows")
for flow in result.flows()[:5]:  # First 5 examples
    print(f"  {flow}")
```

#### Find Unreachable Filter Lines

```python
# Find unused ACL rules
result = bf.filterLineReachability(
    filters=["acl_outside", "acl_inside"]
)

for filter_result in result:
    print(f"\nFilter: {filter_result.filter}")
    print(f"Unreachable lines: {filter_result.unreachableLines}")
```

#### Compare Filters

```python
# Compare two versions of a filter
result = bf.compareFilters(
    filters="acl_outside",
    snapshot1="before_change",
    snapshot2="after_change"
)

for diff in result:
    print(f"Line {diff.line1} vs {diff.line2}:")
    print(f"  Matching flows: {len(diff.flows)}")
    print(f"  Difference: {diff.difference}")
```

### Routing Analysis

#### Get Routes

```python
# All routes in network
routes = bf.get_routes()

# Routes for specific node
routes = bf.get_routes(nodes="router1")

# Routes for specific prefix
routes = bf.get_routes(prefix="10.0.1.0/24")

# Routes by protocol
routes = bf.get_routes(nodes="router1", protocol="bgp")

# Main routes only (not BGP RIB)
routes = bf.get_routes(nodes="router1", rib="main")
```

#### Get BGP Routes

```python
# All BGP routes
bgp_routes = bf.get_bgp_routes()

# BGP routes for specific router
bgp_routes = bf.get_bgp_routes(nodes="router1")

# BGP routes received from specific neighbor
bgp_routes = bf.get_bgp_routes(
    nodes="router1",
    remote_router="router2"
)

# BGP routes for specific prefix
bgp_routes = bf.get_bgp_routes(prefix="10.0.1.0/24")
```

#### Get BGP Process Info

```python
# BGP neighbors
neighbors = bf.get_bgp_neighbors()

# Specific router's neighbors
neighbors = bf.get_bgp_neighbors(nodes="router1")

# BGP peer status
for neighbor in neighbors:
    print(f"  {neighbor.local_ip} <-> {neighbor.remote_ip}")
    print(f"    AS: {neighbor.remote_as}")
    print(f"    State: {neighbor.peer_state}")
```

#### Search Route Policies

```python
# Find routes matched by policy
result = bf.searchRoutePolicies(
    headers=HeaderConstraints(
        dst_ips=ip_to_header_space("10.0.0.0/8")
    ),
    policies="export_policy",
    action="accept"
)

for route in result.routes():
    print(f"Accepted route: {route}")
```

### Configuration Questions

#### Get Interfaces

```python
# All interfaces
interfaces = bf.get_interfaces()

# Interfaces on specific node
interfaces = bf.get_interfaces(nodes="router1")

# Filter by interface name
interfaces = bf.get_interfaces(interface_names=["Ethernet1/1"])

# Filter by IP
interfaces = bf.get_interfaces(ip_addresses="10.0.1.1")
```

#### Get ACLs/Filters

```python
# All filters
filters = bf.get_filters()

# Specific filter
acl = bf.get_filters("acl_outside")

# Print filter rules
for line in acl:
    print(f"{line.index}: {line.action} {line.match_condition}")
```

#### Get OSPF Configuration

```python
# OSPF processes
ospf = bf.get_ospf_processes()

# OSPF for specific router
ospf = bf.get_ospf_processes(nodes="router1")

# OSPF areas
for process in ospf:
    print(f"Process: {process.process_id}")
    for area in process.areas:
        print(f"  Area: {area.area_id}")
```

#### Get NAT Rules

```python
# All NAT rules
nat_rules = bf.get_nat_rules()

# NAT for specific router
nat_rules = bf.get_nat_rules(nodes="router1")

# Print NAT rules
for rule in nat_rules:
    print(f"{rule.type}: {rule.src_ip} -> {rule.nat_ip}")
```

### Differential Analysis

#### Compare Reachability

```python
# What changed between snapshots?
result = bf.differentialReachability(
    headerConstraints=HeaderConstraints(
        dst_ports=[80, 443],
        protocols=["TCP"]
    ),
    snapshot1="before",
    snapshot2="after"
)

# Flows that worked before but not after
print(f"Broken flows: {len(result.onlyInSnapshot1)}")
for flow in result.onlyInSnapshot1[:5]:
    print(f"  BROKEN: {flow}")

# Flows that work after but didn't before
print(f"New flows: {len(result.onlyInSnapshot2)}")
for flow in result.onlyInSnapshot2[:5]:
    print(f"  NEW: {flow}")
```

#### Compare Routes

```python
# Route differences
diff = bf.differentialRoutes(
    snapshot1="baseline",
    snapshot2="modified"
)

# Routes that changed
for route_diff in diff:
    print(f"Route: {route_diff.prefix}")
    print(f"  Before: {route_diff.routes1}")
    print(f"  After: {route_diff.routes2}")
```

---

## Development Reference

### Bazel Commands

#### Build

```bash
# Build everything
bazel build //...

# Build specific target
bazel build //projects/batfish/src/main/java/com/example:MyClass

# Build with optimizations
bazel build -c opt //...

# Build specific package
bazel build //projects/batfish/...
```

#### Test

```bash
# Run all tests
bazel test //...

# Run specific test
bazel test //projects/batfish/src/test/java/com/example:MyTest

# Run specific test method
bazel test --test_filter=MyClass#myTestMethod //path/to:tests

# Run tests in verbose mode
bazel test --test_output=all //path/to:tests

# Run tests without cache
bazel test --nocache_test_results //path/to:tests

# Run multiple test targets
bazel test //projects/batfish:tests //projects/common:tests
```

#### Run Batfish Server

```bash
# Start Batfish server
./tools/bazel_run.sh //projects/batfish:batfish

# Start with specific port
./tools/bazel_run.sh //projects/batfish:batfish -p 9999

# Start with custom settings
./tools/bazel_run.sh //projects/batfish:batfish -coordinatorArgs="-workerThreads 4"
```

#### Bazel Utilities

```bash
# Clean build artifacts
bazel clean

# Clean everything (including external repos)
bazel clean --expunge

# Query dependencies
bazel query deps(//projects/batfish:batfish)

# Find all targets under a path
bazel query 'kind(.*_test, //projects/batfish/...)'

# Check configuration
bazel info

# Show Bazel version
bazel version
```

### Testing Commands

#### Run Specific Tests

```bash
# Run a specific test class
bazel test \
  --test_filter=org.batfish.grammar.flatjuniper.JunosMplsAdminGroupTest \
  //projects/batfish/src/test/java/org/batfish/grammar/flatjuniper:tests

# Run a specific test method
bazel test \
  --test_filter=org.batfish.grammar.flatjuniper.JunosMplsAdminGroupTest#testAdminGroupDefinitions \
  //projects/batfish/src/test/java/org/batfish/grammar/flatjuniper:tests
```

#### Run Reference Tests

```bash
# Update reference files
./tools/update_refs.sh

# Run reference tests
bazel test //tests/parsing-tests:ref_tests

# Run specific reference test
bazel test //tests/parsing-tests/org/batfish/parsing:parser_test
```

#### Test Configuration

```bash
# Run with specific CPU count
bazel test --jobs=4 //...

# Run with memory limit
bazel test --test_timeout=300 //...

# Run tests and show output
bazel test --test_output=errors //...

# Run flaky test multiple times
bazel test --flaky_test_attempts=3 //path/to:test
```

### Code Formatting

#### Pre-commit Hooks

```bash
# Install pre-commit
pip install pre-commit
pre-commit install

# Run pre-commit manually
pre-commit run --all-files

# Run on specific files
pre-commit run --files MyClass.java

# Update pre-commit hooks
pre-commit autoupdate
```

#### Manual Formatting

```bash
# Format Java files
bazel run //tools/formatter:formatter -- --replace $(git ls-files '*.java')

# Check Java formatting (dry-run)
bazel run //tools/formatter:formatter -- --dry-run $(git ls-files '*.java')

# Format specific file
bazel run //tools/formatter:formatter -- --replace src/main/java/com/example/MyClass.java

# Format BUILD files
bazel run //tools/formatter:buildifier
```

### Git Workflow

#### Feature Branch Workflow

```bash
# Create feature branch
git checkout -b feature/my-feature

# Make changes and commit
git add .
git commit -m "feat: add new feature"

# Push to remote
git push origin feature/my-feature

# Update branch with latest main
git fetch origin main
git rebase origin/main
```

#### Interactive Rebase

```bash
# Squash last 3 commits
git rebase -i HEAD~3

# Clean up commit history
# Change "pick" to "squash" or "fixup" for commits to squash
# Change "pick" to "drop" for commits to remove

# Continue after fixing conflicts
git add .
git rebase --continue
```

#### Resolve Merge Conflicts

```bash
# After conflict, mark files as resolved
git add <resolved-files>
git commit

# Or abort the merge
git merge --abort
```

---

## Common Patterns

### Pattern 1: Verify Configuration Change

```python
# Setup
baseline = bf.init_snapshot('configs/', name='baseline')
# ... make changes ...
test = bf.init_snapshot('configs_modified/', name='test')

# Verify intended effect
new_service = bf.reachability(
    headerConstraints=HeaderConstraints(dst_ports=[8080]),
    snapshot=test
)
assert new_service.hasFlows(), "New service not reachable"

# Verify no regressions
diff = bf.differentialReachability(
    headerConstraints=HeaderConstraints(dst_ports=[80, 443]),
    snapshot1=baseline,
    snapshot2=test
)
assert not diff.onlyInSnapshot1, "Regressions detected!"
```

### Pattern 2: Find Security Violations

```python
# Find paths bypassing firewall
result = bf.reachability(
    headerConstraints=HeaderConstraints(
        dst_ips=ip_to_header_space("10.0.10.0/24")  # Sensitive servers
    ),
    startLocation="internet",
    pathConstraints=PathConstraints(
        forbidLocations=["firewall"]  # Should go through firewall
    ),
    returnPermissionsResult=True
)

if result.hasFlows():
    print("SECURITY ISSUE: Traffic bypassing firewall")
    for flow in result.flows():
        print(f"  {flow}")
```

### Pattern 3: Validate Routing Policies

```python
# Verify route filtering
result = bf.searchRoutePolicies(
    headers=HeaderConstraints(
        dst_ips=ip_to_header_space("0.0.0.0/0")
    ),
    policies="edge_router_export",
    action="accept"
)

# Check if private IPs are leaked
private_prefixes = ["10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16"]
leaked_routes = [r for r in result.routes() if r.network in private_prefixes]

if leaked_routes:
    print("SECURITY: Leaking private IPs to internet")
    for route in leaked_routes:
        print(f"  {route}")
```

### Pattern 4: Test Redundancy

```python
# Verify multiple paths exist
result = bf.reachability(
    headerConstraints=HeaderConstraints(
        dst_ips=ip_to_header_space("10.0.1.10")
    ),
    startLocation="datacenter1"
)

paths = result.getDistinctPaths()
if len(paths) >= 2:
    print(f"VERIFIED: {len(paths)} redundant paths exist")
else:
    print("WARNING: Single point of failure")
```

### Pattern 5: Find Loops

```python
# Detect forwarding loops
loops = bf.detectLoops()

if loops.hasFlows():
    print("LOOPS DETECTED:")
    for flow in loops.flows():
        print(f"  {flow}")
        print(f"  Loop: {flow.traces()[0].get_loop()}")
```

### Pattern 6: Compare Configurations

```python
# Find configuration differences
diff = bf.differentialConfig(
    snapshot1="before",
    snapshot2="after",
    includeType=True,
    includeDescription=True
)

for change in diff:
    print(f"{change.hostname}: {change.property}")
    print(f"  Before: {change.oldValue}")
    print(f"  After: {change.newValue}")
```

---

## Troubleshooting

### Common Issues

#### OutOfMemoryError

**Problem:** Java runs out of heap space

**Solution:**
```bash
# Increase heap for Batfish server
export JAVA_OPTS="-Xmx20g"
./tools/bazel_run.sh //projects/batfish:batfish

# Or in Bazel
bazel test --jvmopt="-Xmx8g" //path/to:test
```

#### Snapshot Initialization Fails

**Problem:** `bf.init_snapshot()` fails

**Debug:**
```python
# Check for parsing errors
bf.init_snapshot('configs/', name='test', dryRun=True)

# Check specific files
bf.init_snapshot('configs/router1.cfg', name='test')

# Increase verbosity
import logging
logging.basicConfig(level=logging.DEBUG)
```

#### Test Failures

**Problem:** Test passes locally but fails in CI

**Debug:**
```bash
# Run without cache
bazel test --nocache_test_results //path/to:test

# Run multiple times
for i in {1..5}; do
  bazel test //path/to:test
done

# Run with verbose output
bazel test --test_output=all //path/to:test
```

#### Slow Analysis

**Problem:** Reachability analysis is slow

**Optimizations:**
```python
# Tighten constraints to reduce search space
result = bf.reachability(
    headerConstraints=HeaderConstraints(
        dst_ips=ip_to_header_space("10.0.1.10"),  # Specific
        dst_ports=[443],  # Specific port
        protocols=["TCP"],  # Specific protocol
        src_ports=range(1024, 65535)  # Reduce ephemeral range
    )
)
```

---

## Keyboard Shortcuts

### IntelliJ IDEA (Recommended IDE)

| Action | Mac | Windows/Linux |
|--------|-----|---------------|
| Find class | ⌘O | Ctrl+N |
| Find file | ⇧⌘O | Ctrl+Shift+N |
| Find action | ⇧⌘A | Ctrl+Shift+A |
| Go to definition | ⌘B | Ctrl+B |
| Find usages | ⌥F7 | Alt+F7 |
| Format code | ⌘⌥L | Ctrl+Alt+L |
| Optimize imports | ⌃⌥O | Ctrl+Alt+O |
| Run | Ctrl+R | Shift+F10 |
| Debug | Ctrl+D | Shift+F9 |
| Toggle breakpoint | ⌘F8 | Ctrl+F8 |

### Vim

| Action | Command |
|--------|---------|
| Save | `:w` |
| Quit | `:q` |
| Save and quit | `:wq` |
| Undo | `u` |
| Redo | `Ctrl+r` |
| Find | `/pattern` |
| Replace | `:s/old/new` |

### Git Bash

| Action | Command |
|--------|---------|
| Status | `git status` |
| Log | `git log --oneline` |
| Diff | `git diff` |
| Branch | `git branch` |
| Checkout | `git checkout` |
| Commit | `git commit` |
| Push | `git push` |
| Pull | `git pull` |

---

## Summary

**Key resources:**
- **Pybatfish**: Python client for Batfish analysis
- **Bazel**: Build system for compiling and testing
- **Pre-commit**: Automatic code formatting
- **Git**: Version control

**Common workflows:**
1. Make code changes → Format with pre-commit → Test with Bazel → Commit
2. Create snapshots → Run analysis questions → Verify results
3. Compare snapshots → Find differences → Validate changes

**Getting help:**
- [Batfish documentation](https://pybatfish.readthedocs.io/)
- [Batfish Slack](https://join.slack.com/t/batfish-org/shared_invite/)
- [GitHub issues](https://github.com/batfish/batfish/issues)
