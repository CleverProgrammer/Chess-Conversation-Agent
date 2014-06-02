$(document).ready(function() {
  // Chessboard configuration
  var cfg = {
    draggable: true,
    dropOffBoard: 'trash',
    position: 'start'
  };
  var board = new ChessBoard('board', cfg);

  superagent
    .get('/chess/')
    .end(function(res) {
      console.log('hello');
    });
});