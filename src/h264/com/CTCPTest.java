package h264.com;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

class CTCPServerThread extends Thread {
	
	private VView mView;
	
	// �׽��ַ���ӿ�
	private ServerSocket mServerSocket = null;
	
	// The number of received packets
	public int mRecvPacketNum = 0;

	public CTCPServerThread(VView view) {

		// ��ʼ�������׽���
		try {
			//�����׽��ַ���ʾ��
			mServerSocket = new ServerSocket(ClientConfig.CONFIG_SERVER_TCP_PORT);
		}
		catch(IOException e) {
			
			e.printStackTrace();
		}
		
		mView = view;
	}

	@Override
	public void run() {
		
		//��������
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

	// ��ʼ����
	private void startServer() {
		Socket socket = null;
		try {

			while(true) {

				System.out.println("connecting...");

				// ���ܿͻ�������
				socket = mServerSocket.accept();

				System.out.println("connect successfully!");

				// �����ͻ��˷����̣߳���ʼһ���Ự��
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
		
		// ��ͻ���ͨ���׽���
		private boolean mIsFinish = false;
		private InputStream inputstream;
		FileInputStream fis = null; 

		public CTCPSessionThread(VView view, Socket socket) {

			// ��ȡ�Ự������/�����
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

				// ��ȡ�ͻ�������
				try {
					byte buf[] = new byte[ClientConfig.CONFIG_BUFFER_SIZE];
					byte btNalLen[] = new byte[2]; 

					inputstream.read(btNalLen, 0, 2);
					
					int highBit =  btNalLen[0]<0 ? 256 + btNalLen[0] : btNalLen[0];
					int lowBit = btNalLen[1]<0 ? 256 + btNalLen[1] : btNalLen[1];
					int nalLen = (highBit<<8) + lowBit;

					Log.d("NalLen", "" + nalLen);

					int bufLen = inputstream.read(buf, 0, nalLen);
					
					if( bufLen > 0 ) {
						mRecvPacketNum++;
						Log.d("pIC", "TCP recv len: " + bufLen);
					}
//					
//					if( mRecvPacketNum % 20 == 0 )
//						continue;

					mView.decodeNalAndDisplay(buf, bufLen);
						
					
					// received the PPS and SPS
					if( 2 == mRecvPacketNum ) {
						
						Log.d("pIC", "received the PPS and SPS");
						
				    	Log.d("pIC", "start the RTP Thread");
				    	
				    	CRTPClientThread rtpClientThrd = new CRTPClientThread(mView);
				    	rtpClientThrd.start();
				    	
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


		// ���ø��߳��Ƿ�����ı�ʶ
		public void setIsFinish(boolean isFinish) {
			
			this.mIsFinish = isFinish;
		}
	}
}

