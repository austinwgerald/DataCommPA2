JFLAGS = -g
JCC = javac

default: client.class server.class packet.class serial.class

client.class: client.java
	$(JCC) $(JFLAGS) client.java

server.class: server.java
	$(JCC) $(JFLAGS) server.java

packet.class: packet.java
	$(JCC) $(JFLAGS) packet.java

serial.class: serial.java
	$(JCC) $(JFLAGS) serial.java

clean: 
	$(RM) *.class received.txt
