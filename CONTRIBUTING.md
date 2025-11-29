# Conventional Commits

This project follows the [Conventional Commits](https://www.conventionalcommits.org/) specification.

## Format

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

## Types

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation only changes
- **style**: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
- **refactor**: A code change that neither fixes a bug nor adds a feature
- **perf**: A code change that improves performance
- **test**: Adding missing tests or correcting existing tests
- **chore**: Changes to the build process or auxiliary tools and libraries

## Examples

```
feat(auth): add JWT token refresh endpoint
fix(device): resolve device key validation issue
docs(readme): update installation instructions
refactor(service): extract common validation logic
test(controller): add integration tests for user management
chore(deps): update Spring Boot to 3.2.0
```

## Scopes

Common scopes for this project:
- **auth**: Authentication and authorization
- **device**: Device management
- **data**: Data collection and processing
- **dashboard**: Dashboard and UI-related
- **config**: Configuration changes
- **security**: Security-related changes
- **api**: API-related changes

## Setup Git Hooks

To automatically validate commit messages:

```bash
# Set up git hooks directory
git config core.hooksPath .githooks

# Make hooks executable (Linux/Mac)
chmod +x .githooks/pre-commit
```

## Commit Message Validation

The pre-commit hook will validate your commit messages and ensure:
1. Code is properly formatted
2. Tests pass
3. No style violations
4. Commit message follows conventional format
