# H2 Database Tool

> **TL;DR** This is a scratch my itch project. I like to use the H2 database, but for the life of me
> cannot remember which special incantations I need to start & stop a database server. Create databases.
> In additional I hate the effort just to setup my IDE database/editor up just to have the ability to
> quickly trash a database, and create a new one with scripts and schemas.

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

To build and generate a local installation, run the following shell command:

```shell
./gradlew build 
```

You will find the executable(s), (one for windows, and one for unix/linux)
under `app/build/install/h2` folder.

## Installation
I find the easiest installation method is too just to add the `app/build/install/h2/bin` on your path. Most commands
have defaults optional values which can be changed via the following environment variables.

## Configuration

By default, all newly created databases will be created under `<your-home-folder>/.h2/data`. Several
environment variables can be used to change such default behaviours.
