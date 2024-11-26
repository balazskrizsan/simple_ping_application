# SIMPLE PING APP

## Development env
 
* Win 10
* Intellij IDEA
* Java 23
* No framework used, except JUnit and Mockito
* Pings were tested on this app locally: https://github.com/balazskrizsan/smart_scrum_poker_backend_native
* Dockerfile: openjdk:21-jdk-slim

# Artifacts

## Github Action: Build list

Click a build, and scroll down to download the .jar files (14 days retention):

https://github.com/balazskrizsan/simple_ping_application/actions

## Surefire report examples

* Successful report: https://github.com/balazskrizsan/simple_ping_application/actions/runs/12039488488/job/33567405916
* Failed report: https://github.com/balazskrizsan/simple_ping_application/actions/runs/12039549529/job/33567599389 

## Docker Hub: Built docker images by tags

https://hub.docker.com/repository/docker/kbalazsworks/simple_ping_application/tags

# Application

## Environment variables

| Key                                    | Example value                                                                           |
|----------------------------------------|-----------------------------------------------------------------------------------------|
| PING_SERVICE__APP_LOG_PATH             | logs/app.log                                                                            |
| PING_SERVICE__HOSTS                    | localhost.balazskrizsan.com, localhost2.balazskrizsan.com, localhost3.balazskrizsan.com |
| PING_SERVICE__ICMP_DELAY               | 5000                                                                                    |
| PING_SERVICE__REPORT_URL               | https://www.postb.in/1732320982251-4400979713536t                                       |
| PING_SERVICE__TCP_DELAY                | 5000                                                                                    |
| PING_SERVICE__TCP_PING_PORT_END_POINT  | :46031/health/200ok                                                                     |
| PING_SERVICE__TCP_PROTOCOL             | https                                                                                   |
| PING_SERVICE__TCP_TIMEOUT              | 3000                                                                                    |
| PING_SERVICE__TRACEROUTE_DELAY         | 5000                                                                                    |

## Send to the configured host

### Test with https://www.postb.in/ (mock website)
* Go to the website
* Press create bin
* Copy the URL and set as env variable: PING_SERVICE__REPORT_URL
* Refresh the pastb.in page

## Ping report access

### Available on the configured file path in this format

```
2024-11-25 02:06:12 [pool-2-thread-1] WARN  com.kbalazsworkds.services.ReportService - Ping report: ...
```

## Kubernetes + Windows + Minikube + Kustomize run with master

### Setup namespace

[setup-namespace.yaml](k8s%2Fsetup-namespace.yaml)

### Start the app

[start.sh](k8s%2Fmaster--all-in-one-start%2Fstart.sh)

### Delete the app

[delete.sh](k8s%2Fmaster--all-in-one-start%2Fdelete.sh)

### Environment variables change

[kustomization.yaml](k8s%2Fbase%2Flatest%2Fsecrets%2Fkustomization.yaml)

# IDEA Settings

## App run:

### Environment variables example (copy and paste):

```
PING_SERVICE__APP_LOG_PATH=logs/app.log;PING_SERVICE__HOSTS=localhost.balazskrizsan.com, localhost2.balazskrizsan.com, localhost3.balazskrizsan.com;PING_SERVICE__ICMP_DELAY=5000;PING_SERVICE__REPORT_URL=https://www.postb.in/1732320982251-4400979713536t;PING_SERVICE__TCP_DELAY=5000;PING_SERVICE__TCP_PING_PORT_END_POINT=:46031/health/200ok;PING_SERVICE__TCP_PROTOCOL=https;PING_SERVICE__TCP_TIMEOUT=3000;PING_SERVICE__TRACEROUTE_DELAY=5000
```

## Test run:

### Environment variables example (copy and paste):

```
PING_SERVICE__APP_LOG_PATH=logs/app.log;PING_SERVICE__HOSTS=localhost.balazskrizsan.com, localhost2.balazskrizsan.com, localhost3.balazskrizsan.com;PING_SERVICE__ICMP_DELAY=5000;PING_SERVICE__REPORT_URL=https://www.postb.in/1732320982251-4400979713536t;PING_SERVICE__TCP_DELAY=5000;PING_SERVICE__TCP_PING_PORT_END_POINT=:46031/health/200ok;PING_SERVICE__TCP_PROTOCOL=https;PING_SERVICE__TCP_TIMEOUT=3000;PING_SERVICE__TRACEROUTE_DELAY=5000
```
