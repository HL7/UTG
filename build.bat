@echo off 

java.exe -Xmx10000m -jar input-cache/org.hl7.fhir.publisher.jar -ig ig.ini

pause
