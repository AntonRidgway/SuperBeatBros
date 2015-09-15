Super Beat Bros
===============

A Java-based defense game about avoiding projectiles and dancing to funky beats.

The game is played by moving the Beat Bros' van around with the WASD keys, and using the mouse to collect musical energy (from falling notes) and draw musical-energy shields. The shields block incoming alien projectiles, which increase in intensity as the game progresses. At times a wyrm will also appear, whose head must be clicked to vanquish him.
A .jar file is provided for convenience of execution-- all assets are packaged inside.

The bulk of the code base is adapted from Andrew Davison's "Killer Game Programming in Java" example code. It can be found (as of 9/15/15) at [the book's website](http://fivedots.coe.psu.ac.th/~ad/jg/code/index.html).
Novel development for the game was done in the following classes:

- BeatBrosGame, adapted from Davison's WormChase class and Mailler's modifications
- GameFrame, adapted from Davison's GameFrame class and Mailler's modifications
- entities.DefenseField
- entities.ExplosionSprite
- entities.MissileSprite
- entities.NoteSprite
- entities.PlayerSprite
- entities.Sprite, adapted from Davison's Sprite class
- entities.Wyrm, adapted from Davison's Worm class
- framework.GameMenu
- framework.RibbonsManager, adapted from Davison's RibbonsManager class
- framework.ScoreTable
- sound.MusicManager

All art assets are original.
Midi files are drawn from various sources around the net, which at this point I honestly cannot recall.
Most sound effects from freesound.org, with explosion sounds from Andrew Davison's examples.
