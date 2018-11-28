![StreamRegistryLogo](architecture/SR-logo.svg)

# Stream Registry

A stream-registry is what its name implies: it is a registry of streams. As enterprises increasingly scale in size,
the need to organize and develop around streams of data becomes paramount.  Synchronous calls are attracted to the edge,
and a variety of synchronous and asynchronous calls permeate the enterprise.  The need for a declarative, central
authority for discovery and orchestration of stream management emerges.  This is what a stream registry provides.
In much the same way that DNS provides a name translation service for an ip address, by way of analogy, a
stream-registry provides a “name service” for streams. By centralizing stream metadata, a stream translation service
for producer and/or consumer stream coördinates becomes possible. This centralized, yet democratized, stream metadata
function thus streamlines operational complexity via stream lifecycle management, stream discovery,
stream availability and resiliency.

## Why Stream Registry? ##

We believe that as the change to business requirements accelerate, time to market pressures increase,
competitive measures grow, migrations to cloud and different platforms are required, and so on, systems will
increasingly need to become more reactive and dynamic in nature.

<p align="center">The issue of <em>state</em> arises.</p>

We see many systems adopting _event-driven-architectures_ to facilitate the changing business needs in these high stakes
environments.  We hypothesize there is an emerging need for a centralized "stream metadata" service in the
industry to help streamline the complexities and operations of deploying stream platforms that serve as a
distributed federated nervous system in the enterprise.

## What is Stream Registry?
Put simply, Stream Registry is a centralized service for stream metadata.

The stream registry can answer the following question:

1. Who owns the stream?  
2. Who are the producers and consumers of the stream?  
3. Management of stream replication across clusters and regions
4. Management of stream storage for permanent access
5. Management of stream triggers for legacy stream sources

## Why Did HomeAway Build a Stream Registry?
The stream team at [HomeAway](https://www.homeaway.com) undertook the responsibility and complexity of maintaining
the entire stream platform at HomeAway. That platform processes north of 30 billion msgs/day. That's some
massive scale! Whereas we acknowledge that this has been called out by
[Thoughtworks ESB as an anti-pattern][ESB_AntiPattern], with our move to the cloud, the resultant shift
towards microservices, and the explosion of data science/analytics/machine-learning this number is only going
to further add complexity and risk to the stream team.

Our existing streaming platform essentially functioned as the de-facto nervous system
of HomeAway's infrastructure. Considering how crucial the platform has become, we needed to change how
we managed our streaming infrastructure by building the basic blocks that enable federated clusters dedicated
to each portfolio team.  These dedicated clusters not only resolve the
[Thoughtworks ESB as an anti-pattern][ESB_AntiPattern] dilemma, but they also serve to decrease blast radius when
errors inevitably occur.

[ESB_AntiPattern]: https://www.thoughtworks.com/radar/techniques/recreating-esb-antipatterns-with-kafka

Our historical way of collecting this information had been pretty clunky and inefficient - a wiki page +
a ticket process.

Now with stream-registry, we hope we are providing a better experience both for administrators and developers
alike to further serve the needs of a growing streaming ecosystem.
