# Gherkin Test Automation
_Work in progress_


# Target Users

- Testautomatisierer (TA)
- Entwickler (E)
- Tester (T)
- Fachliche Teams (FT)

# Use Cases

- Tests natürlich spezifieren -> _T, TA, E, FT_
- Akzeptanztest-getrieben entwickeln -> _E_
- Testautomatisierung vereinfachen
- Weboberflächen testen
- Backends testen
- Test automatisiert ausführen -> _TA_
  - (Alle sind am Ergebnis interessiert. Nicht direkt an der automatisierten Testausführung)
  - (Interessant: kurze Feedback-Zeit -> für _E, TA, T_)

* **!durchgestrichen!** Testreport erstellen -> _Use Case der Applikation, aber ohne Akteur_

## GTA vereinfacht...

- Dependency Management
- Spezifizieren von Testfällen
- Nutzbarkeit von Testautomatisierung
- das kombinierte Testen verschiedener Zieltechnologien
  - _Ziel: Testen_
  - _Technologie: z.B. Browser_

> **GTA stellt dir ein Framework zur Verfügung, mit dem du die Testautomatisierung mit Gherkin vereinfachst,**
> **indem du dich nicht mehr um Dependencies, Technologien und Ecosysteme kümmern musst (aber kannst).**


# Warum muss ich micht mehr um Dependencies kümmern?
_Beispiel: Weboberfläche testen_

-> ohne GTA

- man bräuchte ~30 Dependencies und viel Wissen über die Konfiguration, bspw.
  - Selenium
  - WebDriver
  - Cucumber
  - Serenity: aufwändig zu konfigurieren
  - JUnit
  - u.v.m. ...

-> mit GTA

- 1 Dependency (GTA-Web), die in einem Projekt integriert wird
- Autoinstall von Webdriver
- mehr ist nicht nötig

Anschließend: Testautomatisierung erstellen.

# Was steckt jetzt in GTA?

GTA besteht aus ca. 11 Modulen:

- Core-Module: Serenity, Cucumber -> für Reporting, Testspezifikation
  - alle anderen module sind vom Core-Modul abhängig
- WebApplications (mit Selenium, Gallien): UI-Tests, WebDriver-Manager (lädt automatisch Binaries runter)
- WebServices: Testen von REST-APIs, REST-Assured, _zukünftig GraphQL_
- Datenbank: Testen von Datenbanken, mit eigenem SQL-Driver-Manager
- Mobile-Modul: Appium
- Templating-Modul (mit Thymeleaf): Egal wie groß, Text, xml, html abstrahiert konfigurieren
- GherkinScript Modul (Java Reflections)
    - mit Gherkin Java-Objekte befüllen (Setable fields, Expression language)
    - vereinfacht die Nutzung von Gherkin, z.B. dynamische Inhalte (Gherkin-Methods)
- MessageQueues (mit JMS): Für Systeme, die eine MessageQueue sind
- XML Validation (mit XMLDog): Inhalte und Schema von XML Antworten validieren
- FileTransfer (mit FTP, local, ScP): Äquivalent zu MessageQue, REST
- Phasensteuerung: Testausführung orchestrieren (unterbrechen von TestCases, Neuansteuern) -> "Fork/Join-Pattern"

Die Module können alleinstehend genutzt werden, aber auch in Kombination miteinander. 

# Bedeutung der Module
- Core-Modul
- Use: WebApplication, WebServices, Datenbank, Mobile, MQ, FileTransfer
- Utils: TemplatingXML Validation, GherkinScript, Phasensteuerung 

> GTA ist quasi das Docker für Testautomatisierung. 

