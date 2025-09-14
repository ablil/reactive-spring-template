# havent yet decided if this makefile should be commited and maintained

.PHONY: compile

compile:
	./gradlew assemble

help:
	grep -E '^[a-z]+:' Makefile # display all targets

clean:
	./gradlew clean

containers:
	docker compose up -d

run:
	./gradlew bootRun
