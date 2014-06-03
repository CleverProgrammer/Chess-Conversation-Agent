function BoardClass() {}

// Setting up conversation board
BoardClass.setupBoard = function() {}

// Add conversation results
BoardClass.addUserLine = function(line) {
  var userLine = '<tr class="userRow"> \
      <td class="iconColumn">User</td> \
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