# Writing a Custom Question

**Time**: 1-2 hours | **Difficulty**: Beginner

This tutorial teaches you how to add a new analysis question to Batfish. You'll create a question that answers "Which devices have BGP neighbors in an established state?"

## What You'll Build

By the end of this tutorial, you'll have:
- Created a new Java question class
- Registered it in the questions library
- Tested your question
- Understood the question framework

**Why this question?**
- BGP status is a common network verification task
- It's simple enough for a first question
- Demonstrates key concepts without overwhelming complexity

---

## Prerequisites

Before starting, ensure you have:

1. **Java knowledge**: Comfortable with basic Java syntax
2. **Batfish builds successfully**: `bazel build //projects/allinone:allinone_main`
3. **Understanding of Batfish questions**: Read [Question Development](../question_development/README.md)

**Not there yet?** Start with [Contributing Your First PR](contributing_first_pr.md).

---

## Step 1: Understand Batfish Questions

### Question Types

Batfish supports several question types:

1. **Structured questions**: Return structured data (tables, lists)
2. **Differential questions**: Compare two snapshots
3. **Predicate questions**: Yes/no answers
4. **Template questions**: Parameterized questions

We'll create a **structured question** that returns a table of BGP sessions.

### Question Anatomy

A Batfish question consists of:

```
Question Class (Java)
    ├── Properties (parameters)
    ├── Answerer (computes answer)
    └── Metadata (description, documentation)
```

---

## Step 2: Find Question Location

Batfish questions live in the `questions/` directory:

```bash
# List existing questions
ls questions/

# Examples:
# questions/bgp_sessions.py
# questions/traceroute.py
# questions/parse_error.py
```

For Java-based questions, look in:
```bash
# Java questions
find projects/batfish/src/main/java -name "*Question.java"
```

---

## Step 3: Create the Question Class

### File Location

We'll create a Java question in:
```
projects/batfish/src/main/java/org/batfish/question/bgp/
```

### Create the Class

Create file: `projects/batfish/src/main/java/org/batfish/question/bgp/BgpEstablishedNeighborsQuestion.java`

```java
package org.batfish.question.bgp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllEdgesEdgeSpecifier;
import org.batfish.specifier.EdgeSpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;

/**
 * A question that finds BGP neighbors in an established state.
 */
public class BgpEstablishedNeighborsQuestion extends Question {

    private static final String PROP_NODES = "nodes";
    private static final String PROP_EDGES = "edges";
    private static final String PROP_REMOTE_AS = "remoteAs";

    @Nonnull private final NodeSpecifier _nodes;
    @Nonnull private final EdgeSpecifier _edges;
    @Nullable private final Long _remoteAs;

    @JsonCreator
    public static BgpEstablishedNeighborsQuestion create(
        @JsonProperty(PROP_NODES) NodeSpecifier nodes,
        @JsonProperty(PROP_EDGES) EdgeSpecifier edges,
        @JsonProperty(PROP_REMOTE_AS) Long remoteAs
    ) {
        return new BgpEstablishedNeighborsQuestion(
            nodes != null ? nodes : AllNodesNodeSpecifier.INSTANCE,
            edges != null ? edges : AllEdgesEdgeSpecifier.INSTANCE,
            remoteAs
        );
    }

    public BgpEstablishedNeighborsQuestion(
        @Nonnull NodeSpecifier nodes,
        @Nonnull EdgeSpecifier edges,
        @Nullable Long remoteAs
    ) {
        _nodes = nodes;
        _edges = edges;
        _remoteAs = remoteAs;
    }

    @JsonProperty(PROP_NODES)
    public @Nonnull NodeSpecifier getNodes() {
        return _nodes;
    }

    @JsonProperty(PROP_EDGES)
    public @Nonnull EdgeSpecifier getEdges() {
        return _edges;
    }

    @JsonProperty(PROP_REMOTE_AS)
    public @Nullable Long getRemoteAs() {
        return _remoteAs;
    }

    @Override
    public boolean getDataPlane() {
        return false; // We use control plane data (BGP config)
    }

    @Override
    public String getName() {
        return "bgpEstablishedNeighbors";
    }
}
```

---

## Step 4: Create the Answerer

The answerer computes the answer to the question.

Create file: `projects/batfish/src/main/java/org/batfish/question/bgp/BgpEstablishedNeighborsAnswerer.java`

```java
package org.batfish.question.bgp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import org.batfish.common.plugin.Batfish;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpSession;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswer;
import org.batfish.driver.Driver;
import org.batfish.question.QuestionPlugin;
import org.batfish.specifier.NodeEdgeSpecifiers;

/**
 * Answerer for BGP established neighbors question.
 */
public class BgpEstablishedNeighborsAnswerer {

    public static final String COL_NODE = "Node";
    public static final String COL_REMOTE_IP = "Remote_IP";
    public static final String COL_REMOTE_AS = "Remote_AS";
    public static final String COL_LOCAL_AS = "Local_AS";
    public static final String COL_STATUS = "Status";

    private final Batfish _batfish;
    private final BgpEstablishedNeighborsQuestion _question;

    public BgpEstablishedNeighborsAnswerer(
        IBatfish batfish,
        BgpEstablishedNeighborsQuestion question
    ) {
        _batfish = (Batfish) batfish;
        _question = question;
    }

    public AnswerElement answer() {
        // Get BGP sessions from configuration
        Set<BgpSession> bgpSessions = _batfish.loadBgpSessions();

        // Filter based on question parameters
        Set<Row> rows = new HashSet<>();
        for (BgpSession session : bgpSessions) {
            // Filter by node specifier
            if (!_question.getNodes().matches(
                    session.getNode1().getConfig(),
                    _batfish.getVendorConfigurationId())) {
                continue;
            }

            // Filter by remote AS if specified
            if (_question.getRemoteAs() != null
                    && !session.getAs2().equals(_question.getRemoteAs())) {
                continue;
            }

            // Add row for established sessions
            String status = session.getProperties().getStatus();
            if (status.equals("Established")
                    || status.equals("BGP_ESTABLISHED")) {
                rows.add(Row.of(
                    COL_NODE, session.getNode1().getName(),
                    COL_REMOTE_IP, session.getAddress2(),
                    COL_REMOTE_AS, session.getAs2(),
                    COL_LOCAL_AS, session.getAs1(),
                    COL_STATUS, status
                ));
            }
        }

        // Create table answer
        return TableAnswer.of(
            ImmutableList.of(
                COL_NODE,
                COL_REMOTE_IP,
                COL_REMOTE_AS,
                COL_LOCAL_AS,
                COL_STATUS
            ),
            rows
        );
    }
}
```

---

## Step 5: Register the Question

Questions must be registered with Batfish's plugin system.

Create file: `projects/batfish/src/main/java/org/batfish/question/bgp/BgpEstablishedNeighborsPlugin.java`

```java
package org.batfish.question.bgp;

import org.batfish.common.plugin.Plugin;
import org.batfish.question.QuestionPlugin;

/**
 * Plugin for BGP established neighbors question.
 */
public class BgpEstablishedNeighborsPlugin implements Plugin, QuestionPlugin {

    @Override
    public void initialize() {
        // Registration happens via service loader
    }

    @Override
    public Class<? extends Question> getQuestionClass() {
        return BgpEstablishedNeighborsQuestion.class;
    }
}
```

Register the plugin in `projects/batfish/META-INF/services/org.batfish.question.QuestionPlugin`:

```
org.batfish.question.bgp.BgpEstablishedNeighborsPlugin
```

---

## Step 6: Test Your Question

### Unit Test

Create: `projects/batfish/src/test/java/org/batfish/question/bgp/BgpEstablishedNeighborsQuestionTest.java`

```java
package org.batfish.question.bgp;

import org.junit.Test;
import org.batfish.question.Question;
import static org.junit.Assert.*;

public class BgpEstablishedNeighborsQuestionTest {

    @Test
    public void testCreateQuestion() {
        BgpEstablishedNeighborsQuestion q =
            BgpEstablishedNeighborsQuestion.create(null, null, null);

        assertNotNull(q);
        assertEquals("bgpEstablishedNeighbors", q.getName());
        assertFalse(q.getDataPlane());
    }

    @Test
    public void testWithParameters() {
        BgpEstablishedNeighborsQuestion q =
            BgpEstablishedNeighborsQuestion.create(
                null, null, 65001L
            );

        assertEquals(Long.valueOf(65001L), q.getRemoteAs());
    }
}
```

Run the test:

```bash
bazel test //projects/batfish/src/test/java/org/batfish/question/bgp:BgpEstablishedNeighborsQuestionTest
```

### Integration Test

Test with actual network configs:

```bash
# Start Batfish service
bazel run //projects/allinone:allinone_main

# In Python (Pybatfish)
from pybatfish.client.session import Session

bf = Session(host="localhost")
bf.init_snapshot('test_configs/', name='test')

# Run your question
result = bf.q.bgpEstablishedNeighbors()
print(result)
```

---

## Step 7: Add Documentation

Document your question in `questions/bgp_established_neighbors.md`:

```markdown
# bgpEstablishedNeighbors

Find BGP sessions that are in an established state.

## Parameters

- **nodes** (optional): Filter to specific nodes
- **edges** (optional): Filter to specific edges
- **remoteAs** (optional): Filter by remote AS number

## Returns

A table with columns:
- **Node**: Local node name
- **Remote_IP**: Remote BGP peer IP
- **Remote_AS**: Remote AS number
- **Local_AS**: Local AS number
- **Status**: BGP session status

## Example

```python
bf.q.bgpEstablishedNeighbors(remoteAs=65001)
```
```

---

## Step 8: Build and Verify

```bash
# Build Batfish
bazel build //projects/allinone:allinone_main

# Run all tests
bazel test //projects/batfish/src/test/java/org/batfish/question/bgp:...

# Start service and test manually
bazel run //projects/allinone:allinone_main
```

---

## Common Issues and Solutions

### Issue: "Question not found"

**Problem**: Your question doesn't appear in the question list.

**Solutions**:
1. Verify plugin is registered in META-INF/services
2. Check that the plugin class is in the classpath
3. Rebuild with `bazel clean --expunge && bazel build //...`

---

### Issue: Empty results

**Problem**: Question runs but returns no rows.

**Debugging**:
```java
// Add debug logging
System.err.println("Total BGP sessions: " + bgpSessions.size());
System.err.println("Filtered rows: " + rows.size());
```

**Common causes**:
1. No BGP sessions in the snapshot
2. Filters are too restrictive
3. Status string doesn't match

---

### Issue: Compilation error

**Problem**: Won't compile, missing imports or symbols.

**Solutions**:
1. Check imports in IntelliJ: View → Tool Windows → Dependencies
2. Run `bazel build` to see full error message
3. Look at similar questions for examples

---

## Next Steps

Now that you've created a basic question:

1. **Add more parameters**: Filter by local AS, interface, etc.
2. **Improve performance**: Cache computations, optimize queries
3. **Add differential support**: Compare two snapshots
4. **Document for users**: Write user-facing documentation

---

## Advanced: Template Question

To make your question configurable via templates, create a JSON template:

`questions/templates/bgp_established_neighbors.json`:

```json
{
  "variables": [
    {
      "name": "nodes",
      "description": "Nodes to query",
      "type": "nodeSpec",
      "optional": true
    },
    {
      "name": "remoteAs",
      "description": "Remote AS number",
      "type": "long",
      "optional": true
    }
  ],
  "description": "Find BGP sessions in established state"
}
```

This allows users to run your question with custom parameters via the web UI.

---

## Quick Reference

### Key Files

```
Question class: .../question/bgp/BgpEstablishedNeighborsQuestion.java
Answerer: .../question/bgp/BgpEstablishedNeighborsAnswerer.java
Plugin: .../question/bgp/BgpEstablishedNeighborsPlugin.java
Test: .../question/bgp/BgpEstablishedNeighborsQuestionTest.java
Plugin registration: .../batfish/META-INF/services/org.batfish.question.QuestionPlugin
Template: questions/templates/bgp_established_neighbors.json
```

### Commands

```bash
# Create question files
vim projects/batfish/src/main/java/org/batfish/question/bgp/BgpEstablishedNeighborsQuestion.java

# Build
bazel build //projects/allinone:allinone_main

# Test
bazel test //projects/batfish/src/test/java/org/batfish/question/bgp:...

# Run
bazel run //projects/allinone:allinone_main
```

---

## Related Documentation

- [Question Development Guide](../question_development/README.md)
- [Symbolic Engine](../symbolic_engine/README.md)
- [BDD Best Practices](../development/bdd_best_practices.md)
- [Development Guide](../development/README.md)
