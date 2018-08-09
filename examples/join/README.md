# join

A bad clone of the unix [join](https://linux.die.net/man/1/join) utility.

Provided to give a look at how you might use this library on the JVM.

## Examples

```
$ lein run resources/a1.txt resources/a2.txt
251308 30000 Preeti
251311 25000 Joseph
251315 10000 Abishek
251321 12255 Ankita
```

```
$ cat resources/a1.txt | lein run resources/a2.txt
251308 30000 Preeti
251311 25000 Joseph
251315 10000 Abishek
251321 12255 Ankita
```
