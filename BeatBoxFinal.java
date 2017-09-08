package FinishedBeatBox;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class BeatBoxFinal {
	JFrame theFrame;
	JPanel mainPanel;
	JList incomingList;
	JTextField userMessage;
	ArrayList<JCheckBox> checkboxList;
	int nextNum;
	Vector<String> listVector=new Vector<String>();//����������
	String userName;
	ObjectOutputStream out;
	ObjectInputStream in;
	HashMap<String,boolean[]> otherSeqsMap=new HashMap<String,boolean[]>();//������¼����
	
	Sequencer sequencer;
	Sequence sequence;
	Sequence mySequence=null;
	Track track;
	
	String[] instrumentNames={"��˹","��-(���ӹ�)","��-(���ӹ�)","����","���ӹ�","������","High Tom",
			"Hi Bongo","ɳ��","����","����Conga","ţ��","����","Low-mid Tom","����Agogo","Open Hi Conga"};//һЩ��������
	
	int[] instruments={35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};//������Ӧ������
	
	public static void main(String[] args){
		
		new BeatBoxFinal().startUp(args[0]);   //���args[0]����ʾ�����־���ID
	}
	
	public void startUp(String name){
		userName=name;
		//��ʼ���ӷ�����,�������磬������� ����reader���̣߳�
		try{
			Socket sock=new Socket("127.0.0.1",4243);
			out=new ObjectOutputStream(sock.getOutputStream());
			in=new ObjectInputStream(sock.getInputStream());
			Thread remote=new Thread(new RemoteReader());
			remote.start();
			
		}catch(Exception ex){System.out.println("������ô��ֻ���Լ�����");}
	
	setUpMidi();
	buildGUI();
	}
	public void buildGUI(){
		theFrame=new JFrame("Cyber BeatBox");
		BorderLayout layout=new BorderLayout();
		JPanel background=new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));//�������ߣ�
		
		checkboxList=new ArrayList<JCheckBox>();
		
		Box buttonBox=new Box(BoxLayout.Y_AXIS);//boxlayout���������а�ť
		JButton start=new JButton("����");
		start.addActionListener(new MyStartListener());
		buttonBox.add(start);
		
		JButton stop=new JButton("�𣬱����");
		stop.addActionListener(new MyStopListener());
		buttonBox.add(stop);
		
		JButton upTempo=new JButton("���㾢");
		upTempo.addActionListener(new MyUpTempoListener());
		buttonBox.add(upTempo);
		
		JButton downTempo=new JButton("��һ������");
		downTempo.addActionListener(new MyDownTempoListener());
		buttonBox.add(downTempo);
		
		JButton sendIt=new JButton("����");
		sendIt.addActionListener(new MySendItListener());
		buttonBox.add(sendIt);
		
		userMessage=new JTextField();
		
		buttonBox.add(userMessage);
		
		incomingList=new JList();
		incomingList.addListSelectionListener(new MyListSelectionListener());
		incomingList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		JScrollPane theList=new JScrollPane(incomingList);
		buttonBox.add(theList);
		incomingList.setListData(listVector);
		//��ʼʱû����Ϣ������������ʾ���ŵ������
		
		Box nameBox=new Box(BoxLayout.Y_AXIS);
		for(int i=0;i<16;i++){
			nameBox.add(new Label(instrumentNames[i]));
		}
		
		background.add(BorderLayout.EAST,buttonBox);
		background.add(BorderLayout.WEST,nameBox);
		
		theFrame.getContentPane().add(background);
		GridLayout grid=new GridLayout(16,16);
		grid.setVgap(1);
		grid.setHgap(2);//����ˮƽ����ֱ���
		mainPanel=new JPanel(grid);
		background.add(BorderLayout.CENTER,mainPanel);
		
		for(int i=0;i<256;i++){
			JCheckBox c=new JCheckBox();
			c.setSelected(false);
			checkboxList.add(c);
			mainPanel.add(c);
		}
		
		theFrame.setBounds(50,50,300,300);
		theFrame.pack();
		theFrame.setVisible(true);
	}//ͼ�ν��洴�����
	
	public void setUpMidi(){
		try{
			sequencer=MidiSystem.getSequencer();
			sequencer.open();
			sequence=new Sequence(Sequence.PPQ,4);
			track=sequence.createTrack();
			sequencer.setTempoInBPM(120);
		}catch(Exception e){e.printStackTrace(); System.out.println("Midi������");}
		
	}//���ֲ�����ɡ�
	
	public void buildTrackAndStart(){
		ArrayList<Integer> trackList=null;//���ڼ�¼ÿ�������Ľ���
		sequence.deleteTrack(track);
		track=sequence.createTrack();
		
		for(int i=0;i<16;i++){
			trackList=new ArrayList<Integer>();
			for(int j=0;j<16;j++){
				JCheckBox jc=(JCheckBox) checkboxList.get(j+(16*i));
				if(jc.isSelected()){
					int key=instruments[i];
					trackList.add(new Integer(key));
				}else{trackList.add(null);}//û��ѡ�о�����0��
			}
			makeTracks(trackList);
		}
		track.add(makeEvent(192,9,1,0,15));//ȷ����16�����ģ������ſ���ѭ����ȥ��
		try{
			sequencer.setSequence(sequence);
			sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);  //xunhuan
			sequencer.start();
			sequencer.setTempoInBPM(120);
		}catch(Exception e){e.printStackTrace();}
	}
		
		 public class MyStartListener implements ActionListener{
			public void actionPerformed(ActionEvent a){
				buildTrackAndStart();}
		}
		 public class MyStopListener implements ActionListener{
				public void actionPerformed(ActionEvent a){
					sequencer.stop();}
			}
		 public class MyUpTempoListener implements ActionListener{
				public void actionPerformed(ActionEvent a){
					float tempoFactor=sequencer.getTempoFactor();
					sequencer.setTempoFactor((float)(tempoFactor*1.03));}
			}
		 public class MyDownTempoListener implements ActionListener{
				public void actionPerformed(ActionEvent a){
					float tempoFactor=sequencer.getTempoFactor();
					sequencer.setTempoFactor((float)(tempoFactor*0.97));}
			}
		 
		 public class MySendItListener implements ActionListener{
			 public void actionPerformed(ActionEvent a){
				 //���һ��Arraylist����¼֮ǰ�Ľ��ࣻ
				 boolean[] checkboxState=new boolean[256];
				 for(int i=0;i<256;i++){
					 JCheckBox check=(JCheckBox) checkboxList.get(i);
					 if(check.isSelected()){
						 checkboxState[i]=true;
					 }
				 }
					 String messageToSend=null;
					 try{
						 out.writeObject(userName+nextNum++ +":"+userMessage.getText());
						 out.writeObject(checkboxState);
					 }catch(Exception ex){
						 System.out.println("��..û�����͵���������");
					 }
					 userMessage.setText("");
				 		}
		 }
			 public class MyListSelectionListener implements ListSelectionListener{
				 public void valueChanged(ListSelectionEvent le){
					 if(!le.getValueIsAdjusting()){
						 String selected=(String) incomingList.getSelectedValue();
						 if(selected!=null){
							 //��ת��ͼ������ʼ���ŵ�ѡ����Ϣ
							 boolean[] selectedState=(boolean[]) otherSeqsMap.get(selected);
							 changeSequence(selectedState);
							 sequencer.stop();
							 buildTrackAndStart();
						 }
						 
					 }
				 }
			 }
			 
			 public class RemoteReader implements Runnable{
				 boolean[] checkboxState=null;
				 String nameToShow=null;
				 Object obj=null;
				 public void run(){//�߳�ִ�е�����run
					 try{
						 while((obj=in.readObject())!=null){
							 System.out.println("�ӷ��������յ���Ϣ");
							 System.out.println(obj.getClass());
							 String nameToShow=(String) obj;
							 checkboxState=(boolean[]) in.readObject();
							 otherSeqsMap.put(nameToShow, checkboxState);
							 listVector.add(nameToShow);
							 incomingList.setListData(listVector);
						 }
					 }catch(Exception ex){ex.printStackTrace();}
					 }//finish run
				 }//finish innerclass:remote
			 
			 public class MyPlayMineListener implements ActionListener{
				 public void actionPerformed(ActionEvent a){
					 if(mySequence!=null){
						 sequence=mySequence;
						 
					 }
				 }
			 }
			 
			 public void changeSequence(boolean[] checkboxState){
				 for(int i=0;i<256;i++){
					 JCheckBox check=(JCheckBox) checkboxList.get(i);
					 if(checkboxState[i]){
						 check.setSelected(true);
					 }else{check.setSelected(false);}
				 }
			 }
			 public void makeTracks(ArrayList list){
				 Iterator it=list.iterator();
				 for(int i=0;i<16;i++){
					 Integer num=(Integer)it.next();
					 if(num!=null){
						 int numKey=num.intValue();
						 track.add(makeEvent(144,9,numKey,100,i));
						 track.add(makeEvent(128,9,numKey,100,i+1));
					 }
				 }
			 }
			 public MidiEvent makeEvent(int comd,int chan,int one,int two,int tick){
				 MidiEvent event=null;
				 try{
					 ShortMessage a=new ShortMessage();
					 a.setMessage(comd,chan,one,two);
					 event=new MidiEvent(a,tick);
				 }catch(Exception a){}
				 return event;
			 }
}
		 
