The purpose of this branch (JDK9) is to enable a smooth startup on Java 9 and beyond.

See also: http://bugs.jython.org/issue2582


There are at least two jnr-posix issues, one of them can be found here 
https://github.com/jnr/jnr-posix/issues/110
The output of java 10 is as follows:
java --illegal-access=warn -jar jython-standalone.jar
WARNING: Illegal reflective access by jnr.posix.JavaLibCHelper$ReflectiveAccess (file:/Users/oti/Downloads/jython-standalone.jar) to method sun.nio.ch.SelChImpl.getFD()
WARNING: Illegal reflective access by jnr.posix.JavaLibCHelper$ReflectiveAccess (file:/Users/oti/Downloads/jython-standalone.jar) to field sun.nio.ch.FileChannelImpl.fd
WARNING: Illegal reflective access by jnr.posix.JavaLibCHelper$ReflectiveAccess (file:/Users/oti/Downloads/jython-standalone.jar) to field java.io.FileDescriptor.fd
Jython 2.7.2a1+ (, Jul 29 2018, 10:16:02) 
[Java HotSpot(TM) 64-Bit Server VM ("Oracle Corporation")] on java10.0.2
Type "help", "copyright", "credits" or "license" for more information.
>>> 


JRuby as well is struggling with those: 
https://github.com/jruby/jruby/issues/4834
And there you can find a workaround:
java --illegal-access=warn --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED -jar jython-standalone.jar
Jython 2.7.2a1+ (, Jul 29 2018, 10:16:02) 
[Java HotSpot(TM) 64-Bit Server VM ("Oracle Corporation")] on java10.0.2
Type "help", "copyright", "credits" or "license" for more information.
>>>

AND

java --illegal-access=debug -Dpython.launcher.tty=true -jar jython-standalone.jar
Jython 2.7.2a1+ (, Jul 29 2018, 10:16:02) 
[Java HotSpot(TM) 64-Bit Server VM ("Oracle Corporation")] on java10.0.2
Type "help", "copyright", "credits" or "license" for more information.
>>>


jdeps --jdk-internals should not list out scary stuff.
At the moment there are:
 - split package: org.w3c.dom.html
   The interface org.w3c.dom.html.HTMLDOMImplementation is part of the JDK since Java 7,
   therefore it can be safely removed from our version of xercesImpl.jar
   See also: https://issues.apache.org/jira/browse/XERCESJ-1689
 - netty handler uses sun.security.* classes (see https://github.com/netty/netty/issues/6679)
 - usages of sun.misc.Unsafe, sun.misc.Signal, sun.misc.SignalHandler (should have replacements according to http://openjdk.java.net/jeps/260)
   (see also: https://wiki.openjdk.java.net/display/JDK8/Java+Dependency+Analysis+Tool)
   

    --------------------


Jython: Python for the Java Platform

Welcome to Jython 2.7.2a1.

This is an alpha release of the 2.7.2 version of Jython. Along with
language and runtime compatibility with CPython 2.7, Jython 2.7
provides substantial support of the Python ecosystem. This includes
built-in support of pip/setuptools (you can use with bin/pip) and a
native launcher for Windows (bin/jython.exe), with the implication
that you can finally install Jython scripts on Windows.

**Note that if you have JYTHON_HOME set, you should unset it to avoid
problems with the installer and pip/setuptools.**

Jim Baker presented a talk at PyCon 2015 about Jython 2.7, including
demos of new features: https://www.youtube.com/watch?v=hLm3garVQFo

The release was compiled on OSX using JDK 7 and requires a minimum of
Java 7 to run.

Please try this release out and report any bugs at
http://bugs.jython.org You can test your installation of Jython (not
the standalone jar) by running the regression tests, with the command:

jython -m test.regrtest -e -m regrtest_memo.txt

For Windows, there is a simple script to do this: jython_regrtest.bat.
In either case, the memo file regrtest_memo.txt will be useful in the
bug report if you see test failures. The regression tests can take
about half an hour.

See ACKNOWLEDGMENTS for details about Jython's copyright, license,
contributors, and mailing lists; and NEWS for detailed release notes,
including bugs fixed, backwards breaking changes, and new features. We
sincerely thank all who contribute to Jython, including - but not
limited to - bug reports, patches, pull requests, documentation
changes, support emails, and fantastic conversation on Freenode at
#jython. Join us there for your questions and answers!
