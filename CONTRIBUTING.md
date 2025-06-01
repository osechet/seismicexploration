# Contribution Guide

Thank you for your interest in contributing to Seismic Exploration!

## How to Contribute

1. **Fork** the repository and clone it locally.
2. **Create a branch** for your feature or bugfix.
3. **Develop** your changes with clear commit messages.
4. **Test** your changes before submitting a PR.
5. **Open a Pull Request** and clearly describe your contribution.

## Commit Messages

This project uses [Conventional Commits](https://www.conventionalcommits.org/) for commit messages. Please follow this
format:

- `feat`: for new features
- `fix`: for bug fixes
- `docs`: for documentation changes
- `refactor`: for code refactoring
- `test`: for adding or updating tests
- `chore`: for maintenance tasks

If your change introduces a breaking change, add a `!` after the type (e.g. `feat!:` or `fix!:`).

Example: `feat!: change sensor API`

## Coding Rules

- Follow standard Java code style.
- Document public methods.
- Add tests if possible.

## Reporting Bugs or Suggesting Features

- Use the appropriate issue template.
- Provide as much detail as possible.

Please respect the project’s code of conduct.

## Release Process

To publish a new release of the mod:

1. **Validate changes**
   Make sure all changes are tested and the build passes locally:
   `./gradlew build`

2. **Create a Git tag**
   Create an annotated tag that follows [SemVer](https://semver.org/) and starts with the `v` prefix (for example,
   `v1.2.3`):

  ```sh
  git tag -a vX.Y.Z -m "Release vX.Y.Z"
  git push origin vX.Y.Z
  ```

Note: The version is automatically calculated from the SCM using `git describe`. The release page on GitHub is created
by the release workflow. No manual update is required.
