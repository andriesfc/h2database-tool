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
under `app/build/install/h2` folder.

## Installation

I find the easiest installation method is too just to add the `app/build/install/h2/bin` on your path. Most commands
have defaults optional values which can be changed via the following environment variables:

-

## Configuration

By default, all newly created databases will be created under `<your-home-folder>/.h2/data`. Several
environment variables can be used to change such default behaviours.

