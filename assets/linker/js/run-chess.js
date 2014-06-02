function runChess() {
  async.waterfall([
    // Setup chess
    function(callback) {
      // Setup chess and chessboard
      callback(null, setupChess());
    },
    // Setup annyang
    function(chessObject, callback) {
      // Get game and board
      var game = chessObject.game;
      var board = chessObject.board;

      // Setup annyang
      annyangStopped = false;
      setupAnnyang(game, board);

      callback(null, null);
    },
    // Setup buttons
    function(nothing, callback) {
      setupButtons();

      callback(null, null);
    }
  ],
  function(err, result) {
    // DONE
  });
}

function setupChess() {
  var game = new Chess();

  // Chessboard configuration

  // Do not pick up pieces if the game is over
  // Only pick up pieces for the side to move
  var onDragStart = function(source, piece, position, orientation) {
    if (game.game_over() === true || 
      (game.turn() === 'w' && piece.search(/^b/) !== -1) ||
      (game.turn() === 'b' && piece.search(/^w/) !== -1)) {
      return false;
    }
  }

  var onDrop = function(source, target) {
    // Check if the move is legal
    var move = game.move({
      from: source,
      to: target,
      promotion: 'q' // Queen promotion for example simplicity
    });

    // Illegal move
    if (move === null) return 'snapback';
  }

  // Update the board position after the piece snap
  var onSnapEnd = function() {
    board.position(game.fen());
  }

  var cfg = {
    draggable: true,
    dropOffBoard: 'trash',
    position: 'start',
    onDragStart: onDragStart,
    onDrop: onDrop,
    onSnapEnd: onSnapEnd
  };
  var board = new ChessBoard('board', cfg);

  var chessObject = {
    board: board,
    game: game
  }
  return chessObject;
}