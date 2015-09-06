package swingDialog;

import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class IconCreator {
	public static Icon create(String filePath,int width,int height){
		if(filePath==null)
			return null;
        ImageIcon icon = new ImageIcon(filePath);  
        if(width<=0 || height<=0)
        	return icon;
		Image temp = icon.getImage().getScaledInstance(width,height, Image.SCALE_SMOOTH);  
        return new ImageIcon(temp);  
	}
	public static Icon create(String filePath,float xScale,float yScale){
		if(filePath==null)
			return null;
        ImageIcon icon = new ImageIcon(filePath);  
        if(xScale<=0 || yScale<=0)
        	return icon;
		Image temp = icon.getImage().getScaledInstance((int)(icon.getIconWidth()*xScale),
				(int)(icon.getIconHeight()*yScale), Image.SCALE_SMOOTH);  
        return new ImageIcon(temp);  
	}
}