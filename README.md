# H2 Database Tool

> **TL;DR** A command line app to work with the H2 database in local and server mode. Includes a configuration for
> server operation via standard UNIX style environment variables as well as command line options. Can be run both as JVM
> based app and native executable.

## Usage

- Quickly create & and initialize a database.
  ```shell
  h2db init shoppingbasket -i scripts/sql/h2/localdev.sql
  ```
- Start a TCP server to access your databases.
  ```shell
  h2db serve & disown
  ```
- Stop a running TCP server.
  ```shell
  h2db shutdown
  ```
- Display local host & server configuration:
  ```shell
  h2db env
  ```

## Build the `h2db` tool

> **IMPORTANT**: To build the tool ensure you have atleast Java version 21 available/installed.

1. Checkout this project.
2. Run the gradle script:
   ```shell
   ./gradlew installDist
   ```

After this you will find the installation under `<project-dir>/app/build/install/h2db/bin/`

## Building

### Executable JAR

To build and generate a local installation, run the following shell command:

```shell
./gradlew build 
```

You will find the executable(s), (one for windows, and one for unix/linux)
under `app/build/install/h2` folder.

### Native Executable (via GraalVM)

> **NB**: You should have a _GraalVM_ toolchain installed

Build:

```shell
./gradlew nativeCompile
```

## Installation

I find the easiest installation method is too just to add the `app/build/install/h2/bin` on your path. Most commands
have defaults optional values which can be changed via the following environment variables.

## Configuration

By default, all newly created databases will be created under `<your-home-folder>/.h2/data`. Several
environment variables can be used to change such default behaviours.

Here are notable environment variables:

|                                 Variable | Default    | Description                                                                                                 |
|-----------------------------------------:|:-----------|-------------------------------------------------------------------------------------------------------------|
|                        `H2TOOL_DATA_DIR` | ~/.h2/data | The directory in which H2 databases reside.                                                                 |
| `H2TOOL_SERVER_ALLOW_REMOTE_CONNECTIONS` | false      | Allow remote connections to the server.                                                                     |
|   `H2TOOL_SERVER_ENABLE_VIRTUAL_THREADS` | false      | Enable experimental virtual threads on the JVM.                                                             |
|                     `H2TOOL_SERVER_HOST` | localhost  | Bind address of the server connection of the clients.                                                       |
|         `H2TOOL_SERVER_PERMIT_CREATE_DB` | false      | Allowing remote client to automatically create a database via an URL.                                       |
|                     `H2TOOL_SERVER_PORT` | 2029       | Port for clients connecting to the server.                                                                  |
|                   `H2TOOL_DATABASE_USER` | sa         | Default database user name when the tool creates, and/or connects to database.                              |
|               `H2TOOL_DATABASE_PASSWORD` | secret     | (**Please change this**) Default database user password when the tool creates, and/or connects to database. |

> **NOTE**: These variables may be overridden via command line. Please run `h2db --help` for more information.

