package com.stone.clientinputcontroler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.stone.softkeyboard.LatinKeyboard;
import com.stone.softkeyboard.LatinKeyboardView;
import com.stone.softkeyboard.SoftKeyboard;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements
		KeyboardView.OnKeyboardActionListener {
	private String mServerIP = "192.168.1.101";
	private int mServerPort = 8888;

	private final static String IP_REGLEX = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
			+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
			+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
			+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";

	private EditText mIP;
	private EditText mPort;
	private EditText mCommand;

	private SeekBar mStep;
	private TextView mStepValue;

	private ExecutorService mExecutors = Executors.newSingleThreadExecutor();

	private Button mSendButton, mClickButton, mRightClickButton,
			mDoubleClickButton, mUpButton, mLeftButton, mRightButton,
			mDownButton;
	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			updateServerInfo();

			int id = v.getId();
			if (id == R.id.bt_send) {// send
				String command = mCommand.getText().toString();
				if (command != null && command.length() > 0) {
					mExecutors.execute(new SocketRunnable("##" + command,
							mServerIP, mServerPort));
				} else {
					Toast.makeText(getApplicationContext(),
							"Please input command !", Toast.LENGTH_SHORT)
							.show();
				}
				mCommand.setText("");
			} else if (id == R.id.bt_click) {// single click
				mExecutors.execute(new SocketRunnable(MouseAction.CLICK,
						mServerIP, mServerPort));
			} else if (id == R.id.bt_right_click) {// right click
				mExecutors.execute(new SocketRunnable(MouseAction.RIGHT_CLICK,
						mServerIP, mServerPort));
			} else if (id == R.id.bt_double_click) {// double click
				mExecutors.execute(new SocketRunnable(MouseAction.DOUBLE_CLICK,
						mServerIP, mServerPort));
			} else if (id == R.id.bt_up) {// up
				mExecutors.execute(new SocketRunnable(MouseAction.UP,
						mServerIP, mServerPort));
			} else if (id == R.id.bt_left) {// left
				mExecutors.execute(new SocketRunnable(MouseAction.LEFT,
						mServerIP, mServerPort));
			} else if (id == R.id.bt_right) {// right
				mExecutors.execute(new SocketRunnable(MouseAction.RIGHT,
						mServerIP, mServerPort));
			} else if (id == R.id.bt_down) {// down
				mExecutors.execute(new SocketRunnable(MouseAction.DOWN,
						mServerIP, mServerPort)); 
			}
		}
	};

	private AutoReproductSocketRunnable mLongClickSocketRunnable;
	private boolean mLongClickBegin = false;
	private OnLongClickListener mOnLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
			mLongClickBegin = true;
			updateServerInfo();

			mLongClickSocketRunnable = null;
			int id = v.getId();
			if (id == R.id.bt_send) {// send
				// do nothing
			} else if (id == R.id.bt_click) {// single click
				// do nothing
			} else if (id == R.id.bt_right_click) {// right click
				// do nothing
			} else if (id == R.id.bt_double_click) {// double click
				// do nothing
			} else if (id == R.id.bt_up) {// up
				mLongClickSocketRunnable = new AutoReproductSocketRunnable(
						MouseAction.UP, mExecutors, mServerIP, mServerPort);
			} else if (id == R.id.bt_left) {// left
				mLongClickSocketRunnable = new AutoReproductSocketRunnable(
						MouseAction.LEFT, mExecutors, mServerIP, mServerPort);
			} else if (id == R.id.bt_right) {// right
				mLongClickSocketRunnable = new AutoReproductSocketRunnable(
						MouseAction.RIGHT, mExecutors, mServerIP, mServerPort);
			} else if (id == R.id.bt_down) {// down
				mLongClickSocketRunnable = new AutoReproductSocketRunnable(
						MouseAction.DOWN, mExecutors, mServerIP, mServerPort);
			}

			if (mLongClickSocketRunnable != null) {
				mExecutors.execute(mLongClickSocketRunnable);
			}

			return true;
		}
	};

	private OnTouchListener mOnTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == MotionEvent.ACTION_CANCEL
					|| event.getAction() == MotionEvent.ACTION_UP) {
				if (mLongClickBegin) {
					mLongClickBegin = false;
					if (mLongClickSocketRunnable != null) {
						mLongClickSocketRunnable.disableReproduct();
					}
				}
			}

			return false;
		}
	};

	private void updateServerInfo() {
		String IP = mIP.getText().toString();
		String port = mPort.getText().toString();
		if (IP != null && port != null && IP.matches(IP_REGLEX)
				&& port.length() == 4) {
			mServerIP = IP;
			mServerPort = Integer.valueOf(port);
		} else {
			Toast.makeText(getApplicationContext(),
					MainActivity.this.getString(R.string.toast_invalid_ip),
					Toast.LENGTH_SHORT).show();
			mIP.setText(mServerIP);
			mPort.setText(String.valueOf(mServerPort));
		}
	}

	private OnSeekBarChangeListener mChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			updateServerInfo();
			int step = seekBar.getProgress();
			if (step < 1) {
				step = 1;
				seekBar.setProgress(step);
			}
			String command = "#" + String.valueOf(step);
			if (command != null && command.matches("#[1-9]+[0-9]*")) {
				mExecutors.execute(new SocketRunnable(command, mServerIP,
						mServerPort));
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			mStepValue.setText(String.valueOf(progress));
		}
	};

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				String ip = (String) msg.obj;
				mIP.setText(ip);

				Toast.makeText(getApplicationContext(), "Find Server IP",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// init
		mIP = (EditText) findViewById(R.id.ip);
		mPort = (EditText) findViewById(R.id.port);
		mCommand = (EditText) findViewById(R.id.msg);

		mSendButton = (Button) findViewById(R.id.bt_send);
		mClickButton = (Button) findViewById(R.id.bt_click);
		mRightClickButton = (Button) findViewById(R.id.bt_right_click);
		mDoubleClickButton = (Button) findViewById(R.id.bt_double_click);
		mUpButton = (Button) findViewById(R.id.bt_up);
		mLeftButton = (Button) findViewById(R.id.bt_left);
		mRightButton = (Button) findViewById(R.id.bt_right);
		mDownButton = (Button) findViewById(R.id.bt_down);
		mStep = (SeekBar) findViewById(R.id.sb_step);
		mStepValue = (TextView) findViewById(R.id.label_step);

		// for click
		mSendButton.setOnClickListener(mClickListener);
		mClickButton.setOnClickListener(mClickListener);
		mRightButton.setOnClickListener(mClickListener);
		mDoubleClickButton.setOnClickListener(mClickListener);
		mUpButton.setOnClickListener(mClickListener);
		mLeftButton.setOnClickListener(mClickListener);
		mRightClickButton.setOnClickListener(mClickListener);
		mDownButton.setOnClickListener(mClickListener);

		// for long and touch listener
		mUpButton.setOnLongClickListener(mOnLongClickListener);
		mUpButton.setOnTouchListener(mOnTouchListener);
		mLeftButton.setOnLongClickListener(mOnLongClickListener);
		mLeftButton.setOnTouchListener(mOnTouchListener);
		mRightButton.setOnLongClickListener(mOnLongClickListener);
		mRightButton.setOnTouchListener(mOnTouchListener);
		mDownButton.setOnLongClickListener(mOnLongClickListener);
		mDownButton.setOnTouchListener(mOnTouchListener);

		// for seekbars
		mStep.setOnSeekBarChangeListener(mChangeListener);

		mKeyboardView = (LatinKeyboardView) findViewById(R.id.keyboard);
		mQwertyKeyboard = new LatinKeyboard(this, R.xml.qwerty);
		mQwertyKeyboard.setShifted(mShifted);
		mKeyboardView.setOnKeyboardActionListener(this);
		mKeyboardView.setKeyboard(mQwertyKeyboard);

		mSymbolsKeyboard = new LatinKeyboard(this, R.xml.symbols);
		mSymbolsShiftedKeyboard = new LatinKeyboard(this, R.xml.symbols_shift);

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				boolean toFind = true;
				Socket socket = null;
				String ip = "192.168.1.";
				int i = 99;
				while (toFind && i < 255) {
					try {
						socket = new Socket(ip + (i++), mServerPort);
						Thread.sleep(50);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						socket = null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						socket = null;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						socket = null;
					}

					if (socket != null) {
						Message msg = new Message();
						msg.what = 1;
						msg.obj = ip + (i - 1);
						mHandler.sendMessage(msg);
						try {
							socket.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						toFind = false;
					}
				}
			}
		});
	}

	private LatinKeyboardView mKeyboardView;
	private LatinKeyboard mQwertyKeyboard, mSymbolsKeyboard,
			mSymbolsShiftedKeyboard;

	private void setQwertyKeyboard() {
		mKeyboardView.setKeyboard(mQwertyKeyboard);
		mQwertyKeyboard.setShifted(mShifted);
		// mKeyboardView.invalidateAllKeys();
	}

	private void setSymbolsKeyboard() {
		Keyboard currentKeyboard = mKeyboardView.getKeyboard();
		if (mSymbolsKeyboard == currentKeyboard) {
			mSymbolsKeyboard.setShifted(true);
			mKeyboardView.setKeyboard(mSymbolsShiftedKeyboard);
			mSymbolsShiftedKeyboard.setShifted(true);
		} else {
			mSymbolsShiftedKeyboard.setShifted(false);
			mKeyboardView.setKeyboard(mSymbolsKeyboard);
			mSymbolsKeyboard.setShifted(false);
		}

		// mKeyboardView.invalidateAllKeys();
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// getMenuInflater().inflate(R.menu.activity_main, menu);
	// return true;
	// }

	private enum MouseAction {
		LEFT, RIGHT, UP, DOWN, CLICK, RIGHT_CLICK, DOUBLE_CLICK, SCROLL
	}

	private static class SocketRunnable implements Runnable {
		protected MouseAction mAction = null;
		protected String mCommandStr = null;
		protected String mDstHost;
		protected int mDstPort;

		public SocketRunnable(MouseAction action, String dstHost, int dstPort) {
			if (action == null) {
				throw new RuntimeException("Invalid MouseAction!");
			}

			mAction = action;
			mDstHost = dstHost;
			mDstPort = dstPort;
		}

		public SocketRunnable(String command, String dstHost, int dstPort) {
			if (command == null || command.length() < 1)
				throw new RuntimeException("Invalid Command!");

			mCommandStr = command;
			mDstHost = dstHost;
			mDstPort = dstPort;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mAction != null) {
				sendServerMsg(mAction);
			} else {
				sendServerMsg(mCommandStr);
			}
		}

		public void sendServerMsg(String msg) {
			try {
				Socket socket = new Socket(mDstHost, mDstPort);
				BufferedWriter br = new BufferedWriter(new OutputStreamWriter(
						socket.getOutputStream()));
				br.write(msg);
				br.flush();
				br.close();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void sendServerMsg(MouseAction action) {
			String msg = null;
			switch (action) {
			case LEFT:
				msg = "l";
				break;
			case RIGHT:
				msg = "r";
				break;
			case UP:
				msg = "u";
				break;
			case DOWN:
				msg = "d";
				break;
			case CLICK:
				msg = "s";
				break;
			case DOUBLE_CLICK:
				msg = "t";
				break;
			case RIGHT_CLICK:
				msg = "m";
				break;
			case SCROLL:
				msg = "o";
				break;
			default:
				break;
			}

			if (msg != null)
				sendServerMsg(msg);
		}

	}

	private static class AutoReproductSocketRunnable extends SocketRunnable {
		private volatile boolean mGoOn = true;
		private BufferedWriter mWriter = null;
		private ExecutorService mSocketExecutor;

		public AutoReproductSocketRunnable(MouseAction action,
				ExecutorService executor, String dstHost, int dstPort) {
			super(action, dstHost, dstPort);
			// TODO Auto-generated constructor stub

			if (executor == null)
				throw new RuntimeException("ExecutorService cannot be null !");
			mSocketExecutor = executor;
		}

		public void enableReproduct() {
			mGoOn = true;
		}

		public void disableReproduct() {
			mGoOn = false;
		}

		@Override
		public void sendServerMsg(String msg) {
			// TODO Auto-generated method stub
			try {
				if (mWriter == null) {
					Socket socket = new Socket(mDstHost, mDstPort);
					mWriter = new BufferedWriter(new OutputStreamWriter(
							socket.getOutputStream()));
				}
				mWriter.flush();
				mWriter.write(msg);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();

			if (mGoOn) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mSocketExecutor.execute(this);

			} else {
				try {
					if (mWriter != null)
						mWriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	// ////KeyboardView.OnKeyboardActionListener //begin
	private boolean mShifted = false;

	@Override
	public void onPress(int primaryCode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRelease(int primaryCode) {
		// TODO Auto-generated method stub
		if (-1 == primaryCode) {// shift
			if ((LatinKeyboard) mKeyboardView.getKeyboard() == mQwertyKeyboard) {// Qwerty
																					// shift
				mShifted = !mShifted;
				mQwertyKeyboard.setShifted(mShifted);
				mKeyboardView.invalidateAllKeys();
			} else {// symbols shift
				setSymbolsKeyboard();
			}
		} else if (-2 == primaryCode) {// 123
			setSymbolsKeyboard();
		} else if (-22 == primaryCode) {// symbol abc
			setQwertyKeyboard();
		}
	}

	// private char[]
	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		// TODO Auto-generated method stub
		if (-1 == primaryCode) {// shift
			return;
		}

		if (primaryCode == -2) {// key 123
			return;
		}

		// symbols keyboard begin
		if (primaryCode == -22) {// abc
			return;
		}

		if (primaryCode == -11) {// shift
			return;
		}

		if (primaryCode == -3) {// hide keyboard
			return;
		}

		// symbols keyboard end

		if (primaryCode == -5) {// delete key
			primaryCode = 0x08;
		} else if (primaryCode >= 97 && primaryCode <= 122) {// 'a'-'z'
			if (mShifted) {
				primaryCode -= 32;// 'A'-'Z'
			}
		}
		mExecutors.execute(new SocketRunnable("##" + (char) primaryCode,
				mServerIP, mServerPort));
	}

	@Override
	public void onText(CharSequence text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeLeft() {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeRight() {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeUp() {
		// TODO Auto-generated method stub

	}
	// ////KeyboardView.OnKeyboardActionListener //end

}