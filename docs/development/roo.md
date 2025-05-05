# Roo

[Roo](https://roocode.com) is an AI coding assistant integrated with this project to help with development tasks. This document provides a brief overview of how Roo is configured and used in the Batfish project.

## What is Roo?

Roo is an AI coding assistant that helps with various development tasks including:

- Writing and modifying code
- Debugging issues
- Designing systems and architecture
- Answering questions about the codebase
- Creating and maintaining documentation
- Testing and reviewing code
- Managing git operations

## Roo Modes

Roo operates in different "modes" that specialize its behavior for specific tasks. Each mode has different capabilities, expertise, and file access permissions. The Batfish project has configured several custom modes:

- **💻 Code**: Implements features and fixes bugs
- **🏗️ Architect**: Designs systems and creates implementation plans
- **❓ Ask**: Answers questions about the codebase without making changes
- **🪲 Debug**: Diagnoses and resolves issues
- **🪃 Orchestrator**: Coordinates complex tasks across multiple domains
- **🧪 Tester**: Creates and maintains tests
- **👀 Reviewer**: Reviews code for quality and adherence to standards
- **🔄 Git Maintainer**: Manages git operations and maintains clean history
- **📝 Documentation Writer**: Creates and updates documentation

## Mode Configuration

The detailed configuration for each mode is maintained in the `.roo/rules-*` directories at the root of the project. For example:

- Code mode: `.roo/rules-code/`
- Git Maintainer mode: `.roo/rules-git-maintainer/`

These directories contain the configuration that Roo uses to understand how to behave in each mode, including responsibilities, file access restrictions, and mode-specific guidelines.

## Using Roo

To use Roo with this project:

1. Install the [Roo extension for VS Code](https://marketplace.visualstudio.com/items?itemName=Continue.roo)
2. Open the Batfish project in VS Code
3. Access Roo through the sidebar or by using keyboard shortcuts

To switch between modes:

1. Open the command palette (Ctrl+Shift+P or Cmd+Shift+P)
2. Type "Roo: Switch Mode" and select it
3. Choose one of the available modes

Alternatively, use the mode selector in the Roo sidebar or type a slash command like `/code` or `/debug` in the Roo chat.

## Resources

- [Roo Documentation](https://docs.roocode.com/)
- [Using Modes](https://docs.roocode.com/basic-usage/using-modes/)
- [Custom Modes](https://docs.roocode.com/features/custom-modes)
- [Available Tools](https://docs.roocode.com/advanced-usage/available-tools/)
