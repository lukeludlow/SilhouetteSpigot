
in spigot.yml, set `entity-tracking-range` for `players` to a very large number, e.g. 9999 or 2147483647 (java int max).
this should not have a noticeable effect on performance.

e.g.
```
world-settings:
    entity-tracking-range:
      players: 2147483647
      animals: 48
      monsters: 48
      misc: 32
      other: 64
```


TODO figure out if my named entity spawn stuff messes up when people have a named dog or iron golem or something


okayy. so if you set entity tracking range to int max 
then it actually causes problems. so maybe do 1,000,000. 9999 works for sure



