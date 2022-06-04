---
name: Bug report
about: Report Batfish output incorrect
title: ''
labels: ''
assignees: ''

---

**Describe the bug and expected behavior**
A clear and concise description of what the bug is and what you expect to happen instead.

**Runnable example**
```py
from pybatfish.client.session import Session
TXT = """
# your configuration here.
"""
bf = Session()
bf.set_network("github-bug-report")
bf.init_snapshot_from_text(TXT)
# Verify that Batfish recognized the vendor format correctly
print(bf.q.fileParseStatus().answer())
# Insert command(s) below to demonstrate the problem
print(bf.q.initIssues().answer())
```

Fill in the `TXT` above and add commands or questions (e.g., `bf.q.initIssues().answer()`) so that the code snippet, when run, serves as a standalone, working example of your problem.

The Batfish team will run your code example as-is and expect to see the problem demonstrated. Failure to provide a runnable, working example will very likely delay or entirely prevent a response to your issue.

You may attach a configuration file or an entire snapshot to the issue, and we can use `bf.init_snapshot` instead, however, the example must when run demonstrate your issue.

If you are concerned about the secrecy of your configuration, feel free to anonymize it manually or use [netconan](https://pypi.org/project/netconan/) to anonymize it. However, the anonymized configuration must still demonstrate your issue when we run the code you provide.

**Additional context**
Add any other context about the problem here.
