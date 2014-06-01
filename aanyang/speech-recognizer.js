(function($) {

    $(document).ready(function() {

        try {
            var recognition = new webkitSpeechRecognition();
        } catch(e) {
            var recognition = Object;
        }
        recognition.continuous = true;
        recognition.interimResults = true;

		var input = '';
        var interimResult = '';
        var textArea = $('#speech-page-content');
        var textAreaID = 'speech-page-content';

        var startRecognition = function() {
            $('.speech-content-mic').removeClass('speech-mic').addClass('speech-mic-works');
            textArea.focus();
            recognition.start();
        };
		
		
        recognition.onresult = function (event) {
            //var pos = textArea.getCursorPosition() - interimResult.length;
            input = (input.replace(interimResult, ''));
            interimResult = '';
            //textArea.setCursorPosition(pos);
			finished = false;
			for (var i = event.resultIndex; i < event.results.length; ++i) {
				if (event.results[i].isFinal) {
					input += event.results[i][0].transcript;
					textArea.val(input);
					finished = true;
                }
				else {
					interimResult += event.results[i][0].transcript + '\u200B';
					input += interimResult
				}
            }
			if (finished) {
				recognition.stop();
			}
        };

        recognition.onend = function() {
            $('.speech-content-mic').removeClass('speech-mic-works').addClass('speech-mic');
        };
		
		startRecognition();
    });
})(jQuery);