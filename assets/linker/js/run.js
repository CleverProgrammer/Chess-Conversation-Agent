$(document).ready(function() {
  // Chessboard configuration
  var cfg = {
    draggable: true,
    dropOffBoard: 'trash',
    position: 'start'
  };
  var board = new ChessBoard('board', cfg);

  // Annyang
  // runAnnyang();

  // Testing superagent
  superagent
    .get('/chess/')
    .end(function(res) {
      console.log('Testing superagent...');
    });
});