// ｢海ゲーム｣クライアントプログラムRobot.java
// 競技会用UmiServer3.java 対応版
// このプログラムは,海ゲームのクライアントプログラムです
// 決められた手順で海ゲームをプレイします
// 使い方java Robot 接続先サーバアドレスゲーム参加者名
// 起動後,指定したサーバと接続し,自動的にゲームを行います
// 起動後,指定回数の繰り返しの後,logoutします
// このプログラムはlogoutコマンドがありません
// プログラムを途中で停止するには,以下の手順を踏んでください
// （１）コントロールC を入力してRobotプログラムを停止します
// （２）T1.javaプログラムなど,別のクライアントを使ってRobotと同じ名前でloginします
// （３）logoutします
// 別クライアントからのlogout作業を省略すると,サーバ上に情報が残ってしまいます

// ライブラリの利用
import java.net.*;// ネットワーク関連
import java.io.*;
import java.util.*;

class Tank {
	int x, y, ene;

	Tank(int x, int y, int ene) {
		this.x = x;
		this.y = y;
		this.ene = ene;
	}
}

// Robotクラス
public class Robot2 {
	// ロボットの動作タイミングを規定する変数sleeptime
	int sleeptime = 5 ;
	// コンストラクタ
	public Robot2 (String[] args)
	{
		login(args[0],args[1]) ;
		// Random rand = new Random();
		while(true){
		try{
			Thread.sleep(500);
				out.println("stat");
				out.flush();
				String line = in.readLine();

				int shipX = 0;
				int shipY = 0;
				List<Tank> tanks = new ArrayList<>();

				while (!"ship_info".equalsIgnoreCase(line))
					line = in.readLine();
				line = in.readLine();
				while (!".".equals(line)) {
					StringTokenizer st = new StringTokenizer(line);
					String obj_name = st.nextToken().trim();
					if (obj_name.equals(name)) {
						shipX = Integer.parseInt(st.nextToken());
						shipY = Integer.parseInt(st.nextToken());
						System.out.println("ship infomation: ");
						System.out.println("" + "name: " + obj_name + " x: " + shipX + " y: " +  shipY);
					}
					line = in.readLine();
				}

				while (!"energy_info".equalsIgnoreCase(line))
					line = in.readLine();
				line = in.readLine();
				while (!".".equals(line)) {
					StringTokenizer st = new StringTokenizer(line);
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());
					int ene = Integer.parseInt(st.nextToken());
					tanks.add(new Tank(x, y, ene));
					System.out.println("enegy position: ");
					System.out.println("" + "x: " + x + " y: " + y + " point: " + ene);
					line = in.readLine();
				}

				Tank targetTank = null;
				double minDistance = Double.MAX_VALUE;
				for (Tank tank : tanks) {
					double distance = Math.sqrt(Math.pow(shipX - tank.x, 2) + Math.pow(shipY - tank.y, 2));
					if (distance < minDistance) {
						minDistance = distance;
						targetTank = tank;
					}
				}

				// Sort tanks by energy in descending order
				tanks.sort((Tank a, Tank b) -> b.ene - a.ene);

				if (targetTank == null || (shipX == targetTank.x && shipY == targetTank.y)) {
					// Sort tanks by energy in descending order
					tanks.sort((Tank a, Tank b) -> b.ene - a.ene);
					targetTank = tanks.get(0);
				}

				int dx = Math.abs(shipX - targetTank.x);
				int dy = Math.abs(shipY - targetTank.y);

				if (dx > dy) {
					if (shipX < targetTank.x) {
						sendCommand("right");
					} else {
						sendCommand("left");
					}
					if (shipY < targetTank.y) {
						sendCommand("up");
					} else {
						sendCommand("down");
					}
				} else {
					if (shipY < targetTank.y) {
						sendCommand("up");
					} else {
						sendCommand("down");
					}
					if (shipX < targetTank.x) {
						sendCommand("right");
					} else {
						sendCommand("left");
					}
				}

				if (shipX == targetTank.x && shipY == targetTank.y) {
					targetTank = null;
				}

		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	}

	// login関連のオブジェクト
	Socket server;// ゲームサーバとの接続ソケット
	int port = 10000;// 接続ポート
	BufferedReader in;// 入力ストリーム
	PrintWriter out;// 出力ストリーム
	String name;// ゲーム参加者の名前


	// loginメソッド
	// サーバへのlogin処理を行います
	void login(String host, String name){
		try {
			// サーバとの接続
			this.name = name;
			server = new Socket(host, port);
			in = new BufferedReader(new InputStreamReader(
			  server.getInputStream()));
			out = new PrintWriter(server.getOutputStream());

			// loginコマンドの送付
			out.println("login " + name);
			out.flush();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	void moving(int[] num){
		for(int i=0 ; i < num.length ;i++){
			String move = switch(num[i]){
				case 1 -> "right";
				case 2 -> "left";
				case 3 -> "up";
				case 0 -> "down";
				default -> "right";
			};
			out.println(move);
			out.println("stat");
		}
	}

	void sendCommand(String s) {
		if ("up".equals(s)) {
			out.println("up");
		} else if ("down".equals(s)) {
			out.println("down");
		} else if ("left".equals(s)) {
			out.println("left");
		} else if ("right".equals(s)) {
			out.println("right");
		}
		out.flush();
	}


	// mainメソッド
	// Robotを起動します
	public static void main(String[] args){
		new Robot2(args);
	}
}