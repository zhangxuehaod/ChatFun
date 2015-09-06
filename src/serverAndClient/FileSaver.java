package serverAndClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileSaver {
	private String InforFilePath;
	public FileSaver(String filePath){
		setInforFilePath(filePath);
	}
	public synchronized void setInforFilePath(String filePath){
		if(filePath!=null && filePath.length()>0)
			InforFilePath=filePath;
	}
	public synchronized boolean saveInfors(String[] infs){
		try {
			File userId=new File(InforFilePath);
			OutputStreamWriter ow=new OutputStreamWriter(new FileOutputStream(userId),"UTF-8");
			BufferedWriter bw=new BufferedWriter(ow);
			for(String s:infs){
				bw.write(s+"\n");
			}
			bw.close();
			ow.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}
	public synchronized String[] loadInfors(){
		String[] infs=null;
		try {
			File file=new File(InforFilePath);
			if(file.isFile()){
				InputStreamReader ir=new InputStreamReader(new FileInputStream(file),"UTF-8");
				BufferedReader br=new BufferedReader(ir);
				ArrayList<String> inf=new ArrayList<String>();
				String line;
				while((line=br.readLine())!=null){
					inf.add(line);
				}
				infs=new String[inf.size()];
				inf.toArray(infs);
				br.close();
				ir.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		return  infs;
	}
}
