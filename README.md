# eViewBox

Visualiseur d'images médicales et bitmap écrit en Java AWT. Léger, rapide, sans dépendance : un seul JAR, démarrage instantané, lecture native du DICOM, GIF, JPEG, BMP et PPM.

Pensé pour qui veut ouvrir et manipuler une image sans lancer une suite lourde — utile au quotidien, notamment en imagerie médicale.

## Fonctionnalités

- Lecture DICOM avec décodeur intégré
- Reconstruction multiplanaire (MPR)
- Diaporama et navigation dans les séries
- Encodeurs JPEG, GIF, BMP et PPM maison
- Outils de dessin et d'annotation sur l'image
- Impression
- Envoi d'images par mail (SMTP/POP3 intégrés)

Le tout en AWT pur, sans bibliothèque externe : l'application reste compacte et démarre sans délai.

## Compiler

```sh
make
```

Le JAR exécutable est généré dans :

```text
build/eviewbox.jar
```

## Lancer

```sh
make run
```

Compile si nécessaire, génère `build/eviewbox.jar` et lance l'application.

On peut aussi lancer directement le JAR :

```sh
java -jar build/eviewbox.jar
```

## Nettoyer

```sh
make clean
```

## Origine

eViewBox existe depuis 2001 et tourne toujours : le code compile sans modification sous OpenJDK 17. Ce dépôt est la migration du projet historique, publié à l'origine sur SourceForge (https://sourceforge.net/projects/eviewbox/) et auparavant suivi sous CVS.

Les sources ont été passées en UTF-8 pour la maintenance, et la partie applet d'origine (`e_ViewBox_Applet`) est conservée pour mémoire mais n'est plus compilée par défaut — le point d'entrée actuel est l'application desktop.

## Idées d'évolution

- Moderniser progressivement les appels AWT historiques
- Couper les traces de debug dans les lecteurs DICOM
- Ajouter quelques images de test non sensibles (GIF/DICOM/JPEG/BMP)
