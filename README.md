# customer-service
Simple service for learning akka-http and slick.

Now customer service have only 2 endpoints for create and get customer

For using and tests need install docker and setup postgres database:

For used service:
```
docker run  --name customer -u postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:9.6
```

If tests not started with error:
```
Cause: java.lang.IllegalStateException: Can not connect to Ryuk at localhost:32770
```
try to change docker settings: Settings/Preferences -> General -> turn of "Use gRPC FUSE for file sharing"

For run tests with coverage (check that docker is running):
```
sbt clean coverage test coverageReport
```
