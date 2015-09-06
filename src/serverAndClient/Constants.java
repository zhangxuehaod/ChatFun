package serverAndClient;



import java.awt.Color;
import java.util.Random;

/**
 * Created by apple on 15-5-13.
 */
public class Constants {
/*
    public final static int MyColors[] = new int[]{
            Color.rgb(0x40, 0xE0, 0xD0),//Turquoise
            Color.rgb(0x94, 0x00, 0xD3),//DarkViolet
            Color.rgb(0xFF, 0x14, 0x93),//DeepPink
            Color.rgb(0xFF, 0x00, 0xFF), //Magenta
            Color.rgb(0x80, 0x00, 0x00), //Maroon
            Color.rgb(0xFF, 0xFF, 0x00), //Yellow
            Color.rgb(0xFF, 0xA5, 0x00), //Orange
            Color.rgb(0x88, 0x45, 0x13), //SaddleBrown
            Color.rgb(0x80, 0x80, 0x00),//olive
            Color.BLUE,
            Color.BLACK,
            Color.GREEN,
            Color.RED,
    };
 */
    public static Color transform(int color){
    	switch(color){
    	case 0:
    		return new Color(0x40, 0xE0, 0xD0);
    	case 1:
    		return new Color(0x94, 0x00, 0xD3);
    	case 2:
    		return new Color(0xFF, 0x14, 0x93);
    	case 3:
    		return new Color(0xFF, 0x00, 0xFF);
    	case 4:
    		return new Color(0x80, 0x00, 0x00);
    	case 5:
    		return new Color(0xFF, 0xFF, 0x00);
    	case 6:
    		return new Color(0xFF, 0xA5, 0x00);
    	case 7:
    		return new Color(0x88, 0x45, 0x13);
    	case 8:
    		return new Color(0x80, 0x80, 0x00);
    	case 9:
    		return Color.BLUE;
    	case 10:
    		return Color.BLACK;
    	case 11:
    		return Color.GREEN;
    	case 12:
    		return Color.RED;
    	}
    	return null;
    }
    public final static int MyColors[] = new int[]{
            0,//Turquoise
            1,//DarkViolet
            2,//DeepPink
            3, //Magenta
            4, //Maroon
            5, //Yellow
            6, //Orange
            7, //SaddleBrown
            8,//olive
            9,//BLUE
            10,//BLACK
            11,//GREEN
            12,//RED
    };
    public static class GameColor {
        public static final int
                Turquoise = MyColors[0], DarkViolet = MyColors[1], DeepPink = MyColors[2],
                Magenta = MyColors[3], Maroon = MyColors[4], Yellow = MyColors[5],
                Orange = MyColors[6], SaddleBrown = MyColors[7], olive = MyColors[8],
                 BLUE =MyColors[9], BLACK = MyColors[10], GREEN = MyColors[11], RED = MyColors[12];
    }
    public final static int IconColors[] = new int[]{
            GameColor.Turquoise,
            GameColor.DarkViolet,
            GameColor.DeepPink,
            GameColor.Magenta,
            GameColor.Maroon,
            GameColor.Yellow,
            GameColor.Orange,
            GameColor.SaddleBrown,
            GameColor.olive
    };
    public static final Random IntRandom = new Random();
    public static final int Vertical_GridNum = 20;
    public static final int Horizontal_GridNum = 20;
    public static final int BorderColor = GameColor.BLUE;
    public static final int BackgroundColor = GameColor.BLACK;
    public static final int UserDotColor = GameColor.GREEN;
    public static final int LinkLineColor = GameColor.RED;
    public static final int CrossColor = GameColor.BLUE;

    public static final int NoPlayer = -1, SingleSelect=0,MultiSelect = 1, DoneSelect = 2, Started = 3, OnRunning = 4, Overed = 5;
    public static final int None = 0, User = 1, Other = 2, System = 3, Win = 4, Lose = 5, Offline = 6;
    public static final int Left = 0, Top = 1, Right = 2, Bottom = 3, Start = 4, Stop = 5, PlayAgain = 6, Restart = 7,Pause=8;
    public static final int Select = 0, SetMutilSelect = 2, Confirm = 3, SwitchMark = 4;
    public static final int Directions[] = new int[]{Left, Top, Right, Bottom};

    public static int OpositDir(int dir) {
        switch (dir) {
            case Left:
                return Right;
            case Top:
                return Bottom;
            case Right:
                return Left;
            case Bottom:
                return Top;
        }
        return -1;
    }
    public static int[] VeriticalDirs(int dir){
        int[] dirs=new int[2];
        int id=createRandom(2);
        if(dir== Constants.Left || dir== Constants.Right){
            dirs[id%2]= Constants.Top;
            dirs[(id+1)%2]= Constants.Bottom;
        }
        else{
            dirs[id%2]= Constants.Left;
            dirs[(id+1)%2]= Constants.Right;
        }
        return dirs;
    }

    public static int isCorrect(int dir1, int dir2) {
        if (dir1 == Left || dir1 == Right)
            return (dir2 == Top || dir2 == Bottom) ? 0 : 2;
        else
            return (dir2 == Left || dir2 == Right) ? 0 : 1;
    }

    public static int IndexOf(int dir) {
        switch (dir) {
            case Left:
                return 0;
            case Top:
                return 1;
            case Right:
                return 2;
            case Bottom:
                return 3;
        }
        return -1;
    }

    public static boolean isVertical(int dir) {
        return dir == Top || dir == Bottom;
    }



    public static int createRandom(int mode) {
        return Math.abs(Constants.IntRandom.nextInt()) % mode;
    }

    public static final long MovePeriod = 300;
    public static final long FrameTime = 20;
    public static final long DrawThreadFrameNum = MovePeriod / FrameTime;


    public static final String PlayAgainStr = "重新开始";
    public static final String WinStr = "赢";
    public static final String LoseStr = "输";
    public static final String QuitStr = "退出游戏";
    public static final String OfflineStr = "离线";
    public static final String StartStr = "游戏开始";
    public static final String WrongDirStr = "转向错误";
    public static final String PauseStr = "游戏暂停";
    public static final String ReadyStr = "准备就绪";
    public static final String OverStr = "游戏结束";
    public static final String NotStatStr = "游戏未开始";
    public static final String CountStr = "秒";
    public static final long MsgLiveTime = 1000;

}
