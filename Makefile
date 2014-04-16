#--------------------------------------------------------
# the grammar (see src)
XMOC=MOC
XASM=ASM
#--------------------------------------------------------
# directories containing egg
EDIR=.
# associated jars
EJAR=$(EDIR)/eggc-5.3.1.jar
GJAR=$(EJAR):.
#--------------------------------------------------------
# java, javac, jar
JDIR=/usr/bin
#--------------------------------------------------------
all : src att class

pdf:
	cd report; make pdf

src:
	(cd moc ; $(JDIR)/java -jar ../$(EJAR) $(XMOC).egg)
	(cd moc ; $(JDIR)/java -jar ../$(EJAR) $(XASM).egg)

att:
	$(JDIR)/javac -classpath $(GJAR) moc/compiler/*.java
	$(JDIR)/javac -classpath $(GJAR) moc/tds/*.java
	$(JDIR)/javac -classpath $(GJAR) moc/type/*.java
	$(JDIR)/javac -classpath $(GJAR) moc/gc/*.java

class:
	$(JDIR)/javac -classpath $(GJAR) moc/egg/*.java

clean:
	find -name "*.class" -delete
	find -name "*.tam" -delete
	rm -rf moc/egg
	cd report; make clean
