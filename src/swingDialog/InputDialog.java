package swingDialog;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.util.ArrayList;

import javax.swing.JFrame;

public class InputDialog extends Dialog{
	private static final long serialVersionUID = 6504652974616249525L;
	private ArrayList<Input> inputs=new ArrayList<Input>();
	public InputDialog(JFrame parent,String title,String[] inputType,String[] initInputValues,String btnTxt){
        super(parent,true);
        this.initComponents(inputType,initInputValues,btnTxt);
        super.setTitle(title);
        pack();
        super.setOnParentCenter(parent);
	}
	
    private void initComponents(String[] inputType,String[] inputValues,String btnTxt) {  
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
    	for(int i=0;i<inputType.length;i++){
    		String def=null;
    		if(inputValues!=null && inputValues.length>i && inputValues[i]!=null)
    			def=inputValues[i];
    		Input in=new Input(inputType[i],def,Color.red,16);
    		mainPanel.add(in,gridBagConstraints);
    		inputs.add(in);
    	}
    	super.closeButton.setText(btnTxt);
    }
    @Override
    public void open(){
    	super.open();
    }
	public void close(){
		super.closeDialog();
	}
	public String[] getInput(){
		String[] ins=new String[inputs.size()];
		int i=0;
		for(Input in:inputs)
			ins[i++]=in.getInput();
		return ins;
	}

	public String[][] getInputTypeValues(){
		String[][] ins=new String[inputs.size()][];
		int i=0;
		for(Input in:inputs)
			ins[i++]=in.getInputTypeAndValue();
		return ins;
	}
	public String getInput(int id){
		if(id>=0 && id<inputs.size())
			return inputs.get(id).getInput();
		return null;
	}
}
