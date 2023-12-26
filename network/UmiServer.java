// ｢海ゲーム｣サーバプログラムUmiServer.java
// このプログラムは,海ゲームのサーバプログラムです
// 使い方java UmiServer
// 起動すると,ポート番号10000 番に対するクライアントからの接続を待ちます
// プログラムを停止するにはコントロールC を入力してください

// ライブラリの利用
import java.io.*;
import java.net.*;
import java.util.*;

// UmiServerクラス
// UmiServerクラスは,UmiServerプログラムの本体です
public class UmiServer {
	static final int DEFAULT_PORT = 10000;
							//UmiServer接続用ポート番号
	static ServerSocket serverSocket;
	static Vector connections;
				//クライアントとのコネクションを保持するVectorオブジェクト
	static Vector energy_v; // 燃料タンクの位置情報リスト
	static Hashtable userTable = null;
				// クライアント関連情報登録用テーブル
	static Random random = null;

	// addConnectionメソッド
	// クライアントとの接続をVectorオブジェクトconnectionsに登録します
	public static void addConnection(Socket s){
		if (connections == null){//初めてのコネクションの場合は,
			connections = new Vector();// connectionsを作成します
		}
		connections.addElement(s);
	}

	// deleteConnectionメソッド
	// クライアントとの接続をconnectionsから削除します
	public static void deleteConnection(Socket s){
		if (connections != null){
			connections.removeElement(s);
		}
	}

	// loginUserメソッド
	// loginコマンドの処理として,利用者の名前や船の位置を登録します
	public static void loginUser(String name){
		if (userTable == null){// 登録用テーブルがなければ作成します
			userTable = new Hashtable();
		}
		if (random == null){// 乱数の準備をします
			random = new Random();
		}
		// 船の初期位置を乱数で決定します
		int ix = Math.abs(random.nextInt()) % 256;
		int iy = Math.abs(random.nextInt()) % 256;

		// クライアントの名前や船の位置を表に登録します
		userTable.put(name, new Ship(ix, iy));
		// サーバ側の画面にもクライアントの名前を表示します
		System.out.println("login:" + name);
		System.out.flush();
	}

	// logoutUserメソッド
	// クライアントのログアウトを処理します
	public static void logoutUser(String name){
		// サーバ側画面にログアウトするクライアントの名前を表示します
		System.out.println("logout:" + name);
		System.out.flush();
		// 登録用テーブルから項目を削除します
		userTable.remove(name);
	}

	// leftメソッド
	// ある特定の船を左に動かして,燃料タンクが拾えるかどうか判定します
	// 判定にはcalculationメソッドを使います
	public static void left(String name){
		Ship ship = (Ship) userTable.get(name);
		ship.left();
		calculation();
	}

	// rightメソッド
	// ある特定の船を右に動かして,燃料タンクが拾えるかどうか判定します
	// 判定にはcalculationメソッドを使います
	public static void right(String name){
		Ship ship = (Ship) userTable.get(name);
		ship.right();
		calculation();
	}

	// upメソッド
	// ある特定の船を上に動かして,燃料タンクが拾えるかどうか判定します
	// 判定にはcalculationメソッドを使います
	public static void up(String name){
		Ship ship = (Ship) userTable.get(name);
		ship.up();
		calculation();
	}

	// downメソッド
	// ある特定の船を下に動かして,燃料タンクが拾えるかどうか判定します
	// 判定にはcalculationメソッドを使います
	public static void down(String name){
		Ship ship = (Ship) userTable.get(name);
		ship.down();
		calculation();
	}

	// calculationメソッド
	// 燃料タンクと船の位置関係を調べて,燃料タンクが拾えるかどうか判定します
	static void calculation(){
		if (userTable != null && energy_v != null){
			// すべてのクライアントについて判定します
			for (Enumeration users = userTable.keys();
				 users.hasMoreElements();) {
				// 判定するクライアントの名前と船の位置を取り出します
				String user = users.nextElement().toString();
				Ship ship = (Ship) userTable.get(user);
				// 燃料タンクすべてについて,船との位置関係を調べます
				for (Enumeration energys = energy_v.elements();
					 energys.hasMoreElements();) {
					// 燃料タンクの位置と船の位置を調べ,距離を計算します
					int[] e = (int []) energys.nextElement();
					int x = e[0] - ship.x;
					int y = e[1] - ship.y;
					double r = Math.sqrt(x * x + y * y);
					// 距離"10"以内なら燃料タンクを取り込みます
					if (r < 10) {
						energy_v.removeElement(e);
						ship.point++;
					}
				}
			}
		}
	}

	// statInfoメソッド
	// STATコマンドを処理します
	// クライアントに対して,船の情報（ship_info)と,
	// 海上を漂流している燃料タンクの情報を(energy_info)を送信します
	public static void statInfo(PrintWriter pw){
		// 船の情報(ship_info)の送信
		pw.println("ship_info");
		if (userTable != null){
			for (Enumeration users = userTable.keys();
				 users.hasMoreElements();) {
				String user = users.nextElement().toString();
				Ship ship = (Ship) userTable.get(user);
				pw.println(user + " " + ship.x + " "
								+ ship.y + " " + ship.point);
			}
		}
		pw.println(".");// ship_infoの終了
		// 燃料タンクの情報（energy_info）の送信
		pw.println("energy_info");
		if (energy_v != null){
			// すべての燃料タンクの位置情報をクライアントに送信します
			for (Enumeration energys = energy_v.elements();
				 energys.hasMoreElements();) {
				int[] e = (int []) energys.nextElement();
				pw.println(e[0] + " " + e[1]);
			}
		}
		pw.println(".");// enegy_infoの終了
		pw.flush();
	}

	// putEnergyメソッド
	// 燃料タンクを１つだけ,海上にランダムに配置します
	public static void putEnergy(){
		if (energy_v == null){// 初めて配置する場合の処理
			energy_v = new Vector();
		}
		if (random == null){// 初めて乱数を使う場合の処理
			random = new Random();
		}
		// 乱数で位置を決めて海上に配置します
		int[] e = new int[2];
		e[0] = Math.abs(random.nextInt()) % 256;
		e[1] = Math.abs(random.nextInt()) % 256;

		energy_v.addElement(e);
	}

	// mainメソッド
	// サーバソケットの作成とクライアント接続の処理
	// および適当なタイミングでの燃料タンクの逐次追加処理を行います
	public static void main(String[] arg){
		try {// サーバソケットの作成
			serverSocket = new ServerSocket(DEFAULT_PORT);
		}catch (IOException e){
			System.err.println("can't create server socket.");
			System.exit(1);
		}
		// 燃料タンクを順に追加するスレッドetを作ります
		Thread et = new Thread(){
			public void run(){
				while(true){
					try {
						sleep(10000);// スレッドetを10000ミリ秒休止させます
					}catch(InterruptedException e){
						break;
					}
					// 海上に１つ燃料タンクを配置します
					UmiServer.putEnergy();
				}
			}
		};
		// etをスタートします
		et.start();
		// ソケットの受付と,クライアント処理プログラムの開始処理を行います
		while (true) {// 無限ループ
			try {
				Socket cs = serverSocket.accept();
				addConnection(cs);// コネクションを登録します
				// クライアント処理スレッドを作成します
				Thread ct = new Thread(new clientProc(cs));
				ct.start();
			}catch (IOException e){
				System.err.println("client socket or accept error.");
			}
		}
	}
}

// clientProcクラス
// clientProcクラスは,クライアント処理スレッドのひな形です
class clientProc implements Runnable {
	Socket s; // クライアント接続用ソケット
	// 入出力ストリーム
	BufferedReader in;
	PrintWriter out;
	String name = null;// クライアントの名前

	// コンストラクタclientProc
	// ソケットを使って入出力ストリームを作成します
	public clientProc(Socket s) throws IOException {
		this.s = s;
		in = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
		out = new PrintWriter(s.getOutputStream());
	}

	// runメソッド
	// クライアント処理スレッドの本体です
	public void run(){
		try {
			//LOGOUTコマンド受信まで繰り返します
			while (true) {
				// クライアントからの入力を読み取ります
				String line = in.readLine();
				// nameが空の場合にはLOGINコマンドのみを受け付けます
				if (name == null){
					StringTokenizer st = new StringTokenizer(line);
					String cmd = st.nextToken();
					if ("login".equalsIgnoreCase(cmd)){
						name = st.nextToken();
						UmiServer.loginUser(name);
					}else{
						// LOGINコマンド以外は,すべて無視します
					}
				}else{
					// nameが空でない場合はログイン済みですから,コマンドを受け付けます
					StringTokenizer st = new StringTokenizer(line);
					String cmd = st.nextToken();// コマンドの取り出し
					// コマンドを調べ,対応する処理を行います
					if ("STAT".equalsIgnoreCase(cmd)){
						UmiServer.statInfo(out);
					} else if ("UP".equalsIgnoreCase(cmd)){
						UmiServer.up(name);
					} else if ("DOWN".equalsIgnoreCase(cmd)){
						UmiServer.down(name);
					} else if ("LEFT".equalsIgnoreCase(cmd)){
						UmiServer.left(name);
					} else if ("RIGHT".equalsIgnoreCase(cmd)){
						UmiServer.right(name);
					} else if ("LOGOUT".equalsIgnoreCase(cmd)){
						UmiServer.logoutUser(name);
						// LOGOUTコマンドの場合には繰り返しを終了します
						break;
					}
				}
			}
			// 登録情報を削除し,接続を切断します
			UmiServer.deleteConnection(s);
			s.close();
		}catch (IOException e){
			try {
				s.close();
			}catch (IOException e2){}
		}
	}
}

// Shipクラス
// 船の位置と,獲得した燃料タンクの数を管理します
class Ship {
	// 船の位置座標
	int x;
	int y;
	// 獲得した燃料タンクの個数
	int point = 0;

	// コンストラクタ
	// 初期位置をセットします
	public Ship(int x, int y){
		this.x = x;
		this.y = y;
	}

	// leftメソッド
	// 船を左に動かします
	public void left(){
		x -= 10;
		// 左の辺は右の辺につながっています
		if (x < 0)
			x += 256;
	}

	// rightメソッド
	// 船を右に動かします
	public void right(){
		x += 10;
		// 右の辺は左の辺につながっています
		x %= 256;
	}

	// upメソッド
	// 船を上に動かします
	public void up(){
		y += 10;
		// 上の辺は下の辺につながっています
		y %= 256;
	}

	// downメソッド
	// 船を下に動かします
	public void down(){
		y -= 10;
		// 下の辺は上の辺につながっています
		if (y < 0)
			y += 256;
	}
}