include $(CURDIR)/config.mk

PROJECT_NAME?=$(shell TOP=$(git rev-parse --show-toplevel); echo ${TOP##*/})
TAG=$(shell git describe --tags --exclude "*/*" --abbrev=0 || echo "0.0.0")
TARGET_NAMES=$(shell sed -n -e '/^FROM/ s/.*\ as //p' Dockerfile)
TARGET=$(lastword $(TARGET_NAMES))

ROOT_DIR:=$(patsubst %/,%,$(dir $(abspath $(lastword $(MAKEFILE_LIST)))))

info:
	$(info $(PROJECT_NAME):$(TAG))
	$(info Possible targets: $(TARGET_NAMES))
	$(info Default: $(TARGET))


clean:
	mvn clean

install: clean
	mvn -DskipTests install

dev-env:
	cp scripts/pre-commit.sh .git/hooks/

dev-install: dev-env
	mvn install

# The lintin rules should match the sonarlint that will be run as part of CI/CD
lint: $(pytest)
	# to view lint rules
	mvn lint:list
	mvn com.lewisd:lint-maven-plugin:check

test: clean
	mvn test

# Not sure there is tools for this in java other than sonarQube
# scan:
# 	$(bandit) -r analytics_project_base

run:
	mvn exec:java -Dexec.mainClass="com.kafka.app.App"

docker-clean:
	docker image ls -qf dangling=true | xargs  docker image rm -f

build: build-$(TARGET)

shell: shell-$(TARGET)

$(TARGET_NAMES:%=build-%): build-% :
	docker build -t $(PROJECT_NAME)-$*:$(TAG) . --build-arg ARTIFACTORY_HOSTNAME=$(ARTIFACTORY_HOSTNAME) --build-arg ARTIFACTORY_USERNAME=$(ARTIFACTORY_USERNAME) --build-arg ARTIFACTORY_PASSWORD=$(ARTIFACTORY_PASSWORD) --build-arg PROJECT_NAME=$(PROJECT_NAME) --target $*

$(TARGET_NAMES:%=shell-%): shell-% : build-%
	$(info Building a shell for $*)
	docker run -p 5000:5000 -it -v $(CURDIR):/src $(PROJECT_NAME)-$*:$(TAG) /bin/bash

docker-shell-base:
	$(info HI there i am making target = $(TARGET))
	docker run -it $(ARTIFACTORY_HOSTNAME)/causeway_data_fabric-docker-dev/causeway-ubuntu1804/base:latest

docker-shell-build: TARGET=base
docker-shell-build: docker-shell-base
	$(info HELLO there $(TARGET))


docker-build:
	$(info building docker image $(PROJECT_NAME):$(TAG))
	docker build -t $(PROJECT_NAME):$(TAG) . --build-arg ARTIFACTORY_HOSTNAME=$(ARTIFACTORY_HOSTNAME) --build-arg ARTIFACTORY_USERNAME=$(ARTIFACTORY_USERNAME) --build-arg ARTIFACTORY_PASSWORD=$(ARTIFACTORY_PASSWORD)

# docker-start-dev-env: docker-build
# 	docker build -t $(PROJECT_NAME) .
# 	docker run --entrypoint /bin/sh -v $(CURDIR):/src --name $(PROJECT_NAME)-dev-env -itd $(PROJECT_NAME)
# 	docker exec $(PROJECT_NAME)-dev-env pip3 install -e .[dev]
# 	docker attach $(PROJECT_NAME)-dev-env

# docker-stop-dev-env:
# 	docker stop $(PROJECT_NAME)-dev-env
# 	docker rm $(PROJECT_NAME)-dev-env



docker-run:
	docker run -p 5000:5000 --name $(PROJECT_NAME) -td $(PROJECT_NAME):$(TAG)
	docker ps -l

docker-stop:
	docker stop $(PROJECT_NAME)

docker-remove:
	docker rmi -f $(PROJECT_NAME)
