import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.border.Border;

import org.omg.PortableInterceptor.TRANSPORT_RETRY;

import javax.swing.*;

public class BeatBox {
	
	//Instance Variables
	JPanel mainPanel;
	ArrayList<JCheckBox> checkBoxList; //Store checkboxes in an ArrayList
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	JFrame theFrame;
	
	String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Accoustic Snare", "Crash Cymbal",
								"Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap",
								"Low-mid Tom", "High Agogo", "Open Hi Conga"};
	int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
	
	public static void main(String[] args) {
		new BeatBox().buildGUI();
	}//main end
	
	public void buildGUI() {
		theFrame = new JFrame("Cyber BeatBox");
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		checkBoxList = new ArrayList<JCheckBox>();
		Box buttonBox = new Box(BoxLayout.Y_AXIS);
		
		//Buttons
		
		JButton start = new JButton("Start");
		start.addActionListener(new MyStartListener());
		buttonBox.add(start);
		
		JButton stop = new JButton("Stop");
		stop.addActionListener(new MyStopListener());
		buttonBox.add(stop);
		
		JButton upTempo = new JButton("Tempo Up");
		upTempo.addActionListener(new MyUpTempoListener());
		buttonBox.add(upTempo);
		
		JButton downTempo = new JButton("Tempo Up");
		downTempo.addActionListener(new MyDownTempoListener());
		buttonBox.add(downTempo);
		
		JButton save = new JButton("Save");
		save.addActionListener(new MySendListener());
		buttonBox.add(save);
		
		JButton load = new JButton("Load");
		load.addActionListener(new MyReadInListener());
		buttonBox.add(load);
		
		
		Box nameBox = new Box(BoxLayout.Y_AXIS);
		for (int i = 0; i<16; i++) {
			nameBox.add(new Label(instrumentNames[i]));
		}//loop end
		
		background.add(BorderLayout.EAST, buttonBox);
		background.add(BorderLayout.WEST, nameBox);
		
		theFrame.getContentPane().add(background);
		
		GridLayout grid = new GridLayout(16, 16);
		grid.setVgap(1);
		grid.setHgap(2);
		mainPanel = new JPanel(grid);
		background.add(BorderLayout.CENTER, mainPanel);
		
		for (int i = 0; i<256; i++) {
			JCheckBox c = new JCheckBox();
			c.setSelected(false);
			checkBoxList.add(c);
			mainPanel.add(c);
		}//loop end
		
		setUpMidi();
		
		theFrame.setBounds(50, 50, 300, 300);
		theFrame.pack();
		theFrame.setVisible(true);
		
	}//buildGUI end
	
	public void setUpMidi() {
		try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequence = new Sequence(Sequence.PPQ, 4);
			track = sequence.createTrack();
			sequencer.setTempoInBPM(120);
		} catch (Exception e) {e.printStackTrace();}
		
	}//setUpMidi end
	
	public void buildTrackAndStart() {
		int[] trackList = null;
		
		sequence.deleteTrack(track);
		track = sequence.createTrack();
		
		for (int i=0; i<16; i++) {
			trackList = new int[16];
			
			int key = instruments[1];
			
			for (int j=0; j<16; j++) {
				JCheckBox jc = checkBoxList.get(j + 16*i);
				if (jc.isSelected()) {
					trackList[j] = key;
				} else {
					trackList[j] = 0;
				}//else end
			}//inner loop end
			makeTracks(trackList);
			track.add(makeEvent(176, 1, 127, 0 , 16));
		}// close outer loop
		
		track.add(makeEvent(192, 9, 1, 0, 15));
		try {
			sequencer.setSequence(sequence);
			sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
			sequencer.setTempoInBPM(120);
			
		} catch (Exception e) {e.printStackTrace();}
	}//buildTrackAndStart end

	/**
	 * 
	 * @param comd
	 * @param chan
	 * @param one
	 * @param two
	 * @param tick
	 * @return
	 */
	private MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
		MidiEvent event = null;
		try {
			ShortMessage a = new ShortMessage();
			a.setMessage(comd,chan,one,two);
			event = new MidiEvent(a, tick);
			
		} catch (Exception e) {e.printStackTrace();}
		return event;
	}//makeEvent end

	private void makeTracks(int[] trackList) {
		
		for (int i=0; i<16; i++) {
			int key = trackList[i];
			
			if(key != 0) {
				track.add(makeEvent(144,9,key,100,i));
				track.add(makeEvent(128,9,key,100,i+1));
			}
		}//end loop
		
	}//end makeTracks()

	public class MyStartListener implements  ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			buildTrackAndStart();
			
		}
		
	}//inner class end
	
	public class MyStopListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			sequencer.stop();
			
		}
		
	}//inner class end
	
	public class MyUpTempoListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempoFactor * 1.03));
			
		}
		
	}//inner class end
	
	public class MyDownTempoListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempoFactor * .97));
			
		}
		
	}//inner class end
	
	/*
	 * Serializing a pattern/Saving to a file
	 */

	public class MySendListener implements ActionListener {
		
		
		public void actionPerformed(ActionEvent a) {
			boolean[] checkboxState = new boolean[256];
			
			for (int i = 0; i < 256; i++) {
				
				JCheckBox check = (JCheckBox) checkBoxList.get(i);
				if(check.isSelected()) {
					checkboxState[i] = true;
				}
				
			}// loop end
			try {
				FileOutputStream fileStream = new FileOutputStream(new File("Checkbox.ser"));
				ObjectOutputStream os = new ObjectOutputStream(fileStream);
				os.writeObject(checkboxState);
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}// actionPerformed() end 
	}// inner class end
	
	
	/*
	 * Restoring a pattern/Load file
	 * 
	 */
	public class MyReadInListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			boolean[] checkboxState = null;
			
			try {
				FileInputStream fileIn = new FileInputStream(new File("Checkbox.ser"));
				ObjectInputStream is = new ObjectInputStream(fileIn);
				checkboxState = (boolean[]) is.readObject();
				
			} catch (Exception e) {e.printStackTrace();}
			
			for(int i = 0; i<256; i++) {
				JCheckBox check = (JCheckBox) checkBoxList.get(i);
				if (checkboxState[i]) {
					check.setSelected(true);
				}else {
					check.setSelected(false);
				}
			}
			sequencer.stop();
			buildTrackAndStart();
		}// actionPerformed() end
	}// inner class end

}//class end
