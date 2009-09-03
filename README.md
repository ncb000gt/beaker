Beaker
==

Beaker is an experiment with the Scala programming language. Scala itself lends some nice helpers in the concurrency arena that Java natively does not. What happens is that the developer is then forced to do things like "synchronization" or waiting. Threads doing this run the potential of blocking and thus your app runs into deadlock. The methods to prevent the deadlock add a fair amount of overhead to the runtime execution of the language. Scala skirts this with the availability of Actors.

So, in my quest to learn Scala, I thought, "what better way to learn concurrency than a web server?" I'm positive there are other ways but this is a fun one so why not right?

Beaker was born.

How To
===

To use beaker, you pull the source down and then run `ant build`.

After that, all you have to do is ./start.sh

**Note** The ant file does have hardcoded paths to the scala libs. This is based on my Kubuntu setup. Sorry if it doesn't match your setup, read first couple paragraphs. If I ever decide to make this more than an experiment, I'll fix it. :)