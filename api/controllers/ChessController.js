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
var java = require('java');
var request = require('request');

module.exports = {
    
  index : function(req, res) {
    var input = 'kaniht to ay3';
    var input2 = 'castle quin side';

    var MyClass = java.import('classifier.MoveAnalyzer');
    var analyzer = new MyClass();
    analyzer.initSync();
    var string = analyzer.classifyMoveSync(input);
    var string2 = analyzer.classifyMoveSync(input2);
    console.log('Testing string: ');
    console.log(string);
    console.log(string2);

    res.view();
  },

  classifyMove : function(req, res) {
    // Get input
    var input = req.param('input');

    console.log('# In classifyMove backend...');
    console.log(input);

    // Call classifier backend
    var MoveAnalyzerClass = java.import('classifier.MoveAnalyzer');
    var analyzer = new MoveAnalyzerClass();
    analyzer.initSync();
    var output = analyzer.classifyMoveSync(input);

    console.log(output);

    return res.json({ output: output });
  },

  requestWit : function(req, res) {
    // Get message
    var message = req.param('message');

    console.log('# In requestWit backend...');
    console.log(message);

    var options = {
      url: 'https://api.wit.ai/message?q=' + encodeURIComponent(message),
      headers: {
        'Authorization': 'Bearer ENVUGDRN5DQJMXN4YQJWEUYOQ66TVU3J',
        'Accept' : 'application/vnd.wit.20140401'
      }
    };

    request(options, function(err, res, body) {
      if (err) {
        console.log(err);
      } else {
        console.log(body);
        return body;
      }
    });
  },


  /**
   * Overrides for the settings in `config/controllers.js`
   * (specific to ChessController)
   */
  _config: {}

  
};
