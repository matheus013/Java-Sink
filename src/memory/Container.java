package memory;

import java.util.ArrayList;

import app.SenseMsg;

public abstract class Container {
	protected ArrayList<SenseMsg> content;

	public abstract void add(SenseMsg msg);

	public int length() {
		return content.size();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<SenseMsg> get() {
		return (ArrayList<SenseMsg>) content.clone();
	}
	public void save(){

	}

}
