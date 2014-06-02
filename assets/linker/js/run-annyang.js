// Setting up and running annyang
function runAnnyang() {
  if (annyang) {
    // Define commands
    var commands = {
      'hello' : function() {
        console.log('Hello!');
      },
      'show me *term' : function(term) {
        console.log(term);
      },
      'pawn *term' : function(term) {
        console.log(term);
      },
      'bishop *term' : function(term) {
        console.log('bishop' + term);
      }
    }

    // Add commands to annyang
    annyang.addCommands(commands);

    // Start annyang
    annyang.start();
  }

}