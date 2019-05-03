# `eg-master` Migration

This file is to document major work that will be necessary in migration from `master` to `eg-master`.

# Lifecycle

The intended lifecycle of this document should exactly match the lifecycle of `eg-master` branch.
In other words, this document should start when `eg-master` starts, 
and should be removed when `eg-master` becomes the new master.

# Migration tasks/notes

##### - avdl change to `com.expediagroup` (#175)

This will result in an internal data migration of the schemas. 

###### Mitigation

One way to mitigate would be to export the old metadata into an external datastore. 
Scripts could be made available to import from the external datastore into the new schemas.  
At some level, this could be an MVP for a disaster recovery tool which arguably should be available anyway.

