# Improving the `initDb` command usage

## Metrics

| Metric             | Source | Path                            | Date       | Duration | Note                                                            |
|--------------------|:------:|---------------------------------|------------|:--------:|-----------------------------------------------------------------|
| start              |   FC   |                                 | 2024-12-13 |   15h    | Started to work, but should have done more upfront spec/design. |
| feature-spec       |   FC   |                                 | 2024-12-14 |    3h    | Proper spec/design work.                                        |
| feature-dev-branch |   FC   | feature/improved-command-initdb |            |          |                                                                 |

## Problem statement

### Output too much/and or devoid of pertinent information.

As this tool is not supposed to cater for use in Continues Integration/Delivery operations, but primarily focusing on
developers working with the H2 database for fast prototyping. The current output does not work well as intended due to:

1. A wall of text which may or may not, contain pertinent information.
2. Missing information on how to connect based on the configured environment.
3. No way to notice important pieces of information visually.

As stated, point **#2** increases the friction of using the H2 database as there are so many options to
consider (based on their [documentation](https://www.h2database.com/html/features.html))

### Inconsistent CLI Behaviour

The `initDb` command attempts to protect the user from accidentally from damaging their database by providing and
`--if-exists` option. Currently, the two most prominent option values (`force` and `fail`) behaviour's does not conform
to the stated documentations:

> 1. **`--if-exists force`:** Always attempts to destroy the database.
> 2. **`--if-exists initSchemas`:** Only runs the schema initialization scripts (if present).

Specifically the mode of `--if-exists force` falls back to the behaviour of `--if-exists initSchemas` if schema
initializers when the command line arguments include schema initializers (in any form)

## Solution

### Fix the behaviour

- `--if-exists force` should **_always_** destroy the database before continuing with initialization.

### Enhance command output

1. De-emphasize non-important information by dimming the terminal output message.
2. Highlight destructive operations with a "Dazzling Red" colour, (hex code: `#D42106FF`).
3. Highlight pertinent information pieces in a message by applying the "Illuminating Emerald" (hex dode: `#359570FF`)
   colour to text fragment.
4. The command should after successful completion provide the user with the following pertinent information (pre-
   configured with the runtime information passed the tool):
    1. JDBC url to connect to the database via TCP database server.
    2. JDBC url to connect to the database via embedded mode.
    3. Admin JDBC user credentials connect to the database.
    4. A List of all schemas and where or not, they were initialized via a script.

## Conclusion

### Highlights

### Unresolved Issues

### Known errors

## Release Notes
