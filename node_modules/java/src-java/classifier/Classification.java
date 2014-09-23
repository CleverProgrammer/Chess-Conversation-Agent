package classifier;

public class Classification<O> {
	public final O result;
	public final double confidence;
	
	public Classification(O r, double c) {
		result = r;
		confidence = c;
	}
}
