all: compile
	@echo -e '[INFO] Done!'
clean:
	@echo -e '[INFO] Cleaning Up..'
	@-rm -rf cs455/**/**/*.class Carbonari_Amanda_ASG1.tar
 
compile: 
	@echo -e '[INFO] Compiling the Source..'
	@javac -d . cs455/**/**/*.java

package:
	@echo -e '[INFO] Packaging the source..'
	@-tar cvf Carbonari_Amanda_ASG1.tar cs455/**/**/*.java Makefile
