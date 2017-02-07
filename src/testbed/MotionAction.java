package testbed;

import server.game.Vector2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;

public class MotionAction extends AbstractAction implements ActionListener {

	private Vector2 dir;
	private TestEnvironment env;
	
	public MotionAction(String name, TestEnvironment env, Vector2 dir){
		
		super(name);
		this.dir = dir;
		this.env = env;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		env.movePlayer(dir);

	}

}
