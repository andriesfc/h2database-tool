Create a local database in the H2 base directory

**Configuration & Defaults**

The command uses the following defaults for:

1. H2 base directory
2. Quoting schema names
3. Admin database user
4. Admin database user password

A user may change these defaults per invocation via options, or use the 
following environment variables:

- `H2TOOL_ALWAYS_QUOTE_SCHEMA`
- `H2TOOL_BASE_DIR`
- `H2TOOL_DATABASE_PASSWORD`
- `H2TOOL_DATABASE_USER`

**Use of initializing SQL scripts**

The tool allows the execution of initializing scripts up successful creation of the database in the following exact
order:

1. Initializing of schemas
2. A final SQL script upon successful completion of the schema creation (if any)

> **NOTE**: schema scripts always execute with the schema the
> user specifies on the command line.

**Example 1:** The following example demonstrates how to create two schemas, and passing an SQL script to each schema:

```shell
h2 initdb bearDSM\
 --init-schema public pub.sql\
 --init-schema accounting ../accounting.sql
```

**Example 2:** Passing only an init script

```shell
h2 bearDSM --init initiasetbdsm.sql
```
