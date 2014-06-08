function ChessClass() {}

ChessClass.runChess = function() {
  async.waterfall([
    // Setup chess
    function(callback) {
      // Setup chess and chessboard
      callback(null, ChessClass.setupChess());
    },
    // Setup annyang
    function(chessObject, callback) {
      // Setup annyang
      AnnyangClass.annyangStopped = false;
      AnnyangClass.setupAnnyang();

      callback(null, null);
    },
    // Setup conversation board
    function(nothing, callback) {
      // Setup conversation board
      BoardClass.setupBoard();

      callback(null, null);
    },
    // Setup buttons
    function(nothing, callback) {
      ButtonClass.setupButtons();

      callback(null, null);
    }
  ],
  function(err, result) {
    // DONE
  });
}

ChessClass.setupChess = function() {
  // Define LETTERS constant
  ChessClass.LETTERS = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'];

  // Define NUMBERS constant
  ChessClass.NUMBERS = ['1', '2', '3', '4', '5', '6', '7', '8'];

  // Define PIECE_MAP constant
  ChessClass.PIECE_MAP = {
    'pawn': '',
    'rook': 'R',
    'knight': 'N',
    'bishop': 'B',
    'queen': 'Q',
    'king': 'K'
  };

  // Define REVERSE_PIECE_MAP constant
  ChessClass.REVERSE_PIECE_MAP = {
    '': 'pawn',
    'R': 'rook',
    'N': 'knight',
    'B': 'bishop',
    'Q': 'queen',
    'K': 'king'
  };

  // Set current player to white
  BoardClass.player = 'w';

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

  ChessClass.board = board;
  ChessClass.game = game;
}

// Helper to parse and return possible move from term string
ChessClass.returnMoveFromTerm = function(pieceName, term) {
  // Check if '*term' is simply a '{#letter} {#number}' string
  var piece = ChessClass.PIECE_MAP[pieceName];
  var split = term.split(' ');
  if (split.length === 2) {
    // Possible '{#letter} {#number}' string
    var letter = split[0].toLowerCase();
    var number = split[1].toLowerCase();
    
    return ChessClass.game.move(piece + letter + number);
  } else if (split.length === 1) {
    // Possible '{#letter}{#number}' string
    return ChessClass.game.move(piece + term);
  }
}

// Classify move from the input move string
ChessClass.classifyMove = function(input, callback) {
  console.log('# Classifying move...');
  console.log(input);

  // Hit classifyMove backend
  superagent
    .get('/chess/classifyMove?input=' + input)
    .end(function(res) {
      var output = res.body.output;

      console.log('# Classifier returned move...');
      console.log(output);

      // Check if [MOVE] or [CASTLE] or [?]
      if (output.startsWith('[MOVE]')) {
        // Check if ambiguous
        // if (ChessClass.ambiguous) {
          // [AMBIGUOUS]
        //  callback(null, ChessClass.resolveAmbiguity(output));
        // } else {
          // [MOVE]
          callback(null, ChessClass.processMoveOutput(output));
        // }
      } else if (output.startsWith('[CASTLE]')) {
        // [CASTLE]
        callback(null, ChessClass.processCastleOutput(output));
      } else {
        // [?]
        callback(null, '[?]');
      }
    });
}

// startsWith
String.prototype.startsWith = function(str) {
  return this.indexOf(str) === 0;
}

// endsWith
String.prototype.endsWith = function(str) {
  return this.indexOf(str, this.length - str.length) !== -1;
}

// startsWithChessLetter
String.prototype.startsWithChessLetter = function() {
  for (var j = 0; j < ChessClass.LETTERS.length; j++) {
    var letter = ChessClass.LETTERS[j];
    if (this.startsWith(letter)) {
      return true;
    }
  }
  return false;
}

// Process move
ChessClass.processMove = function(move) {
  if (move) {
    if (typeof move === 'string') {
      if (move === '[?]') {
        // [?]
        // Prompt user again
        AnnyangClass.promptUser('Move not recognized. Please try again...');
        console.log('FAILURE TO RECOGNIZE');
      } // else if (move === '[AMBIGUOUS]') {
        // console.log('AMBIGUITY');
        // }
    } else {
      // [MOVE] or [CASTLE]
      ChessClass.board.position(ChessClass.game.fen());
      BoardClass.addUserMove(move);
    }
  } else {
    // Illegal move
    // Prompt user again
    AnnyangClass.promptUser('Illegal move. Please try again...');
    console.log('ILLEGAL MOVE');
  }
}

// Process [MOVE] output
ChessClass.processMoveOutput = function(output) {  
  // [MOVE]
  var moveSplit = output.split(' ');
  var piece = ChessClass.PIECE_MAP[moveSplit[1]];
  var letter = moveSplit[2];
  var number = moveSplit[3];

  // console.log('# In [MOVE]...');
  // console.log(piece + letter + number);

  var move = ChessClass.game.move(piece + letter + number);
  if (!move) {
    // Check for possible capture
    var captureMoves = ChessClass.checkCapture(piece, letter, number);
    // console.log(captureMoves);

    if (captureMoves.length === 1) {
      // Only one possible capture
      return ChessClass.game.move(captureMoves[0]);  
    } // else if (captureMoves.length === 2) {
      // Two possible captures, resolve ambiguity
      // Prompt user for piece
      // AnnyangClass.promptUserForPiece(piece);

      // Set ambiguity variables
      // ChessClass.ambiguity = true;
      // ChessClass.ambiguityType = 'capture';
      // ChessClass.ambiguousMoves = captureMoves;

      // return '[AMBIGUOUS]';
      // }
  } else {
    return move;
  }
}

// Process [CASTLE] output
ChessClass.processCastleOutput = function(output) {
  // [CASTLE]
  var castleSplit = output.split(' ');
  var side = castleSplit[1];

  if (side === 'king') {
    // O-O
    return ChessClass.game.move('O-O');
  } else {
    // O-O-O
    return ChessClass.game.move('O-O-O');
  }
}

// Check for possible capture
ChessClass.checkCapture = function(piece, letter, number) {
  console.log('# Checking capture');
  // console.log(piece + letter + number);
  var captureMoves = [];

  // Get all legal moves
  var legalMoves = ChessClass.game.moves();
  // console.log(legalMoves);

  // Check each legal move for 'x#{letter]#{number}' near the end
  for (var i = 0; i < legalMoves.length; i++) {
    var move = legalMoves[i];

    if (move.endsWith('x' + letter + number)) {
      if (piece === '') {
        // Pawn
        if (move.startsWithChessLetter()) {
          captureMoves.push(move[0] + 'x' + letter + number);
        }
      } else {
        // Not pawn
        if (move.startsWith(piece)) {
          captureMoves.push(piece + 'x' + letter + number);
        }
      }
    }
  }
  return captureMoves;
}

// Resolve ambiguity
ChessClass.resolveAmbiguity = function(output) {
  if (ChessClass.ambiguityType === 'capture') {
    return ChessClass.resolveCaptureAmbiguity(output);
  }
}

// Resolve capture ambiguity
ChessClass.resolveCaptureAmbiguity = function(output) {
  var moveSplit = output.split(' ');
  var piece = ChessClass.PIECE_MAP[moveSplit[1]];
  var letter = moveSplit[2];
  var number = moveSplit[3];

  for (var i = 0; i < ChessClass.ambiguousMoves.length; i++) {
    var move = ChessClass.ambiguousMoves[i];


  }
}