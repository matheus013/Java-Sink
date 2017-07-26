package memory.data.reduction;

import java.util.Random;

import app.SenseMsg;
import memory.Container;

public class RandomRemoval extends Container {
	private int capacity;

	public RandomRemoval(int capacity) {
		super();
		this.capacity = capacity;
	}

	@Override
	public void add(SenseMsg msg) {
		// TODO Auto-generated method stub
		if (length() < capacity) {
			content.add(msg);
		} else {
			Random rand = new Random();
			content.set(rand.nextInt(capacity), msg);
		}

	}

}
