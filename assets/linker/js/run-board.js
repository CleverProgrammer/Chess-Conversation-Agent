function BoardClass() {}

// Setting up conversation board
BoardClass.setupBoard = function() {}

// Add conversation results
BoardClass.addUserLine = function(line) {
  var iconSrc = '';
  if (BoardClass.player === 'w') {
    iconSrc = 'images/white_king.gif';
    BoardClass.player = 'b';
  } else {
    iconSrc = 'images/black_king.gif';
    BoardClass.player = 'w';
  }

  var userLine = '<tr class="userRow"> \
      <td class="iconColumn"><img src="' + iconSrc + '"></td> \
      <td class="lineColumn">' + line + '</td> \
      </tr>';

  $("#conversationBoard").append(userLine);
}

BoardClass.addUserMove = function(move) {
  var piece = "";
  if (move.piece !== 'p') {
    piece = move.piece;
  }
  BoardClass.addUserLine(piece + move.to);
}