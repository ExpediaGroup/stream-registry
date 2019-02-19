# Stream-Registry Helm Chart
This helm chart creates a [Homeaway Stream-Registry server](https://homeaway.github.io/stream-registry/).

## Prerequisites
* Kubernetes 1.6
* A running Kafka Installation
* A running Zookeeper Installation
* A running confluent schema registry

## Chart Details

This chart will do the following:

* Deploy Stream registry and all its dependencies

## Installing the Chart

To install the chart, use the following:
1. clone this github repo code
2. configure helm to point to kubernetes cluster (for minikube refer [here](https://docs.helm.sh/using_helm/) )
3. move to the directory where the code is cloned
4. run the following commands
```console
$ helm dependency update ./charts/
$ helm install --name stream-registry ./charts
```

## Configuration

The following table lists the configurable parameters of the  stream-registry chart and their default values.

| Parameter                   | Description                                                                                | Default         |
|:----------------------------|:-------------------------------------------------------------------------------------------|:----------------|
| `image.repository`          | Container image to use                                                                     | `registry`      |
| `image.tag`                 | Container image tag to deploy                                                                 `0.4.3`
| `replicaCount`              | k8s replicas                                                                               | `1`             |
| `updateStrategy`            | update strategy for deployment                                                             | `{}`            |
| `resources.limits.cpu`      | Container requested CPU                                                                    | `nil`           |
| `resources.limits.memory`   | Container requested memory                                                               | `[]`            |
| `ingress.enabled`           | If true, Ingress will be created                                                           | `false`         |
| `ingress.annotations`       | Ingress annotations                                                                        | `{}`            |
| `ingress.path`              | Ingress service path                                                                       | `/`             |
| `ingress.hosts`             | Ingress hostnames                                                                          | `[]`            |

Specify each parameter using the `--set key=value[,key=value]` argument to
`helm install`.
