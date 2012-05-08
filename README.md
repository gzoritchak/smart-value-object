smart-value-object
==================

This is a very old project which goal is to track modification on a graph of object. I put here as an archive.
initial presentation : 

The Smart Value Object allows server components to track client-side modification of business objects in a rich client/J2EE server environment, by using the latest features offered by bytecode processing tools.

When developing client/server applications, the usual way is to implement the Value Object (VO) pattern, through a set of Java objects representing a lightweight graph of persistent server objects, that the server sends to the client for user modification.

The Smart Value Object project (SVO) intents to go a little further by :

* transparently managing concurrency in a multi-user environment,
* analyzing the actual modifications done on the value object, to optimize data exchange and persistence issues,
* allowing a simple way of defining the graph of retrieved VOs.

The design makes the SVO independent from the persistence layer (entity EJB, JDO, Hibernate, ...).

This project is part of the Bright Side Framework, which goal is to provide ready-to-use high level components to quickly build business J2EE applications accessed by rich java/Swing clients on HTTP. The developers involved in the SVO project are Gaetan Zoritchak and Jan Berkel.

project planning : started in November 2003, the goal is to have a first version running in January 2004.

project status : currently, we are at the prototype stage, testing implementations. We encountered difficulties around the ClassLoading of our modified VO in a J2EE server and had to work around it by performing a post compilation modification of VO instead of a dynamic runtime modification. We will work again on this deployment part in the future.

technical consideration : the modification of the bytecode adds versioning info to the VO to allow a server service to verify the concurrency. It also modifies the access to the attributes by adding a field interceptor to flag the VO as dirty when needed. In case of a graph of VOs using collections or maps, these are replaced by our specific implementations in order to flag the added or removed dependent objects.
