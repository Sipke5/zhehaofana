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
	Vector<String> listVector=new Vector<String>();//向量型数据
	String userName;
	ObjectOutputStream out;
	ObjectInputStream in;
	HashMap<String,boolean[]> otherSeqsMap=new HashMap<String,boolean[]>();//用来记录节奏
	
	Sequencer sequencer;
	Sequence sequence;
	Sequence mySequence=null;
	Track track;
	
	String[] instrumentNames={"贝斯","自-(架子鼓)","呲-(架子鼓)","军鼓","架子鼓","鼓掌声","High Tom",
			"Hi Bongo","沙球","耳语","低音Conga","牛铃","音叉","Low-mid Tom","高音Agogo","Open Hi Conga"};//一些乐器声音
	
	int[] instruments={35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};//乐器对应的声音
	
	public static void main(String[] args){
		
		new BeatBoxFinal().startUp(args[0]);   //这个args[0]是显示的名字就是ID
	}
	
	public void startUp(String name){
		userName=name;
		//开始连接服务器,设置网络，输入输出 创建reader的线程；
		try{
			Socket sock=new Socket("127.0.0.1",4243);
			out=new ObjectOutputStream(sock.getOutputStream());
			in=new ObjectInputStream(sock.getInputStream());
			Thread remote=new Thread(new RemoteReader());
			remote.start();
			
		}catch(Exception ex){System.out.println("连不上么，只好自己玩了");}
	
	setUpMidi();
	buildGUI();
	}
	public void buildGUI(){
		theFrame=new JFrame("Cyber BeatBox");
		BorderLayout layout=new BorderLayout();
		JPanel background=new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));//设置留边；
		
		checkboxList=new ArrayList<JCheckBox>();
		
		Box buttonBox=new Box(BoxLayout.Y_AXIS);//boxlayout中竖着排列按钮
		JButton start=new JButton("走起！");
		start.addActionListener(new MyStartListener());
		buttonBox.add(start);
		
		JButton stop=new JButton("别，别放了");
		stop.addActionListener(new MyStopListener());
		buttonBox.add(stop);
		
		JButton upTempo=new JButton("来点劲");
		upTempo.addActionListener(new MyUpTempoListener());
		buttonBox.add(upTempo);
		
		JButton downTempo=new JButton("慢一点试试");
		downTempo.addActionListener(new MyDownTempoListener());
		buttonBox.add(downTempo);
		
		JButton sendIt=new JButton("发送");
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
		//开始时没有信息，（以上是显示来信的组件）
		
		Box nameBox=new Box(BoxLayout.Y_AXIS);
		for(int i=0;i<16;i++){
			nameBox.add(new Label(instrumentNames[i]));
		}
		
		background.add(BorderLayout.EAST,buttonBox);
		background.add(BorderLayout.WEST,nameBox);
		
		theFrame.getContentPane().add(background);
		GridLayout grid=new GridLayout(16,16);
		grid.setVgap(1);
		grid.setHgap(2);//设置水平和竖直间距
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
	}//图形界面创建完成
	
	public void setUpMidi(){
		try{
			sequencer=MidiSystem.getSequencer();
			sequencer.open();
			sequence=new Sequence(Sequence.PPQ,4);
			track=sequence.createTrack();
			sequencer.setTempoInBPM(120);
		}catch(Exception e){e.printStackTrace(); System.out.println("Midi处出错");}
		
	}//音乐播放完成。
	
	public void buildTrackAndStart(){
		ArrayList<Integer> trackList=null;//用于记录每件乐器的节奏
		sequence.deleteTrack(track);
		track=sequence.createTrack();
		
		for(int i=0;i<16;i++){
			trackList=new ArrayList<Integer>();
			for(int j=0;j<16;j++){
				JCheckBox jc=(JCheckBox) checkboxList.get(j+(16*i));
				if(jc.isSelected()){
					int key=instruments[i];
					trackList.add(new Integer(key));
				}else{trackList.add(null);}//没被选中就算作0；
			}
			makeTracks(trackList);
		}
		track.add(makeEvent(192,9,1,0,15));//确保有16个节拍，这样才可以循环下去；
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
				 //设计一个Arraylist来记录之前的节奏；
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
						 System.out.println("这..没法发送到服务器上");
					 }
					 userMessage.setText("");
				 		}
		 }
			 public class MyListSelectionListener implements ListSelectionListener{
				 public void valueChanged(ListSelectionEvent le){
					 if(!le.getValueIsAdjusting()){
						 String selected=(String) incomingList.getSelectedValue();
						 if(selected!=null){
							 //跳转到图，并开始播放点选的信息
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
				 public void run(){//线程执行的任务，run
					 try{
						 while((obj=in.readObject())!=null){
							 System.out.println("从服务器上收到信息");
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
		 
