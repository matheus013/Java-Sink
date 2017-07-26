package memory;

import app.SenseMsg;
import memory.data.reduction.KeepingAll;
import memory.data.reduction.RandomRemoval;

public class Memory {
	private static KeepingAll keepingAll;
	private static RandomRemoval randomRemoval;
	private static int capacity;

	static public KeepingAll fullGet() {
		if (keepingAll == null) {
			keepingAll = new KeepingAll();
		}
		return keepingAll;
	}

	static public RandomRemoval getMemory() {
		if (randomRemoval == null) {
			randomRemoval = new RandomRemoval(capacity);
		}
		return randomRemoval;
	}

	static public void setCapacity(int capacity) {
		Memory.capacity = capacity;
	}

	static public void add(SenseMsg msg) {
		fullGet().add(msg);
		getMemory().add(msg);
	}

}
