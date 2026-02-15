# Simpler FilterLineReachability results

@dhalperi 2018-12-12

**Update 2022-09-06:**
This document details the design of `FilterLineReachability` as implemented in [#2823](https://github.com/batfish/batfish/pull/2823). As of the time of updating, the algorithm has not changed much although it has been extended with new semantics such as for `AclAclLine`.

# Background

FilterLineReachability is noisy for a blocked line when the full space of packets it is intended to handle is matched by many lines. The goal of this doc is to produce concise, relevant, correct answers that report only the blocking lines that the user cares about.

Throughout this doc, we will consider the following 5 running examples. In each case, the last line (marked with *) is the line we are interested in reporting an answer for.


## Example 1 - DDOS - single blocker with many tiny overlaps

```
10 - deny ip 1.1.1.1/32 any
20 - deny ip 1.1.1.2/32 any
30 - deny ip 1.1.1.3/32 any
…
2550 - deny ip 1.1.1.255/32 any
3000 - permit ospf any any
*3010 - permit ospf any 224.0.0.6/32 
```

## Example 2 - Multiple partial overlaps

```
10 - permit ip any 1.2.3.0/25
20 - deny ip any 1.2.3.128/25
*30 - permit ip any 1.2.3.0/24
```

## Example 3 - Multiple blockers, not all used

```
10 - permit ip any 1.2.3.4/32
20 - deny tcp any any
*30 - permit tcp any 1.2.3.4/32
```

## Example 4 - One almost-blocker, one blocker

```
10 - deny tcp any 1.2.3.4/32 neq 80
20 - permit tcp any any
*30 - permit tcp any 1.2.3.4/32
```

## Example 5 - TCP established: a few metadata bits set

```
10 - permit tcp any any established   ! means ACK or RST is true.
20 - deny tcp any 1.2.3.4/32
*30 - permit tcp any 1.2.3.4/32 eq 80
```

# Output properties

Here's a relevant subset of the output columns in FilterLineReachability:

**Blocked Line**: integer like 30

**Blocking Lines**: list of integers, like {10, 20}

**Different action**: boolean meaning if the blocked line has a different action then some blocking line.


## Dan's intuition for answers:

**Example 1**: Blocked = 3010, blocking = 3000, different action = False

The tiny overlaps in lines 10-2550 are basically irrelevant; the permit ospf any any on line 3000 is the real blocker. Since 3000 is a permit line, both lines have the same action. 

**Example 2**: Blocked = 30, blocking = [10,20], different action = True

Only the combination of lines 10 and 20 block 30. At least one of them disagrees on the action, so different action is true.

**Example 3**: Blocked = 30, blocking = [10], different action = False

Line 10 actually handles all traffic that line 30 would – line 20 does not process any of that space of packets. Note that different action is False since line 10 permits all traffic that line 30 also intends to permit.

Including line 20 would be misleading because it can't actually deny a single packet, and would misrepresent the action being different.

**Example 4**: Blocked = 30, blocking = [10, 20], different action = True

Line 10 denies almost all of the traffic, but port 80 is allowed by line 20. Line 20 is a complete cover on its own, but if we exclude line 10 from the report, we'd misrepresent that the action is not different.

**Example 5**: Blocked = 30, blocking = [20], different action = True

Line 20 will prevent any inbound flows from being established on port 80, so line 10 is basically irrelevant. (Unless those flows can be established over some other path that does not traverse the ACL, then somehow end up on a path where they do traverse the ACL).


# Approaches

In each of the approaches below, I <span style="text-decoration:underline;">underline</span> where the answer disagrees with my intuition.


## A - Old approach - all terminating lines

If line N is blocked, report all lines 1..N-1 that actually terminate some of the flows that line N would terminate.

Example 1: <span style="text-decoration:underline;">all lines</span>, different action = <span style="text-decoration:underline;">True</span>

Example 2: all lines, different action = True

Example 3: 10 only, different = False

Example 4: <span style="text-decoration:underline;">20 only</span>, different = <span style="text-decoration:underline;">False</span>

Example 5: <span style="text-decoration:underline;">all lines</span>, different = True

**Discussion:** This approach disagrees with Dan for examples 1 & 5, especially for Example 1 – but in that case the answer is *really* wrong. Unfortunately, most real large ACLs look like Example 1 and 5 – there's a lot of small filters near the start.   (The difference between 1 and 5 will become clear in later approaches).


## B - Terminating lines weighted by actual packet space terminated

Let S be the total space of packets represented by the blocked line. Report all lines 1..N-1 that actually terminate at least a fraction _f_ of S – say, _f_ = 0.3.

Example 1: 3000, different action = False

Example 2: all lines, different action = True

Example 3: 10 only, different = False

Example 4: <span style="text-decoration:underline;">10 only</span>, different = True

Example 5: <span style="text-decoration:underline;">10 only</span>, different = <span style="text-decoration:underline;">False</span>

**Discussion**: Compared to A: this approach fixes example 1, deviates for example 4, and _ruins_ example 5.

Example 5 was especially illuminating for me – as the packet space is ignorant of the real relationships between header bits, **tcp established** is 3/4 of all possible packets (ACK | RST, rest don't care) and thus line 10 terminates 3/4 of all possible flows. For _f=0.3_, we would only report line 10; for lower fractions we would report both line 10 and line 20, which matches the current approach.

Note that this approach also may not report a full set of blocking lines, as you can see with example 5.


## C - Terminating lines weighted by space overlap with blocked line

Let S be the total space of packets represented by the blocked line. For each line L that terminates at least one packet, compute the size TL of the overlap of that line with the target space. Sort these lines by TL descending, and keep the smallest prefix of this list that covers the blocked line.

Example 1: 3000, different action = False

Example 2: all lines, different action = True

Example 3: 10 only, different = False

Example 4: <span style="text-decoration:underline;">20 only</span>, different = <span style="text-decoration:underline;">False</span>

Example 5: 20 only, different = True

**Discussion:** This approach has restored example 5, but now may be misleading about example 4. Because we sort by overlap with the blocking line and completely ignore how much of the flow is actually handled, we miss the fact that in example 4 most of the traffic is actually terminated by line 10.


## D - C + include the largest line with different action **[SELECTED/IMPLEMENTED in [#2823](github.com/batfish/batfish/pull/2823)]**

Use the approach in C, plus ensure that the largest terminating line with a different action than the blocked line is included in the answer when present. This adaptation makes sure that a user will see the largest contradicting line, if one exists, in all cases.

Example 1: <span style="text-decoration:underline;">[10,3000]</span>, different action = <span style="text-decoration:underline;">True</span>

Example 2: all lines, different action = True

Example 3: 10 only, different = False

Example 4: all lines, different = True

Example 5: 20 only, different = True

This algorithm recovers example 4, but adds the first DDOSer to the report for Example 1. Fundamentally, it seems hard to encode logic that gets these both right. It seems that we should always include some matching line with a different action if one exists – we can't tell what the user's intent is, so it's best to just tell them that the ACL behaves differently than the blocked line on at least some packets.


# Vendor-Specific Filtering

## Cisco FTD Two-Stage Filtering

Cisco FTD (Firepower Threat Defense) implements a two-stage filtering architecture using Prefilter policies:

**Stage 1 (Prefilter):** Rules with `trust` actions that fast-path traffic, bypassing regular ACL evaluation.

**Stage 2 (Regular ACL):** Standard permit/deny rules, evaluated only if no Prefilter rule matched.

**Example:**
```
10 - trust tcp 10.5.73.0/24 any eq 1994    # Prefilter (fast-path)
20 - permit tcp 10.5.73.0/24 any eq 80     # Regular ACL (never evaluated)
30 - permit tcp 10.5.73.0/24 any eq 443    # Regular ACL (never evaluated)
```

In this example, lines 20 and 30 are technically unreachable (shadowed by line 10), but this is **intentional behavior** - the Prefilter `trust` rule handles the traffic more efficiently.

**Implementation:**
- Prefilter trust rules are identified during FTD config conversion with metadata markers:
  - `"Prefilter-FTD"` in the policy name
  - `" trust "` action keyword (with spaces to avoid matching "trustworthy" etc)
- When a regular ACL line is blocked by a Prefilter trust rule, it is **not reported** as unreachable
- This reduces false positives by ~99% in real FTD configurations

**Reference:** [Cisco FTD Prefilter Policies Documentation](https://www.cisco.com/c/en/us/td/docs/security/firepower/660/configuration-guide/fpmg-cli-config-guide-660/as_a_acl_prefilter_policies.html)
