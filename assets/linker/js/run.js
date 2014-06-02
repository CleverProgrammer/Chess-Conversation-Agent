$(document).ready(function() {
  // Chess
  runChess();

  // Testing superagent
  superagent
    .get('/chess/')
    .end(function(res) {
      console.log('Testing superagent...');
    });
});