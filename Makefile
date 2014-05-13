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
all: src att

src: moc/egg/$(XMOC).java

moc/egg/$(XMOC).java: moc/$(XMOC).egg
	(cd moc ; $(JDIR)/java -jar ../$(EJAR) $(XMOC).egg)

att:
	$(JDIR)/javac -Xlint:all,-fallthrough -classpath $(GJAR) \
	    moc/compiler/*.java \
	    moc/symbols/*.java \
	    moc/type/*.java \
	    moc/gc/llvm/*.java \
	    moc/gc/tam/*.java \
	    moc/gc/*.java \
	    moc/egg/*.java

test: test-llvm test-tam

save-llvm:
	(for i in tests/success/*.ll; do cp $$i $$i.old; done)
cmp-llvm:
	(for i in tests/success/*.ll.old; do echo $$i; diff $$i $${i%.old}; done || true)
test-llvm:
	@cd tests; ./test.py llvm
	make cmp-llvm

save-tam:
	(for i in tests/success/*.tam; do cp $$i $$i.old; done)
cmp-tam:
	(for i in tests/success/*.tam.old; do echo $$i; diff $$i $${i%.old}; done || true)
test-tam:
	@cd tests; ./test.py tam
	make cmp-tam

pdf:
	cd report; make pdf

.PHONY: javadoc
javadoc:
	javadoc -private -d javadoc -classpath $(GJAR) \
	    moc.compiler \
	    moc.gc \
	    moc.gc.llvm \
	    moc.gc.tam \
	    moc.symbols \
	    moc.type

clean:
	find . -name "*.class" -delete
	find tests/ -name "*.tam" -delete
	find tests/ -name "*.ll" -delete
	find tests/ -name "*.s" -delete
	find tests/ -name "*.old" -delete
	rm -rf moc/egg
	rm -rf javadoc
	rm -f a.out
	cd report; make clean
