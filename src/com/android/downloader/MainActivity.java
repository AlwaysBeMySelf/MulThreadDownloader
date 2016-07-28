package com.android.downloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.android.network.DownloadProgressListener;
import com.android.network.FileDownloader;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private EditText downloadpathText;
	private TextView resultView;
	private ProgressBar progressBar;

	/**
	 * ��Handler��������������������ĵ�ǰ�̵߳���Ϣ���У�������������Ϣ���з�����Ϣ ��Ϣ�����е���Ϣ�ɵ�ǰ�߳��ڲ����д���
	 * ʹ��Handler����UI������Ϣ��
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				progressBar.setProgress(msg.getData().getInt("size"));
				float num = (float) progressBar.getProgress() / (float) progressBar.getMax();
				int result = (int) (num * 100);
				resultView.setText(result + "%");

				// ��ʾ���سɹ���Ϣ
				if (progressBar.getProgress() == progressBar.getMax()) {
					Toast.makeText(MainActivity.this, R.string.success, 1).show();
				}
				break;
				case 2:
					progressBar.setProgress(msg.arg1);
					float num1 = (float) progressBar.getProgress() / (float) progressBar.getMax();
					int result1 = (int) (num1 * 100);
					resultView.setText(result1 + "%");
					break;
			case -1:
				// ��ʾ���ش�����Ϣ
				Toast.makeText(MainActivity.this, R.string.error, 1).show();
				break;
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		downloadpathText = (EditText) this.findViewById(R.id.path);
		progressBar = (ProgressBar) this.findViewById(R.id.downloadbar);
		resultView = (TextView) this.findViewById(R.id.resultView);
		Button button = (Button) this.findViewById(R.id.button);
		Button buttonStop = (Button) this.findViewById(R.id.buttonStop);

		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String path = downloadpathText.getText().toString();
				System.out.println(Environment.getExternalStorageState() + "------" + Environment.MEDIA_MOUNTED);

				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					// ��ʼ�����ļ�
					download(path, Environment.getExternalStorageDirectory());
//					downloadSingle(path, Environment.getExternalStorageDirectory());
				} else {
					// ��ʾSDCard������Ϣ
					Toast.makeText(MainActivity.this, R.string.sdcarderror, 1).show();
				}
			}
		});
		buttonStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (null != loader) {
					loader.stop();
				}

			}
		});
	}

	/**
	 * ���߳�(UI�߳�) ������ʾ�ؼ��Ľ������ֻ����UI�̸߳���������ڷ�UI�̸߳��¿ؼ�������ֵ�����º����ʾ���治�ᷴӳ����Ļ��
	 * ������ø��º����ʾ���淴ӳ����Ļ�ϣ���Ҫ��Handler���á�
	 * 
	 * @param fileUrlStr
	 * @param savedir
	 */
	FileDownloader loader;

	private void download(final String fileUrlStr, final File savedir) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// ����3���߳̽�������
				loader = new FileDownloader(MainActivity.this, fileUrlStr, savedir, 3);
				progressBar.setMax(loader.getFileSize());// ���ý����������̶�Ϊ�ļ��ĳ���

				try {
					loader.download(new DownloadProgressListener() {
						@Override
						public void onDownloadSize(int size) {// ʵʱ��֪�ļ��Ѿ����ص����ݳ���
							Message msg = handler.obtainMessage();
							msg.what = 1;
							msg.getData().putInt("size", size);
							msg.sendToTarget();
						}
					});
				} catch (Exception e) {
					handler.obtainMessage(-1).sendToTarget();
				}
			}
		}).start();
	}

	private void downloadSingle(final String fileUrlStr, final File savedir) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				HttpURLConnection http;
				try {
					http = (HttpURLConnection) new URL(fileUrlStr).openConnection();
					http.setConnectTimeout(5 * 1000);
					http.setRequestMethod("GET");
					http.setRequestProperty("Accept",
							"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
					http.setRequestProperty("Accept-Language", "zh-CN");
					// http.setRequestProperty("Referer", downUrl.toString());
					http.setRequestProperty("Charset", "UTF-8");
					http.setRequestProperty("Connection", "Keep-Alive");
					http.connect();
					progressBar.setMax(http.getContentLength());
					InputStream inStream = http.getInputStream();
					byte[] buffer = new byte[1024];
					int offset = 0;
					RandomAccessFile threadfile = new RandomAccessFile(new File(savedir,"youdao.apk"), "rwd");
					threadfile.seek(0);

					int len=0;
					while ((offset = inStream.read(buffer, 0, 1024)) != -1) {
						threadfile.write(buffer, 0, offset);
						len+=offset;
						Message message = handler.obtainMessage(2);
						message.arg1=len;
						message.sendToTarget();
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();

	}

}