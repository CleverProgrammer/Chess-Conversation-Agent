Chess Conversation Agent
---

Proof-of-concept chess playing agent using the power of speech! Created as a small project for the speech recognition class.

How to Install
---

* Install `node.js` following the instructions found on http://sailsjs.org/#/getStarted.
* Install `sails.js` by running `sudo npm -g install sails`.
* Set up `https`:
  
    * Run `mkdir ssl`.
    * Run `cd ssl`.
    * From the `ssl` directory, run the commands to generate a self-signed certificate from this site (http://docs.nodejitsu.com/articles/HTTP/servers/how-to-create-a-HTTPS-server). You'll need `key.pem` and `csr.pem` in this directory.

* Run `npm install` to install dependencies.

How to Run
---
* Download this project as `ZIP`.
* Run `copy_java` to compile `java` files.
* Run `sudo sails lift` to start the `sails` server.
* On Google Chrome, go to `https://localhost:1337`. You may have to override Chrome's warnings on running `https` locally.
* Check that `annyang` is running by looking for the pulsating red dot on the Chrome tab.
* Play a short game of chess!

How to Play
---
* The move classifier is _not robust at all_, so you may have to enunciate clearly and slowly. You may also have to repeat saying stuff over and over and over again. Patience, you must have my young padawan.  
* The agent mostly uses algebraic chess notation. To move a piece, just say the name of the piece and then its destination square. For example, to move a pawn to e4, just say "Pawn e4" or "Pawn to e4".
* To castle, just say `Kingside castle` or `Queenside castle`.
* To capture, just say the name of the piece, the word `takes`, and then the destination square. For example, for a knight capturing a pawn on e5, just say `Knight takes e5`.
