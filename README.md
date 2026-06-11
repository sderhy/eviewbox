# eViewBox

Visualiseur d'images médicales et bitmap écrit en Java AWT. Léger, rapide, sans dépendance : un seul JAR, démarrage instantané, lecture native du DICOM, GIF, JPEG, BMP et PPM.

Pensé pour qui veut ouvrir et manipuler une image sans lancer une suite lourde — utile au quotidien, notamment en imagerie médicale.

## Avertissement / Disclaimer

**EViewBox n'est pas un dispositif médical.** Ce logiciel est fourni à des fins éducatives, de recherche et de démonstration uniquement ; il ne doit pas être utilisé pour le diagnostic médical ni pour des décisions de traitement. Le logiciel est fourni « tel quel », sans aucune garantie ; l'auteur décline toute responsabilité quant à son utilisation (voir les sections 7 et 8 de la [licence Apache 2.0](LICENSE)).

**EViewBox is not a medical device.** This software is provided for educational, research and demonstration purposes only; it must not be used for medical diagnosis or treatment decisions. The software is provided "as is", without warranty of any kind; the author accepts no liability for its use (see sections 7 and 8 of the [Apache 2.0 license](LICENSE)).

## Télécharger

Des installeurs autonomes (Java inclus) sont publiés sur la [page Releases](https://github.com/sderhy/eviewbox/releases) : `.dmg` macOS (signé et notarisé), `.msi` Windows et `.deb` Linux.

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
