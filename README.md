# H2 Database Tool

> **TL;DR** This is a scratch my itch project. I like to use the H2 database, but for the life of me
> cannot remember which special incantations I need to start & stop a database server. Create databases.
> In additional I hate the effort just to setup my IDE database/editor up just to have the ability to
> quickly trash a databsae, and create a new one with scripts and schemas.

### Build tools and other requirements

The project uses Gradle. Any JDK 21 or higher needs to be available on the path, or set via a `JAVA_HOME` variable.

## Building

To build and generate a local installation, run the following shell command:

```shell
./gradlew build 
```

You will find the executable(s), (one for windows, and one for unix/linux)
under [app/build/install/h2](app/build/install/h2) folder.

## Installation

I find the easiest installation method is too just to add the `app/build/install/h2/bin` on your path.

## Configuration

By default, all newly created databases will be created under `<your-home-folder>/.h2/data`. Several
environment variables can be used to change such default behaviours.

For reference, consult this table:

| Environment Variable                    | Default (if not set) | What it effects                                                                                                                                                  |
|-----------------------------------------|:---------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `H2TOOL_ADMIN_PASSWORD_BITS`            | 16                   | Number of bits used to generate a random admin password with, (in case of none supplied.)                                                                        |
| `H2TOOL_ADMIN_PASSWORD_GENERATOR_SIZE`  | 16                   | The number of bits a newly generated admin passwords. Note that only  certain sizes are permitted to ensure that passwords are reasonably secure and random.     |
| `H2TOOL_ALWAYS_QUOTE_SCHEMA`            | false                | Always quote the schema name when creating a new schema. This preserves the case, but makes for awkward SQL                                                      |
| `H2TOOL_DATA_DIR`                       | ~/.h2/data           | The default location used to when creating a new H2 database.                                                                                                    |
| `H2TOOL_DATABASE_PASSWORD`              | secret               | Default database user password when the tool creates, and/or connects to database.                                                                               |
| `H2TOOL_DATABASE_USER`                  | sa                   | The default admin user name when creating database.                                                                                                              |
| `H2TOOL_SERVER_ALLOW_REMOTE_CONNECTION` | false                | Determine if running database server allows network connections from other than the host the server runs.                                                        |
| `H2TOOL_SERVER_ENABLE_VIRTUAL_THREADS`  | false                | Determine if the database server should employ virtual threads to handle client requests.                                                                        |
| `H2TOOL_SERVER_FORCE_SHUTDOWN`          | false                | Attempts a 2nd time to shutdown a running database server.                                                                                                       |
| `H2TOOL_SERVER_HOST`                    | localhost            | The name of the host/network address the database server should bind to on startup.                                                                              |
| `H2TOOL_SERVER_PASSWORD`                | **NOT SET**          | Server admin password used to remotely shutdown a running database server. (Note that if not set, or is empty, the tool will create a random one time password). |
| `H2TOOL_SERVER_PERMIT_CREATE_DB`        | false                | Whether or not, to allow connecting clients to automatically create database on connecting to a non existent database.                                           |
| `H2TOOL_SERVER_PORT`                    | 2029                 | The port exposed to clients connecting to a running database server.                                                                                             |
| `H2TOOL_TRACE_CALLS`                    | false                | If true, the server will also create trace file to record client calls.                                                                                          |

## Commands

To see an updated list of wrapped tools, run the following command:

```shell
h2 --help
```

For now, this tool exposes the following wrapped H2 tool applications via dog standard friendly command line interface:

- `initDb`: To initialize a new local database.
- `serveDb`: To start the database sever on the configured bound to host on a given port.
- `shutdownServer`: Attempts to shut down a database server running bound to a specific host on given port.
- `generateAdminPassword`: Generates a reasonably secure password used to start up a new server.

  > **NOTE:** This command will generate a random password each time a server starts up without either this variable
  > being set or not passed to the command via an option.

In addition, the tool also provides the following command(s):

- `about`: List information about the tool, H2 and configuration.







