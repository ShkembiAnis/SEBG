# SEBG
Sports Exercise Battle Game

# Git: 
https://github.com/ShkembiAnis/SEBG 
 
 
# Design:
Die Architektur des Projekts besteht aus 2 Packages bezüglich Server und Client, welche verschieden Klassen haben. 
 
Server Package besteht aus: 
- Server 
- PostGre 
- Battlefield 
- Verb (Enum) 
 
Client Package besteht aus: 
- Client 
- Push_Up_History 
 
Am Ende steht die main Klasse wo ein ServerSocket auf port 10001 erstellt wurde und für jedes neues Request gibt es „new client“ zurück damit man weiß, wenn eine neue Request kommt. 
 
Server -> Class Server: 
- Hier wurden alle Methoden die von curl gerufen wird, damit man das Programm teste, ob es richtig gebaut ist. 
 
Server -> Class PostGre: 
- Nach der verbindung mit der Datenbank hier wurden alle selects, inserts, updates auf der Datenbank gemacht.  
 
Server -> Class Battlefield: 
- Hier nimmt der Battle statt und die Benutzer machen eine Race welche von denen mehr push_ups in 2 Minuten macht. Es kann auch sein, dass keiner gewinnt und jeder ein Punkt bekommt. 
 
Server -> Class Verb (Enum): 
- Hier wurden die http Kommanden aufgelistet (post, get, put, delete) die auf der Class Server später geruft werden. 
 
Client -> Class Client: 
- Hier wird der Benutzer deklariert (Objekt erstellt) und dann auf Class PostGre initialisiert. Das hilft die Benutzer zu aktualisieren.  
 
 
Client -> Class Push_Up_History 
- Hier werden die Push Ups von Benutzer deklariert, aber diese Klasse wurde nicht verwendet.
 
 
# Integration Tests: 
Die Integration Tests wurden mittels provided curls script gemacht. Nur die edit User Test hat ein kleines Problem, weil es wurde eine Column name erstellt und da name ein Stichwort in sql ist, gibt es ein bisschen Probleme. Ansonsten andere Tests funktionieren. 
 
 
# Datenbank: 
Der Datenbank besteht aus 3 Tabellen bezüglich „users“, „push_up_history“, „tournament“. Auf die „users“ Tabelle werden alle Daten von Benutzes gespeichert. Auf der „push_up_history“ werden die Übungen von Benutzer gespeichern  (push-ups, duration of exercise). Auf der “tournament” tabelle wird der Battle zwischen Benutzern gespeichert. Der Primary Key von „users“ wird als Foreign Key für die 2 andere Tabellen gespeichert, damit man eine Verbindung zwischen alle 3 Tabellen macht. SQL Statements zum Erstellen der Datenbank sind dem GitRepository beigefügt. database.sql 
 
# Log File: 
Die Log Funktion ist statik erstellt. D.h dass man braucht keine Objekt erstellen, sondern man kann es rufen Classname.name. 
 
# Unit Tests:
Die Datenbank Methoden wurde am meisten getestet, denn alle Methoden von Server verwenden die Datenbank Methoden. Rest wurde nicht getestet, da es selbt getestet ist. 
 
# Time Tracking:  
Die Zeit für dieses Projekt war ungefähr 40 Stunden. Es wurden nicht so viel, da ein paar Methoden von der Alte Projekt implementiert wurden  
 
