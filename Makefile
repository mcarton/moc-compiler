#--------------------------------------------------------
# the grammar (see src)
XMOC=MOC
#--------------------------------------------------------
# directories containing egg
EDIR=.
# associated jars
EJAR=$(EDIR)/eggc-6.0.0.jar
GJAR=$(EJAR):.
#--------------------------------------------------------
# java, javac, jar
JDIR=/usr/bin
#--------------------------------------------------------
all: src att class

src:
	(cd moc ; $(JDIR)/java -jar ../$(EJAR) $(XMOC).egg)

att:
	$(JDIR)/javac -classpath $(GJAR) moc/compiler/*.java
	$(JDIR)/javac -classpath $(GJAR) moc/symbols/*.java
	$(JDIR)/javac -classpath $(GJAR) moc/type/*.java
	$(JDIR)/javac -classpath $(GJAR) moc/gc/llvm/*.java
	$(JDIR)/javac -classpath $(GJAR) moc/gc/tam/*.java
	$(JDIR)/javac -classpath $(GJAR) moc/gc/*.java

class:
	$(JDIR)/javac -classpath $(GJAR) moc/egg/*.java

test: test-llvm test-tam

test-llvm:
	@cd tests; ./test.py llvm

test-tam:
	@cd tests; ./test.py tam

pdf:
	cd report; make pdf

clean:
	find . -name "*.class" -delete
	find tests/ -name "*.tam" -delete
	find tests/ -name "*.ll" -delete
	find tests/ -name "*.s" -delete
	rm -rf moc/egg
	cd report; make clean
