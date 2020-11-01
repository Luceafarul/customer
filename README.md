# customer-service
Simple service for learning akka-http and slick.

Now customer service have only 2 endpoints for create and get customer

For using and tests need install docker and setup postgres database:

For used service:
```
docker run  --name customer -u postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:9.6
```
