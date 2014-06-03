function AnnyangClass() {}

// Setting up and running annyang
AnnyangClass.setupAnnyang = function() {
  if (annyang) {
    // Define commands
    var chessCommands = {
      'hello' : function() {
        console.log('Hello!');
        BoardClass.addUserLine('Hello!');
      },
      'pawn (to) *term' : AnnyangClass.pawnMoveCommand,
      'pawn (takes) *term' : AnnyangClass.pawnMoveCommand,
      'rook (to) *term' : AnnyangClass.rookMoveCommand,
      'rook (takes) *term': AnnyangClass.rookMoveCommand,
      'knight (to) *term' : AnnyangClass.knightMoveCommand,
      'knight (takes) *term' : AnnyangClass.knightMoveCommand,
      'night (to) *term' : AnnyangClass.knightMoveCommand,
      'night (takes) *term' : AnnyangClass.knightMoveCommand,
      'bishop (to) *term' : AnnyangClass.bishopMoveCommand,
      'bishop (takes) *term' : AnnyangClass.bishopMoveCommand,
      'queen (to) *term' : AnnyangClass.queenMoveCommand,
      'queen (takes) *term' : AnnyangClass.queenMoveCommand,
      'king (to) *term' : AnnyangClass.kingMoveCommand,
      'king (takes) *term' : AnnyangClass.kingMoveCommand,
      '*term' : AnnyangClass.unknownCommand
    }

    // Add commands to annyang
    annyang.addCommands(chessCommands);

    // Start annyang
    AnnyangClass.startAnnyang();
  }
};

// Pawn move command
AnnyangClass.pawnMoveCommand = function(term) {
  console.log(term);
  var move = ChessClass.returnMoveFromTerm('pawn', term);
  if (!move) {
    async.series({
      // Hit classifier backend on '{#pieceName} *term' move
      pawn : function(callback) { 
        ChessClass.classifyMove('pawn ' + term, callback);
      }
    },
    function(err, results) {
      move = results.pawn;
      ChessClass.processMove(move);
    });  
  } else {
    ChessClass.processMove(move);
  }
}

// Rook move command
AnnyangClass.rookMoveCommand = function(term) {
  console.log(term);
  var move = ChessClass.returnMoveFromTerm('rook', term);
  if (!move) {
    async.series({
      // Hit classifier backend on '{#pieceName} *term' move
      rook : function(callback) { 
        ChessClass.classifyMove('rook ' + term, callback);
      }
    },
    function(err, results) {
      move = results.rook;
      ChessClass.processMove(move);
    });  
  } else {
    ChessClass.processMove(move);
  }
}

// Knight move command
AnnyangClass.knightMoveCommand = function(term) {
  console.log(term);
  var move = ChessClass.returnMoveFromTerm('knight', term);
  if (!move) {
    async.series({
      // Hit classifier backend on '{#pieceName} *term' move
      knight : function(callback) { 
        ChessClass.classifyMove('knight ' + term, callback);
      }
    },
    function(err, results) {
      move = results.knight;
      ChessClass.processMove(move);
    });  
  } else {
    ChessClass.processMove(move);    
  }
}

// Bishop move command
AnnyangClass.bishopMoveCommand = function(term) {
  console.log(term);
  var move = ChessClass.returnMoveFromTerm('bishop', term);
  if (!move) {
    async.series({
      // Hit classifier backend on '{#pieceName} *term' move
      bishop : function(callback) { 
        ChessClass.classifyMove('bishop ' + term, callback);
      }
    },
    function(err, results) {
      move = results.bishop;
      ChessClass.processMove(move);
    });  
  } else {
    ChessClass.processMove(move);
  }
}

// Queen move command
AnnyangClass.queenMoveCommand = function(term) {
  console.log(term);
  var move = ChessClass.returnMoveFromTerm('queen', term);
  if (!move) {
    async.series({
      // Hit classifier backend on '{#pieceName} *term' move
      queen : function(callback) { 
        ChessClass.classifyMove('queen ' + term, callback);
      }
    },
    function(err, results) {
      move = results.queen;
      ChessClass.processMove(move);
    });  
  } else {
    ChessClass.processMove(move);
  }
}

// King move command
AnnyangClass.kingMoveCommand = function(term) {
  console.log(term);
  var move = ChessClass.returnMoveFromTerm('king', term);
  if (!move) {
    async.series({
      // Hit classifier backend on '{#pieceName} *term' move
      king : function(callback) { 
        ChessClass.classifyMove('king ' + term, callback);
      }
    },
    function(err, results) {
      move = results.king;
      ChessClass.processMove(move);
    });  
  } else {
    ChessClass.processMove(move);
  }
}

// Unknown command
AnnyangClass.unknownCommand = function(term) {
  console.log('UNKNOWN - ' + term);
  async.series({
    // Hit classifier backend on '{#pieceName} *term' move
    unknown : function(callback) { 
      ChessClass.classifyMove(term, callback);
    }
  },
  function(err, results) {
    var move = results.unknown;
    ChessClass.processMove(move);
  });  
}

// Helper to prompt user with message
AnnyangClass.promptUser = function(message) {
  console.log(message);
}

// Helper to prompt user for piece
AnnyangClass.promptUserForPiece = function(piece) {
  var pieceName = ChessClass.REVERSE_PIECE_MAP[piece];
  console.log('Which ' + pieceName + ' are you referring to?'); 
}

// Helper to start annyang
AnnyangClass.startAnnyang = function() {
  AnnyangClass.annyangStopped = false;
  annyang.start();
};

// Helper to stop annyang
AnnyangClass.stopAnnyang = function() {
  AnnyangClass.annyangStopped = true;
  annyang.abort();
}