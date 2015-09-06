package swingDialog;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Input extends JPanel
{
	private static final long serialVersionUID = 106610907371383952L;
	private JTextField textField;
	private JLabel title;

	public Input(String label,String defaultValue,Color textColor,int textSize){
		super.setForeground(textColor);
		this.setOpaque(false);
		Font font=new Font("default",Font.BOLD,textSize);
		textField=new JTextField(6);
		textField.setFont(font);
		if(defaultValue!=null)
			textField.setText(defaultValue);
		title=new JLabel(label);
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.add(title);
		this.add(textField);
	}
	public String getInput(){
		return this.textField.getText().toString();
	}
	public String[] getInputTypeAndValue(){
		return new String[]{this.title.getText().toString(),this.textField.getText().toString()};
	}
	public void setInput(String str){
		this.textField.setText(str);
	}
}
