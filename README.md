# Onkostar Plugin FormInfo

Dieses Onkostar-Plugin ermöglicht das Abfragen von Informationen über Formulare.

## Funktionalität

Das Plugin stellt die Methode `getContent()` bereit um den Inhalt eines Formulars bzw. einer Prozedur abzurufen.

Dadurch können zum Beispiel Inhalte eines Formulars bei einem Formularverweis abgerufen und mithilfe
eines Formularscripts in einem Dialog angezeigt werden.

### Beispiel

Bei Aufruf in einem Formularscript wird den Inhalt des Formulars in diesem Beispiel in der Entwicklerkonsole ausgeben.

```
executePluginMethod(
   'FormInfoPlugin',
   'getContent',
   { id: 1234 },
   function (result) {console.log(result);},
   false
);
```

Beispiel für das zurückgegebene Ergebnis mit den Inhalten in `result`.

```json
{
  "status": {
    "code": 1,
    "exception": null,
    "message": "The method executed successfully."
  },
  "result": [
    {
      "field": "datum",
      "description": "Datum der Utersuchung",
      "value": "01.01.2023",
      "type": "INPUT"
    },{
      "field": "grpEvidenzlevel",
      "description": "Evidenzlevel",
      "value": null,
      "type": "GROUP"
    },{
      "field": "evidenzlevel",
      "description": "Evidenzlevel",
      "value": "m1A",
      "type": "INPUT"
    },{
      "field": "evidenzlevelzusatz",
      "description": "Evidenzlevel (Zusatz)",
      "value": "Z",
      "type": "INPUT"
    }
  ]
}
```

## Beispiel zur Nutzung in einem Formularscript

Das folgende Beispiel lädt einen simplen Dialog aus dem Plugin und zeigt die Inhalte des Formulars zu einem Formularverweis an.

```javascript
Ext.syncRequire('app.plugins.forminfo.FormInfoPlugin', () => {
    let FormInfoPlugin = Ext.ClassManager.get('FormInfoPlugin');
    if (FormInfoPlugin === null) {
        // ggf Warnung ausgeben
        return;
    }
    FormInfoPlugin.showFormContentInfo(this, getFieldValue('reftumorkonferenz').id);
});
```

![Beispiel des Infodialogs](./examples/image.png)

## Build

Für das Bauen des Plugins ist zwingend JDK in Version 11 erforderlich.
Spätere Versionen des JDK beinhalten einige Methoden nicht mehr, die von Onkostar und dort benutzten Libraries verwendet
werden.

Voraussetzung ist das Kopieren der Datei `onkostar-api-2.11.1.1.jar` (oder neuer) in das Projektverzeichnis `libs`.

**_Hinweis_**: Bei Verwendung einer neueren Version der Onkostar-API muss die Datei `pom.xml` entsprechend angepasst
werden.

Danach Ausführen des Befehls:

```shell
./mvnw package
```