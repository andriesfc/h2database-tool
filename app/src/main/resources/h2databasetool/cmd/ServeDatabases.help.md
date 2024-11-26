Serves databases via a TCP connection from the base directory.

**Configuration & Defaults**

The command uses defaults for the following options:
1. Enable generation of **_dot-trace_** files. 
2. The base directory. 
3. The TCP server port.
4. Use of virtual threads for the server. 
5. Whether, or not to, allow clients to create databases automatically. 

A user may change via these defaults either via command options or via the following command line variables:

- `H2TOOL_TRACE_CALLS`
- `H2TOOL_BASE_DIR` 
- `H2TOOL_SERVER_PORT`
- `H2TOOL_TCP_SERVER_ENABLE_VIRTUAL_THREADS`
- `H2TOOL_PERMIT_DB_CREATION`

**Important Notes**

1. The tool may arbitrary attempt to use another free/available port in case of
   it being in use. Note the port at startup for clients. 
2. The **serveDb** command only prints the TCP server password if user does not supply a value.
