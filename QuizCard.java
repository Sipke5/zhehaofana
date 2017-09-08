package seriableTest;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class QuizCard implements Serializable {
	String Question;
	String Answer;
	
	public QuizCard(String q,String a){
		Question=q;
		Answer=a;
	}
	
	public String getAnswer(){
		return Answer;
	}
	public String getQuestion(){
		return Question;
	}
	
	

}
