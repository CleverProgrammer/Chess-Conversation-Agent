// Setting up and running annyang
function setupAnnyang(game, board) {
  if (annyang && game && board) {
    // Define LETTERS constant
    var LETTERS = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'];

    // Define NUMBERS constant
    var NUMBERS = ['1', '2', '3', '4', '5', '6', '7', '8'];

    // Define commands
    var chessCommands = {
      'hello' : function() {
        console.log('Hello!');
      },
      'pawn (to) *term' : function(term) {
        console.log(term);
        var split = term.split(' ');
        if (split.length == 2) {
          console.log('Has two terms');
          var letter = split[0].toLowerCase();
          var number = split[1].toLowerCase();
          if ($.inArray(letter, LETTERS) !== -1 && 
            $.inArray(number, NUMBERS) !== -1) {
            var moveString = letter + number;
            console.log(moveString);
            var move = game.move(letter + number);
          }
        } else {
          var move = game.move(term);
        }
        if (move) {
          console.log('Move succesful!');
          board.position(game.fen());
        }
      },
      'rook (to) *term' : function(term) {
        console.log(term);
        var move = game.move('R' + term);
        if (move) {
          console.log('Move succesful!');
          board.position(game.fen());
        } 
      },
      'knight (to) *term' : function(term) {
        console.log(term);
        var move = game.move('N' + term);
        if (move) {
          console.log('Move succesful!');
          board.position(game.fen());
        } 
      },
      'night (to) *term' : function(term) {
        console.log(term);
        var move = game.move('N' + term);
        if (move) {
          console.log('Move succesful!');
          board.position(game.fen());
        } 
      },
      'bishop (to) *term' : function(term) {
        console.log(term);
        var move = game.move('B' + term);
        if (move) {
          console.log('Move succesful!');
          board.position(game.fen());
        }
      },
      'queen (to) *term' : function(term) {
        console.log(term);
        var move = game.move('Q' + term);
        if (move) {
          console.log('Move succesful!');
          board.position(game.fen());
        } 
      },
      'king (to) *term' : function(term) {
        console.log(term);
        var move = game.move('K' + term);
        if (move) {
          console.log('Move succesful!');
          board.position(game.fen());
        } 
      },
    }

    // Add commands to annyang
    annyang.addCommands(chessCommands);

    // Start annyang
    startAnnyang();
  }
};

function startAnnyang() {
  annyang.start();
};

function stopAnnyang() {
  annyang.abort();
}