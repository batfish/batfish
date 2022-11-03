# Contributing code to Batfish

This document fleshes out coding standards and processes for making contributions to Batfish.

## Prerequisites

Before writing any code, follow the steps in this section:

1. First, set up the prerequisites in the
   [building and running instructions](../building_and_running/README.md#prerequisites)
   instructions.
1. Prior to making commits, set up [pre-commit](https://pre-commit.com/) to automatically apply
   our formatting configurations your changed files. **If you neglect to execute this step, it is
   very likely that any code you contribute in a PR will not pass CI pre-merge checks.**
    1. (Optional, recommended) Create a python virtualenv to be used for pre-commit. We suggest you
       use [pyenv](https://github.com/pyenv/pyenv) to manage your python installations and
       environments. To get started with pyenv:
        1. Install the python
           [build dependencies](https://github.com/pyenv/pyenv/wiki#suggested-build-environment) for
           your system
        2. Run the handy [pyenv installer](https://github.com/pyenv/pyenv-installer#install)
        3. Follow any instructions for modifying your shell startup printed at the end of the
           previous step.
        4. *In a new shell*, build the version of python you want to use. E.g for python 3.9.15, do:
           ```
           pyenv install 3.9.15
           ```
        5. Create a virtualenv for pre-commit using the python version you built:
           ```
           pyenv virtualenv 3.9.15 pre-commit
           ``` 
        6. Activate the virtualenv you just created:
           ```
           pyenv activate pre-commit
           ```
    1. Install pre-commit in your python environment:
        ```
        pip install pre-commit
        ```
    1. Finally, in the of your cloned batfish repository, run:
       ```
       pre-commit install
       ```
   At this point, `pre-commit` will be applied on staged files whenever you run `git commit`. If
   pre-commit checks fail, you should have unstaged changes with `pre-commit` formatting applied.
   Inspect the changes with `git diff`, and if they look good, add the modified files with `git add`
   before running `git commit` again.
2. For a smooth reviewing process, familiarize yourself with the
   [contribution and review standards](#contribution-and-review-standards) below.

# Contribution and review standards

*Wherever the content in this section is framed as a guide to reviewers, it is just as relevant for
outside contributors. Knowing what reviewers are looking for will help you write quality code from
the outset. Aside from its intrinsic value this brings, it also saves time in the long run by
yielding fewer and faster review cycles.*

The standards we have developed below are the result of continuous iteration over the years. They
may continue to change over time, and in general we are open to suggestions to improvement.

The goal of these standards is to produce code that is, in decreasing order of importance:

1. Correct
1. Safe
1. Maintainable
1. Legible

When reviewing PRs, consider:

* Correctness and safety issues in submitted code are the most important concerns, and generally
  should be resolved prior to merge. In exceptional cases where achieving full correctness may be
  infeasible, the limitations must be explicitly documented.
* Maintainable code is preferred (nice-to-have), but there are often situations where the effort
  far outweighs the reward. Allowing leeway is often appropriate, especially for outside
  contributors.
* We have CI checks for a reason. Reviewers need not comment specifically on pre-commit or test
  failures unless asked for advice on a solution.
* Where we provide standards on style, the goal is to maximize legibility. However, adherence to
  style is low priority compared to other considerations. In general, strict adherence to style
  should not be a review blocker. Instead, we should prefer automatic style enforcement and
  correction via pre-commit/CI tooling.

## Java coding standards

### Null annotations (maintainability, legibility)

NPEs (`NullPointerException`s) are one of the most common runtime exceptions thrown in production
code, and are almost always indicative of a bug. Typically, they result from code that performs an
operation on a variable/return value/field/etc. without properly handling the case where it may be
`null`.

The standard Java typing system unfortunately does not require types (function return types, local
variables, fields, etc.) to be marked as either potentially or never `null`. The result is that in
general developers often need to expend extra effort to figure out whether to check a value is null,
and what to do in that case. Mistakes made in this regard are not typically caught by the compiler.
This can be partially mitigated by good documentation, but documentation alone is a poor substitute
for static enforcement.

With good tooling, however, null annotations can be applied to make this task less burdensome.
Modern IDEs recognize null annotations and will warn inline when a value is used in an unsafe way
with respect to its possible null-ness.

In Batfish, we use the following annotations to enable static nullness checks by tools that
support them (e.g. Intellij):

* `javax.annotation.ParametersAreNonnullByDefault`
    * May annotate an entire package (in its `package-info.java`) or a class
    * Indicates that all function parameters (not return types!) are considered non-null unless
      otherwise annotated.
* `javax.annotation.Nonnull`
    * May annotate a variable, field, function parameter, or function return type
    * Indicates that its value is never `null`, so code that uses it need not handle the `null` case
* `javax.annotation.Nullable`
    * May annotate a variable, field, function parameter, or function return type
    * Indicates that its value may sometimes be `null`, and this case should be handled.

While it would be optimally safe to provide effective null annotations on every possible entity,
Batfish code does not consistently follow this practice for a combination of historical reasons and
the excessive burden it may impose when compared to its benefit.

To strike a balance between verbosity and utility, we prefer that new code be effectively
null-annotated as follows:

1. Don't annotate primitive values or return types (not applicable)
1. Don't bother with redundant null-annotations, e.g.
    * No need to mark a function parameter `@Nonnull` if its containing class or package is already
      marked `@ParametersAreNonNullByDefault`
    * No need to mark parameters of `@Override` functions (instead mark the appropriate base class
      or interface function if possible)
1. No need to annotate local variables unless you find it helpful
1. New packages should be annotated `@ParamatersAreNonnullByDefault`
1. Parameters of new functions should be annotated `@Nullable` if applicable; or `@Nonnull` if
   applicable and there is no inherited null annotation
1. Return types of new functions should be annotated `@Nonnull` or `@Nullable`
1. New fields should be annotated `@Nonnull` or `@Nullable`

### Documentation (maintainability, legibility)

In order to use code, you have to know what it does. Documentation isn't just for reviewers or other
developers; you are unlikely to remember the function of a piece of code you wrote after any
significant time has passed.

We want documentation to provide a benefit without being a chore. So we recommend you add javadocs
for:

1. New production code:
    1. All new non-private classes
    1. All new non-private functions, **EXCEPT**:
        1. Boilerplate functions
            1. Simple getters that return similary-named field
            1. Simple setters
            1. Static `instance()` method for singletons
            1. Visitor functions
        1. `@Override` functions (instead document the function in the base class or interface if
           possible)
        1. Otherwise super-obvious functions
1. New test code:
    1. New tests with complicated setup, e.g. the creation of a multi-device test network
    1. New non-obvious non-private test library classes and functions

Reviewers should avoid blocking a PR that does not adhere strictly to the above recommendations
except where the lack of documentation is likely to impose a significant burden in the future.