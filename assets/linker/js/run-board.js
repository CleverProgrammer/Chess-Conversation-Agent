function BoardClass() {}

// Setting up conversation board
BoardClass.setupBoard = function() {}

// Add conversation results
BoardClass.addUserLine = function(line) {
  var userLine = '<div class="userRow"> \
      <p> <span class="lineWho">You said... </span><span class="lineContent">' + line + '</span></p> \
      </div>';

  $("#conversationBoard").append(userLine);
}

BoardClass.addComputerLine = function(line) {
  var computerLine = '<div class="computerRow"> \
    <p class="lineWho"></p><p class="lineContent">' + line + '</p> \
    </div>';

  $("#conversationBoard").append(computerLine);
}

BoardClass.addUserMove = function(move) {
  console.log(move);
  if (move.piece === 'p' && move.to === 'b5') {
    BoardClass.addComputerLine("That's not a very good move...");
  } else {
    BoardClass.addComputerLine('Good move!');
  }
  $("#pgn").html(ChessClass.game.pgn());
}

BoardClass.addHint = function() {
  BoardClass.addComputerLine('You should play 2 Nf3!');
}

BoardClass.addPieceAtSquare = function() {
  BoardClass.addComputerLine("It's a bishop!");
}

"Hello!"
"Pawn to e4"
"Pawn to e5"
"What should I play next?"
"Thanks"
"Knight to f3"
"What is the piece at square f1?"
"Pawn to b5"
"Thanks"
"Bishop takes b5"
"Bishop to c5"
"Castle kingside"