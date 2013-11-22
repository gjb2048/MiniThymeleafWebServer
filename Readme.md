Mini Thymeleaf Web Server
=========================

Introduction
------------
A small web server that can process Thymeleaf templates (http://www.thymeleaf.org/).

Using
-----
With Maven (http://maven.apache.org/) create an executable Java archive: mvn package.

Run using: java -jar ./target/MiniThymeleafWebServer-1.0-SNAPSHOT.jar

The default port is 8084.  If you wish to use another add the port after the '.jar', i.e.
java -jar ./target/MiniThymeleafWebServer-1.0-SNAPSHOT.jar 80

The output of the program will show the list of addresses served.  They are also listed
by the index page '/'.

Purpose
-------
I created this program as a means of learning Thymeleaf and test driven development with
mocking and stub methods.  In addition to see if a template engine could be employed on
a low specification machine such as the Raspberry Pi.

License
=======
© G J Barnard 2013 - Attribution-NonCommercial-ShareAlike 3.0 Unported - http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB.

If you wish to use the code in a commercial setting, please contact me.

Version Information
===================
22nd November 2013 Version 1.0
1.  Initial version.


No Warranties
-------------
G J Barnard expressly disclaims any warranty for the SOFTWARE PRODUCT.  The SOFTWARE PRODUCT is provided 'As Is' without any express
or implied warranty of any kind, including but not limited to any warranties of merchantability, noninfringement, or fitness of a
particular purpose.  G J Barnard does not warrant or assume responsibility for the accuracy or completeness of any information, text,
graphics, links or other items contained within the SOFTWARE PRODUCT.  G J Barnard makes no warranties respecting any harm that may be
caused by the transmission of a computer virus, worm, time bomb, logic bomb, or other such computer program.  G J Barnard
further expressly disclaims any warranty or representation to Authorized Users or to any third party.

USE ENTIRLY AT YOUR OWN RISK.

Limitation Of Liability
-----------------------
In no event shall G J Barnard be liable for any damages (including, without limitation, lost profits, business interruption, or lost
information) rising out of 'Authorized Users' use of or inability to use the SOFTWARE PRODUCT, even if G J Barnard has been advised
of the possibility of such damages.  In no event will G J Barnard be liable for loss of data or for indirect, special, incidental,
consequential (including lost profit), or other damages based in contract, tort or otherwise. G J Barnard shall have no liability
with respect to the content of the SOFTWARE PRODUCT or any part thereof, including but not limited to errors or omissions contained
therein, libel, infringements of rights of publicity, privacy, trademark rights, business interruption, personal injury, loss of
privacy, moral rights or the disclosure of confidential information.

Me
==
G J Barnard MSc. BSc(Hons)(Sndw). MBCS. CEng. CITP. PGCE. - 22nd November 2013.
http://about.me/gjbarnard
