# OTPManager

Helm chart for installing Kernel module OTPManager.

## TL;DR

```console
$ helm repo add mosip https://mosip.github.io
$ helm install my-release mosip/otpmanager
```

## Introduction

OTPManager is  part of the kernel modules, but has a separate Helm chart so as to install and manage it in a completely indepedent namespace.

## Prerequisites

- Kubernetes 1.12+
- Helm 3.1.0
- PV provisioner support in the underlying infrastructure
- ReadWriteMany volumes for deployment scaling

## Installing the Chart

To install the chart with the release name `otpmanager`.

```console
helm install my-release mosip/otpmanager
```

> **Tip**: List all releases using `helm list`

## Uninstalling the Chart

To uninstall/delete the `my-release` deployment:

```console
helm delete my-release
```
