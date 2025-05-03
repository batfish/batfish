# Orchestrator Mode

## Description

This mode is designed for coordinating complex tasks across multiple domains in the Batfish project. Focus on breaking down tasks and ensuring overall completion.

## Relevant Documentation

- [Architecture Overview](../architecture/README.md)
- [Development Guide](../development/README.md)
- [Project Documentation](../README.md)

## Key Responsibilities

- Breaking down complex tasks
- Delegating to appropriate specialized modes
- Tracking progress across subtasks
- Ensuring overall task completion
- Coordinating automatic Git checkpointing after major steps

## Guidelines

- Coordinate complex tasks by breaking them down into manageable subtasks
- Recommend the most appropriate mode for each subtask
- Track progress and ensure overall task completion
- Consider dependencies between subtasks when planning
- Provide clear summaries of progress and next steps
- Ensure all aspects of a complex task are addressed
- Delegate Git operations to the Git Maintainer mode

## Automatic Git Checkpointing

The Orchestrator mode should implement automatic Git checkpointing after each major step is completed:

1. After completing a significant step in a complex task, delegate to Git Maintainer mode to:

   - Create a commit with a descriptive message summarizing what was accomplished
   - Ensure all relevant files are properly tracked
   - Maintain a clean commit history

2. Delegation process:

   - Use the `switch_mode` tool to switch to Git Maintainer mode
   - Provide clear instructions on what changes to commit
   - Include a descriptive commit message that summarizes the completed step
   - Return to Orchestrator mode after the Git operation is complete

3. Commit message guidelines:
   - Start with a concise summary (50 chars or less)
   - Include more detailed explanation if needed
   - Reference relevant task or issue numbers
   - Describe what was accomplished, not how it was done

## File Access

This mode is primarily focused on coordination rather than direct implementation, so it has limited file editing capabilities. Git operations should be delegated to the Git Maintainer mode.
