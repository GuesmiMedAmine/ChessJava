# Refactoring du Projet d'Échecs

Ce projet a été refactoré pour améliorer sa structure, sa lisibilité et corriger plusieurs bugs.

## Modifications principales

### 1. Architecture des décorateurs

- Création d'une classe utilitaire `DecoratorMoveUtils` pour centraliser les mouvements communs
- Création d'une classe abstraite `DecoDirectionnel` pour les pièces qui se déplacent dans des directions spécifiques
- Simplification des décorateurs Tour, Fou et Dame en utilisant l'héritage et la composition
- Suppression de code dupliqué dans les décorateurs

### 2. Correction des bugs

- **Pions qui bougent à l'envers**: Correction de la direction des pions dans `DecoPion`
- **Rafraîchissement graphique**: Amélioration du rafraîchissement dans `VueControleur`
- **Tour de joueur**: Correction de la gestion du tour dans `Controlleur`
- **Échecs/mats/pat mal détectés**: Refactoring de `DecoRoi` pour une meilleure détection

### 3. Nettoyage du code

- Suppression des commentaires inutiles et verbeux
- Simplification de la classe `Piece` en supprimant les getters redondants
- Amélioration de la classe `Deco` en supprimant les méthodes déplacées dans les utilitaires
- Javadoc minimale mais claire sur toutes les classes et méthodes importantes

### 4. Structure modulaire

- Organisation des décorateurs en classes spécialisées
- Séparation claire des responsabilités entre modèle, vue et contrôleur
- Réduction du couplage entre les composants

## Organisation des packages

- `modele.pieces`: Classes de base pour les pièces
- `modele.deco`: Décorateurs pour les mouvements des pièces
- `modele.plateau`: Plateau et cases
- `modele.jeu`: Logique du jeu
- `vue`: Interface graphique
- `controlleur`: Gestion des interactions utilisateur

## Améliorations futures possibles

- Ajouter des tests unitaires
- Implémenter un système de notation algébrique complet
- Ajouter une fonctionnalité de sauvegarde/chargement de partie
- Développer une IA pour jouer contre l'ordinateur 