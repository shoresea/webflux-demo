<p align="center">
  <a href="https://github.com/shoresea/webflux-demo/actions"><img alt="shoresea/webflux-demo status" src="https://github.com/shoresea/webflux-demo/workflows/ci/badge.svg"></a>
</p>

# Spring WebFlux Demo
This project is an example of a reactive micro-service developed using Spring WebFlux.
It also has a Server-Side Event example. (Try at http://localhost:8080/index.html after running this demo on your local machine)

## Run
```bash
./gradlew clean build
./gradlew bootRun
```


## Endpoints

#### Get all products
```bash
curl --request GET \
  --url http://localhost:8080/products
```

#### Get product by ID
```bash
curl --request GET \
  --url http://localhost:8080/products/{id}
```

#### Add product
```bash
curl --request POST \
  --url http://localhost:8080/products \
  --header 'content-type: application/json' \
  --data '{
	"name": "Black Tea with Milk",
	"price": 3.49
}'
```

#### Update product
```shell
curl --request PUT \
  --url http://localhost:8080/products/{id} \
  --header 'content-type: application/json' \
  --data '{
	"name": "Black Tea with Milk",
	"price": 3.99
}'
```

#### Delete product
```shell
curl --request DELETE \
  --url http://localhost:8080/products/{id}
```

#### Delete all products
```shell
curl --request DELETE \
  --url http://localhost:8080/products
```

#### Get product event Stream
```shell
curl --request GET \
  --url http://localhost:8080/products/events
```
