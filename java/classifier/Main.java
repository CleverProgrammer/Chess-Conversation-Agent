package classifier;

import java.util.ArrayList;
import java.util.List;

// Features are individual, non-space characters
class CharacterParser implements ParseNaiveFeatures {
	@Override
	public List<String> parse(String data) {
		List<String> features = new ArrayList<String>();
		
		for (int i = 0; i < data.length(); i++) {
			char c = data.charAt(i);
			if (!Character.isSpaceChar(c)) {
				features.add(Character.toString(c));
			}
		}
		return features;
	}
}

// Features are anything separated by spaces
class SimpleTokenizer implements ParseNaiveFeatures {
	@Override
	public List<String> parse(String data) {
		List<String> features = new ArrayList<String>();
		
		String curWord = "";
		for (int i = 0; i < data.length(); i++) {
			char c = data.charAt(i);
			// Single digits, others as words
			if (!Character.isSpaceChar(c))
				curWord += c;
			else if (curWord.length() > 0) {
				features.add(curWord);
				curWord = "";	
			}
		}
		if (curWord.length() > 0)
			features.add(curWord);
		
		return features;
	}
}

// Features are words/symbols separated by spaces + individual digits
class WordParser implements ParseNaiveFeatures {
	@Override
	public List<String> parse(String data) {
		List<String> features = new ArrayList<String>();
		
		String curWord = "";
		for (int i = 0; i < data.length(); i++) {
			char c = data.charAt(i);
			// Single digits, others as words
			if (Character.isAlphabetic(c) || (!Character.isDigit(c) && !Character.isSpaceChar(c)))
				curWord += c;
			else {
				if (!Character.isSpaceChar(c))
					features.add(Character.toString(c));
				
				if (curWord.length() > 0)
					features.add(curWord);
				
				curWord = "";	
			}
		}
		if (curWord.length() > 0)
			features.add(curWord);
		
		return features;
	}
	
}
