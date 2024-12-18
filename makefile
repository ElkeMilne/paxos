JAVA = java
JAVAC = javac
CPTEST = -cp lib/*:bin/
MAIN_SOURCES = src/main/communication/Communicate.java \
          src/main/member/Member.java \
          src/main/member/AcceptedProposal.java \
          src/main/votingServer/MessageHandler.java \
          src/main/votingServer/VotingServer.java \
          src/main/Paxos.java

TEST_SOURCES = src/test/PaxosTest.java \
					src/test/MemberTest.java \
					src/test/VoteServerTest.java

TEST_MAIN_CLASS = org.junit.platform.console.ConsoleLauncher

all: compile

compile:
	mkdir -p bin
	javac $(CPTEST) $(MAIN_SOURCES) -d bin

compile-test: compile
	@$(JAVAC) $(CPTEST) $(MAIN_SOURCES) $(TEST_SOURCES) -d bin

test: compile-test
	@$(JAVA) $(CPTEST) $(TEST_MAIN_CLASS) --scan-classpath

run: compile
	java -cp bin main.Paxos

clean:
	rm -rf bin/*
	rm -rf src/main/*/*.class
	rm -rf src/main/*.class
	rm -rf src/test/*.class