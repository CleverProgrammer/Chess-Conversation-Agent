$(document).ready(function() {
  // Chessboard configuration
  var cfg = {
    draggable: true,
    dropOffBoard: 'snapback',
    position: 'start'
  };
  var board = new ChessBoard('board', cfg);

  var request = require('superagent');
  
  request
    .get('http://google.com')
    .end(function(res) {
      console.log('hello');
    });
});