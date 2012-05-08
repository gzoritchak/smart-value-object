public class ExampleVO {
	private int id;
	public java.util.Collection foo = new java.util.ArrayList();
	
	public ExampleVO(int id) {
		this.id = id;
	}

	public void setId(int id) { 
		this.id = id; 
	}

	public void add(ExampleVO e) {
		foo.add(e);
	}

	public void remove(ExampleVO e) {
		foo.remove(e);
	}

	public java.util.Collection getCollection() {
		return foo;
	}

	public String toString() { return "ExampleVO id=" + id; }
}
