moca-file-transfer
==================
Use this software to transfer files between your local machine and a moca
server.  Currently, this only works with moca-ng, that is, using an 
HttpConnection.  You will need to obtain a copy of moca-core.jar and place 
it in your local maven repository before building this project.

2014 June 3
- Added refresh (F5 or ⌘R)
- Also supports legacy moca connections (DirectConnection) - type in the host:port  
- Can now directly type in a local or remote path   

2014 June 12
- Can now right click in the tables to upload, download, or delete files
- Stores URL and user id from last connection for convenience (until the list of connections feature is implemented)

Check out the [project wiki](../../wiki).