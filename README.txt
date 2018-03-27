The purpose of this branch (JDK9) is to enable a smooth startup on Java 9 and beyond.

See also: http://bugs.jython.org/issue2582


There are at least two jnr-posix issues, one of them can be found here 
https://github.com/jnr/jnr-posix/issues/110

JRuby as well is struggling with those: 
https://github.com/jruby/jruby/issues/4834

And there is our very own PySystemState console encoding hack:
WARNING: Illegal reflective access by org.python.core.PySystemState (file:/.../jython-standalone.jar) to method java.io.Console.encoding()

I wrote a little test program to find out the settings on my main platforms.
Here are the results:

OS                               | JDK       | encoding() | defaultCharset() | file.encoding
---------------------------------|-----------|------------|------------------|--------------
macOS HighSierra 10.13.3 (17D47) | 1.7.0_79  | null       | UTF-8            | UTF-8
macOS HighSierra 10.13.3 (17D47) | 1.8.0_162 | null       | UTF-8            | UTF-8
macOS HighSierra 10.13.3 (17D47) | 9.0.4     | null       | UTF-8            | UTF-8
                                 |           |            |                  |
Ubuntu 17.10 (4.13.0-31-generic) | 1.7.0_79  | null       | UTF-8            | UTF-8
Ubuntu 17.10 (4.13.0-31-generic) | 1.8.0_162 | null       | UTF-8            | UTF-8
Ubuntu 17.10 (4.13.0-31-generic) | 9.0.4     | null       | UTF-8            | UTF-8
                                 |           |            |                  |
Windows 10 Home (en_US)          | 1.7.0_79  | cp437      | windows-1252     | Cp1252
Windows 10 Home (en_US)          | 1.8.0_162 | cp437      | windows-1252     | Cp1252
Windows 10 Home (en_US)          | 9.0.4     | cp437      | windows-1252     | Cp1252
                                 |           |            |                  |
Windows 8.1 Pro (de_CH)          | 9.0.4     | cp850      | windows-1252     | Cp1252
                                 |           |            |                  |
Windows 8.1 Enterprise (en_UK)   | 1.8.0_162 | cp850      | windows-1252     | Cp1252
Windows 8.1 Enterprise (en_UK)   | 9.0.4     | cp850      | windows-1252     | Cp1252
                                 |           |            |                  |
Fedora 27                        | 1.8.0_161 | null       | UTF-8            | UTF-8
                                 |           |            |                  |
CentOS 7                         | 1.8.0_131 | null       | UTF-8            | UTF-8
                                 |           |            |                  |
OpenSUSE Leap 42.3               | 1.8.0_144 | null       | UTF-8            | UTF-8
                                 |           |            |                  |
SunOS openindiana 5.11 illumos   | 1.8.0_152 | null       | UTF-8            | UTF-8


"cmd /c chcp" reveals the actual console encoding on Windows



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
