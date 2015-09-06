package swingDialog;



import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;


public class Dialog extends JDialog{
	private static final long serialVersionUID = -6078856801377323258L;
	protected Point origin=new Point(0,0);
	protected Point pos=new Point();
	public Dialog(){
		super();
        this.initComponents();
	}
    public Dialog(JFrame parent,boolean modal){
        super(parent,modal);
        this.initComponents();
    }
    public void setOnScreenCenter(){
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();    
        Dimension frameSize = getSize();
        this.setLocation(new Point((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2)); //设置窗口位于屏幕�?
    }
    public void setOnParentCenter(Component parent){
        Rectangle parentBounds = parent.getBounds();
        Point loc=parent.getLocationOnScreen();
        Dimension size = getSize();
        int x = Math.max(loc.x, loc.x + (parentBounds.width - size.width) / 2);  // Center in the parent
        int y = Math.max(loc.y, loc.y + (parentBounds.height - size.height) / 2);
        setLocation(new Point(x, y));
    }
    private void initComponents() {                          
        mainPanel = new javax.swing.JPanel();
        closeButton = new JButton();
        
        getContentPane().setLayout(new java.awt.GridBagLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        mainPanel.setLayout(new java.awt.GridBagLayout());
        mainPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(11, 11, 12, 12)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        closeButton.setMnemonic('C');
        closeButton.setFont(font);
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeDialog();
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        getContentPane().add(closeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(mainPanel, gridBagConstraints);
    }                        

    protected void closeDialog() {                                            
        setVisible(false);
        dispose();
    }                                        
    public void open(){
    	this.setVisible(true);
    }
    protected JButton closeButton;
    protected javax.swing.JPanel mainPanel;
    protected java.awt.GridBagConstraints gridBagConstraints;
    protected Font font=new Font("default",Font.BOLD,14);

}


