package h264.com;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

class CTCPServerThread extends Thread {
	
	private VView mView;
	
	// 套接字服务接口
	private ServerSocket mServerSocket = null;
	
	// The number of received packets
	public int mRecvPacketNum = 0;

	public CTCPServerThread(VView view) {

		// 初始化服务套接字
		try {
			//创建套接字服务示例
			mServerSocket = new ServerSocket(ClientConfig.CONFIG_SERVER_PORT);
		}
		catch(IOException e) {
			
			e.printStackTrace();
		}
		
		mView = view;
	}

	@Override
	public void run() {
		//启动服务
		startServer();

		if(mServerSocket == null) {
			return;
		}

		try {
			mServerSocket.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	// 开始服务
	private void startServer() {
		Socket socket = null;
		try {

			while(true) {

				System.out.println("connecting...");

				// 接受客户端连接
				socket = mServerSocket.accept();

				System.out.println("connect successfully!");

				// 启动客户端服务线程（开始一个会话）
				CTCPSessionThread sessionThrd = new CTCPSessionThread(mView, socket);
				sessionThrd.start();
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				socket.close();
			} 
			catch (IOException e) {

				e.printStackTrace();
			}
		}
	}
	
	class CTCPSessionThread extends Thread {
		
		VView mView;
		
		// 与客户端通信套接字
		private boolean mIsFinish = false;
		private InputStream inputstream;
		FileInputStream fis = null; 

		public CTCPSessionThread(VView view, Socket socket) {

			// 获取会话用输入/输出流
			try {
				inputstream = socket.getInputStream();
			}
			catch(IOException e) {
				
				e.printStackTrace();
			}
			
			mView = view;
		}

		@Override
		public void run() {
			
			while(!mIsFinish) {

				// 获取客户端请求
				try {
					byte buf[] = new byte[ClientConfig.CONFIG_BUFFER_SIZE];
					byte btNalLen[] = new byte[2]; 


					inputstream.read(btNalLen, 0, 2);

					int highBit = ((int)btNalLen[0]>=0)?((int)btNalLen[0]):(256+(int)btNalLen[0]);
					int lowBit = ((int)btNalLen[1]>=0)?((int)btNalLen[1]):(256+(int)btNalLen[1]);

					int nalLen = highBit*256+lowBit;

					Log.d("NalLen", ""+nalLen);

					int bufLen = inputstream.read(buf, 0, nalLen);

					mView.decodeNalAndDisplay(buf, bufLen);
					
					mRecvPacketNum++;
					
					// received the PPS and SPS
					if( 2 == mRecvPacketNum ) {
						Log.d("pIC", "received the PPS and SPS");
						break;
					}

					Log.d("pIC", "end process input");

				}
				catch(IOException e) {
					e.printStackTrace();
					Log.d("nullpoint", "inputstream is null");
				}
			}
		}


		// 设置该线程是否结束的标识
		public void setIsFinish(boolean isFinish) {
			
			this.mIsFinish = isFinish;
		}
	}
}

