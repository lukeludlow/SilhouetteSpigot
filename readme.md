
in spigot.yml, set `entity-tracking-range` for `players` to a very large number, e.g. 1000000 (but DON'T use java int max).
this should not have a noticeable effect on performance.

e.g.
```
world-settings:
    entity-tracking-range:
      players: 1000000
      animals: 48
      monsters: 48
      misc: 32
      other: 64
```


TODO figure out if my named entity spawn stuff messes up when people have a named dog or iron golem or something


