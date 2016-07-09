package experiment;

public class IdScore implements Comparable<IdScore> {
	int id;

	double score;

	public int compareTo(IdScore other) {
		return (int) Math.signum(other.score - this.score);
	}

	public IdScore(int id, double score) {
		this.id = id;
		this.score = score;
	}
	
	public String toString(){
		return "[ "+id+" : "+score+" ] ";
	}
}
