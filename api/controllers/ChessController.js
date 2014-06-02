/**
 * ChessController
 *
 * @module      :: Controller
 * @description	:: A set of functions called `actions`.
 *
 *                 Actions contain code telling Sails how to respond to a certain type of request.
 *                 (i.e. do stuff, then send some JSON, show an HTML page, or redirect to another URL)
 *
 *                 You can configure the blueprint URLs which trigger these actions (`config/controllers.js`)
 *                 and/or override them with custom routes (`config/routes.js`)
 *
 *                 NOTE: The code you write here supports both HTTP and Socket.io automatically.
 *
 * @docs        :: http://sailsjs.org/#!documentation/controllers
 */

module.exports = {
    
  index : function(req, res) {
    var java = require('java');
    // console.log(java.classpath);

    var input="kaniht to ay3";

    var MyClass = java.import('classifier.MoveAnalyzer');
    var analyzer = new MyClass();
    analyzer.initSync();
    var string = analyzer.classifyPieceSync(input);
    var letter = analyzer.classifyLetterSync(input);
    var number = analyzer.classifyNumberSync(input);
    console.log('Testing string: ');
    console.log(string + " " + letter + " " + number);

    res.view();
  },


  /**
   * Overrides for the settings in `config/controllers.js`
   * (specific to ChessController)
   */
  _config: {}

  
};
