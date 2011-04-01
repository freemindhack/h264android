package h264.com;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

class CTCPServerThread extends Thread {
	
	VView mView;
	
	// �׽��ַ���ӿ�
	private ServerSocket mServerSocket = null;

	public CTCPServerThread(VView view) {

		// ��ʼ�������׽���
		try {
			//�����׽��ַ���ʾ��
			mServerSocket = new ServerSocket(ClientConfig.CONFIG_SERVER_PORT);
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
			//fis = new FileInputStream("/sdcard/640x480.yuv");
		}
		catch(IOException e) {
			
			e.printStackTrace();
		}
		
		mView = view;
	}

	//		private byte[] InputStreamToByte(InputStream is) throws IOException {
	//			ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
	//			int ch;
	//			while ((ch = is.read()) != -1) {
	//				bytestream.write(ch);
	//			}
	//			byte imgdata[] = bytestream.toByteArray();
	//			bytestream.close();
	//			return imgdata;
	//		}

	@Override
	public void run() {
		while(!mIsFinish) {

			// ��ȡ�ͻ�������
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
				
//				// �Ի�ȡ�����ݽ��д���
//
//				int iTemp = DecoderNal(buf, bufLen, mPixel);
//
//				//    				InputStream is = new ByteArrayInputStream(mPixel); 
//				//    				
//				//    				is.read(mRealPixel, 0, mRealPixel.length);
//
//				if(iTemp>0)
//					postInvalidate(); 
//
//				try {
//					Thread.currentThread().sleep(300);
//				} catch (InterruptedException e) {
//
//					e.printStackTrace();
//				}

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