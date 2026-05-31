GRADLE_VERSION=9.1.0

.PHONY: run test build clean docker-up docker-down smoke wrapper

run:
	./gradlew bootRun

test:
	./gradlew test

build:
	./gradlew clean bootJar

clean:
	./gradlew clean

wrapper:
	gradle wrapper --gradle-version $(GRADLE_VERSION)

docker-up:
	docker compose up --build

docker-down:
	docker compose down

smoke:
	bash scripts/smoke-test.sh
