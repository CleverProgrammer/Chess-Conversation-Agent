$(document).ready(function() {
  // Chessboard configuration
  var cfg = {
    draggable: true,
    dropOffBoard: 'snapback',
    position: 'start'
  };
  var board = new ChessBoard('board', cfg);

  superagent
    .get('/chess/')
    .end(function(res) {
      console.log('hello');
    });
});