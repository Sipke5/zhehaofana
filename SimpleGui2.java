package oop.polymorphism.myServlet;
import java.awt.*;
import javax.swing.*;


public class SimpleGui2{
	public static void main(String[] args ){
		JFrame frame=new JFrame();
		MyDrawPanel m=new MyDrawPanel();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(m);
		frame.setSize(300,300);
		frame.setVisible(true);}
		
		  static class MyDrawPanel extends JPanel{
			  public void paintComponent(Graphics g){
				  g.setColor(Color.orange);
				  
				  g.fillRect(100, 50, 10, 100);}
		  }
	}

				  
				


