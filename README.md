Message Center
==============

This part of the Sakai project provides the Messages tool and the Forums tool. It also used to provide the MessageCenter
tool but this hasn't been part of Sakai since 2.6.

The context ID seen in the code is often the site ID. The context Site ID seen in the code is often the site
reference.

Dependencies
------------

It depends on the Type API for storing little bits of data. In the DB schema you will often see a type_uuid column
which is a reference to a entry in the TypeManager. It seems that it's only msgcntr that uses the type API so it could
probably be move in here.

Problems
--------

The services internally use lots of threadlocals that are only setup when requests come through the portal.
The services also don't have good tests.
Need to contain Sakai dependencies inside one class so it's easy to test the code.
Error handling, not really any use of exceptions.
Permissions on services?
Type API doesn't handle i18n.
Should use placement IDs rather than site IDs to allow multiple forums per site.
Should use the standard Sakai authz API rather than re-implementing permissions.
Doesn't have clean responsibilities between services.
Too much mutability on entities exposed through the API.

Should things like forum titles be i18ned?

Area is known as "Template Settings" in the interface.

DB Changes
----------
These columns can all go as  it's a set rather than an list now.
MFR_AP_ACCESSORS_T.accessors_index_col 
MFR_AP_MODERATORS_T.moderators_index_col
MFR_AP_CONTRIBUTORS_T.contributors_index_col