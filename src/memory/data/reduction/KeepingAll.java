package memory.data.reduction;

import app.SenseMsg;
import memory.Container;

public class KeepingAll extends Container{

	@Override
	public void add(SenseMsg msg) {
		// TODO Auto-generated method stub
		content.add(msg);
		
	}

}
