---
name: release-notes
description: Draft the GitHub release notes for a new Batfish release. Use when asked to prep or cut a release, draft release notes, or summarize what changed since the last tag. Produces a short curated summary written for someone deciding whether to upgrade, credits the people who filed the issues behind each change, and leaves it as an unpublished draft for review.
---

# Drafting a Batfish release

A release covers 500+ commits, and almost none of them belong in the notes. The
reader is an operator deciding whether to upgrade and what will break, not a
contributor reading the diff. Anything they cannot observe from outside Batfish
is noise — the `**Full Changelog**` link at the bottom is what serves anyone who
wants the commit-by-commit view.

Two failure modes dominate:

- **Transcribing the shortlog.** A bullet per merged PR, or a curated list that
  still includes refactors, dependency bumps, and engine internals. The signal
  is buried and the notes go unread.
- **Uncredited contributions.** A fix lands because somebody outside the project
  filed a good issue, and the notes never say so. Most of these are invisible in
  `git log`, because the fix was authored by a maintainer — see step 4.

## 1. Read the last two releases first

The format has changed between releases; the most recent one is the model, not
the older ones.

```bash
gh release list --repo batfish/batfish --limit 5
gh release view <previous-tag> --repo batfish/batfish
```

Tags are date-based (`v2026.08.27`). **Ask which date to use** rather than
guessing: it goes in the title, the tag, and the changelog link.

## 2. Seed the mechanical parts

GitHub can compute the PR list and the first-time-contributor list:

```bash
gh api -X POST repos/batfish/batfish/releases/generate-notes \
  -f tag_name=<new-tag> -f target_commitish=master \
  -f previous_tag_name=<previous-tag> -q .body > working/generated-notes.md
```

Take **`## New Contributors` only**, and strip bot lines (`dependabot[bot]`,
`github-actions[bot]`) and any `@dhalperi with @Copilot` line — a maintainer is
not a new contributor. The generated PR list is raw material for step 3, not
something to paste in.

## 3. Survey the range and find the themes

```bash
PREV=v2025.07.07
git fetch origin master --tags
git log --oneline "$PREV"..origin/master | wc -l
# who did the work, bots included, so you know what to discount
git log --format='%an' "$PREV"..origin/master | sort | uniq -c | sort -rn
# what areas moved: subject prefixes, most-touched first
git log --format='%s' "$PREV"..origin/master \
  | sed -E 's/ \(#[0-9]+\)$//; s/:.*//' | sort | uniq -c | sort -rn | head -40
```

A prefix with 30+ commits and no prior release note is usually a **new vendor**
or a new subsystem — that is the headline. Read the whole non-bot PR list from
step 2 once; it is a few hundred lines and there is no substitute for reading it.

## 4. Credit whoever asked for the change

**`Fixes` trailers find only some of it.** Many fixes land with no trailer, and
the reporter's name appears nowhere in the commit. Sweep closed issues over the
release window instead, then read the authors:

```bash
git log --format='===%h|%s%n%b' "$PREV"..origin/master \
  | awk '/^===/{c=$0} /(Fixes|For|Closes) batfish\/batfish#[0-9]+/{print c" >> "$0}'

gh issue list --repo batfish/batfish --state closed --limit 200 \
  --search "closed:>=<previous-release-date>" \
  --json number,title,author,closedAt \
  -q '.[] | [.closedAt[0:10], .number, .author.login, .title] | @tsv' | sort
```

Anything authored by someone outside the project is a credit. Confirm what closed
it before claiming the fix (`gh api repos/batfish/batfish/issues/<N>/timeline`),
since issues also get closed as stale or as duplicates.

Credit inline, next to the change, not in a trailing thank-you list:
`(#10113, thanks @user)`. Long-open feature requests are worth naming
specifically, along with anyone who made the work possible — a vendor contact who
supplied a lab image counts.

## 5. Ground cross-repo claims in the other repo

Users experience Batfish through the Docker images and pybatfish, so packaging
and client features belong in these notes even though they are not in this
repo's log. They are also the claims most easily gotten wrong, so read the source
rather than recalling it: `batfish/docker` for image and publishing changes
(check `.github/workflows/reusable-upload.yml` for what platforms actually get
pushed), `batfish/pybatfish` for the client and the MCP server (`pyproject.toml`
for the extra and entry point, `pybatfish/mcp/server.py` for the tool set). Say
which component ships the feature, and carry over any beta warning the code
itself states.

## 6. Decide what belongs

| Include | Leave out |
|---|---|
| New vendors and config formats | Refactors, renames, code organization |
| New user-facing capability (MCP server, image platforms, new questions) | Dependency and lockfile churn |
| Vendor modeling fixes and new syntax support, grouped by vendor | Internal performance work, BDD/engine internals |
| Protocol correctness fixes (BGP, OSPF, IS-IS, EVPN) | Symbolic route policy engine internals |
| New or changed warnings that alter what users see | Grammar hygiene, test-only changes, CI |
| Breaking changes | Docs and developer tooling, unless a contributor-facing release |

Collapse everything internal into a single line — "General performance and
scaling work throughout the stack, covering parsing, preprocessing, and the
dataplane" — rather than enumerating it.

## 7. Call out breaking changes explicitly

Give these their own short list under a `Breaking changes:` heading, even if past
releases had no such section. Anything that silently changes results or stops a
working setup qualifies: a raised JDK floor, removed question columns, removed
entry points. Name the replacement where there is one. Be specific about which
component: "the Java Batfish CLI's interactive mode" and "pybatfish" are
different things to a reader.

## 8. Create it as an unpublished draft

Write the body to `working/release-notes.md` and never publish:

```bash
gh release create <new-tag> --repo batfish/batfish --draft --target master \
  --title "Batfish <new-tag>" --notes-file working/release-notes.md
gh release edit <untagged-id> --repo batfish/batfish --notes-file working/release-notes.md
```

Mechanics worth knowing: a draft has no real tag yet, so `gh` addresses it by an
`untagged-<hash>` id, **and that id changes on every edit** — take the URL from
each command's output rather than reusing the previous one. `--tag` and `--title`
on `gh release edit` retag a draft cleanly if the date changes, but the
`**Full Changelog**` link is plain text in the body and must be edited too.
Publishing is the user's call; hand them the draft URL.

## 9. Writing the bullets

Bullets are prose, one sentence or two, no bold-first labels. State what changed
and what a user can now do. Bold only the headline items, such as a new vendor.

- **The change, not the implementation.** "IKE gateways and IPsec tunnels are
  modeled" beats naming the classes that model them.
- **Group by vendor or theme**, one bullet each, with the specifics as a clause:
  a bullet per PR fragments what is really one story.
- **No implementation-detail nouns.** Class names, grammar rule names, and PR
  titles are for the changelog.
- **Stop at the claim.** Do not append a clause explaining why it matters.
- Fifteen to twenty bullets for a year of work is right. Prefer cutting a bullet
  to shortening every bullet.
