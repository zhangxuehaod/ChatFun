package swingDialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.UIManager;

public class OptionDialog {
	Component comp;
	String title;
	String content;
	String[] options;
	boolean isShowed=false;
	private JDialog dialog;
	private JOptionPane pane ;
	public OptionDialog(Component comp,String title,String content,String... options){
		this.comp=comp;
		init(title,content,options);
	}
	private void init(String title,String content,String... options){
		this.title=title;
		this.content=content;
		this.options=options;

	}
	@SuppressWarnings("deprecation")
	public void open(){
		if(!isShowed){
			isShowed=true;
			this.initOptionDialog(comp, content, title, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
					null, options, options[0]);
			dialog.show();
			close();
		}
	}
	public int getOption(){
		Object  selectedValue = pane.getValue();
		if(selectedValue == null)
			return JOptionPane.CLOSED_OPTION;
		if(options == null) {
			if(selectedValue instanceof Integer)
				return ((Integer)selectedValue).intValue();
			return JOptionPane.CLOSED_OPTION;
		}
		for(int counter = 0, maxCounter = options.length;
				counter < maxCounter; counter++) {
			if(options[counter].equals(selectedValue))
				return counter;
		}
		return JOptionPane.CLOSED_OPTION;
	}
	@SuppressWarnings("deprecation")
	public synchronized void close(){ 
		if(isShowed){
			isShowed=false;	
			dialog.hide();
			dialog.dispose();
		}
	}

	private  int styleFromMessageType(int messageType) {
		switch (messageType) {
		case JOptionPane.ERROR_MESSAGE:
			return JRootPane.ERROR_DIALOG;
		case JOptionPane.QUESTION_MESSAGE:
			return JRootPane.QUESTION_DIALOG;
		case JOptionPane.WARNING_MESSAGE:
			return JRootPane.WARNING_DIALOG;
		case JOptionPane.INFORMATION_MESSAGE:
			return JRootPane.INFORMATION_DIALOG;
		case JOptionPane.PLAIN_MESSAGE:
		default:
			return JRootPane.PLAIN_DIALOG;
		}
	}


	private  void initDialog(JOptionPane pane,final JDialog dialog, int style, Component parentComponent) {
		dialog.setComponentOrientation(pane.getComponentOrientation());
		Container contentPane = dialog.getContentPane();

		contentPane.setLayout(new BorderLayout());
		contentPane.add(pane, BorderLayout.CENTER);
		dialog.setResizable(false);
		if (JDialog.isDefaultLookAndFeelDecorated()) {
			boolean supportsWindowDecorations =
					UIManager.getLookAndFeel().getSupportsWindowDecorations();
			if (supportsWindowDecorations) {
				dialog.setUndecorated(true);
				pane.getRootPane().setWindowDecorationStyle(style);
			}
		}
		dialog.pack();
		dialog.setLocationRelativeTo(parentComponent);

		final PropertyChangeListener listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				// Let the defaultCloseOperation handle the closing
				// if the user closed the window without selecting a button
				// (newValue = null in that case).  Otherwise, close the dialog.
				if (dialog.isVisible() && event.getSource() == pane &&
						(event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) &&
						event.getNewValue() != null &&
						event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
					dialog.setVisible(false);
				}
			}
		};

		WindowAdapter adapter = new WindowAdapter() {
			private boolean gotFocus = false;
			public void windowClosing(WindowEvent we) {
				pane.setValue(null);
			}

			public void windowClosed(WindowEvent e) {
				pane.removePropertyChangeListener(listener);
				dialog.getContentPane().removeAll();
			}

			public void windowGainedFocus(WindowEvent we) {
				// Once window gets focus, set initial focus
				if (!gotFocus) {
					pane.selectInitialValue();
					gotFocus = true;
				}
			}
		};
		dialog.addWindowListener(adapter);
		dialog.addWindowFocusListener(adapter);
		dialog.addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent ce) {
				// reset value to ensure closing works properly
				pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			}
		});
		pane.addPropertyChangeListener(listener);
	}
	private  JDialog createDialog(JOptionPane pane,Component parentComponent, String title,
			int style)
					throws HeadlessException {
		final JDialog dialog;
		Window window = (Window)parentComponent;
		if (window instanceof Frame) {
			dialog = new JDialog((Frame)window, title, true);
		} else {
			dialog = new JDialog((Dialog)window, title, true);
		}
		initDialog(pane,dialog, style, parentComponent);
		return dialog;
	}
	private void initOptionDialog(Component parentComponent,
			Object message, String title, int optionType, int messageType,
			Icon icon, Object[] options, Object initialValue)
					throws HeadlessException {
		//icon=new NumberIcon();
		pane = new JOptionPane(message, messageType,optionType, icon,options, initialValue);
		pane.setInitialValue(initialValue);
		pane.setComponentOrientation(parentComponent.getComponentOrientation());
		int style = styleFromMessageType(messageType);
		dialog = createDialog(pane,parentComponent, title, style);
		pane.selectInitialValue();
	}
}

