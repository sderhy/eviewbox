# eViewBox

eViewBox est une application Java AWT historique pour visualiser et manipuler des images medicales et bitmap, avec prise en charge GIF, DICOM, JPEG, BMP et PPM selon les modules presents dans `sources/`.

## Historique

Ce depot GitHub est la suite du projet historique Eviewbox DICOM java project publie sur SourceForge:

https://sourceforge.net/projects/eviewbox/files/Eviewbox/

La page SourceForge reference des archives publiees en 2001, dont `Eviewbox.jar`, `Application and Sources` et `Sources of Eviewbox`.

Le code provient d'un ancien depot CVS: les repertoires `CVS/` sont conserves dans l'arborescence locale, mais ignores par Git.

## Etat actuel

- Le code compile avec OpenJDK 17.
- Les sources Java historiques ont ete converties de MacRoman vers UTF-8 pour faciliter la maintenance sur GitHub.
- La partie applet (`e_ViewBox_Applet`) est historique et n'est plus compilee par le build par defaut. Les navigateurs modernes ne prennent plus en charge les applets Java; le lancement utile aujourd'hui est l'application desktop AWT.
- Plusieurs API AWT et `Thread` utilisees par le code sont obsoletes, mais encore compilables avec avertissements.

## Compiler

```sh
make
```

Le JAR executable est genere ici:

```text
build/eviewbox.jar
```

## Lancer

Depuis la racine du projet, la commande la plus simple est:

```sh
make run
```

Elle compile le projet si necessaire, genere `build/eviewbox.jar`, puis lance l'application.

On peut aussi lancer directement le JAR deja genere:

```sh
java -jar build/eviewbox.jar
```

## Nettoyer

```sh
make clean
```

## Migration GitHub

Initialiser Git si ce n'est pas deja fait:

```sh
git init
git add .
git commit -m "Import legacy eViewBox source"
```

Creer ensuite un depot vide sur GitHub, puis connecter et publier:

```sh
git branch -M main
git remote add origin git@github.com:<compte>/<repo>.git
git push -u origin main
```

## Pistes de remise a niveau

- Convertir les sources en UTF-8 pour un affichage propre sur GitHub et supprimer `-encoding MacRoman`.
- Remplacer progressivement les appels AWT deprecies (`show`, `hide`, `enable`, ancien modele d'evenements).
- Desactiver les traces de debug permanentes dans les lecteurs DICOM.
- Remplacer l'applet par un point d'entree desktop documente ou une interface moderne.
- Ajouter quelques images de test non sensibles pour valider GIF/DICOM/JPEG/BMP.
