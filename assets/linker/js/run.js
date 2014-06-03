$(document).ready(function() {
  // Chess
  ChessClass.runChess();

  // Testing superagent
  superagent
    .get('/chess/')
    .end(function(res) {
      console.log('Testing superagent...');
    });
});